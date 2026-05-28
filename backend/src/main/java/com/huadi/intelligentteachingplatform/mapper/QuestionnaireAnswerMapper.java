package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.QuestionnaireAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface QuestionnaireAnswerMapper {

    /** 插入学生提交的答案 */
    int insert(QuestionnaireAnswer answer);

    /** 查询某份问卷的所有答案 */
    List<QuestionnaireAnswer> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    /** 查询某学生在某问卷中是否已提交 */
    QuestionnaireAnswer selectByQuestionnaireAndStudent(@Param("questionnaireId") Long questionnaireId,
                                                         @Param("studentId") Long studentId);

    /** 计算某份问卷的平均分 */
    BigDecimal calculateAvgScore(@Param("questionnaireId") Long questionnaireId);
}
