package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Course;
import com.huadi.intelligentteachingplatform.entity.CourseResource;
import com.huadi.intelligentteachingplatform.mapper.CourseResourceMapper;
import com.huadi.intelligentteachingplatform.service.AiServiceClient;
import com.huadi.intelligentteachingplatform.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses") // 统一使用规范的复数形式 RESTful 路径
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private final CourseResourceMapper courseResourceMapper;
    private final AiServiceClient aiServiceClient;

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

    /**
     * 课件图片版面检测 — 调用 AI /layout/detect（旧版，需要上传图片）
     */
    @PostMapping("/resources/{id}/analyze-layout")
    public ApiResponse<CourseResource> analyzeLayout(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "文件不能为空");
        }
        try {
            CourseResource resource = courseService.analyzeLayout(id, file);
            return ApiResponse.ok("版面检测完成", resource);
        } catch (Exception e) {
            return ApiResponse.fail(500, "版面检测失败: " + e.getMessage());
        }
    }

    /**
     * 课件内容 AI 分析 — 根据已存 file_url 调用 /courseware/detect
     */
    @PostMapping("/resources/{id}/analyze")
    public ApiResponse<CourseResource> analyzeResource(@PathVariable Long id) {
        try {
            CourseResource resource = courseService.analyzeResource(id);
            return ApiResponse.ok("课件分析完成", resource);
        } catch (Exception e) {
            return ApiResponse.fail(500, "课件分析失败: " + e.getMessage());
        }
    }

    /**
     * 试卷版面分析代理 — 调用 Flask /layout/analyze-and-ocr
     */
    @PostMapping("/resources/{id}/paper-analyze")
    public ApiResponse<Map<String, Object>> paperAnalyze(@PathVariable Long id) {
        try {
            // 直接查资源表
            var resource = courseResourceMapper.selectById(id);
            if (resource == null) return ApiResponse.fail(404, "资源不存在");

            // 通过 AiServiceClient 调用 Flask
            com.huadi.intelligentteachingplatform.dto.ai.LayoutOcrResult result =
                    aiServiceClient.analyzeAndOcrByUrl(resource.getFileUrl());
            Map<String, Object> data = new HashMap<>();
            data.put("layout_boxes", result.getLayoutBoxes());
            data.put("ocr_regions", result.getOcrRegions());
            data.put("combined_text", result.getCombinedText());
            data.put("anomaly_score", result.getAnomalyScore());
            data.put("image_size", result.getImageSize());
            return ApiResponse.ok(data);
        } catch (Exception e) {
            return ApiResponse.fail(500, "试卷分析失败: " + e.getMessage());
        }
    }
}