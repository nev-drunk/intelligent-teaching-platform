package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    List<Questionnaire> selectList(@Param("teacherId") Long teacherId);

    Questionnaire selectById(@Param("id") Long id);

    int insert(Questionnaire questionnaire);

    int update(Questionnaire questionnaire);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
