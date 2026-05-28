package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_name")
    private String courseName;

    @TableField("teacher_id")
    private Long teacherId;

    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;
}