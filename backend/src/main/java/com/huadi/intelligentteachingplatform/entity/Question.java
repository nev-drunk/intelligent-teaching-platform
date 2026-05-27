package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Long id;
    private Long courseId;
    private String type; // SINGLE, MULTI, JUDGE, GAP, ESSAY
    private String content;
    private String options; // 数据库中存的是 JSON 字符串，Java 里用 String 接收
    private String answer;
    private Integer isLlmGenerated;
    private String asrAudioUrl;
    private LocalDateTime createTime;
}