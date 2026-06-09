package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;

/**
 * 版面检测 — 单个检测框
 */
@Data
public class LayoutBox {
    private String label;
    private Double confidence;
    private Double x1;
    private Double y1;
    private Double x2;
    private Double y2;
}
