package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.dto.ai.*;
import com.huadi.intelligentteachingplatform.entity.*;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.huadi.intelligentteachingplatform.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 作业提交服务类
 * 提供作业提交、AI批改、教师复核等核心业务功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final TaskMapper taskMapper;
    private final QuestionMapper questionMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final BaiduOcrService baiduOcrService;
    private final AiServiceClient aiServiceClient;
    private final DeepSeekService deepSeekService;

    /** 提交状态常量 */
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_AI_PROCESSED = "AI_PROCESSED";
    public static final String STATUS_GRADED = "GRADED";

    // ── 查询方法 ──────────────────────────────────────────────────

    public List<Submission> getSubmissionsByTeacherId(Long teacherId) {
        return submissionMapper.selectByTeacherId(teacherId);
    }

    public Submission getSubmissionById(Long id) {
        return submissionMapper.selectById(id);
    }

    public List<Submission> getSubmissionsWithTaskInfo(Long taskId, Integer page, Integer size) {
        return submissionMapper.selectSubmissionsWithTaskInfo(taskId, page, size);
    }

    public long countSubmissions(Long taskId) {
        return submissionMapper.countSubmissions(taskId);
    }

    public Submission saveSubmission(Submission submission) {
        if (submission.getId() == null) {
            submissionMapper.insert(submission);
        } else {
            submissionMapper.updateById(submission);
        }
        return submission;
    }

    public void deleteSubmission(Long id) {
        submissionMapper.deleteById(id);
    }

    // ── 教师批改（旧版兼容） ──────────────────────────────────────

    public Submission updateComment(Long id, String comment, Integer score) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }
        submission.setTeacherComment(comment);
        if (score != null) {
            submission.setTeacherScore(score);
        }
        submission.setStatus(STATUS_GRADED);
        submissionMapper.updateById(submission);
        return submission;
    }

    // ═══════════════════════════════════════════════════════════════
    // AI 全自动批改（唯一入口）
    // ═══════════════════════════════════════════════════════════════

    /**
     * AI 全自动批改 — 集成 Flask AI 微服务
     *
     * 流程:
     * 1. 查 tb_submission 获取 file_url 和 task_id
     * 2. 通过 task_id 查 tb_task 获取 paper_id
     * 3. 通过 paper_id 查 tb_paper_question + tb_question 获取题目和答案
     * 4. 调用 recognizeHandwritingByUrl → 存 ocr_raw_text
     * 5. 按题型比对计分
     * 6. 加权计算总分 → 存 ai_score
     * 7. 生成评语 → 存 ai_comment
     * 8. 调用 detectPaperLayoutByUrl → 存 plagiarism_rate
     * 9. anomaly_score > 0.5 → is_cheated = 1
     * 10. status → AI_PROCESSED
     */
    @Transactional(rollbackFor = Exception.class)
    public Submission autoGrade(Long submissionId) {
        log.info("开始 AI 全自动批改，提交ID: {}", submissionId);

        // 1. 查询提交记录
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 1.5 检查任务是否关联试卷
        String fileUrl = submission.getFileUrl();
        boolean hasImage = fileUrl != null && !fileUrl.trim().isEmpty();
        boolean hasSubmitText = submission.getSubmitText() != null && !submission.getSubmitText().trim().isEmpty();

        if (!hasImage && !hasSubmitText) {
            // 既无图片也无文本 → 无法批改
            submission.setAiComment("⚠️ 该提交无图片文件也无提交文本，无法进行AI批改，请教师手动批改");
            submission.setAiScore(0);
            submission.setStatus(STATUS_AI_PROCESSED);
            submissionMapper.updateById(submission);
            log.warn("AI批改中止，提交ID: {} — 无图片无文本", submissionId);
            return submission;
        }

        // 2. 一体化流水线：版面检测 → 裁剪作答区 → 逐区域 OCR
        String ocrText = null;
        String layoutJson = null;

        if (hasImage) {
            try {
                // 调用一体化端点
                LayoutOcrResult pipelineResult = aiServiceClient.analyzeAndOcrByUrl(fileUrl);
                ocrText = pipelineResult.getCombinedText();
                layoutJson = com.alibaba.fastjson2.JSON.toJSONString(pipelineResult);
                log.info("版面+OCR流水线完成，提交ID: {}, {}框, {}OCR区域",
                        submissionId,
                        pipelineResult.getLayoutBoxes() != null ? pipelineResult.getLayoutBoxes().size() : 0,
                        pipelineResult.getOcrRegions() != null ? pipelineResult.getOcrRegions().size() : 0);
            } catch (Exception e) {
                log.warn("版面+OCR流水线失败，回退百度OCR，提交ID: {}", submissionId, e);
                try {
                    ocrText = baiduOcrService.executeOcr(convertFileToBase64(fileUrl));
                } catch (Exception ex) {
                    ocrText = submission.getSubmitText();
                }
            }
        } else {
            ocrText = submission.getSubmitText();
            log.info("提交ID: {} — 无图片文件，使用提交文本", submissionId);
        }

        // 3. 保存 OCR 结果（含版面检测JSON供前端可视化）
        if (!hasImage && ocrText != null) {
            submission.setOcrRawText("{\"note\":\"无图片文件\",\"text\":\"" + ocrText + "\"}");
        } else if (layoutJson != null) {
            submission.setOcrRawText(layoutJson);
        } else {
            submission.setOcrRawText(ocrText);
        }

        // 4. 与标准答案比对计分
        int totalScore = 0;
        int correctCount = 0;
        int totalQuestions = 0;
        int maxScore = 0;
        String aiComment;

        if (ocrText != null && !ocrText.trim().isEmpty()) {
            try {
                Task task = taskMapper.selectById(submission.getTaskId());
                if (task != null && task.getPaperId() != null) {
                    // 获取试卷关联的题目和分值
                    List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                            new LambdaQueryWrapper<PaperQuestion>()
                                    .eq(PaperQuestion::getPaperId, task.getPaperId()));
                    if (paperQuestions != null && !paperQuestions.isEmpty()) {
                        totalQuestions = paperQuestions.size();
                        for (PaperQuestion pq : paperQuestions) {
                            Question question = questionMapper.selectById(pq.getQuestionId());
                            if (question == null) continue;

                            int qScore = pq.getScore() != null ? pq.getScore() : 0;
                            maxScore += qScore;

                            boolean isCorrect = checkAnswer(question, ocrText);
                            if (isCorrect) {
                                correctCount++;
                                totalScore += qScore;
                            }
                        }
                    }
                    aiComment = generateDetailedComment(totalQuestions, correctCount, totalScore, maxScore);
                } else {
                    // 无试卷关联 → 按任务类型处理
                    String taskType = task.getType();
                    if ("CHOICE".equalsIgnoreCase(taskType)) {
                        // 选择题作业：教师上传答案模板，逐一比对
                        String answerTemplate = task.getContentText();
                        String[] answers = parseAnswerTemplate(answerTemplate);
                        if (answers != null && answers.length > 0) {
                            totalQuestions = answers.length;
                            maxScore = answers.length * 10; // 每题10分
                            for (int i = 0; i < answers.length; i++) {
                                String expected = answers[i].trim().toUpperCase();
                                // 在OCR文本中查找对应位置的答案
                                String studentAnswer = extractChoiceAnswer(ocrText, i);
                                if (studentAnswer != null && studentAnswer.equals(expected)) {
                                    correctCount++;
                                    totalScore += 10;
                                }
                            }
                            aiComment = String.format("选择题作业：共%d题，答对%d题，得分%d/%d",
                                    totalQuestions, correctCount, totalScore, maxScore);
                        } else {
                            aiComment = "未找到答案模板，请教师在任务描述中以 [ANSWERS:B,D,A,C] 格式设置";
                        }
                    } else if ("HOMEWORK".equalsIgnoreCase(taskType)) {
                        // 文本作业 → DeepSeek 智能判分
                        String prompt = task.getContentText();
                        try {
                            String sysPrompt = "你是作业批改助手。根据作业要求和学生提交内容，给出0-100的分数和简短评语。返回JSON: {\"score\":85,\"comment\":\"评语\"}";
                            String userPrompt = String.format("作业要求：%s\n学生提交：%s",
                                    prompt != null ? prompt : "无具体要求",
                                    ocrText != null ? ocrText : (submission.getSubmitText() != null ? submission.getSubmitText() : "无"));
                            String result = deepSeekService.generateText(sysPrompt, userPrompt);
                            // 解析DeepSeek返回的JSON
                            var json = com.alibaba.fastjson2.JSON.parseObject(result);
                            if (json != null) {
                                totalScore = json.getIntValue("score");
                                aiComment = "【DeepSeek智能评阅】" + json.getString("comment");
                            } else {
                                totalScore = 60;
                                aiComment = "DeepSeek 评阅完成，请教师复核。";
                            }
                        } catch (Exception e) {
                            log.warn("DeepSeek作业评阅失败: {}", e.getMessage());
                            totalScore = 60;
                            aiComment = "DeepSeek评阅异常，请教师手动批改。";
                        }
                        maxScore = 100;
                        totalQuestions = 1;
                        correctCount = totalScore >= 60 ? 1 : 0;
                    } else {
                        // PRACTICE 或其他类型
                        try {
                            int score = calculateAiScore(submission.getTaskId(), ocrText);
                            totalScore = score;
                            aiComment = generateAiComment(score);
                        } catch (Exception e) {
                            aiComment = "自动批改异常，请教师手动复核";
                        }
                    }
                }
                log.info("AI 评分完成，提交ID: {}, 得分: {}/{}, 正确: {}/{}",
                        submissionId, totalScore, maxScore, correctCount, totalQuestions);
            } catch (Exception e) {
                log.warn("AI 评分异常，提交ID: {}, 原因: {}", submissionId, e.getMessage());
                aiComment = "自动批改异常，请教师手动复核";
            }
        } else {
            aiComment = "未检测到作答内容，请教师手动复核";
        }

        // 附加图片状态提示
        if (!hasImage) {
            aiComment = "⚠️ 该提交无图片文件（仅基于文本批改）。" + aiComment;
        }

        // 调用 DeepSeek 生成 AI 评语
        String deepSeekReview = null;
        try {
            String systemPrompt = "你是一个高校教师助手。请根据学生的作答情况，用中文写一段50-100字的评语。" +
                    "语气专业、鼓励性，指出优缺点。只返回评语文本，不要加前缀。";
            String userPrompt = String.format(
                    "学生作答内容：%s\n总分：%d/%d，答对%d/%d题",
                    ocrText != null ? ocrText.substring(0, Math.min(ocrText.length(), 300)) : "无",
                    totalScore, maxScore, correctCount, totalQuestions);
            deepSeekReview = deepSeekService.generateText(systemPrompt, userPrompt);
            log.info("DeepSeek 评语生成成功，提交ID: {}", submissionId);
        } catch (Exception e) {
            log.warn("DeepSeek 评语生成失败，使用默认评语: {}", e.getMessage());
        }

        String finalComment = aiComment;
        if (deepSeekReview != null && !deepSeekReview.isBlank()) {
            finalComment = aiComment + " 【AI评语】" + deepSeekReview;
        }

        submission.setAiScore(totalScore);
        submission.setAiComment(finalComment);

        // 5. 抄袭痕迹 / 版面异常检测
        BigDecimal plagiarismRate = BigDecimal.ZERO;
        Integer isCheated = 0;
        if (hasImage) {
            try {
                LayoutDetectResult layoutResult = aiServiceClient.detectPaperLayoutByUrl(fileUrl);
                double anomalyScore = layoutResult.getAnomalyScore() != null
                        ? layoutResult.getAnomalyScore() : 0.0;
                plagiarismRate = BigDecimal.valueOf(Math.round(anomalyScore * 10000.0) / 100.0);
                isCheated = anomalyScore > 0.5 ? 1 : 0;
                log.info("异常检测完成，提交ID: {}, 异常分数: {}, 抄袭率: {}, 是否作弊: {}",
                        submissionId, anomalyScore, plagiarismRate, isCheated);
            } catch (Exception e) {
                log.warn("异常检测失败，提交ID: {}, 原因: {}", submissionId, e.getMessage());
            }
        }

        // 6. 更新提交记录
        submission.setPlagiarismRate(plagiarismRate);
        submission.setIsCheated(isCheated);
        submission.setStatus(STATUS_AI_PROCESSED);
        submissionMapper.updateById(submission);

        log.info("AI 全自动批改完成，提交ID: {}, AI得分: {}, 抄袭率: {}%, 状态: {}",
                submissionId, totalScore, plagiarismRate, submission.getStatus());
        return submission;
    }

    // ═══════════════════════════════════════════════════════════════
    // 教师复核（含 TTS 语音评语生成）
    // ═══════════════════════════════════════════════════════════════

    /**
     * 教师复核评分 — 含 TTS 语音评语合成
     *
     * 1. 更新 teacher_score 和 teacher_comment
     * 2. 调用 synthesizeSpeech(comment) 生成音频 → 存 ai_review_voice_url
     * 3. status 改为 GRADED
     */
    @Transactional(rollbackFor = Exception.class)
    public Submission saveTeacherGrade(Long submissionId, Integer score, String comment) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        submission.setTeacherScore(score);
        submission.setTeacherComment(comment);

        // 调用 TTS 合成教师评语语音
        if (comment != null && !comment.trim().isEmpty()) {
            try {
                String audioUrl = aiServiceClient.synthesizeSpeech(comment);
                if (audioUrl != null) {
                    submission.setAiReviewVoiceUrl(audioUrl);
                    log.info("TTS 语音评语生成成功，提交ID: {}, 音频URL: {}", submissionId, audioUrl);
                }
            } catch (Exception e) {
                log.warn("TTS 语音合成失败，提交ID: {}, 原因: {}", submissionId, e.getMessage());
                // TTS 失败不影响主流程
            }
        }

        submission.setStatus(STATUS_GRADED);
        submissionMapper.updateById(submission);

        log.info("教师复核完成，提交ID: {}, 评分: {}, 状态: {}", submissionId, score, STATUS_GRADED);
        return submission;
    }

    // ═══════════════════════════════════════════════════════════════
    // 答案匹配逻辑
    // ═══════════════════════════════════════════════════════════════

    /**
     * 根据题目类型匹对答案
     */
    private boolean checkAnswer(Question question, String ocrText) {
        String type = question.getType();
        String answer = question.getAnswer();
        if (answer == null || answer.trim().isEmpty()) return false;

        switch (type.toUpperCase()) {
            case "SINGLE":
            case "JUDGE":
                // 单选题和判断题：直接字符串匹配（忽略大小写和空格）
                return matchSingleAnswer(ocrText, answer);
            case "MULTI":
                // 多选题：排序后比对
                return matchMultiAnswer(ocrText, answer);
            case "GAP":
            case "ESSAY":
                // 填空/简答：相似度 >= 0.7 算对
                return matchSimilarityAnswer(ocrText, answer);
            default:
                return ocrText.contains(answer.trim());
        }
    }

    private boolean matchSingleAnswer(String ocrText, String answer) {
        String normalizedOcr = ocrText.toLowerCase().replaceAll("\\s+", "");
        String normalizedAnswer = answer.toLowerCase().replaceAll("\\s+", "");
        return normalizedOcr.contains(normalizedAnswer)
                || normalizedOcr.contains(normalizedAnswer.replace(".", ""));
    }

    private boolean matchMultiAnswer(String ocrText, String answer) {
        String normalizedOcr = ocrText.toLowerCase().replaceAll("\\s+", "");
        // 假设多选题答案格式为 "A,B,C" 或 "ABC"
        String[] answers = answer.toLowerCase().split("[,，]");
        String[] ocrParts = normalizedOcr.split("[,，]");

        // 排序后比对
        List<String> sortedAnswers = Arrays.stream(answers)
                .map(s -> s.trim().replace(".", ""))
                .sorted()
                .toList();
        List<String> sortedOcr = Arrays.stream(ocrParts)
                .map(s -> s.trim().replace(".", ""))
                .sorted()
                .toList();

        return sortedAnswers.equals(sortedOcr);
    }

    private boolean matchSimilarityAnswer(String ocrText, String answer) {
        try {
            double score = aiServiceClient.compareSimilarity(ocrText, answer);
            return score >= 0.7;
        } catch (Exception e) {
            log.warn("相似度计算失败，回退为包含匹配", e);
            return ocrText.toLowerCase().contains(answer.toLowerCase().trim());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════

    private String generateDetailedComment(int totalQuestions, int correctCount, int score, int maxScore) {
        return String.format("共%d题，答对%d题，得分%d/%d", totalQuestions, correctCount, score, maxScore);
    }

    private String generateAiComment(Integer score) {
        if (score == null || score < 0) return "自动批改未完成";
        // 百分制评语
        if (score >= 90) return "回答优秀，正确率很高！";
        else if (score >= 70) return "回答良好，继续加油！";
        else if (score >= 60) return "回答及格，需要加强练习。";
        else return "回答较差，请认真复习相关知识。";
    }

    private Integer calculateAiScore(Long taskId, String ocrText) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) return 0;
        if (task.getPaperId() != null) {
            return calculateScoreByPaper(task.getPaperId(), ocrText);
        }
        return 0;
    }

    private Integer calculateScoreByPaper(Long paperId, String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) return 0;
        List<Question> questions = questionMapper.selectByPaperId(paperId);
        if (questions == null || questions.isEmpty()) return 0;

        int totalScore = 0;
        for (Question question : questions) {
            if (checkAnswer(question, ocrText)) {
                totalScore += question.getScore() != null ? question.getScore() : 10;
            }
        }
        return totalScore;
    }

    private byte[] loadImageBytes(String fileUrl) throws IOException {
        String filePath = fileUrl;
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            filePath = filePath.replaceFirst("^https?://[^/]+/", "");
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            path = Paths.get(System.getProperty("user.dir"), filePath);
        }
        if (!Files.exists(path)) {
            throw new BusinessException(404, "图片文件不存在: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    private String convertFileToBase64(String fileUrl) {
        try {
            byte[] fileBytes = loadImageBytes(fileUrl);
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片文件失败: " + e.getMessage());
        }
    }

    /**
     * 从任务描述中解析选择题答案模板
     * 格式: [ANSWERS:B,D,A,C,B,A,D,C] 或直接 B,D,A,C
     */
    private String[] parseAnswerTemplate(String contentText) {
        if (contentText == null || contentText.isBlank()) return null;
        // 尝试匹配 [ANSWERS:...] 格式
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\[ANSWERS:\\s*([A-Da-d,]+)\\]", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(contentText);
        if (m.find()) {
            return m.group(1).split("[,，]");
        }
        // 尝试直接解析逗号分隔的字母
        if (contentText.matches("^[A-Da-d,，\\s]+$")) {
            return contentText.split("[,，]");
        }
        return null;
    }

    /**
     * 从OCR文本中提取第i题的答案字母
     */
    private String extractChoiceAnswer(String ocrText, int index) {
        if (ocrText == null || ocrText.isBlank()) return null;
        // OCR结果可能包含多个字符，取所有识别到的字母
        String cleaned = ocrText.replaceAll("[^A-Da-d]", "").toUpperCase();
        if (index < cleaned.length()) {
            return String.valueOf(cleaned.charAt(index));
        }
        // 回退：在原文中搜索
        String[] parts = ocrText.split("\\s+");
        if (index < parts.length) {
            String p = parts[index].trim().toUpperCase();
            if (p.length() == 1 && p.charAt(0) >= 'A' && p.charAt(0) <= 'D') {
                return p;
            }
        }
        return null;
    }
}
