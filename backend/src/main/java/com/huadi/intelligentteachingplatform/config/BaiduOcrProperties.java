package com.huadi.intelligentteachingplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度 OCR 配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "baidu.ocr")
public class BaiduOcrProperties {

    /**
     * 百度云 App ID
     */
    private String appId;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;

    /**
     * Token 获取 URL
     */
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";

    /**
     * 通用文字识别 URL
     */
    private String ocrUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
}