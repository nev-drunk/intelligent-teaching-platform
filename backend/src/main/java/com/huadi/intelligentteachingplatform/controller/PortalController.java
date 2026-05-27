package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import com.huadi.intelligentteachingplatform.mapper.PortalNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    @Autowired
    private PortalNoticeMapper portalNoticeMapper;

    /**
     * 门户轮播图（前端展示用，暂无独立数据表）
     */
    @GetMapping("/carousel")
    public ApiResponse<List<Map<String, String>>> carousel() {
        List<Map<String, String>> slides = List.of(
                slide("智能教学支持平台", "大模型 · TTS · OCR 一体化教师中心", "#1e40af"),
                slide("网站门户系统", "通知公告自动语音播报", "#2563eb"),
                slide("2024级软件工程1班", "欢迎张教授登录后台管理", "#3b82f6")
        );
        return ApiResponse.ok(slides);
    }

    @GetMapping("/notices")
    public ApiResponse<List<PortalNotice>> notices() {
        return ApiResponse.ok(portalNoticeMapper.selectAllOrderByCreateTimeDesc());
    }

    @PostMapping("/notices")
    public ApiResponse<PortalNotice> publish(@RequestBody Map<String, Object> body) {
        Long teacherId = body.get("teacherId") != null
                ? Long.valueOf(body.get("teacherId").toString())
                : 1L;
        String title = (String) body.get("title");
        String content = (String) body.get("content");

        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return ApiResponse.fail(400, "标题与正文不能为空");
        }

        PortalNotice notice = new PortalNotice();
        notice.setTeacherId(teacherId);
        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        notice.setTtsAudioUrl("client://speechSynthesis");
        portalNoticeMapper.insert(notice);
        return ApiResponse.ok("发布成功", notice);
    }

    @DeleteMapping("/notices/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        PortalNotice notice = portalNoticeMapper.selectById(id);
        if (notice == null) {
            return ApiResponse.fail(404, "公告不存在");
        }
        portalNoticeMapper.deleteById(id);
        return ApiResponse.ok("删除成功");
    }

    private static Map<String, String> slide(String title, String subtitle, String color) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("subtitle", subtitle);
        m.put("color", color);
        return m;
    }
}
