package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.auth.LoginRequest;
import com.huadi.intelligentteachingplatform.dto.auth.LoginVO;
import com.huadi.intelligentteachingplatform.dto.auth.RegisterRequest;
import com.huadi.intelligentteachingplatform.dto.auth.SessionUserVO;
import com.huadi.intelligentteachingplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ApiResponse.ok("登录成功", authService.login(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(401, e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<SessionUserVO> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return ApiResponse.fail(401, "凭证缺失：请重新登录");
        }
        try {
            return ApiResponse.ok(authService.getSessionUser(authHeader));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(401, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ApiResponse.ok("退出成功", null);
    }

    /**
     * 教师注册接口
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ApiResponse.ok("注册成功", authService.register(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
