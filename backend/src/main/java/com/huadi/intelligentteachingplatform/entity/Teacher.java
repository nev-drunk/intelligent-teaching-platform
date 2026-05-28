package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String phone;
    @TableField("create_time")
    private LocalDateTime createTime;
}
