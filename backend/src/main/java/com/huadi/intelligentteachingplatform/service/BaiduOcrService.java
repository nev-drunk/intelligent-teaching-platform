package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.config.BaiduOcrProperties;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 百度OCR核心服务类
 * 实现Token自动缓存与刷新机制，提供通用文字识别能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduOcrService {

    private final BaiduOcrProperties baiduOcrProperties;
    private final RestTemplate baiduOcrRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 缓存的 Access Token
     */
    private volatile String accessToken;

    /**
     * Token 过期时间（时间戳，毫秒）
     */
    private volatile long tokenExpireTime;

    /**
     * 双重检查锁，保证线程安全
     */
    private final ReentrantLock tokenLock = new ReentrantLock();

    /**
     * Token 有效期：25天（比官方30天少5天，留有余地）
     */
    private static final long TOKEN_VALID_DAYS = 25;
    private static final long TOKEN_VALID_MILLIS = TOKEN_VALID_DAYS * 24 * 60 * 60 * 1000L;

    /**
     * 执行OCR识别
     *
     * @param base64Image Base64编码的图片数据
     * @return 识别出的文本内容
     */
    public String executeOcr(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new BusinessException(400, "图片数据不能为空");
        }

        try {
            String token = getAccessToken();
            return callOcrApi(token, base64Image);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR识别失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "OCR识别服务异常，请稍后重试");
        }
    }

    /**
     * 获取AccessToken（带缓存机制）
     *
     * @return AccessToken
     */
    private String getAccessToken() {
        // 双重检查锁，减少锁竞争
        if (isTokenValid()) {
            return accessToken;
        }

        tokenLock.lock();
        try {
            // 再次检查，防止重复获取
            if (isTokenValid()) {
                return accessToken;
            }

            log.info("开始获取百度OCR AccessToken...");
            return fetchAndCacheToken();
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * 检查Token是否有效
     */
    private boolean isTokenValid() {
        return accessToken != null && !accessToken.isEmpty() && Instant.now().toEpochMilli() < tokenExpireTime;
    }

    /**
     * 从百度服务器获取Token并缓存
     */
    private String fetchAndCacheToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "client_credentials");
            params.add("client_id", baiduOcrProperties.getApiKey());
            params.add("client_secret", baiduOcrProperties.getSecretKey());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = baiduOcrRestTemplate.exchange(
                    baiduOcrProperties.getTokenUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("获取Token失败，HTTP状态码: {}", response.getStatusCode());
                throw new BusinessException(500, "获取OCR服务Token失败");
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String newToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;

            if (newToken == null || newToken.isEmpty()) {
                log.error("获取Token失败，响应内容: {}", response.getBody());
                throw new BusinessException(500, "获取OCR服务Token失败");
            }

            // 缓存Token并设置过期时间
            accessToken = newToken;
            tokenExpireTime = Instant.now().toEpochMilli() + TOKEN_VALID_MILLIS;
            log.info("获取百度OCR AccessToken成功，有效期至: {}", Instant.ofEpochMilli(tokenExpireTime));

            return accessToken;
        } catch (RestClientException e) {
            log.error("调用百度Token接口失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "调用OCR认证服务失败，请检查网络连接");
        } catch (Exception e) {
            log.error("解析Token响应失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "解析OCR认证响应失败");
        }
    }

    /**
     * 调用百度OCR API进行文字识别
     */
    private String callOcrApi(String token, String base64Image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image", base64Image);
            // 添加语言类型，默认为CHN_ENG（中英文混合）
            params.add("language_type", "CHN_ENG");

            String url = baiduOcrProperties.getOcrUrl() + "?access_token=" + token;
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = baiduOcrRestTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("OCR API调用失败，HTTP状态码: {}", response.getStatusCode());
                throw new BusinessException(500, "OCR识别服务调用失败");
            }

            return parseOcrResult(response.getBody());
        } catch (RestClientException e) {
            log.error("调用百度OCR接口失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "调用OCR识别服务失败，请检查网络连接");
        } catch (Exception e) {
            log.error("解析OCR响应失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "解析OCR识别结果失败");
        }
    }

    /**
     * 解析OCR识别结果
     */
    private String parseOcrResult(String responseBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // 检查错误码
        if (jsonNode.has("error_code")) {
            int errorCode = jsonNode.get("error_code").asInt();
            String errorMsg = jsonNode.has("error_msg") ? jsonNode.get("error_msg").asText() : "未知错误";
            log.error("OCR识别失败，错误码: {}, 错误信息: {}", errorCode, errorMsg);

            // 根据错误码处理
            switch (errorCode) {
                case 110:
                case 111:
                    // Token过期或无效，强制刷新
                    tokenLock.lock();
                    try {
                        accessToken = null;
                        tokenExpireTime = 0;
                    } finally {
                        tokenLock.unlock();
                    }
                    throw new BusinessException(401, "OCR认证已过期，请重试");
                case 100:
                    throw new BusinessException(400, "API Key或Secret Key错误");
                case 216100:
                case 216101:
                case 216102:
                    throw new BusinessException(400, "图片格式错误或图片为空");
                default:
                    throw new BusinessException(500, "OCR识别失败: " + errorMsg);
            }
        }

        // 解析识别结果
        StringBuilder result = new StringBuilder();
        JsonNode wordsResult = jsonNode.get("words_result");
        if (wordsResult != null && wordsResult.isArray()) {
            for (JsonNode item : wordsResult) {
                String words = item.has("words") ? item.get("words").asText() : "";
                if (!words.isEmpty()) {
                    if (result.length() > 0) {
                        result.append("\n");
                    }
                    result.append(words);
                }
            }
        }

        String text = result.toString().trim();
        log.debug("OCR识别结果: {}", text);
        return text;
    }
}