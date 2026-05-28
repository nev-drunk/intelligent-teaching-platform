package com.huadi.intelligentteachingplatform.dto.paper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SavePaperRequest {
    @NotNull(message = "courseId 不能为空")
    private Long courseId;

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    private Integer totalScore = 100;
    private Long classId;
    private Long teacherId;

    @Valid
    private List<PaperQuestionItemDTO> questions;
}
