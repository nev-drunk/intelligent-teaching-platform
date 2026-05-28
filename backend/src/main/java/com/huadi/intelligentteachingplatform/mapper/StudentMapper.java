package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadi.intelligentteachingplatform.dto.student.StudentVO;
import com.huadi.intelligentteachingplatform.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 分页条件查询学生，关联班级表
     *
     * @param page         分页参数
     * @param studentName  学生姓名（模糊查询）
     * @param studentNo    学号（精确查询）
     * @param classId      班级ID（精确查询）
     * @return 学生VO分页数据
     */
    IPage<StudentVO> selectStudentPage(
            Page<StudentVO> page,
            @Param("studentName") String studentName,
            @Param("studentNo") String studentNo,
            @Param("classId") Long classId
    );

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生信息
     */
    @Select("SELECT * FROM tb_student WHERE student_no = #{studentNo}")
    Student selectByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据班级ID查询学生
     *
     * @param classId 班级ID
     * @return 学生列表
     */
    @Select("SELECT * FROM tb_student WHERE class_id = #{classId}")
    List<Student> selectByClassId(Long classId);
}