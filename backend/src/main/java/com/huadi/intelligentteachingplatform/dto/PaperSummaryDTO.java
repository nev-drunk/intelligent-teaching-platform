package com.huadi.intelligentteachingplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperSummaryDTO {
    private Long id;
    private String title;
    private Integer totalScore;
    private Long classId;
    private LocalDateTime createTime;
    private Integer questionCount;
}
