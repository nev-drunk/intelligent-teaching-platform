package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.entity.EvaluationReport;
import com.huadi.intelligentteachingplatform.mapper.EvaluationReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationReportService {

    @Autowired
    private EvaluationReportMapper evaluationReportMapper;

    /** 查询评价报告列表，可按教师ID筛选 */
    public List<EvaluationReport> list(Long teacherId) {
        return evaluationReportMapper.selectList(teacherId);
    }

    /** 查询单个报告 */
    public EvaluationReport getById(Long id) {
        return evaluationReportMapper.selectById(id);
    }

    /** 创建评价报告 */
    public EvaluationReport create(EvaluationReport report) {
        if (report.getTeacherId() == null) {
            report.setTeacherId(1L); // 设置默认教师ID
        }
        evaluationReportMapper.insert(report);
        return report;
    }

    /** 更新满意度得分 */
    public boolean update(EvaluationReport report) {
        return evaluationReportMapper.update(report) > 0;
    }

    /** 删除报告 */
    public boolean delete(Long id) {
        return evaluationReportMapper.deleteById(id) > 0;
    }

    /**
     * 【AI预留】更新大模型生成的诊断报告
     * 后期对接 DeepSeek，将全班平均分、问卷分丢给大模型，
     * 生成的诊断报告存入 llm_analysis_report 字段
     */
    public boolean updateAiReport(Long id, String llmAnalysisReport) {
        return evaluationReportMapper.updateAiReport(id, llmAnalysisReport) > 0;
    }
}