package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import com.huadi.intelligentteachingplatform.mapper.ExamPaperMapper;
import com.huadi.intelligentteachingplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/question")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private QuestionService questionService;

    /**
     * 1. 获取指定课程下的试题列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> getList(@RequestParam Long courseId) {
        return ResponseEntity.ok(questionMapper.selectByCourseId(courseId));
    }

    /**
     * 2. 调取指定教师、指定课程下的历史已发布卷子列表
     */
    @GetMapping("/papers")
    public ResponseEntity<?> getPapersByTeacher(@RequestParam Long teacherId, @RequestParam Long courseId) {
        return ResponseEntity.ok(examPaperMapper.selectPapersByTeacher(teacherId, courseId));
    }

    /**
     * 3. 手动录入普通题目 (非AI生成)
     */
    @PostMapping("/add")
    public ResponseEntity<?> addQuestion(@RequestBody Question question) {
        question.setIsLlmGenerated(0);
        questionMapper.insertQuestion(question);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "录入成功"));
    }

    /**
     * 4. 💥 接入 DeepSeek 大模型自动出题 (已瘦身，拒绝臃肿)
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<?> aiGenerate(@RequestBody Map<String, Object> params) {
        try {
            Long courseId = Long.parseLong(params.get("courseId").toString());
            String keyword = params.get("keyword").toString();

            // 核心逻辑交给 Service 层，Controller 只管收发数据，避免长耗时阻塞 Tomcat 线程池
            Question question = questionService.generateAndSaveAiQuestion(courseId, keyword);

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "AI出题成功并已自动入库",
                    "data", question
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "code", 500,
                    "msg", "AI出题失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 5. 组卷与发布试卷
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/create-paper")
    public ResponseEntity<?> createPaper(@RequestBody Map<String, Object> params) {
        try {
            Long courseId = Long.parseLong(params.get("courseId").toString());
            String title = params.get("title").toString();
            Long classId = params.get("classId") != null ? Long.parseLong(params.get("classId").toString()) : null;
            Long teacherId = params.get("teacherId") != null ? Long.parseLong(params.get("teacherId").toString()) : null;

            List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) params.get("items");
            if (itemsRaw == null || itemsRaw.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "组卷失败：请至少勾选一道试题"));
            }
            if (classId == null) {
                return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "发布失败：请选择目标发布班级"));
            }
            if (teacherId == null) {
                return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "发布失败：未获取到当前教师身份"));
            }

            int totalScore = 0;
            List<Map<String, Object>> targetItems = new ArrayList<>();
            for (Map<String, Object> item : itemsRaw) {
                int score = Integer.parseInt(item.get("score").toString());
                totalScore += score;
                Map<String, Object> map = new HashMap<>();
                map.put("questionId", Long.parseLong(item.get("questionId").toString()));
                map.put("score", score);
                targetItems.add(map);
            }

            ExamPaper paper = new ExamPaper();
            paper.setCourseId(courseId);
            paper.setTitle(title);
            paper.setTotalScore(totalScore);
            paper.setClassId(classId);
            paper.setTeacherId(teacherId);

            examPaperMapper.insertPaper(paper);
            examPaperMapper.insertPaperQuestions(paper.getId(), targetItems);

            return ResponseEntity.ok(Map.of("code", 200, "msg", "试卷组装成功！已经完美发布至指定班级。"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "code", 500,
                    "msg", "组卷系统异常: " + e.getMessage()
            ));
        }
    }
}