package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private TeacherMapper teacherMapper;

    // 内存 Token 会话存储
    private static final Map<String, Map<String, Object>> TOKEN_SESSION_STORE = new ConcurrentHashMap<>();

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginInfo) {

        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ApiResponse.fail(400, "请输入用户名和密码");
        }

        // 查询数据库
        Teacher teacher = teacherMapper.selectByUsername(username.trim());

        // 校验
        if (teacher == null || !password.equals(teacher.getPassword())) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        // 生成 token
        String token = "MaidCoffeeToken_" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16);

        // 保存会话
        Map<String, Object> sessionUser = new HashMap<>();
        sessionUser.put("teacherId", teacher.getId());
        sessionUser.put("username", teacher.getUsername());
        sessionUser.put("name", teacher.getName());
        sessionUser.put("avatar", teacher.getAvatar());
        sessionUser.put("phone", teacher.getPhone());
        sessionUser.put("role", "ROLE_TEACHER");

        TOKEN_SESSION_STORE.put(token, sessionUser);

        // 返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("teacherId", teacher.getId());
        responseData.put("username", teacher.getUsername());
        responseData.put("name", teacher.getName());
        responseData.put("avatar", teacher.getAvatar());
        responseData.put("role", "ROLE_TEACHER");

        return ApiResponse.ok("登录成功", responseData);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getTeacherInfo(
            @RequestHeader(value = "Authorization", required = false)
            String authHeader) {

        if (authHeader == null || authHeader.isBlank()) {
            return ApiResponse.fail(401, "凭证缺失：请重新登录");
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        Map<String, Object> sessionUser =
                TOKEN_SESSION_STORE.get(token);

        if (sessionUser == null) {
            return ApiResponse.fail(401, "登录状态已过期");
        }

        return ApiResponse.ok(sessionUser);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestHeader(value = "Authorization", required = false)
            String authHeader) {

        if (authHeader != null && !authHeader.isBlank()) {

            String token = authHeader.startsWith("Bearer ")
                    ? authHeader.substring(7)
                    : authHeader;

            TOKEN_SESSION_STORE.remove(token);
        }

        return ApiResponse.ok("退出成功", null);
    }
}