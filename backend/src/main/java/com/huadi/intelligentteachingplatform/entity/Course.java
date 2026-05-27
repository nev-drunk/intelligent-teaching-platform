package com.huadi.intelligentteachingplatform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private String courseName;
    private Long teacherId;
    private String description;
    private LocalDateTime createTime;
}
