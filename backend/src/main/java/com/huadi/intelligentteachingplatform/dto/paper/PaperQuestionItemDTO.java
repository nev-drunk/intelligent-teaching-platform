package com.huadi.intelligentteachingplatform.dto.paper;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaperQuestionItemDTO {
    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotNull(message = "score 不能为空")
    @Min(value = 1, message = "分值至少为 1")
    private Integer score;
}
