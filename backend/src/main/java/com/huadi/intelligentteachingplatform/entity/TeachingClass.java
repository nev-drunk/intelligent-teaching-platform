package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_class")
public class TeachingClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("class_name")
    private String className;
    @TableField("teacher_id")
    private Long teacherId;

    @TableField(exist = false)
    private Teacher teacher;
}
