package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String phone;
    private LocalDateTime createTime;
}