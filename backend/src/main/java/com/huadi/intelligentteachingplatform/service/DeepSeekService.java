package com.huadi.intelligentteachingplatform.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 通用的大模型文本生成核心方法
     * @param systemPrompt 给 AI 设定的角色/格式约束
     * @param userPrompt 具体的题目关键词或需求
     * @return AI 吐出来的干净字符串（通常是符合格式的 JSON）
     */
    public String generateText(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 自动读取配置文件的 Key
        headers.set("Authorization", "Bearer " + apiKey);

        // 完美匹配 DeepSeek 官方标准请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            JSONObject jsonObject = JSON.parseObject(response.getBody());

            // 解析choices[0].message.content
            JSONArray choices = jsonObject.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                return choices.getJSONObject(0).getJSONObject("message").getString("content").trim();
            }
            throw new RuntimeException("大模型没有返回有效的 choices 结果");
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek 通信阻断: " + e.getMessage(), e);
        }
    }
}