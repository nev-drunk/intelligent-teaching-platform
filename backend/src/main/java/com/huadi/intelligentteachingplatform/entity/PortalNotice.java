package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PortalNotice {
    private Long id;
    private Long teacherId;
    private String title;
    private String content;
    private String ttsAudioUrl;
    private LocalDateTime createTime;
}
