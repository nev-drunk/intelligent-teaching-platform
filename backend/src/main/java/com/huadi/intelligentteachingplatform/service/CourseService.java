package com.huadi.intelligentteachingplatform.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.dto.ai.CoursewareDetectResult;
import com.huadi.intelligentteachingplatform.dto.ai.CoursewareSummary;
import com.huadi.intelligentteachingplatform.dto.ai.LayoutBox;
import com.huadi.intelligentteachingplatform.entity.Course;
import com.huadi.intelligentteachingplatform.entity.CourseResource;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.huadi.intelligentteachingplatform.mapper.CourseMapper;
import com.huadi.intelligentteachingplatform.mapper.CourseResourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseResourceMapper courseResourceMapper;
    private final AiServiceClient aiServiceClient;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    // ── 课程 CRUD ─────────────────────────────────────────────────

    public List<Course> getAllCourses() {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreateTime));
    }

    public Optional<Course> getCourseById(Long id) {
        return Optional.ofNullable(courseMapper.selectById(id));
    }

    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, teacherId)
                        .orderByDesc(Course::getCreateTime));
    }

    public Course saveCourse(Course course) {
        if (course.getId() == null) {
            courseMapper.insert(course);
        } else {
            courseMapper.updateById(course);
        }
        return course;
    }

    public boolean deleteCourse(Long id) {
        return courseMapper.deleteById(id) > 0;
    }

    // ── 课程资源 CRUD ─────────────────────────────────────────────

    public List<CourseResource> getResourcesByCourseId(Long courseId) {
        return courseResourceMapper.selectList(
                new LambdaQueryWrapper<CourseResource>()
                        .eq(CourseResource::getCourseId, courseId)
                        .orderByDesc(CourseResource::getUpdateTime));
    }

    public CourseResource uploadResource(MultipartFile file, Long courseId, String title) throws IOException {
        String basePath = System.getProperty("user.dir");
        File uploadDir = new File(basePath, uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".tmp";
        String newFilename = UUID.randomUUID() + extension;

        File destFile = new File(uploadDir, newFilename);
        file.transferTo(destFile);

        CourseResource resource = new CourseResource();
        resource.setCourseId(courseId);
        resource.setTitle(title);
        resource.setFileUrl("/uploads/" + newFilename);
        resource.setSegmentStatus(0);

        courseResourceMapper.insert(resource);
        return resource;
    }

    public boolean updateResource(CourseResource resource) {
        return courseResourceMapper.updateById(resource) > 0;
    }

    public boolean deleteResource(Long id) {
        return courseResourceMapper.deleteById(id) > 0;
    }

    // ═══════════════════════════════════════════════════════════════
    // AI 课件内容分析
    // ═══════════════════════════════════════════════════════════════

    /**
     * 课件图片版面检测 — 上传课件图片时调用 /layout/detect
     * （旧版兼容，保留）
     */
    public CourseResource analyzeLayout(Long resourceId, MultipartFile imageFile) throws IOException {
        CourseResource resource = courseResourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在: " + resourceId);
        }

        com.huadi.intelligentteachingplatform.dto.ai.LayoutResult layoutResult =
                aiServiceClient.detectLayout(imageFile);

        String regionsJson = JSON.toJSONString(layoutResult.getBoxes());
        resource.setSegmentedRegions(regionsJson);
        resource.setSegmentStatus(1);

        courseResourceMapper.updateById(resource);
        return resource;
    }

    /**
     * 课件内容分析 — 根据资源ID从数据库查 file_url，调用 /courseware/detect
     *
     * 流程:
     * 1. 查 tb_course_resource 获取 file_url
     * 2. 调用 detectCoursewareByUrl
     * 3. 将 boxes JSON 存入 segmented_regions
     * 4. segment_status 改为 1
     */
    public CourseResource analyzeResource(Long resourceId) {
        CourseResource resource = courseResourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(404, "资源不存在: " + resourceId);
        }

        String fileUrl = resource.getFileUrl();
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new BusinessException(400, "资源文件路径为空");
        }

        try {
            // 调用课件检测
            CoursewareDetectResult result = aiServiceClient.detectCoursewareByUrl(fileUrl);

            // 将检测结果存储为 JSON
            Map<String, Object> regionsData = new HashMap<>();
            regionsData.put("boxes", result.getBoxes() != null ? result.getBoxes() : List.of());
            if (result.getSummary() != null) {
                regionsData.put("summary", result.getSummary());
            }
            String regionsJson = JSON.toJSONString(regionsData);

            resource.setSegmentedRegions(regionsJson);
            resource.setSegmentStatus(1);
            courseResourceMapper.updateById(resource);

            log.info("课件分析完成，资源ID: {}, 检测到 {} 个区域",
                    resourceId,
                    result.getBoxes() != null ? result.getBoxes().size() : 0);

            return resource;
        } catch (Exception e) {
            log.error("课件分析失败，资源ID: {}", resourceId, e);
            throw new BusinessException(500, "课件分析失败: " + e.getMessage());
        }
    }
}
