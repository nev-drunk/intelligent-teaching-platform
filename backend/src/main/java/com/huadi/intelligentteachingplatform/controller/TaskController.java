package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Task;
import com.huadi.intelligentteachingplatform.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * 获取任务列表（支持按教师/课程/班级筛选）
     */
    @GetMapping
    public ApiResponse<List<Task>> getTasks(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId) {
        
        if (teacherId != null) {
            return ApiResponse.ok(taskService.getTasksByTeacherId(teacherId));
        }
        if (classId != null) {
            return ApiResponse.ok(taskService.getTasksByClassId(classId));
        }
        if (courseId != null) {
            return ApiResponse.ok(taskService.getTasksByCourseId(courseId));
        }
        
        return ApiResponse.ok(taskService.getAllTasks());
    }

    /**
     * 获取单个任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "任务不存在"));
    }

    /**
     * 创建新任务
     */
    @PostMapping
    public ApiResponse<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);
        return ApiResponse.ok("任务创建成功", createdTask);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ApiResponse<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        task.setId(id);
        Task updatedTask = taskService.updateTask(task);
        return ApiResponse.ok("任务更新成功", updatedTask);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        if (taskService.deleteTask(id)) {
            return ApiResponse.ok("任务删除成功", null);
        }
        return ApiResponse.fail(404, "任务不存在");
    }

    /**
     * 根据班级ID获取任务列表
     */
    @GetMapping("/class/{classId}")
    public ApiResponse<List<Task>> getTasksByClass(@PathVariable Long classId) {
        return ApiResponse.ok(taskService.getTasksByClassId(classId));
    }

    /**
     * 根据课程ID获取任务列表
     */
    @GetMapping("/course/{courseId}")
    public ApiResponse<List<Task>> getTasksByCourse(@PathVariable Long courseId) {
        return ApiResponse.ok(taskService.getTasksByCourseId(courseId));
    }
}