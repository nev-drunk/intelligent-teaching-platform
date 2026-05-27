package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Teacher {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String phone;       // 💥 完美对应你的 tb_teacher.phone
    private Date createTime;    // 💥 完美对应你的 tb_teacher.create_time
}