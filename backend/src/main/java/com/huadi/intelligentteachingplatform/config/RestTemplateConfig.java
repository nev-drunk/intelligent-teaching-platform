package com.huadi.intelligentteachingplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 配置连接超时和读取超时，防止因第三方服务波动导致后端线程阻塞
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final BaiduOcrProperties baiduOcrProperties;

    @Bean("baiduOcrRestTemplate")
    public RestTemplate baiduOcrRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(baiduOcrProperties.getConnectionTimeout());
        factory.setReadTimeout(baiduOcrProperties.getReadTimeout());
        return new RestTemplate(factory);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}