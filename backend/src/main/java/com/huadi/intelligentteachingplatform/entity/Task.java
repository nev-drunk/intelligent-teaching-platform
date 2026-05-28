package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("class_id")
    private Long classId;

    private String title;

    private String type;

    @TableField("content_text")
    private String contentText;

    @TableField("paper_id")
    private Long paperId;

    private LocalDateTime deadline;

    @TableField(exist = false)
    private String courseName;
}