package com.huadi.intelligentteachingplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperDetailsDTO {
    private Long id;
    private Long courseId;
    private String title;
    private Integer totalScore;
    private Long classId;
    private Long teacherId;
    private LocalDateTime createTime;
    private List<QuestionInfo> questions;

    @Data
    public static class QuestionInfo {
        private Long questionId;
        private Integer sort;
        private Integer score;
        private String content;
        private String type;
        private String options;
        private String answer;
        private Integer isLlmGenerated;
    }
}
