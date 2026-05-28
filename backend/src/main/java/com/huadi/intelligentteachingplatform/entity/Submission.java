package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_submission")
public class Submission {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("task_id")
    private Long taskId;
    @TableField("student_id")
    private Long studentId;
    @TableField("student_name")
    private String studentName;
    @TableField("submit_text")
    private String submitText;
    @TableField("file_url")
    private String fileUrl;
    @TableField("teacher_score")
    private Integer teacherScore;
    @TableField("teacher_comment")
    private String teacherComment;
    @TableField("status")
    private String status;
    @TableField("submit_time")
    private LocalDateTime submitTime;
    
    // AI批改相关字段
    @TableField("ocr_raw_text")
    private String ocrRawText;
    @TableField("ai_score")
    private Integer aiScore;
    @TableField("ai_comment")
    private String aiComment;
    @TableField("plagiarism_rate")
    private BigDecimal plagiarismRate;
    @TableField("is_cheated")
    private Integer isCheated;
    @TableField("ai_review_voice_url")
    private String aiReviewVoiceUrl;
    
    @TableField(exist = false)
    private String taskTitle;
    @TableField(exist = false)
    private String courseName;
}