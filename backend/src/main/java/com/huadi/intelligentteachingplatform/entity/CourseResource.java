package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseResource {
    private Long id;
    private Long courseId;
    private String title;
    private String fileUrl;
    private Integer segmentStatus;
    private String segmentedRegions;
    private LocalDateTime updateTime;
}
