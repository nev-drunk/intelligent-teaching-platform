package com.huadi.intelligentteachingplatform.dto.portal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublishNoticeRequest {
    private Long teacherId = 1L;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;
}
