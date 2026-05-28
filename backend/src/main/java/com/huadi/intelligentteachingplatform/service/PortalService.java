package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.dto.portal.CarouselSlideVO;
import com.huadi.intelligentteachingplatform.dto.portal.PublishNoticeRequest;
import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import com.huadi.intelligentteachingplatform.mapper.PortalNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PortalService {

    private final PortalNoticeMapper portalNoticeMapper;

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

    public PortalNotice publish(PublishNoticeRequest request) {
        PortalNotice notice = new PortalNotice();
        notice.setTeacherId(request.getTeacherId() != null ? request.getTeacherId() : 1L);
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setTtsAudioUrl("client://speechSynthesis");
        portalNoticeMapper.insert(notice);
        return notice;
    }

    public void deleteNotice(Long id) {
        if (portalNoticeMapper.selectById(id) == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        portalNoticeMapper.deleteById(id);
    }
}
