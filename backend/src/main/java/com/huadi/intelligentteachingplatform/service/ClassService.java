package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.entity.TeachingClass;
import com.huadi.intelligentteachingplatform.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ClassService {

    private final ClassMapper classMapper;

    public List<TeachingClass> getAllClasses() {
        return classMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>().orderByAsc(TeachingClass::getId));
    }

    public Optional<TeachingClass> getClassById(Long id) {
        return Optional.ofNullable(classMapper.selectById(id));
    }

    public List<TeachingClass> getClassesByTeacherId(Long teacherId) {
        return classMapper.selectList(
                new LambdaQueryWrapper<TeachingClass>()
                        .eq(TeachingClass::getTeacherId, teacherId)
                        .orderByAsc(TeachingClass::getId));
    }

    public TeachingClass saveClass(TeachingClass teachingClass) {
        if (teachingClass.getId() == null) {
            classMapper.insert(teachingClass);
        } else {
            classMapper.updateById(teachingClass);
        }
        return teachingClass;
    }

    public boolean deleteClass(Long id) {
        return classMapper.deleteById(id) > 0;
    }
}
