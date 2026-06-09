package com.huadi.intelligentteachingplatform.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.huadi.intelligentteachingplatform.dto.ai.*;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 微服务 HTTP 客户端
 * 封装所有对 Flask AI 服务 (localhost:5000) 的调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    // ═══════════════════════════════════════════════════════════════
    // 1. OCR 手写字符识别
    // ═══════════════════════════════════════════════════════════════

    /**
     * OCR 识别手写文字 — MultipartFile 版本
     * POST /ocr/recognize
     */
    public OcrResult recognizeHandwriting(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            return postMultipartImage(bytes, getFilename(image), "/ocr/recognize", OcrResult.class);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            throw new BusinessException(500, "读取上传图片失败: " + e.getMessage());
        }
    }

    /**
     * OCR 识别手写文字 — 通过文件 URL 读取本地图片
     * POST /ocr/recognize
     */
    public OcrResult recognizeHandwritingByUrl(String fileUrl) {
        try {
            byte[] bytes = loadImageBytes(fileUrl);
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            return postMultipartImage(bytes, filename, "/ocr/recognize", OcrResult.class);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片文件失败: " + e.getMessage());
        }
    }

    /** byte[] 重载（内部调用兼容） */
    public OcrResult recognizeHandwriting(byte[] imageBytes, String filename) {
        return postMultipartImage(imageBytes, filename, "/ocr/recognize", OcrResult.class);
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. 试卷版面检测
    // ═══════════════════════════════════════════════════════════════

    /**
     * 检测试卷版面 — MultipartFile 版本
     * POST /layout/detect
     */
    public LayoutDetectResult detectPaperLayout(MultipartFile image) {
        return detectPaperLayout(image, 0.35);
    }

    public LayoutDetectResult detectPaperLayout(MultipartFile image, double confidence) {
        try {
            byte[] bytes = image.getBytes();
            MultiValueMap<String, Object> body = buildImageBody(bytes, getFilename(image));
            body.add("confidence", String.valueOf(confidence));
            return postMultipart(body, "/layout/detect", LayoutDetectResult.class);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            throw new BusinessException(500, "读取上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 检测试卷版面 — 通过文件 URL
     * POST /layout/detect
     */
    public LayoutDetectResult detectPaperLayoutByUrl(String fileUrl) {
        return detectPaperLayoutByUrl(fileUrl, 0.35);
    }

    public LayoutDetectResult detectPaperLayoutByUrl(String fileUrl, double confidence) {
        try {
            byte[] bytes = loadImageBytes(fileUrl);
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            MultiValueMap<String, Object> body = buildImageBody(bytes, filename);
            body.add("confidence", String.valueOf(confidence));
            return postMultipart(body, "/layout/detect", LayoutDetectResult.class);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片文件失败: " + e.getMessage());
        }
    }

    /**
     * 比较两份试卷的作答区域相似度
     * POST /layout/compare
     */
    public LayoutCompareResult comparePaperLayout(String fileUrl1, String fileUrl2) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("file_url_1", fileUrl1);
            body.put("file_url_2", fileUrl2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/layout/compare", requestEntity, String.class);

            return parseResponse(response.getBody(), LayoutCompareResult.class);
        } catch (RestClientException e) {
            log.error("调用版面比较服务失败", e);
            throw new BusinessException(500, "AI 版面比较服务不可用: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. 课件内容检测
    // ═══════════════════════════════════════════════════════════════

    /**
     * 检测课件图片内容块
     * POST /courseware/detect
     */
    public CoursewareDetectResult detectCourseware(MultipartFile image) {
        return detectCourseware(image, 0.35);
    }

    public CoursewareDetectResult detectCourseware(MultipartFile image, double confidence) {
        try {
            byte[] bytes = image.getBytes();
            MultiValueMap<String, Object> body = buildImageBody(bytes, getFilename(image));
            body.add("confidence", String.valueOf(confidence));
            return postMultipart(body, "/courseware/detect", CoursewareDetectResult.class);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            throw new BusinessException(500, "读取上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 课件检测 — 通过文件 URL
     */
    public CoursewareDetectResult detectCoursewareByUrl(String fileUrl) {
        try {
            byte[] bytes = loadImageBytes(fileUrl);
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            MultiValueMap<String, Object> body = buildImageBody(bytes, filename);
            return postMultipart(body, "/courseware/detect", CoursewareDetectResult.class);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片文件失败: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. 文本相似度
    // ═══════════════════════════════════════════════════════════════

    /**
     * 比较两段文本的相似度
     * POST /similarity/compare
     */
    public double compareSimilarity(String text1, String text2) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text1", text1);
            body.put("text2", text2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/similarity/compare", requestEntity, String.class);

            SimilarityResult result = parseResponse(response.getBody(), SimilarityResult.class);
            return result != null && result.getScore() != null ? result.getScore() : 0.0;

        } catch (RestClientException e) {
            log.error("调用 AI 相似度服务失败", e);
            throw new BusinessException(500, "AI 相似度服务不可用: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. Top3 相似问题检索
    // ═══════════════════════════════════════════════════════════════

    /**
     * Top3 相似问题 — 候选为 String 列表
     * POST /similarity/top3
     */
    public List<SimilarItem> getTop3Similar(String query, List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("candidates", candidates);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/similarity/top3", requestEntity, String.class);

            Top3Result result = parseResponse(response.getBody(), Top3Result.class);
            return result != null && result.getResults() != null
                    ? result.getResults() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("调用 AI Top3 相似度服务失败", e);
            throw new BusinessException(500, "AI 相似度检索服务不可用: " + e.getMessage());
        }
    }

    /**
     * Top3 相似问题 — 候选为 SimilarCandidate 对象列表（带 id 和 text）
     */
    public List<SimilarItem> getTop3SimilarWithId(String query, List<SimilarCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("candidates", candidates);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/similarity/top3", requestEntity, String.class);

            Top3Result result = parseResponse(response.getBody(), Top3Result.class);
            return result != null && result.getResults() != null
                    ? result.getResults() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("调用 AI Top3 相似度服务失败", e);
            throw new BusinessException(500, "AI 相似度检索服务不可用: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. TTS 语音合成
    // ═══════════════════════════════════════════════════════════════

    /**
     * TTS 语音合成 — 指定音色
     * POST /tts/synthesize
     */
    public String synthesizeSpeech(String text, String voice) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", text);
            body.put("voice", voice != null && !voice.isBlank() ? voice : "Cherry");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiServiceUrl + "/tts/synthesize", requestEntity, String.class);

            JSONObject json = JSON.parseObject(response.getBody());
            if (json.containsKey("error")) {
                log.error("TTS 合成错误: {}", json.getString("error"));
                return null;
            }
            return json.getString("audio_url");

        } catch (RestClientException e) {
            log.error("调用 TTS 语音合成服务失败", e);
            throw new BusinessException(500, "TTS 语音合成服务不可用: " + e.getMessage());
        }
    }

    /**
     * TTS 语音合成 — 默认女声
     */
    public String synthesizeSpeech(String text) {
        return synthesizeSpeech(text, "Cherry");
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. 一体化版面检测 + OCR（新流水线）
    // ═══════════════════════════════════════════════════════════════

    /** 版面检测 → 裁剪作答区域 → 逐区域 OCR */
    public LayoutOcrResult analyzeAndOcr(MultipartFile image) {
        return analyzeAndOcr(image, 0.25);
    }

    public LayoutOcrResult analyzeAndOcr(MultipartFile image, double confidence) {
        try {
            byte[] bytes = image.getBytes();
            MultiValueMap<String, Object> body = buildImageBody(bytes, getFilename(image));
            body.add("confidence", String.valueOf(confidence));
            return postMultipart(body, "/layout/analyze-and-ocr", LayoutOcrResult.class);
        } catch (IOException e) {
            log.error("读取图片失败", e);
            throw new BusinessException(500, "读取图片失败: " + e.getMessage());
        }
    }

    /** 通过 URL 调用一体化版面检测+OCR */
    public LayoutOcrResult analyzeAndOcrByUrl(String fileUrl) {
        try {
            byte[] bytes = loadImageBytes(fileUrl);
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            MultiValueMap<String, Object> body = buildImageBody(bytes, filename);
            body.add("confidence", "0.25");
            return postMultipart(body, "/layout/analyze-and-ocr", LayoutOcrResult.class);
        } catch (IOException e) {
            log.error("读取图片失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片失败: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 8. 旧版兼容（CourseService 仍在使用）
    // ═══════════════════════════════════════════════════════════════

    /** 版面检测 — 直接返回 boxes 列表（CourseService.analyzeLayout 使用） */
    public LayoutResult detectLayout(MultipartFile image) {
        return detectLayout(image, 0.35);
    }

    public LayoutResult detectLayout(MultipartFile image, double confidence) {
        try {
            byte[] bytes = image.getBytes();
            MultiValueMap<String, Object> body = buildImageBody(bytes, getFilename(image));
            body.add("confidence", String.valueOf(confidence));
            return postMultipart(body, "/layout/detect", LayoutResult.class);
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            throw new BusinessException(500, "读取上传图片失败: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 私有辅助方法
    // ═══════════════════════════════════════════════════════════════

    private String getFilename(MultipartFile file) {
        return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.png";
    }

    private MultiValueMap<String, Object> buildImageBody(byte[] bytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "image.png";
            }
        });
        return body;
    }

    private <T> T postMultipartImage(byte[] bytes, String filename, String endpoint, Class<T> targetType) {
        MultiValueMap<String, Object> body = buildImageBody(bytes, filename);
        return postMultipart(body, endpoint, targetType);
    }

    private <T> T postMultipart(MultiValueMap<String, Object> body, String endpoint, Class<T> targetType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                aiServiceUrl + endpoint, requestEntity, String.class);
        return parseResponse(response.getBody(), targetType);
    }

    /**
     * 根据文件 URL 将本地图片读取为 byte[]
     */
    private byte[] loadImageBytes(String fileUrl) throws IOException {
        String filePath = fileUrl;
        // 去掉可能的 URL 前缀
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            filePath = filePath.replaceFirst("^https?://[^/]+/", "");
        }

        // 尝试多个路径
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            path = Paths.get(System.getProperty("user.dir"), filePath);
        }
        if (!Files.exists(path)) {
            path = Paths.get(System.getProperty("user.dir"), "uploads", filePath);
        }
        if (!Files.exists(path)) {
            throw new BusinessException(404, "图片文件不存在: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    /**
     * 统一解析 Flask 返回的 JSON 响应
     */
    private <T> T parseResponse(String body, Class<T> targetType) {
        if (body == null || body.isBlank()) {
            throw new BusinessException(500, "AI 服务返回空响应");
        }

        JSONObject json = JSON.parseObject(body);
        if (json.containsKey("error")) {
            String errorMsg = json.getString("error");
            throw new BusinessException(500, "AI 服务错误: " + errorMsg);
        }

        return JSON.parseObject(body, targetType);
    }
}
