package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.question.AiGenerateRequest;
import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<List<Question>> list(@RequestParam(required = false) Long courseId) {
        List<Question> list = courseId != null
                ? questionService.getQuestionsByCourseId(courseId)
                : questionService.getAllQuestions();
        return ApiResponse.ok(list);
    }

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<Question>> listByCourse(@PathVariable Long courseId) {
        return ApiResponse.ok(questionService.getQuestionsByCourseId(courseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Question> getById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "该试题不存在"));
    }

    @PostMapping
    public ApiResponse<Question> create(@RequestBody Question question) {
        if (question.getIsLlmGenerated() == null) {
            question.setIsLlmGenerated(0);
        }
        return ApiResponse.ok("题目保存成功", questionService.saveQuestion(question));
    }

    @PutMapping("/{id}")
    public ApiResponse<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        return ApiResponse.ok("题目更新成功", questionService.saveQuestion(question));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.ok("题目删除成功", null);
    }

    @PostMapping("/ai-generate")
    public ApiResponse<Object> aiGenerate(@Valid @RequestBody AiGenerateRequest request) {
        try {
            Object result = questionService.generateByAi(request);
            String msg = result instanceof List ? "AI 批量出题成功" : "AI 智能出题成功并已自动入库";
            return ApiResponse.ok(msg, result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(500, "大模型生成异常: " + e.getMessage());
        }
    }
}
