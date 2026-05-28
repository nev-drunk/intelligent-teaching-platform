package com.huadi.intelligentteachingplatform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import com.huadi.intelligentteachingplatform.entity.EvaluationReport;
import com.huadi.intelligentteachingplatform.entity.PaperQuestion;
import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.entity.Questionnaire;
import com.huadi.intelligentteachingplatform.mapper.ExamPaperMapper;
import com.huadi.intelligentteachingplatform.mapper.EvaluationReportMapper;
import com.huadi.intelligentteachingplatform.mapper.PaperQuestionMapper;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import com.huadi.intelligentteachingplatform.mapper.QuestionnaireMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final QuestionMapper questionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionnaireMapper questionnaireMapper;
    private final EvaluationReportMapper evaluationReportMapper;

    @Override
    public void run(String... args) {
        initQuestionsAndPapers();
        initQuestionnaires();
        initEvaluationReports();
    }

    private void initQuestionsAndPapers() {
        Long count = questionMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            log.info("数据库已有题目数据，跳过题目初始化。");
            return;
        }

        log.info("开始初始化题库与试卷示例数据...");

        Question q1 = buildQuestion(1L, "什么是牛顿第一定律？", "SINGLE", "A",
                "[\"A. 物体保持静止或匀速直线运动状态\", \"B. 力是改变物体运动状态的原因\", \"C. 作用力与反作用力相等\", \"D. 加速度与力成正比\"]");
        Question q2 = buildQuestion(1L, "勾股定理的公式是什么？", "SINGLE", "B",
                "[\"A. a + b = c\", \"B. a² + b² = c²\", \"C. a - b = c\", \"D. a × b = c\"]");
        Question q3 = buildQuestion(1L, "水的化学式是 H₂O", "JUDGE", "正确", null);

        questionMapper.insert(q1);
        questionMapper.insert(q2);
        questionMapper.insert(q3);

        ExamPaper paper = new ExamPaper();
        paper.setCourseId(1L);
        paper.setTitle("高一物理期中考试");
        paper.setTotalScore(25);
        paper.setTeacherId(1L);
        paper.setClassId(1L);
        examPaperMapper.insert(paper);

        insertLink(paper.getId(), q1.getId(), 1, 10);
        insertLink(paper.getId(), q2.getId(), 2, 15);

        log.info("题库数据初始化完成：3 道题目，1 份试卷。");
    }

    private void initQuestionnaires() {
        Long count = questionnaireMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            log.info("数据库已有问卷数据，跳过问卷初始化。");
            return;
        }

        log.info("开始初始化问卷示例数据...");

        String contentJson = "[{\"id\":\"q1\",\"question\":\"老师授课内容是否清晰易懂？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q2\",\"question\":\"课程重点难点是否讲解透彻？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q3\",\"question\":\"课堂互动是否充分？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q4\",\"question\":\"作业量是否合理？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q5\",\"question\":\"作业批改是否及时？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q6\",\"question\":\"老师教学态度是否认真负责？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q7\",\"question\":\"课程难度是否适中？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q8\",\"question\":\"课程内容是否实用？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q9\",\"question\":\"你对本课程的整体满意度？\",\"type\":\"RATING\",\"maxScore\":10}," +
                "{\"id\":\"q10\",\"question\":\"你是否愿意向其他同学推荐本课程？\",\"type\":\"RATING\",\"maxScore\":10}]";

        Questionnaire q1 = new Questionnaire();
        q1.setTeacherId(1L);
        q1.setTitle("2026学年春季《计算机网络》课程满意度调查");
        q1.setContentJson(contentJson);
        q1.setClassId(1L);
        q1.setCourseId(1L);
        q1.setStatus(1);
        q1.setResponseCount(156);
        q1.setAvgScore(4.7);

        Questionnaire q2 = new Questionnaire();
        q2.setTeacherId(1L);
        q2.setTitle("《高等数学》网课教学质量反馈问卷");
        q2.setContentJson(contentJson);
        q2.setClassId(2L);
        q2.setCourseId(2L);
        q2.setStatus(0);
        q2.setResponseCount(89);
        q2.setAvgScore(4.5);

        Questionnaire q3 = new Questionnaire();
        q3.setTeacherId(1L);
        q3.setTitle("《人工智能导论》课程教学效果评估");
        q3.setContentJson(contentJson);
        q3.setClassId(3L);
        q3.setCourseId(3L);
        q3.setStatus(1);
        q3.setResponseCount(203);
        q3.setAvgScore(4.8);

        questionnaireMapper.insert(q1);
        questionnaireMapper.insert(q2);
        questionnaireMapper.insert(q3);

        log.info("问卷数据初始化完成：3 份问卷。");
    }

    private void initEvaluationReports() {
        Long count = evaluationReportMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            log.info("数据库已有评价报告数据，跳过评价报告初始化。");
            return;
        }

        log.info("开始初始化评价报告示例数据...");

        EvaluationReport report1 = new EvaluationReport();
        report1.setTeacherId(1L);
        report1.setCourseId(1L);
        report1.setAvgSatisfaction(new BigDecimal("87.50"));
        report1.setLlmAnalysisReport("【教学效果诊断报告】\n\n📊 数据概览：\n本次评价满意度得分 87.50 分，整体处于优秀水平。\n\n✅ 教学亮点：\n1. 教师授课态度认真负责，教学准备充分\n2. 课程内容覆盖全面，理论与实践结合紧密\n3. 课后作业设计合理，能有效巩固知识点\n\n⚠️ 存在问题：\n1. 课堂互动环节偏少，学生主动参与率有待提高\n2. 部分难点讲解速度偏快，基础薄弱学生跟不上\n\n💡 改进建议：\n1. 增加课堂小组讨论和随堂测试环节\n2. 针对难点录制微课视频供学生反复观看");
        evaluationReportMapper.insert(report1);

        EvaluationReport report2 = new EvaluationReport();
        report2.setTeacherId(1L);
        report2.setCourseId(1L);
        report2.setAvgSatisfaction(new BigDecimal("82.30"));
        report2.setLlmAnalysisReport(null);
        evaluationReportMapper.insert(report2);

        log.info("评价报告数据初始化完成：2 份报告。");
    }

    private Question buildQuestion(Long courseId, String content, String type, String answer, String options) {
        Question q = new Question();
        q.setCourseId(courseId);
        q.setContent(content);
        q.setType(type);
        q.setAnswer(answer);
        q.setOptions(options);
        q.setIsLlmGenerated(0);
        return q;
    }

    private void insertLink(Long paperId, Long questionId, int sort, int score) {
        PaperQuestion pq = new PaperQuestion();
        pq.setPaperId(paperId);
        pq.setQuestionId(questionId);
        pq.setSort(sort);
        pq.setScore(score);
        paperQuestionMapper.insert(pq);
    }
}
