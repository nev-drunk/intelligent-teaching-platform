package com.huadi.intelligentteachingplatform.dto.auth;

import lombok.Data;

@Data
public class SessionUserVO {
    private Long teacherId;
    private String username;
    private String name;
    private String avatar;
    private String phone;
    private String role;
}
