package com.huadi.intelligentteachingplatform.dto.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentVO {

    /**
     * 学生ID
     */
    private Long id;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}