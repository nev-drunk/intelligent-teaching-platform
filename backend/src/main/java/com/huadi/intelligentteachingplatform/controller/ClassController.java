package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.TeachingClass;
import com.huadi.intelligentteachingplatform.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private ClassMapper classMapper;

    @GetMapping
    public ApiResponse<List<TeachingClass>> list(
            @RequestParam(required = false) Long teacherId) {
        List<TeachingClass> list = teacherId != null
                ? classMapper.selectByTeacherId(teacherId)
                : classMapper.selectAll();
        return ApiResponse.ok(list);
    }
}
