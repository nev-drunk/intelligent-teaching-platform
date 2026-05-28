package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Teacher;
import com.huadi.intelligentteachingplatform.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers") // 标准复数 RESTful 路径
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * 1. 获取所有教师花名册列表 (多用于后台管理中的下拉框选择、教师列表展示)
     */
    @GetMapping
    public ApiResponse<List<Teacher>> getAllTeachers() {
        return ApiResponse.ok(teacherService.getAllTeachers());
    }

    /**
     * 2. 根据教师 ID 获取特定的教师主档信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Teacher> getTeacherById(@PathVariable Long id) {
        return teacherService.getTeacherById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "教职工数据不存在"));
    }

    /**
     * 3. 教务管理员在后台手动开通/录入新教师账号
     */
    @PostMapping
    public ApiResponse<Teacher> createTeacher(@RequestBody Teacher teacher) {
        Teacher savedTeacher = teacherService.saveTeacher(teacher);
        return ApiResponse.ok("新教师入档成功", savedTeacher);
    }

    /**
     * 4. 修改已有教师的数据信息 (如人事调动、改名字、换绑手机号等)
     */
    @PutMapping("/{id}")
    public ApiResponse<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher teacher) {
        teacher.setId(id);
        Teacher updatedTeacher = teacherService.saveTeacher(teacher);
        return ApiResponse.ok("教师档案修改成功", updatedTeacher);
    }

    /**
     * 5. 注销/删除教师账号档案
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ApiResponse.ok("教师数据档案移除成功", null);
    }
}