package com.huadi.intelligentteachingplatform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类 - 支持BCrypt加密和明文兼容
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * 优先使用BCrypt校验，如果失败则尝试明文比对（兼容旧数据）
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            // 优先使用BCrypt校验
            if (encoder.matches(rawPassword, encodedPassword)) {
                return true;
            }
        } catch (Exception e) {
            // BCrypt校验失败，可能是明文密码
        }
        
        // 如果BCrypt校验失败，尝试明文比对（兼容测试数据）
        return rawPassword.equals(encodedPassword);
    }

    /**
     * 判断密码是否已加密（是否为BCrypt格式）
     */
    public boolean isEncrypted(String password) {
        return password != null && password.startsWith("$2a$");
    }
}
