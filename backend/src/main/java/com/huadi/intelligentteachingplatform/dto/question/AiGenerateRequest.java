package com.huadi.intelligentteachingplatform.dto.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiGenerateRequest {
    @NotNull(message = "courseId 不能为空")
    private Long courseId;

    /** 批量出题：自然语言描述 */
    private String prompt;

    /** 单题出题：关键词 */
    private String keyword;

    @Min(value = 1, message = "出题数量至少为 1")
    @Max(value = 10, message = "单次最多生成 10 道题")
    private Integer count = 3;
}
