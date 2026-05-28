package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("student_name")
    private String studentName;
    @TableField("class_id")
    private Long classId;
    @TableField("student_no")
    private String studentNo;
    @TableField("create_time")
    private LocalDateTime createTime;
}