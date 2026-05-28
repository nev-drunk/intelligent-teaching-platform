package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.entity.Course;
import com.huadi.intelligentteachingplatform.entity.CourseResource;
import com.huadi.intelligentteachingplatform.mapper.CourseMapper;
import com.huadi.intelligentteachingplatform.mapper.CourseResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseResourceMapper courseResourceMapper;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

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
}
