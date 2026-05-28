package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.dto.auth.LoginRequest;
import com.huadi.intelligentteachingplatform.dto.auth.LoginVO;
import com.huadi.intelligentteachingplatform.dto.auth.RegisterRequest;
import com.huadi.intelligentteachingplatform.dto.auth.SessionUserVO;
import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.mapper.TeacherMapper;
import com.huadi.intelligentteachingplatform.util.JwtUtil;
import com.huadi.intelligentteachingplatform.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TeacherMapper teacherMapper;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

    public LoginVO login(LoginRequest request) {
        String username = request.getUsername().trim();
        String password = request.getPassword();
        boolean rememberMe = request.getRememberMe() != null && request.getRememberMe();

        // 1. 根据用户名查询教师
        Teacher teacher = teacherMapper.selectByUsername(username);
        if (teacher == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 2. 密码校验（支持BCrypt和明文兼容）
        String storedPassword = teacher.getPassword();
        if (!passwordUtil.matches(password, storedPassword)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 3. 如果数据库中是明文密码，自动升级为BCrypt加密（可选优化）
        if (!passwordUtil.isEncrypted(storedPassword)) {
            log.info("检测到明文密码，建议在数据库中更新为加密密码");
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(teacher.getId(), teacher.getUsername(), teacher.getName(), rememberMe);

        // 5. 构建返回对象
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setTeacherId(teacher.getId());
        vo.setUsername(teacher.getUsername());
        vo.setName(teacher.getName());
        vo.setAvatar(teacher.getAvatar());
        vo.setRole("ROLE_TEACHER");
        return vo;
    }

    public SessionUserVO getSessionUser(String authHeader) {
        String token = normalizeToken(authHeader);
        if (token.isBlank()) {
            throw new IllegalArgumentException("凭证缺失");
        }

        // 验证并解析Token
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("登录状态已过期");
        }

        SessionUserVO session = new SessionUserVO();
        session.setTeacherId(jwtUtil.getTeacherId(token));
        session.setUsername(jwtUtil.getUsername(token));
        session.setName(jwtUtil.getName(token));
        session.setRole("ROLE_TEACHER");
        
        // 可选：从数据库获取完整用户信息
        Teacher teacher = teacherMapper.selectByUsername(session.getUsername());
        if (teacher != null) {
            session.setAvatar(teacher.getAvatar());
            session.setPhone(teacher.getPhone());
        }
        
        return session;
    }

    public void logout(String authHeader) {
        // JWT是无状态的，服务端不需要维护token存储，logout只需要前端清除token即可
        log.info("用户退出登录");
    }

    /**
     * 教师注册
     */
    @Transactional
    public LoginVO register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        Teacher existing = teacherMapper.selectByUsername(request.getUsername().trim());
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 创建新教师记录
        Teacher teacher = new Teacher();
        teacher.setUsername(request.getUsername().trim());
        // 使用BCrypt加密密码
        teacher.setPassword(passwordUtil.encode(request.getPassword()));
        teacher.setName(request.getName().trim());
        teacher.setPhone(request.getPhone());
        teacher.setCreateTime(LocalDateTime.now());

        // 3. 保存到数据库
        teacherMapper.insert(teacher);
        log.info("新教师注册成功: {}", teacher.getUsername());

        // 4. 自动登录，返回Token
        return login(new LoginRequest() {{
            setUsername(request.getUsername());
            setPassword(request.getPassword());
            setRememberMe(false);
        }});
    }

    private String normalizeToken(String authHeader) {
        if (authHeader == null) {
            return "";
        }
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
