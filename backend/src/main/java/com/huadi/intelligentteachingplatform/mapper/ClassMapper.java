package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.TeachingClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassMapper {
    List<TeachingClass> selectByTeacherId(@Param("teacherId") Long teacherId);

    List<TeachingClass> selectAll();
}
