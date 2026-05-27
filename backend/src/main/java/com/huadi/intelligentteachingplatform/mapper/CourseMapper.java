package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT * FROM tb_course WHERE teacher_id = #{teacherId} ORDER BY create_time DESC")
    List<Course> selectByTeacherId(Long teacherId);

    @Select("SELECT * FROM tb_course WHERE id = #{id}")
    Course selectById(Long id);

    @Insert("INSERT INTO tb_course(course_name, teacher_id, description, create_time) VALUES(#{courseName}, #{teacherId}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("UPDATE tb_course SET course_name = #{courseName}, description = #{description} WHERE id = #{id}")
    int update(Course course);

    @Delete("DELETE FROM tb_course WHERE id = #{id}")
    int deleteById(Long id);
}
