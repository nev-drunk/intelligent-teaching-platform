package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.intelligentteachingplatform.entity.Task;
import com.huadi.intelligentteachingplatform.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskMapper taskMapper;
    
    /**
     * 根据教师ID获取该教师的所有任务
     */
    public List<Task> getTasksByTeacherId(Long teacherId) {
        return taskMapper.selectByTeacherId(teacherId);
    }

    /**
     * 获取所有任务
     */
    public List<Task> getAllTasks() {
        return taskMapper.selectList(null);
    }

    /**
     * 根据班级ID获取任务列表
     */
    public List<Task> getTasksByClassId(Long classId) {
        return taskMapper.selectList(new QueryWrapper<Task>().eq("class_id", classId));
    }

    /**
     * 根据课程ID获取任务列表
     */
    public List<Task> getTasksByCourseId(Long courseId) {
        return taskMapper.selectList(new QueryWrapper<Task>().eq("course_id", courseId));
    }

    /**
     * 根据任务ID获取任务详情
     */
    public Optional<Task> getTaskById(Long taskId) {
        return Optional.ofNullable(taskMapper.selectById(taskId));
    }

    /**
     * 创建任务
     */
    public Task createTask(Task task) {
        taskMapper.insert(task);
        return task;
    }

    /**
     * 更新任务
     */
    public Task updateTask(Task task) {
        taskMapper.updateById(task);
        return task;
    }

    /**
     * 删除任务
     */
    public boolean deleteTask(Long taskId) {
        return taskMapper.deleteById(taskId) > 0;
    }
}