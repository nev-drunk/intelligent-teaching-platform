package com.huadi.intelligentteachingplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 【AI预留接口】抄袭痕迹检测
 * 
 * 后期实现方案：
 * 后端用 Levenshtein 编辑距离算法计算两个学生提交文本的相似度，
 * 当相似度超过80%时标记为疑似抄袭，前端红字报警。
 * 结果写入 tb_submission 表的 plagiarism_rate 和 is_cheated 字段。
 */
@RestController
@RequestMapping("/api/plagiarism")
@CrossOrigin(origins = "*")
public class PlagiarismCheckController {

    /**
     * 检测抄袭嫌疑
     * @param body 包含两个提交文本: { "text1": "...", "text2": "..." }
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 501);
        result.put("msg", "抄袭检测功能尚未实现，敬请期待");
        result.put("data", null);
        return ResponseEntity.status(501).body(result);
    }
}