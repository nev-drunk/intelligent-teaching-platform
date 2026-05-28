package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.portal.CarouselSlideVO;
import com.huadi.intelligentteachingplatform.dto.portal.PublishNoticeRequest;
import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import com.huadi.intelligentteachingplatform.service.PortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PortalController {

    private final PortalService portalService;

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
}
