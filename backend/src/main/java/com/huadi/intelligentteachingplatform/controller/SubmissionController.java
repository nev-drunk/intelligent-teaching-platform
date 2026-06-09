package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.dto.submission.TeacherGradeRequest;
import com.huadi.intelligentteachingplatform.entity.Submission;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.huadi.intelligentteachingplatform.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 作业提交控制器
 * 提供作业提交、AI批改、教师复核等RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionService submissionService;

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    /**
     * 文件上传路径
     */
    private static final String SUBMISSION_DIR = "submissions/";

    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /**
     * 接收学生上传的图片文件
     * 
     * POST /api/submission/upload
     * 
     * @param file 上传的图片文件
     * @param taskId 任务ID
     * @param studentId 学生ID
     * @param studentName 学生姓名
     * @return 提交记录
     */
    @PostMapping("/upload")
    public ApiResponse<Submission> uploadSubmission(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("taskId") Long taskId,
            @RequestParam("studentId") Long studentId,
            @RequestParam("studentName") String studentName,
            @RequestParam(value = "submitText", required = false) String submitText) {

        // 1. 参数校验：至少要有文件或文本
        if ((file == null || file.isEmpty()) && (submitText == null || submitText.isBlank())) {
            return ApiResponse.fail(400, "请选择要上传的文件或输入提交文本");
        }

        // 2. 保存文件（如果有的话）
        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isAllowedExtension(originalFilename)) {
                return ApiResponse.fail(400, "只允许上传图片文件（jpg, jpeg, png, gif, bmp）");
            }
            try {
                fileUrl = saveFile(file);
                log.info("文件上传成功: {}", fileUrl);
            } catch (IOException e) {
                log.error("文件保存失败", e);
                return ApiResponse.fail(500, "文件保存失败");
            }
        }

        // 3. 创建提交记录
        Submission submission = new Submission();
        submission.setTaskId(taskId);
        submission.setStudentId(studentId);
        submission.setStudentName(studentName);
        submission.setFileUrl(fileUrl);
        if (submitText != null && !submitText.isBlank()) {
            submission.setSubmitText(submitText);
        }
        submission.setStatus(SubmissionService.STATUS_SUBMITTED);
        submission.setSubmitTime(LocalDateTime.now());

        // 4. 保存到数据库
        Submission savedSubmission = submissionService.saveSubmission(submission);
        log.info("提交记录创建成功，ID: {}", savedSubmission.getId());

        return ApiResponse.ok("提交成功", savedSubmission);
    }

    /**
     * AI全自动批改（唯一入口）
     * 包含: OCR识别 + 标准答案比对 + DeepSeek评语 + 抄袭痕迹检测
     *
     * POST /api/submission/auto-grade/{id}
     */
    @PostMapping("/auto-grade/{id}")
    public ApiResponse<Submission> autoGrade(@PathVariable Long id) {
        try {
            Submission submission = submissionService.autoGrade(id);
            return ApiResponse.ok("AI全自动批改完成", submission);
        } catch (BusinessException e) {
            log.warn("AI全自动批改失败: {}", e.getMessage());
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("AI全自动批改异常", e);
            return ApiResponse.fail(500, "AI全自动批改异常，请稍后重试");
        }
    }

    /**
     * 教师最终复核（含 TTS 语音评语合成）
     *
     * POST /api/submission/grade/{submissionId}
     *
     * @param submissionId 提交记录ID
     * @param request 教师批改请求
     * @return 更新后的提交记录
     */
    @PostMapping("/grade/{submissionId}")
    public ApiResponse<Submission> gradeSubmission(@PathVariable Long submissionId,
                                                    @RequestBody TeacherGradeRequest request) {
        try {
            Submission submission = submissionService.saveTeacherGrade(
                    submissionId,
                    request.getTeacherScore(),
                    request.getTeacherComment()
            );

            log.info("教师复核完成（含TTS），提交ID: {}, 评分: {}",
                    submissionId, request.getTeacherScore());

            return ApiResponse.ok("批改完成（含TTS语音评语）", submission);
        } catch (BusinessException e) {
            log.warn("教师复核失败: {}", e.getMessage());
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("教师复核异常", e);
            return ApiResponse.fail(500, "复核失败，请稍后重试");
        }
    }

    /**
     * 教师最终复核
     *
     * PUT /api/submission/teacher-grade
     *
     * @param request 教师批改请求
     * @return 更新后的提交记录
     */
    @PutMapping("/teacher-grade")
    public ApiResponse<Submission> teacherGrade(@RequestBody TeacherGradeRequest request) {
        if (request.getSubmissionId() == null) {
            return ApiResponse.fail(400, "提交记录ID不能为空");
        }

        try {
            // 使用 saveTeacherGrade（含 TTS 语音评语合成）
            Submission submission = submissionService.saveTeacherGrade(
                    request.getSubmissionId(),
                    request.getTeacherScore(),
                    request.getTeacherComment()
            );

            log.info("教师批改完成，提交ID: {}, 评分: {}",
                    request.getSubmissionId(), request.getTeacherScore());

            return ApiResponse.ok("批改完成（含TTS语音评语）", submission);
        } catch (BusinessException e) {
            log.warn("教师批改失败: {}", e.getMessage());
            return ApiResponse.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("教师批改异常", e);
            return ApiResponse.fail(500, "批改失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取提交记录
     * 
     * GET /api/submission/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<Submission> getSubmissionById(@PathVariable Long id) {
        Submission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResponse.fail(404, "提交记录不存在");
        }
        return ApiResponse.ok(submission);
    }

    /**
     * 分页查询提交列表
     * 
     * GET /api/submission/list
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getSubmissionList(
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        List<Submission> submissions = submissionService.getSubmissionsWithTaskInfo(taskId, page, size);
        long total = submissionService.countSubmissions(taskId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", submissions);
        result.put("total", total);
        result.put("currentPage", page);
        result.put("pageSize", size);

        return ApiResponse.ok(result);
    }

    /**
     * 根据教师ID获取提交列表
     * 
     * GET /api/submission/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<List<Submission>> getSubmissionsByTeacherId(@PathVariable Long teacherId) {
        List<Submission> submissions = submissionService.getSubmissionsByTeacherId(teacherId);
        return ApiResponse.ok(submissions);
    }

    /**
     * 删除提交记录
     * 
     * DELETE /api/submission/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubmission(@PathVariable Long id) {
        Submission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ApiResponse.fail(404, "提交记录不存在");
        }

        // 删除文件
        if (submission.getFileUrl() != null) {
            try {
                Path filePath = Paths.get(submission.getFileUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("删除文件失败: {}", submission.getFileUrl(), e);
            }
        }

        // 删除数据库记录
        submissionService.deleteSubmission(id);

        return ApiResponse.ok("删除成功", null);
    }

    /**
     * 检查文件扩展名是否允许
     */
    private boolean isAllowedExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存上传的文件
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建目录
        Path uploadDir = Paths.get(uploadPath, SUBMISSION_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }
}