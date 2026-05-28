package com.huadi.intelligentteachingplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_course_resource")
public class CourseResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("course_id")
    private Long courseId;
    private String title;
    @TableField("file_url")
    private String fileUrl;
    @TableField("segment_status")
    private Integer segmentStatus;
    @TableField("segmented_regions")
    private String segmentedRegions;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
