package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生问卷答案提交表 - 对应 tb_questionnaire_answer
 */
@Data
public class QuestionnaireAnswer {
    private Long id;
    private Long questionnaireId;
    private Long studentId;
    private String studentName;
    private String scoresJson;
    private BigDecimal totalScore;
    private LocalDateTime submitTime;
}
