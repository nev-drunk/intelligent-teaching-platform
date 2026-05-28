package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.entity.EvaluationReport;
import com.huadi.intelligentteachingplatform.entity.Questionnaire;
import com.huadi.intelligentteachingplatform.entity.QuestionnaireAnswer;
import com.huadi.intelligentteachingplatform.mapper.EvaluationReportMapper;
import com.huadi.intelligentteachingplatform.mapper.QuestionnaireAnswerMapper;
import com.huadi.intelligentteachingplatform.mapper.QuestionnaireMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class QuestionnaireService {

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionnaireAnswerMapper questionnaireAnswerMapper;

    @Autowired
    private EvaluationReportMapper evaluationReportMapper;

    /** 查询问卷列表，可按教师ID筛选 */
    public List<Questionnaire> list(Long teacherId) {
        return questionnaireMapper.selectList(teacherId);
    }

    /** 查询单个问卷 */
    public Questionnaire getById(Long id) {
        return questionnaireMapper.selectById(id);
    }

    /** 创建问卷，默认状态为开启(1) */
    public Questionnaire create(Questionnaire questionnaire) {
        if (questionnaire.getStatus() == null) {
            questionnaire.setStatus(1);
        }
        questionnaireMapper.insert(questionnaire);
        return questionnaire;
    }

    /** 更新问卷标题和内容 */
    public boolean update(Questionnaire questionnaire) {
        return questionnaireMapper.update(questionnaire) > 0;
    }

    /** 删除问卷 */
    public boolean delete(Long id) {
        return questionnaireMapper.deleteById(id) > 0;
    }

    /** 切换问卷状态：0关闭 / 1开启 */
    public boolean toggleStatus(Long id, Integer status) {
        return questionnaireMapper.updateStatus(id, status) > 0;
    }

    /** 学生提交问卷答案 */
    @Transactional
    public QuestionnaireAnswer submitAnswer(Long questionnaireId, QuestionnaireAnswer answer) {
        // 检查问卷是否存在且开启
        Questionnaire q = questionnaireMapper.selectById(questionnaireId);
        if (q == null || q.getStatus() != 1) {
            throw new RuntimeException("问卷不存在或已关闭");
        }

        // 检查是否重复提交
        QuestionnaireAnswer existing = questionnaireAnswerMapper.selectByQuestionnaireAndStudent(
                questionnaireId, answer.getStudentId());
        if (existing != null) {
            throw new RuntimeException("你已经提交过该问卷");
        }

        // 设置问卷ID
        answer.setQuestionnaireId(questionnaireId);

        // 插入答案
        questionnaireAnswerMapper.insert(answer);

        return answer;
    }

    /** 查询某份问卷的所有答案 */
    public List<QuestionnaireAnswer> getAnswers(Long questionnaireId) {
        return questionnaireAnswerMapper.selectByQuestionnaireId(questionnaireId);
    }

    /** 关闭问卷并自动生成评价报告 */
    @Transactional
    public boolean closeAndGenerateReport(Long questionnaireId) {
        // 1. 关闭问卷
        boolean closed = questionnaireMapper.updateStatus(questionnaireId, 0) > 0;
        if (!closed) {
            return false;
        }

        // 2. 计算平均分
        BigDecimal avgScore = questionnaireAnswerMapper.calculateAvgScore(questionnaireId);
        if (avgScore == null) {
            // 没有人提交答案，不生成报告
            return true;
        }

        // 3. 获取问卷信息
        Questionnaire q = questionnaireMapper.selectById(questionnaireId);
        if (q == null) {
            return false;
        }

        // 4. 生成评价报告
        EvaluationReport report = new EvaluationReport();
        report.setTeacherId(q.getTeacherId());
        report.setCourseId(q.getCourseId());
        report.setAvgSatisfaction(avgScore);
        report.setLlmAnalysisReport(null); // AI 报告后续可异步生成

        evaluationReportMapper.insert(report);

        return true;
    }
}
