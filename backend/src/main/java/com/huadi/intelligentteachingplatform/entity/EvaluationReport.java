package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教学效果评价及大模型诊断报告表 - 对应 tb_evaluation_report
 */
@Data
@TableName("tb_evaluation_report")
public class EvaluationReport {
    private Long id;
    private Long teacherId;
    private Long courseId;
    private String courseName;
    private String teacherName;
    private BigDecimal avgSatisfaction;
    private Integer responseCount;
    private String llmAnalysisReport;
    private LocalDateTime generateTime;
}