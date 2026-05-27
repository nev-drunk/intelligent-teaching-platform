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
@CrossOrigin(origins = "*") // 允许跨域（可根据项目实际安全需求保留或移除）
public class AuthController {

    @Autowired
    private TeacherMapper teacherMapper;

    // 内存令牌存储，确保线程安全
    private static final Map<String, Map<String, Object>> TOKEN_SESSION_STORE = new ConcurrentHashMap<>();

    /**
     * 1. 教师登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ApiResponse.fail(400, "请输入用户名和密码");
        }

        // 从数据库动态查询教师信息
        Teacher teacher = teacherMapper.selectByUsername(username.trim());

        // 校验是否存在该教师以及密码是否匹配
        if (teacher == null || !password.equals(teacher.getPassword())) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        // 生成高安全性 Token 凭证（避免直接暴露自增 ID）
        String token = "MaidCoffeeToken_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);

        // 代码层注入角色 "ROLE_TEACHER"，无需污染数据库表结构，完美契合前端路由守卫
        Map<String, Object> sessionUser = new HashMap<>();
        sessionUser.put("teacherId", teacher.getId());
        sessionUser.put("username", teacher.getUsername());
        sessionUser.put("name", teacher.getName());
        sessionUser.put("avatar", teacher.getAvatar());
        sessionUser.put("phone", teacher.getPhone());
        sessionUser.put("role", "ROLE_TEACHER");

        // 写入缓存
        TOKEN_SESSION_STORE.put(token, sessionUser);

        // 组装返回给前端的数据体
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
     * 2. 获取当前登录教师详情（供前端路由、拦截器同步状态使用）
     * 兼容直接传 Token 或带 "Bearer " 前缀的规范请求头
     */
    @GetMapping("/me") // 如果前端已经绑定了 /info，这里可以改成 "/info"
    public ApiResponse<Map<String, Object>> getTeacherInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return ApiResponse.fail(401, "凭证缺失：请重新登录");
        }

        // 提取 Token，兼容标准 Bearer 格式
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Map<String, Object> sessionUser = TOKEN_SESSION_STORE.get(token);

        if (sessionUser == null) {
            return ApiResponse.fail(401, "凭证失效：当前的登录状态已过期");
        }

        return ApiResponse.ok(sessionUser);
    }

    /**
     * 3. 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            TOKEN_SESSION_STORE.remove(token);
        }
        return ApiResponse.ok("会话注销成功", null);
    }
}