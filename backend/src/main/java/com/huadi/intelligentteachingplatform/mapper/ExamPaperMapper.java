package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamPaperMapper {
    // 插入试卷主表
    void insertPaper(ExamPaper examPaper);

    // 批量插入试题关联中间表
    void insertPaperQuestions(
            @Param("paperId") Long paperId,
            @Param("items") List<Map<String, Object>> items
    );

    // 💥 彻底填坑：根据教师ID与课程ID，查询当前老师已发布的试卷列表历史
    List<Map<String, Object>> selectPapersByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId
    );
}