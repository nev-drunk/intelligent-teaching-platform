package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.entity.Task;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT t.*, c.course_name FROM tb_task t LEFT JOIN tb_course c ON t.course_id = c.id WHERE t.class_id IN (SELECT id FROM tb_class WHERE teacher_id = #{teacherId})")
    List<Task> selectByTeacherId(Long teacherId);
}