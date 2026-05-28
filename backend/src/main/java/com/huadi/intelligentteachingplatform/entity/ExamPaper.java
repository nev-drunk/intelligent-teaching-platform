package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_exam_paper")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("course_id")
    private Long courseId;
    private String title;
    @TableField("total_score")
    private Integer totalScore;
    @TableField("class_id")
    private Long classId;
    @TableField("teacher_id")
    private Long teacherId;
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<PaperQuestion> paperQuestions;
}
