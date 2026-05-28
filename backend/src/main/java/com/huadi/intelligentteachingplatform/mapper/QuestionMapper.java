package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 根据试卷ID查询题目列表（联查出每道题在这张试卷中的分值）
     */
    @Select("SELECT q.*, pq.score FROM tb_question q " + // <-- 关键点：这里加上了 , pq.score
            "LEFT JOIN tb_paper_question pq ON q.id = pq.question_id " +
            "WHERE pq.paper_id = #{paperId} " +
            "ORDER BY pq.sort ASC")
    List<Question> selectByPaperId(@Param("paperId") Long paperId);
}

