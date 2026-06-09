package com.huadi.intelligentteachingplatform.dto.ai;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 版面检测 + 逐区域 OCR 一体化结果
 */
@Data
public class LayoutOcrResult {
    @JSONField(name = "layout_boxes")
    private List<LayoutBox> layoutBoxes;

    @JSONField(name = "ocr_regions")
    private List<OcrRegion> ocrRegions;

    @JSONField(name = "combined_text")
    private String combinedText;

    @JSONField(name = "anomaly_score")
    private Double anomalyScore;

    @JSONField(name = "image_size")
    private Map<String, Integer> imageSize;

    @Data
    public static class OcrRegion {
        private LayoutBox box;
        @JSONField(name = "ocr_text")
        private String ocrText;
        @JSONField(name = "ocr_confidence")
        private Double ocrConfidence;
    }
}
