package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private TeacherMapper teacherMapper;
    private static final Map<String, Map<String, Object>> TOKEN_SESSION_STORE = new ConcurrentHashMap<>();

    /**
     * 1. 教师登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "认证失败：用户名或密码不能为空"));
        }

        //初始化进去的用户名（比如 admin）从数据库动态查询
        Teacher teacher = teacherMapper.selectByUsername(username.trim());

        // 校验是否存在该教师以及密码是否匹配
        if (teacher == null || !password.equals(teacher.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", 401, "msg", "认证失败：用户名或密码错误"));
        }

        // ⚡ 生成高规格安全 Token 凭证
        String token = "MaidCoffeeToken_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);

        // 💡 核心外挂设计：因为你的表全是老师，我们直接在代码层给前端注入 "ROLE_TEACHER"
        // 这样你的数据库完全不需要额外加字段，前端路由守卫也能完美识别放行！
        Map<String, Object> sessionUser = new HashMap<>();
        sessionUser.put("teacherId", teacher.getId());
        sessionUser.put("username", teacher.getUsername());
        sessionUser.put("name", teacher.getName());
        sessionUser.put("avatar", teacher.getAvatar());
        sessionUser.put("phone", teacher.getPhone());
        sessionUser.put("role", "ROLE_TEACHER");

        TOKEN_SESSION_STORE.put(token, sessionUser);

        // 返回给前端的标准登录成功报文
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("teacherId", teacher.getId());
        responseData.put("name", teacher.getName());
        responseData.put("role", "ROLE_TEACHER");

        return ResponseEntity.ok(Map.of("code", 200, "msg", "登录成功", "data", responseData));
    }

    /**
     * 2. 获取当前登录教师详情（供前端路由、拦截器同步状态使用）
     */
    @GetMapping("/info")
    public ResponseEntity<?> getTeacherInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", 401, "msg", "凭证缺失：请重新登录"));
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Map<String, Object> sessionUser = TOKEN_SESSION_STORE.get(token);

        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("code", 401, "msg", "凭证失效：当前的登录状态已过期"));
        }

        return ResponseEntity.ok(Map.of("code", 200, "msg", "同步成功", "data", sessionUser));
    }

    /**
     * 3. 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && !authHeader.isEmpty()) {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            TOKEN_SESSION_STORE.remove(token);
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "会话注销成功"));
    }
}