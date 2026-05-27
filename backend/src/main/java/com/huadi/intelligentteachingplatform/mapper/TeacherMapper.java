package com.huadi.intelligentteachingplatform.mapper;
import com.huadi.intelligentteachingplatform.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeacherMapper {
    /**
     * 根据用户名查询教师信息
     */
    Teacher selectByUsername(@Param("username") String username);
}