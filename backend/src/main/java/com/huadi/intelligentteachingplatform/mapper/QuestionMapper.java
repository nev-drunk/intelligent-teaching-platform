package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionMapper {

    // 💥 补齐：根据课程ID查询该课程下的所有题目列表
    List<Question> selectByCourseId(@Param("courseId") Long courseId);

    // 插入单条题目（常规录入或AI录入）
    void insertQuestion(Question question);
}