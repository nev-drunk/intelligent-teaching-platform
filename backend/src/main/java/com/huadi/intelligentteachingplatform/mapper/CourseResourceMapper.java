package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.CourseResource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseResourceMapper {

    @Select("SELECT * FROM tb_course_resource WHERE course_id = #{courseId} ORDER BY update_time DESC")
    List<CourseResource> selectByCourseId(Long courseId);

    @Select("SELECT * FROM tb_course_resource WHERE id = #{id}")
    CourseResource selectById(Long id);

    @Insert("INSERT INTO tb_course_resource(course_id, title, file_url, segment_status, segmented_regions, update_time) VALUES(#{courseId}, #{title}, #{fileUrl}, #{segmentStatus}, #{segmentedRegions}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourseResource resource);

    @Update("UPDATE tb_course_resource SET title = #{title}, file_url = #{fileUrl}, segment_status = #{segmentStatus}, segmented_regions = #{segmentedRegions}, update_time = NOW() WHERE id = #{id}")
    int update(CourseResource resource);

    @Delete("DELETE FROM tb_course_resource WHERE id = #{id}")
    int deleteById(Long id);
}
