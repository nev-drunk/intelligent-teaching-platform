package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.portal.CarouselSlideVO;
import com.huadi.intelligentteachingplatform.dto.portal.PublishNoticeRequest;
import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import com.huadi.intelligentteachingplatform.service.AiServiceClient;
import com.huadi.intelligentteachingplatform.service.PortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PortalController {

    private final PortalService portalService;
    private final AiServiceClient aiServiceClient;

    @GetMapping("/carousel")
    public ApiResponse<List<CarouselSlideVO>> carousel() {
        return ApiResponse.ok(portalService.getCarousel());
    }

    @GetMapping("/notices")
    public ApiResponse<List<PortalNotice>> notices() {
        return ApiResponse.ok(portalService.listNotices());
    }

    @PostMapping("/notices")
    public ApiResponse<PortalNotice> publish(@Valid @RequestBody PublishNoticeRequest request) {
        return ApiResponse.ok("发布成功", portalService.publish(request));
    }

    @DeleteMapping("/notices/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            portalService.deleteNotice(id);
            return ApiResponse.ok("删除成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(404, e.getMessage());
        }
    }

    /**
     * TTS 语音合成代理 — 转发到 Flask /tts/synthesize
     */
    @PostMapping("/tts/synthesize")
    public ApiResponse<Map<String, Object>> ttsSynthesize(@RequestBody Map<String, String> body) {
        try {
            String text = body.get("text");
            if (text == null || text.isBlank()) {
                return ApiResponse.fail(400, "文本不能为空");
            }
            // 通过 AiServiceClient 调用 Flask TTS
            String audioUrl = aiServiceClient.synthesizeSpeech(text);
            if (audioUrl != null) {
                return ApiResponse.ok(Map.of("audio_url", audioUrl));
            }
            return ApiResponse.fail(500, "TTS 合成失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, "TTS 服务异常: " + e.getMessage());
        }
    }
}
