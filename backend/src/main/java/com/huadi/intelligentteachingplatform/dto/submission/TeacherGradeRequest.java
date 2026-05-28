package com.huadi.intelligentteachingplatform.dto.submission;

import lombok.Data;

/**
 * 教师批改请求DTO
 */
@Data
public class TeacherGradeRequest {

    /**
     * 提交记录ID
     */
    private Long submissionId;

    /**
     * 教师评分
     */
    private Integer teacherScore;

    /**
     * 教师评语
     */
    private String teacherComment;
}