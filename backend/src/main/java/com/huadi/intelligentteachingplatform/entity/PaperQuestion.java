package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_paper_question")
public class PaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("paper_id")
    private Long paperId;
    @TableField("question_id")
    private Long questionId;
    private Integer sort;
    private Integer score;

    @TableField(exist = false)
    private Question question;
}
