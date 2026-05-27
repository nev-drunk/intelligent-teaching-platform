package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.entity.Course;
import com.huadi.intelligentteachingplatform.entity.CourseResource;
import com.huadi.intelligentteachingplatform.mapper.CourseMapper;
import com.huadi.intelligentteachingplatform.mapper.CourseResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseResourceMapper courseResourceMapper;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    // ==================== 课程管理接口 ====================

    @GetMapping("/list")
    public ResponseEntity<?> getCourseList(@RequestParam Long teacherId) {
        List<Course> courses = courseMapper.selectByTeacherId(teacherId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", courses);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        int result = courseMapper.insert(course);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("msg", "课程创建成功");
            response.put("data", course);
        } else {
            response.put("code", 500);
            response.put("msg", "课程创建失败");
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCourse(@RequestBody Course course) {
        int result = courseMapper.update(course);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("msg", "课程更新成功");
        } else {
            response.put("code", 500);
            response.put("msg", "课程更新失败");
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        int result = courseMapper.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("msg", "课程删除成功");
        } else {
            response.put("code", 500);
            response.put("msg", "课程删除失败");
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 课程资源管理接口 ====================

    @GetMapping("/resource/list")
    public ResponseEntity<?> getResourceList(@RequestParam Long courseId) {
        List<CourseResource> resources = courseResourceMapper.selectByCourseId(courseId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", resources);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resource/upload")
    public ResponseEntity<?> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long courseId,
            @RequestParam String title) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (file.isEmpty()) {
            response.put("code", 400);
            response.put("msg", "文件不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 使用绝对路径创建上传目录
            String basePath = System.getProperty("user.dir");
            File uploadDir = new File(basePath, uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);

            // 保存到数据库
            CourseResource resource = new CourseResource();
            resource.setCourseId(courseId);
            resource.setTitle(title);
            resource.setFileUrl("/uploads/" + newFilename);
            resource.setSegmentStatus(0);
            courseResourceMapper.insert(resource);

            response.put("code", 200);
            response.put("msg", "上传成功");
            response.put("data", resource);
            
        } catch (IOException e) {
            response.put("code", 500);
            response.put("msg", "文件上传失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/resource/update")
    public ResponseEntity<?> updateResource(@RequestBody CourseResource resource) {
        int result = courseResourceMapper.update(resource);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("msg", "资源更新成功");
        } else {
            response.put("code", 500);
            response.put("msg", "资源更新失败");
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/resource/delete/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id) {
        int result = courseResourceMapper.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 200);
            response.put("msg", "资源删除成功");
        } else {
            response.put("code", 500);
            response.put("msg", "资源删除失败");
        }
        return ResponseEntity.ok(response);
    }
}
