package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.entity.EvaluationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationReportMapper extends BaseMapper<EvaluationReport> {

    List<EvaluationReport> selectList(@Param("teacherId") Long teacherId);

    EvaluationReport selectById(@Param("id") Long id);

    int insert(EvaluationReport report);

    int update(EvaluationReport report);

    int deleteById(@Param("id") Long id);

    int updateAiReport(@Param("id") Long id, @Param("llmAnalysisReport") String llmAnalysisReport);
}
