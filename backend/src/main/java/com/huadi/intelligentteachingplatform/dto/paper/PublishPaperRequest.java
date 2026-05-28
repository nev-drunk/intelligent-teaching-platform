package com.huadi.intelligentteachingplatform.dto.paper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PublishPaperRequest {
    @NotNull(message = "courseId 不能为空")
    private Long courseId;

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    @NotNull(message = "classId 不能为空")
    private Long classId;

    @NotNull(message = "teacherId 不能为空")
    private Long teacherId;

    @NotEmpty(message = "请至少选择一道试题")
    @Valid
    private List<PaperQuestionItemDTO> items;
}
