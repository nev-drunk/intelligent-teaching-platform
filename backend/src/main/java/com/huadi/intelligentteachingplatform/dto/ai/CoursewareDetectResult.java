package com.huadi.intelligentteachingplatform.dto.ai;

import lombok.Data;
import java.util.List;

/**
 * 课件内容检测结果 DTO
 */
@Data
public class CoursewareDetectResult {
    private List<LayoutBox> boxes;
    private CoursewareSummary summary;
}
