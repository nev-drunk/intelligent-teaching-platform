package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Issue;
import com.huadi.intelligentteachingplatform.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issue")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class IssueController {

    private final IssueService issueService;

    /** 答疑列表 */
    @GetMapping("/list")
    public ApiResponse<List<Issue>> list(@RequestParam(required = false) Long courseId) {
        List<Issue> list = issueService.list(courseId);
        return ApiResponse.ok(list);
    }

    /** 答疑详情 */
    @GetMapping("/{id}")
    public ApiResponse<Issue> detail(@PathVariable Long id) {
        Issue issue = issueService.getById(id);
        if (issue == null) {
            return ApiResponse.fail(404, "问题不存在");
        }
        return ApiResponse.ok(issue);
    }

    /**
     * 【只读】检索相似历史问题 — 纯查询，不保存任何数据
     * 用户在输入框中实时查重时调用
     *
     * POST /api/issue/check-similar
     * Body: {"questionText": "...", "courseId": 1}
     */
    @PostMapping("/check-similar")
    public ApiResponse<List<com.huadi.intelligentteachingplatform.dto.ai.SimilarItem>> checkSimilar(
            @RequestBody Map<String, Object> body) {
        try {
            String questionText = (String) body.get("questionText");
            Long courseId = body.get("courseId") != null
                    ? Long.valueOf(body.get("courseId").toString()) : null;

            if (questionText == null || questionText.trim().length() < 3) {
                return ApiResponse.ok(List.of());
            }

            List<com.huadi.intelligentteachingplatform.dto.ai.SimilarItem> results =
                    issueService.checkSimilar(questionText.trim(), courseId);
            return ApiResponse.ok(results);
        } catch (Exception e) {
            log.error("相似问题检索异常", e);
            return ApiResponse.fail(500, "检索失败: " + e.getMessage());
        }
    }

    /** 学生发布问题（仅保存） */
    @PostMapping("/create")
    public ApiResponse<Issue> create(@RequestBody Issue issue) {
        Issue created = issueService.create(issue);
        return ApiResponse.ok("问题发布成功", created);
    }

    /**
     * 发布问题并返回 Top3 相似历史问题
     * 用户点击"发布/确定"时调用，执行保存 + 查重
     */
    @PostMapping("/create-with-similarity")
    public ApiResponse<IssueService.IssueCreationResult> createWithSimilarity(
            @RequestBody Issue issue) {
        try {
            IssueService.IssueCreationResult result =
                    issueService.createWithSimilarityCheck(issue);
            return ApiResponse.ok("问题发布成功，已返回相似问题", result);
        } catch (Exception e) {
            log.error("发布问题（含相似度检测）异常", e);
            return ApiResponse.fail(500, "发布失败: " + e.getMessage());
        }
    }

    /** 教师回复问题 */
    @PutMapping("/{id}/reply")
    public ApiResponse<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String teacherReply = body.get("teacherReply");
        if (teacherReply == null || teacherReply.isBlank()) {
            return ApiResponse.fail(400, "回复内容不能为空");
        }
        boolean ok = issueService.reply(id, teacherReply);
        return ok ? ApiResponse.ok("回复成功", null) : ApiResponse.fail(500, "回复失败");
    }

    /** 删除问题 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boolean ok = issueService.delete(id);
        return ok ? ApiResponse.ok("问题删除成功", null) : ApiResponse.fail(500, "问题删除失败");
    }
}
