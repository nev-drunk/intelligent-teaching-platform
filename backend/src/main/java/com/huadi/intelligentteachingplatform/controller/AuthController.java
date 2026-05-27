package com.huadi.intelligentteachingplatform.controller;


import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TeacherMapper teacherMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        // 1. 去数据库查有没有这个账号
        Teacher teacher = teacherMapper.selectByUsername(username);

        // 2. 校验账号存在且密码正确 (由于是敷衍版，数据库里存的是明文 123456)
        if (teacher != null && teacher.getPassword().equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "登录成功");

            Map<String, Object> data = new HashMap<>();
            data.put("token", "real-db-token-" + teacher.getId());
            data.put("name", teacher.getName()); // 拿到了数据库里真实的“张教授”
            response.put("data", data);

            return ResponseEntity.ok(response);
        }

        // 3. 失败
        Map<String, Object> failResponse = new HashMap<>();
        failResponse.put("code", 401);
        failResponse.put("msg", "用户名或密码错误");
        return ResponseEntity.status(401).body(failResponse);
    }
}