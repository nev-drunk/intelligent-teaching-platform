package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("course_id")
    private Long courseId;
    private String type;
    private String content;
    private String options;
    private String answer;
    @TableField("is_llm_generated")
    private Integer isLlmGenerated;
    @TableField("asr_audio_url")
    private String asrAudioUrl;
    @TableField("create_time")
    private LocalDateTime createTime;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer score;
}
