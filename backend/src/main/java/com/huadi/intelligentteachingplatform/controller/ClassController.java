package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.TeachingClass;
import com.huadi.intelligentteachingplatform.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor // 推荐：使用 lombok 构造器注入，替代 @Autowired
@CrossOrigin(origins = "*") // 跨域支持
public class ClassController {

    private final ClassService classService;

    /**
     * 1. 获取班级列表（支持根据教师 ID 条件过滤）
     * 示例：
     * - 查全校班级：GET /api/classes
     * - 查某老师的班级：GET /api/classes?teacherId=1
     */
    @GetMapping
    public ApiResponse<List<TeachingClass>> getClasses(@RequestParam(required = false) Long teacherId) {
        // 修正点：去除了原本存在语法隐患且未使用的 List<List<...>> 变量声明
        if (teacherId != null) {
            return ApiResponse.ok(classService.getClassesByTeacherId(teacherId));
        }
        return ApiResponse.ok(classService.getAllClasses());
    }

    /**
     * 2. 根据班级 ID 获取详情
     * 示例：GET /api/classes/5
     */
    @GetMapping("/{id}")
    public ApiResponse<TeachingClass> getClassById(@PathVariable Long id) {
        return classService.getClassById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "该班级不存在"));
    }

    /**
     * 3. 创建新班级
     * 示例：POST /api/classes
     */
    @PostMapping
    public ApiResponse<TeachingClass> createClass(@RequestBody TeachingClass teachingClass) {
        TeachingClass savedClass = classService.saveClass(teachingClass);
        return ApiResponse.ok("班级创建成功", savedClass);
    }

    /**
     * 4. 修改班级信息
     * 示例：PUT /api/classes/5
     */
    @PutMapping("/{id}")
    public ApiResponse<TeachingClass> updateClass(@PathVariable Long id, @RequestBody TeachingClass teachingClass) {
        teachingClass.setId(id);
        TeachingClass updatedClass = classService.saveClass(teachingClass);
        return ApiResponse.ok("班级更新成功", updatedClass);
    }

    /**
     * 5. 删除班级
     * 示例：DELETE /api/classes/5
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ApiResponse.ok("班级删除成功", null);
    }
}