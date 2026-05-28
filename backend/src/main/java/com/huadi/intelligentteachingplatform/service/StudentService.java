package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.intelligentteachingplatform.dto.student.StudentSaveDTO;
import com.huadi.intelligentteachingplatform.dto.student.StudentVO;
import com.huadi.intelligentteachingplatform.entity.Student;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.huadi.intelligentteachingplatform.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;

    /**
     * 分页条件查询学生列表
     *
     * @param page         页码
     * @param size         每页条数
     * @param studentName  学生姓名（模糊查询）
     * @param studentNo    学号（精确查询）
     * @param classId      班级ID（精确查询）
     * @return 分页学生数据
     */
    public IPage<StudentVO> selectStudentPage(
            int page,
            int size,
            String studentName,
            String studentNo,
            Long classId
    ) {
        log.info("查询学生列表 - page:{}, size:{}, studentName:{}, studentNo:{}, classId:{}",
                page, size, studentName, studentNo, classId);

        Page<StudentVO> pageParam = new Page<>(page, size);
        return studentMapper.selectStudentPage(pageParam, studentName, studentNo, classId);
    }

    /**
     * 根据ID查询学生
     *
     * @param id 学生ID
     * @return 学生信息
     */
    public Student getStudentById(Long id) {
        return studentMapper.selectById(id);
    }

    /**
     * 新增学生
     *
     * @param dto 学生保存DTO
     * @return 创建的学生信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Student createStudent(StudentSaveDTO dto) {
        log.info("新增学生 - studentName:{}, studentNo:{}, classId:{}",
                dto.getStudentName(), dto.getStudentNo(), dto.getClassId());

        validateStudentNoUnique(null, dto.getStudentNo());

        Student student = new Student();
        student.setStudentName(dto.getStudentName());
        student.setStudentNo(dto.getStudentNo());
        student.setClassId(dto.getClassId());
        student.setCreateTime(LocalDateTime.now());

        studentMapper.insert(student);
        log.info("学生创建成功 - id:{}, studentNo:{}", student.getId(), student.getStudentNo());
        return student;
    }

    /**
     * 修改学生
     *
     * @param dto 学生保存DTO
     * @return 修改后的学生信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Student updateStudent(StudentSaveDTO dto) {
        Long id = dto.getId();
        log.info("修改学生 - id:{}, studentName:{}, studentNo:{}, classId:{}",
                id, dto.getStudentName(), dto.getStudentNo(), dto.getClassId());

        Student existing = studentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("学生不存在");
        }

        validateStudentNoUnique(id, dto.getStudentNo());

        existing.setStudentName(dto.getStudentName());
        existing.setStudentNo(dto.getStudentNo());
        existing.setClassId(dto.getClassId());

        studentMapper.updateById(existing);
        log.info("学生更新成功 - id:{}, studentNo:{}", existing.getId(), existing.getStudentNo());
        return existing;
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        log.info("删除学生 - id:{}", id);

        Student existing = studentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("学生不存在");
        }

        studentMapper.deleteById(id);
        log.info("学生删除成功 - id:{}", id);
    }

    /**
     * 根据班级ID查询学生列表
     *
     * @param classId 班级ID
     * @return 学生列表
     */
    public List<Student> getStudentsByClassId(Long classId) {
        if (classId != null) {
            return studentMapper.selectByClassId(classId);
        }
        return studentMapper.selectList(null);
    }

    /**
     * 校验学号唯一性
     *
     * @param excludeId 排除的学生ID（修改时使用）
     * @param studentNo 学号
     */
    private void validateStudentNoUnique(Long excludeId, String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return;
        }

        Student existing = studentMapper.selectByStudentNo(studentNo);
        if (existing != null) {
            if (excludeId == null || !excludeId.equals(existing.getId())) {
                throw new BusinessException("该学号已存在");
            }
        }
    }
}