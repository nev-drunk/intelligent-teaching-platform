package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.PaperDetailsDTO;
import com.huadi.intelligentteachingplatform.dto.PaperSummaryDTO;
import com.huadi.intelligentteachingplatform.dto.paper.PublishPaperRequest;
import com.huadi.intelligentteachingplatform.dto.paper.SavePaperRequest;
import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import com.huadi.intelligentteachingplatform.service.PaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaperController {

    private final PaperService paperService;

    @GetMapping
    public ApiResponse<List<PaperDetailsDTO>> list() {
        return ApiResponse.ok(paperService.getAllPapersWithQuestions());
    }

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<ExamPaper>> listByCourse(@PathVariable Long courseId) {
        return ApiResponse.ok(paperService.getPapersByCourseId(courseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaperDetailsDTO> getById(@PathVariable Long id) {
        return paperService.getPaperDetailsById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.fail(404, "该试卷不存在"));
    }

    @PostMapping
    public ApiResponse<ExamPaper> save(@Valid @RequestBody SavePaperRequest request) {
        return ApiResponse.ok("试卷保存成功", paperService.savePaperWithQuestions(request));
    }

    @PostMapping("/publish")
    public ApiResponse<ExamPaper> publish(@Valid @RequestBody PublishPaperRequest request) {
        ExamPaper paper = paperService.publishPaper(request);
        return ApiResponse.ok("试卷组装成功！已经发布至指定班级。", paper);
    }

    /** 兼容旧前端路径 */
    @PostMapping("/create-paper")
    public ApiResponse<ExamPaper> createPaperLegacy(@Valid @RequestBody PublishPaperRequest request) {
        return publish(request);
    }

    @GetMapping("/teachers/{teacherId}/courses/{courseId}")
    public ApiResponse<List<PaperSummaryDTO>> listByTeacher(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {
        return ApiResponse.ok(paperService.getPapersByTeacherAndCourse(teacherId, courseId));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExamPaper> update(@PathVariable Long id, @RequestBody ExamPaper paper) {
        paper.setId(id);
        return ApiResponse.ok("试卷更新成功", paperService.updatePaper(paper));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        paperService.deletePaperCascade(id);
        return ApiResponse.ok("试卷删除成功", null);
    }
}
