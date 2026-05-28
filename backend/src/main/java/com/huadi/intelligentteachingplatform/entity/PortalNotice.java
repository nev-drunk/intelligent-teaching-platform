package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_portal_notice")
public class PortalNotice {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("teacher_id")
    private Long teacherId;
    private String title;
    private String content;
    @TableField("tts_audio_url")
    private String ttsAudioUrl;
    @TableField("create_time")
    private LocalDateTime createTime;
}
