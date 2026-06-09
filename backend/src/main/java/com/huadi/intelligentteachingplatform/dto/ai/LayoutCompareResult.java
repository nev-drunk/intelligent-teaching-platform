package com.huadi.intelligentteachingplatform.dto.ai;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * 试卷版面比较结果 DTO
 */
@Data
public class LayoutCompareResult {
    private Double similarity;

    @JSONField(name = "is_suspicious")
    private Boolean isSuspicious;

    private String detail;
}
