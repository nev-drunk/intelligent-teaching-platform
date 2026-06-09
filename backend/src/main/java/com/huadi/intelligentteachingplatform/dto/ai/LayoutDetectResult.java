package com.huadi.intelligentteachingplatform.dto.ai;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 试卷版面检测完整结果 DTO（含异常分数和图片尺寸）
 */
@Data
public class LayoutDetectResult {
    private List<LayoutBox> boxes;

    @JSONField(name = "anomaly_score")
    private Double anomalyScore;

    @JSONField(name = "image_size")
    private Map<String, Integer> imageSize;
}
