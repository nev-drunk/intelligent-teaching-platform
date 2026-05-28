package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问题中心答疑表 - 对应 tb_issue_center
 */
@Data
public class Issue {
    private Long id;
    private Long courseId;
    private Long studentId;
    private String studentName;
    private String questionText;
    private String aiSuggestedAnswer;
    private String teacherReply;
    private Integer status;
    private LocalDateTime createTime;
}
