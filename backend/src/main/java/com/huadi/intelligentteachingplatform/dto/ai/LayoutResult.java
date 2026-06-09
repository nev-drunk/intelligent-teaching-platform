package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * 版面检测结果 DTO
 */
@Data
public class LayoutResult {
    private List<LayoutBox> boxes;
}
