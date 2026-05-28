package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeacherService {

    private final TeacherMapper teacherMapper;

    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>().orderByAsc(Teacher::getId));
    }

    public Optional<Teacher> getTeacherById(Long id) {
        return Optional.ofNullable(teacherMapper.selectById(id));
    }

    public Teacher saveTeacher(Teacher teacher) {
        if (teacher.getId() == null) {
            teacherMapper.insert(teacher);
        } else {
            teacherMapper.updateById(teacher);
        }
        return teacher;
    }

    public void deleteTeacher(Long id) {
        teacherMapper.deleteById(id);
    }
}
