package com.huadi.intelligentteachingplatform.dto.auth;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long teacherId;
    private String username;
    private String name;
    private String avatar;
    private String role;
}
