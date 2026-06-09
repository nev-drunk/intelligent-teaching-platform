package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.dto.ai.LayoutDetectResult;
import com.huadi.intelligentteachingplatform.dto.ai.LayoutBox;
import com.huadi.intelligentteachingplatform.dto.ai.OcrResult;
import com.huadi.intelligentteachingplatform.entity.Submission;
import com.huadi.intelligentteachingplatform.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlagiarismService {

    private final SubmissionMapper submissionMapper;
    private final AiServiceClient aiServiceClient;

    // ================================================================
    // 公开接口
    // ================================================================

    /** 单个提交查重：完整流程 */
    public CheckResult checkBySubmission(Long submissionId) {
        Submission current = submissionMapper.selectById(submissionId);
        if (current == null) return CheckResult.empty();

        // 1. 提取当前学生的答案文本
        String currentText = ensureText(current);
        if (currentText == null || currentText.isBlank()) {
            return CheckResult.empty(current.getStudentName());
        }

        // 2. 获取同任务去重后的其他提交
        List<Submission> others = getDedupedOthers(current.getTaskId(), current.getStudentId());
        if (others.isEmpty()) return CheckResult.noOthers(current.getStudentName());

        // 3. 逐一比对
        List<CheckPair> pairs = new ArrayList<>();
        int maxSim = 0;
        for (Submission other : others) {
            String otherText = ensureText(other);
            if (otherText == null || otherText.isBlank()) continue;
            int sim = jaccardSimilarity(currentText, otherText);
            if (sim > 0) {
                pairs.add(new CheckPair(other, sim, currentText, otherText));
                maxSim = Math.max(maxSim, sim);
            }
        }
        pairs.sort((a, b) -> Integer.compare(b.similarity, a.similarity));

        // 4. 写库
        boolean cheated = maxSim > 80;
        current.setPlagiarismRate(new java.math.BigDecimal(maxSim));
        current.setIsCheated(cheated ? 1 : 0);
        submissionMapper.updateById(current);

        return CheckResult.success(current.getStudentName(), others.size(), maxSim, cheated, pairs);
    }

    // ================================================================
    // 文本提取：OCR优先 → 图片识别 → 空
    // ================================================================

    private String ensureText(Submission s) {
        // 1. 已有 OCR 文本
        if (s.getOcrRawText() != null && !s.getOcrRawText().isBlank()) {
            String parsed = tryParseJsonText(s.getOcrRawText());
            if (parsed != null && !parsed.isBlank()) return parsed;
        }
        // 2. 有提交文本 → 直接用（文本作业）
        if (s.getSubmitText() != null && !s.getSubmitText().isBlank()) {
            return s.getSubmitText().trim();
        }
        // 3. 有图片 → 识别
        if (s.getFileUrl() != null && !s.getFileUrl().isBlank()) {
            try {
                byte[] imgBytes = loadImage(s.getFileUrl());
                String text = recognizeFromImage(s.getFileUrl(), imgBytes);
                if (text != null && !text.isBlank()) {
                    s.setOcrRawText(text);
                    submissionMapper.updateById(s);
                    return text;
                }
            } catch (Exception e) {
                log.warn("图片识别失败 submissionId={}: {}", s.getId(), e.getMessage());
            }
        }
        return null;
    }

    /** OCR JSON → 提取纯文本 */
    private String tryParseJsonText(String raw) {
        if (raw.startsWith("{")) {
            try {
                var json = com.alibaba.fastjson2.JSON.parseObject(raw);
                String combined = json.getString("combined_text");
                if (combined != null && !combined.isBlank()) return combined.trim();
            } catch (Exception ignored) {}
        }
        return raw.trim();
    }

    /** 图片识别：layout detect → crop Text区域 → OCR */
    private String recognizeFromImage(String fileUrl, byte[] imgBytes) {
        // 1. 版面检测
        LayoutDetectResult layout;
        try {
            layout = aiServiceClient.detectPaperLayoutByUrl(fileUrl, 0.25);
        } catch (Exception e) {
            return directOcr(imgBytes);
        }

        // 2. 找置信度最高的Text区域
        List<LayoutBox> boxes = layout.getBoxes();
        if (boxes == null || boxes.isEmpty()) return directOcr(imgBytes);

        LayoutBox bestText = boxes.stream()
                .filter(b -> "Text".equalsIgnoreCase(b.getLabel()))
                .max(Comparator.comparingDouble(b -> b.getConfidence() != null ? b.getConfidence() : 0))
                .orElse(null);

        if (bestText == null) return directOcr(imgBytes);

        // 3. 裁剪
        try {
            BufferedImage full = ImageIO.read(new ByteArrayInputStream(imgBytes));
            int x = Math.max(0, (int) Math.floor(bestText.getX1()));
            int y = Math.max(0, (int) Math.floor(bestText.getY1()));
            int w = Math.min(full.getWidth() - x, (int) Math.ceil(bestText.getX2() - bestText.getX1()));
            int h = Math.min(full.getHeight() - y, (int) Math.ceil(bestText.getY2() - bestText.getY1()));
            if (w <= 0 || h <= 0) return directOcr(imgBytes);

            BufferedImage crop = full.getSubimage(x, y, w, h);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(crop, "png", bos);
            return directOcr(bos.toByteArray());
        } catch (Exception e) {
            return directOcr(imgBytes);
        }
    }

    /** 直接OCR */
    private String directOcr(byte[] imgBytes) {
        try {
            OcrResult result = aiServiceClient.recognizeHandwriting(imgBytes, "img.png");
            return result.getText();
        } catch (Exception e) {
            return null;
        }
    }

    // ================================================================
    // Jaccard Bigram
    // ================================================================

    /** 返回0~100 */
    int jaccardSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        String a = clean(s1), b = clean(s2);
        if (a.length() < 2 && b.length() < 2) return a.equals(b) ? 100 : 0;
        if (a.length() < 2 || b.length() < 2) return 0;
        Set<String> bigA = bigrams(a), bigB = bigrams(b);
        if (bigA.isEmpty() || bigB.isEmpty()) return 0;
        Set<String> union = new HashSet<>(bigA); union.addAll(bigB);
        Set<String> inter = new HashSet<>(bigA); inter.retainAll(bigB);
        return (int) Math.round((double) inter.size() / union.size() * 100);
    }

    private String clean(String s) {
        return s.replaceAll("[\\s\\p{P}\\p{S}]", "").toLowerCase();
    }

    private Set<String> bigrams(String s) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < s.length() - 1; i++) set.add(s.substring(i, i + 2));
        return set;
    }

    // ================================================================
    // 辅助
    // ================================================================

    /** 去重：同一student_id只取最新提交，排除当前学生 */
    private List<Submission> getDedupedOthers(Long taskId, Long currentStudentId) {
        List<Submission> all = submissionMapper.selectByTaskId(taskId);
        if (all == null) return List.of();
        Map<Long, Submission> latest = new LinkedHashMap<>();
        for (Submission s : all) {
            Long sid = s.getStudentId();
            if (sid == null || sid.equals(currentStudentId)) continue;
            Submission exist = latest.get(sid);
            if (exist == null || (s.getSubmitTime() != null &&
                    (exist.getSubmitTime() == null || s.getSubmitTime().isAfter(exist.getSubmitTime())))) {
                latest.put(sid, s);
            }
        }
        return new ArrayList<>(latest.values());
    }

    private byte[] loadImage(String fileUrl) throws IOException {
        String path = fileUrl;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path = path.replaceFirst("^https?://[^/]+/", "");
        }
        Path p = Paths.get(path);
        if (!Files.exists(p)) p = Paths.get(System.getProperty("user.dir"), path);
        if (!Files.exists(p)) throw new FileNotFoundException("文件不存在: " + path);
        return Files.readAllBytes(p);
    }

    // ================================================================
    // DTO
    // ================================================================

    @lombok.Data
    public static class CheckResult {
        private String currentStudent;
        private int checkedCount;
        private int maxSimilarity;
        private boolean isCurrentCheated;
        private List<CheckPair> results;

        static CheckResult empty() { return empty(null); }
        static CheckResult empty(String name) {
            CheckResult r = new CheckResult();
            r.currentStudent = name != null ? name : "未知";
            return r;
        }
        static CheckResult noOthers(String name) {
            CheckResult r = new CheckResult();
            r.currentStudent = name;
            r.results = List.of();
            return r;
        }
        static CheckResult success(String name, int count, int maxSim, boolean cheated, List<CheckPair> pairs) {
            CheckResult r = new CheckResult();
            r.currentStudent = name;
            r.checkedCount = count;
            r.maxSimilarity = maxSim;
            r.isCurrentCheated = cheated;
            r.results = pairs;
            return r;
        }
    }

    @lombok.Data
    public static class CheckPair {
        private String studentName;
        private Long studentId;
        private int similarity;
        private boolean isSuspicious;
        private String sourceText;
        private String compareText;
        private String detail;

        CheckPair(Submission other, int sim, String currentText, String otherText) {
            this.studentName = other.getStudentName();
            this.studentId = other.getId();
            this.similarity = sim;
            this.isSuspicious = sim > 80;
            this.sourceText = truncate(currentText);
            this.compareText = truncate(otherText);
            this.detail = "Jaccard bigram 文本相似度比对";
        }
    }

    private static String truncate(String s) {
        return s != null && s.length() > 100 ? s.substring(0, 100) + "..." : (s != null ? s : "");
    }
}
