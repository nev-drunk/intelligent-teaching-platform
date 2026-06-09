package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.dto.portal.CarouselSlideVO;
import com.huadi.intelligentteachingplatform.dto.portal.PublishNoticeRequest;
import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import com.huadi.intelligentteachingplatform.mapper.PortalNoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PortalService {

    private final PortalNoticeMapper portalNoticeMapper;
    private final AiServiceClient aiServiceClient;

    public List<CarouselSlideVO> getCarousel() {
        return List.of(
                new CarouselSlideVO("智能教学支持平台", "大模型 · TTS · OCR 一体化教师中心", "#1e40af"),
                new CarouselSlideVO("网站门户系统", "通知公告自动语音播报", "#2563eb"),
                new CarouselSlideVO("2024级软件工程1班", "欢迎张教授登录后台管理", "#3b82f6")
        );
    }

    public List<PortalNotice> listNotices() {
        return portalNoticeMapper.selectList(
                new LambdaQueryWrapper<PortalNotice>().orderByDesc(PortalNotice::getCreateTime));
    }

    /**
     * 发布门户公告 — 含 TTS 语音合成
     *
     * 流程:
     * 1. 保存公告到 tb_portal_notice
     * 2. 调用 synthesizeSpeech(title + "。" + content) 生成音频
     * 3. 将返回 audioUrl 存入 tts_audio_url
     */
    public PortalNotice publish(PublishNoticeRequest request) {
        PortalNotice notice = new PortalNotice();
        notice.setTeacherId(request.getTeacherId() != null ? request.getTeacherId() : 1L);
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());

        // 调用 TTS 合成语音
        try {
            String ttsText = notice.getTitle() + "。" + notice.getContent();
            String audioUrl = aiServiceClient.synthesizeSpeech(ttsText);
            if (audioUrl != null) {
                notice.setTtsAudioUrl(audioUrl);
                log.info("TTS 语音公告生成成功: {}", audioUrl);
            } else {
                // TTS 服务不可用时使用浏览器端兜底
                notice.setTtsAudioUrl("client://speechSynthesis");
                log.warn("TTS 语音公告未生成，回退到浏览器端合成");
            }
        } catch (Exception e) {
            log.warn("TTS 语音合成失败，回退到浏览器端合成: {}", e.getMessage());
            notice.setTtsAudioUrl("client://speechSynthesis");
        }

        portalNoticeMapper.insert(notice);
        return notice;
    }

    public void deleteNotice(Long id) {
        PortalNotice notice = portalNoticeMapper.selectById(id);
        if (notice == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        // 删除关联的 TTS 音频文件
        String ttsUrl = notice.getTtsAudioUrl();
        if (ttsUrl != null && !ttsUrl.startsWith("client://") && !ttsUrl.isBlank()) {
            try {
                java.nio.file.Path audioPath = java.nio.file.Paths.get(
                    System.getProperty("user.dir"), ttsUrl);
                java.nio.file.Files.deleteIfExists(audioPath);
                log.info("已删除 TTS 音频: {}", ttsUrl);
            } catch (Exception e) {
                log.warn("删除 TTS 音频失败: {}", e.getMessage());
            }
        }
        portalNoticeMapper.deleteById(id);
    }
}
