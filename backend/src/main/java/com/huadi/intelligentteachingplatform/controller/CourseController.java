package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Course;
import com.huadi.intelligentteachingplatform.entity.CourseResource;
import com.huadi.intelligentteachingplatform.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses") // 统一使用规范的复数形式 RESTful 路径
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    // ==================== 1. 课程管理接口 (RESTful 风格) ====================

    /**
     * 获取课程列表（支持按教师 ID 筛选，留空则查全部）
     */
    @GetMapping
    public ApiResponse<List<Course>> getCourses(@RequestParam(required = false) Long teacherId) {
        List<Course> list = (teacherId != null)
                ? courseService.getCoursesByTeacherId(teacherId)
                : courseService.getAllCourses();
        return ApiResponse.ok(list);
    }

    /**
     * 根据课程 ID 获取详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Course> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "该课程不存在"));
    }

    /**
     * 创建课程
     */
    @PostMapping
    public ApiResponse<Course> createCourse(@RequestBody Course course) {
        Course savedCourse = courseService.saveCourse(course);
        return ApiResponse.ok("课程创建成功", savedCourse);
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        Course updatedCourse = courseService.saveCourse(course);
        return ApiResponse.ok("课程更新成功", updatedCourse);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        if (courseService.deleteCourse(id)) {
            return ApiResponse.ok("课程删除成功", null);
        }
        return ApiResponse.fail(500, "课程删除失败或课程不存在");
    }

    // ==================== 2. 课程资源管理接口 ====================

    /**
     * 获取指定课程的资源列表
     */
    @GetMapping("/{courseId}/resources")
    public ApiResponse<List<CourseResource>> getResourceList(@PathVariable Long courseId) {
        List<CourseResource> resources = courseService.getResourcesByCourseId(courseId);
        return ApiResponse.ok(resources);
    }

    /**
     * 上传课程资源文件
     */
    @PostMapping("/resources/upload")
    public ApiResponse<CourseResource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam String title) {

        if (file.isEmpty()) {
            return ApiResponse.fail(400, "文件不能为空");
        }

        try {
            CourseResource resource = courseService.uploadResource(file, courseId, title);
            return ApiResponse.ok("上传成功", resource);
        } catch (IOException e) {
            return ApiResponse.fail(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 更新资源信息
     */
    @PutMapping("/resources")
    public ApiResponse<Void> updateResource(@RequestBody CourseResource resource) {
        if (courseService.updateResource(resource)) {
            return ApiResponse.ok("资源更新成功", null);
        }
        return ApiResponse.fail(500, "资源更新失败");
    }

    /**
     * 删除指定资源
     */
    @DeleteMapping("/resources/{id}")
    public ApiResponse<Void> deleteResource(@PathVariable Long id) {
        if (courseService.deleteResource(id)) {
            return ApiResponse.ok("资源删除成功", null);
        }
        return ApiResponse.fail(500, "资源删除失败");
    }
}