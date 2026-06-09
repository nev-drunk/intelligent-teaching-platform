package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import com.huadi.intelligentteachingplatform.service.PlagiarismService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plagiarism")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlagiarismCheckController {

    private final PlagiarismService plagiarismService;

    /** 单提交查重 */
    @PostMapping("/check-by-submission/{submissionId}")
    public ApiResponse<PlagiarismService.CheckResult> checkBySubmission(@PathVariable Long submissionId) {
        return ApiResponse.ok(plagiarismService.checkBySubmission(submissionId));
    }
}
