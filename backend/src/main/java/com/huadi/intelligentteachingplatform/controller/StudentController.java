package com.huadi.intelligentteachingplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.student.StudentSaveDTO;
import com.huadi.intelligentteachingplatform.dto.student.StudentVO;
import com.huadi.intelligentteachingplatform.entity.Student;
import com.huadi.intelligentteachingplatform.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/list")
    public ApiResponse<IPage<StudentVO>> listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) Long classId
    ) {
        log.info("查询学生列表接口 - page:{}, size:{}, studentName:{}, studentNo:{}, classId:{}",
                page, size, studentName, studentNo, classId);

        IPage<StudentVO> result = studentService.selectStudentPage(page, size, studentName, studentNo, classId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> getStudent(@PathVariable Long id) {
        log.info("查询学生详情 - id:{}", id);

        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ApiResponse.fail(404, "学生不存在");
        }
        return ApiResponse.ok(student);
    }

    @PostMapping("/add")
    public ApiResponse<Student> addStudent(@Valid @RequestBody StudentSaveDTO dto) {
        log.info("新增学生接口 - studentName:{}, studentNo:{}, classId:{}",
                dto.getStudentName(), dto.getStudentNo(), dto.getClassId());

        Student student = studentService.createStudent(dto);
        return ApiResponse.ok("学生创建成功", student);
    }

    @PutMapping("/update")
    public ApiResponse<Student> updateStudent(@Valid @RequestBody StudentSaveDTO dto) {
        if (dto.getId() == null) {
            return ApiResponse.fail(400, "学生ID不能为空");
        }

        log.info("修改学生接口 - id:{}, studentName:{}, studentNo:{}, classId:{}",
                dto.getId(), dto.getStudentName(), dto.getStudentNo(), dto.getClassId());

        Student student = studentService.updateStudent(dto);
        return ApiResponse.ok("学生更新成功", student);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        log.info("删除学生接口 - id:{}", id);

        studentService.deleteStudent(id);
        return ApiResponse.ok("学生删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<Student>> getStudents(@RequestParam(required = false) Long classId) {
        log.info("根据班级查询学生 - classId:{}", classId);

        List<Student> students = studentService.getStudentsByClassId(classId);
        return ApiResponse.ok(students);
    }
}