package com.huadi.intelligentteachingplatform.dto.ai;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * 课件内容类型统计 DTO
 */
@Data
public class CoursewareSummary {
    @JSONField(name = "text_block_count")
    private Integer textBlockCount;

    @JSONField(name = "table_count")
    private Integer tableCount;

    @JSONField(name = "diagram_count")
    private Integer diagramCount;

    @JSONField(name = "formula_count")
    private Integer formulaCount;
}
