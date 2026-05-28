package com.huadi.intelligentteachingplatform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 教师注册请求DTO
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}