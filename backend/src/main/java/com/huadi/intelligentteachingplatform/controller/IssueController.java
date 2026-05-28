package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.entity.Issue;
import com.huadi.intelligentteachingplatform.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issue")
@RequiredArgsConstructor
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

    /** 学生发布问题 */
    @PostMapping("/create")
    public ApiResponse<Issue> create(@RequestBody Issue issue) {
        Issue created = issueService.create(issue);
        return ApiResponse.ok("问题发布成功", created);
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
