package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问卷调查表 - 对应 tb_questionnaire
 */
@Data
@TableName("tb_questionnaire")
public class Questionnaire {
    private Long id;
    private Long teacherId;
    private Long classId;
    private Long courseId;
    private String title;
    private String contentJson;
    private Integer status;
    private LocalDateTime createTime;
    private Integer responseCount;
    private Double avgScore;
}
