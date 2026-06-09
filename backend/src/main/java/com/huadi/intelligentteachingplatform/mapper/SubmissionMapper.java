package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提交记录Mapper接口
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /**
     * 根据教师ID查询提交记录（连表查询任务和课程信息）
     */
    @Select("SELECT s.id, s.task_id, s.student_id, st.student_name, s.submit_text, " +
            "s.file_url, s.ocr_raw_text, s.ai_score, s.ai_comment, s.plagiarism_rate, s.is_cheated, " +
            "s.ai_review_voice_url, s.teacher_score, s.teacher_comment, s.status, s.submit_time, " +
            "t.title as task_title, c.course_name " +
            "FROM tb_submission s " +
            "LEFT JOIN tb_student st ON s.student_id = st.id " +
            "LEFT JOIN tb_task t ON s.task_id = t.id " +
            "LEFT JOIN tb_course c ON t.course_id = c.id " +
            "WHERE t.class_id IN (SELECT id FROM tb_class WHERE teacher_id = #{teacherId})")
    List<Submission> selectByTeacherId(Long teacherId);

    /**
     * 分页查询提交列表（连表查询任务名称）
     */
    List<Submission> selectSubmissionsWithTaskInfo(
            @Param("taskId") Long taskId,
            @Param("offset") Integer offset,
            @Param("size") Integer size);

    /**
     * 统计提交数量
     */
    long countSubmissions(@Param("taskId") Long taskId);

    /** 根据任务ID查询所有提交 */
    @Select("SELECT id, task_id, student_id, student_name, submit_text, file_url, " +
            "ocr_raw_text, ai_score, ai_comment, plagiarism_rate, is_cheated, " +
            "ai_review_voice_url, teacher_score, teacher_comment, status, submit_time " +
            "FROM tb_submission WHERE task_id = #{taskId}")
    List<Submission> selectByTaskId(@Param("taskId") Long taskId);
}