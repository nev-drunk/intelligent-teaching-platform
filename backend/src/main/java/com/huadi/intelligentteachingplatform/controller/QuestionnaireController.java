package com.huadi.intelligentteachingplatform.controller;

import com.huadi.intelligentteachingplatform.entity.Questionnaire;
import com.huadi.intelligentteachingplatform.entity.QuestionnaireAnswer;
import com.huadi.intelligentteachingplatform.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questionnaire")
@CrossOrigin(origins = "*")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    /** 问卷列表 */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) Long teacherId) {
        List<Questionnaire> list = questionnaireService.list(teacherId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    /** 问卷详情 */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Questionnaire q = questionnaireService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (q == null) {
            result.put("code", 404);
            result.put("msg", "问卷不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", q);
        return ResponseEntity.ok(result);
    }

    /** 创建问卷 */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Questionnaire questionnaire) {
        Questionnaire created = questionnaireService.create(questionnaire);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "问卷创建成功");
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    /** 更新问卷 */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Questionnaire questionnaire) {
        questionnaire.setId(id);
        boolean ok = questionnaireService.update(questionnaire);
        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 200 : 500);
        result.put("msg", ok ? "问卷更新成功" : "问卷更新失败");
        return ResponseEntity.ok(result);
    }

    /** 删除问卷 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean ok = questionnaireService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 200 : 500);
        result.put("msg", ok ? "问卷删除成功" : "问卷删除失败");
        return ResponseEntity.ok(result);
    }

    /** 切换问卷状态（发布/关闭） */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        boolean ok = questionnaireService.toggleStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 200 : 500);
        result.put("msg", ok ? "状态更新成功" : "状态更新失败");
        return ResponseEntity.ok(result);
    }

    /** 学生提交问卷答案 */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitAnswer(@PathVariable Long id, @RequestBody QuestionnaireAnswer answer) {
        Map<String, Object> result = new HashMap<>();
        try {
            QuestionnaireAnswer submitted = questionnaireService.submitAnswer(id, answer);
            result.put("code", 200);
            result.put("msg", "提交成功");
            result.put("data", submitted);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /** 查询某份问卷的所有答案 */
    @GetMapping("/{id}/answers")
    public ResponseEntity<Map<String, Object>> getAnswers(@PathVariable Long id) {
        List<QuestionnaireAnswer> answers = questionnaireService.getAnswers(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", answers);
        return ResponseEntity.ok(result);
    }

    /** 关闭问卷并自动生成评价报告 */
    @PostMapping("/{id}/close-and-generate")
    public ResponseEntity<Map<String, Object>> closeAndGenerateReport(@PathVariable Long id) {
        boolean ok = questionnaireService.closeAndGenerateReport(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", ok ? 200 : 500);
        result.put("msg", ok ? "问卷已关闭并生成评价报告" : "操作失败");
        return ResponseEntity.ok(result);
    }
}
