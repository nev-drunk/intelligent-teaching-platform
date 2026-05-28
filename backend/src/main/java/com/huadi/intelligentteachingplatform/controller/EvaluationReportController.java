package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.EvaluationReport;
import com.huadi.intelligentteachingplatform.service.EvaluationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
public class EvaluationReportController {

    @Autowired
    private EvaluationReportService evaluationReportService;

    /** 报告列表 */
    @GetMapping("/list")
    public ApiResponse<List<EvaluationReport>> list(@RequestParam(required = false) Long teacherId) {
        List<EvaluationReport> list = evaluationReportService.list(teacherId);
        return ApiResponse.ok(list);
    }

    /** 报告详情 */
    @GetMapping("/{id}")
    public ApiResponse<EvaluationReport> detail(@PathVariable Long id) {
        EvaluationReport report = evaluationReportService.getById(id);
        if (report == null) {
            return ApiResponse.fail(404, "报告不存在");
        }
        return ApiResponse.ok(report);
    }

    /** 创建报告 */
    @PostMapping("/create")
    public ApiResponse<EvaluationReport> create(@RequestBody EvaluationReport report) {
        EvaluationReport created = evaluationReportService.create(report);
        return ApiResponse.ok("报告创建成功", created);
    }

    /** 更新满意度得分 */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody EvaluationReport report) {
        report.setId(id);
        boolean ok = evaluationReportService.update(report);
        if (ok) {
            return ApiResponse.ok("报告更新成功", null);
        }
        return ApiResponse.fail(500, "报告更新失败");
    }

    /** 删除报告 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boolean ok = evaluationReportService.delete(id);
        if (ok) {
            return ApiResponse.ok("报告删除成功", null);
        }
        return ApiResponse.fail(500, "报告删除失败");
    }

    /**
     * 【AI预留】调用大模型生成教学效果诊断报告
     * 后期实现：将全班平均分、问卷分等数据丢给 DeepSeek 接口，
     * 拿到 AI 生成的诊断报告，写入 llm_analysis_report 字段
     */
    @PostMapping("/{id}/ai-report")
    public ApiResponse<String> generateAiReport(@PathVariable Long id) {
        // 1. 查询报告详情
        EvaluationReport report = evaluationReportService.getById(id);
        if (report == null) {
            return ApiResponse.fail(404, "报告不存在");
        }
        
        // 2. 生成 AI 诊断报告（简单版本，后期对接 DeepSeek）
        String aiReport = generateSimpleReport(report);
        
        // 3. 写入数据库
        boolean ok = evaluationReportService.updateAiReport(id, aiReport);
        
        // 4. 返回成功
        if (ok) {
            return ApiResponse.ok("AI诊断报告已生成并保存", aiReport);
        } else {
            return ApiResponse.fail(500, "报告保存失败");
        }
    }
    
    /**
     * 生成简单的 AI 诊断报告（后期替换为 DeepSeek API 调用）
     */
    private String generateSimpleReport(EvaluationReport report) {
        double score = report.getAvgSatisfaction().doubleValue();
        String level = score >= 85 ? "优秀" : score >= 70 ? "良好" : "待提升";
        
        return String.format(
            "【教学效果诊断报告】\n\n" +
            "📊 数据概览：\n" +
            "本次评价满意度得分 %.2f 分，整体处于%s水平。\n\n" +
            "✅ 教学亮点：\n" +
            "1. 教师授课态度认真负责，教学准备充分\n" +
            "2. 课程内容覆盖全面，理论与实践结合紧密\n" +
            "3. 课后作业设计合理，能有效巩固知识点\n\n" +
            "⚠️ 存在问题：\n" +
            "1. 课堂互动环节偏少，学生主动参与率有待提高\n" +
            "2. 部分难点讲解速度偏快，基础薄弱学生跟不上\n" +
            "3. 作业反馈周期较长，影响学生学习积极性\n\n" +
            "💡 改进建议：\n" +
            "1. 增加课堂小组讨论和随堂测试环节\n" +
            "2. 针对难点录制微课视频供学生反复观看\n" +
            "3. 建立AI助教自动答疑机制，缩短反馈周期\n" +
            "4. 定期组织线上答疑直播，集中解决共性问题",
            score, level
        );
    }
}