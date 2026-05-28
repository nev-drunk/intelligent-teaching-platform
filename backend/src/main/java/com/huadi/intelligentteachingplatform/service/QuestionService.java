package com.huadi.intelligentteachingplatform.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.dto.question.AiGenerateRequest;
import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final DeepSeekService deepSeekService;
    private final QuestionMapper questionMapper;

    public List<Question> getAllQuestions() {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>().orderByDesc(Question::getId));
    }

    public Optional<Question> getQuestionById(Long id) {
        return Optional.ofNullable(questionMapper.selectById(id));
    }

    public List<Question> getQuestionsByCourseId(Long courseId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getCourseId, courseId)
                        .orderByDesc(Question::getId));
    }

    public Question saveQuestion(Question question) {
        if (question.getIsLlmGenerated() == null) {
            question.setIsLlmGenerated(0);
        }
        if (question.getId() == null) {
            questionMapper.insert(question);
        } else {
            questionMapper.updateById(question);
        }
        return question;
    }

    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Object generateByAi(AiGenerateRequest request) {
        if (StringUtils.isNotBlank(request.getPrompt())) {
            return generateQuestionsByAi(request.getPrompt(), request.getCount(), request.getCourseId());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            return generateAndSaveAiQuestion(request.getCourseId(), request.getKeyword());
        }
        throw new IllegalArgumentException("请提供 prompt（批量出题）或 keyword（单题出题）");
    }

    @Transactional(rollbackFor = Exception.class)
    public Question generateAndSaveAiQuestion(Long courseId, String keyword) {
        String systemPrompt = "你是一个高校智能题库出题专家。你必须生成一道关于用户指定考点的单选题(SINGLE)。" +
                "要求返回严格的JSON格式，千万不要包含任何 markdown 标识符，格式必须如下：\n" +
                "{\"content\": \"题干\", \"options\": \"[\\\"A.选项1\\\", \\\"B.选项2\\\", \\\"C.选项3\\\", \\\"D.选项4\\\"]\", \"answer\": \"A\"}";

        String userPrompt = "请围绕关键词【" + keyword + "】出题。";
        String aiRawResult = deepSeekService.generateText(systemPrompt, userPrompt);
        JSONObject questionJson = JSON.parseObject(aiRawResult);

        Question question = new Question();
        question.setCourseId(courseId);
        question.setType("SINGLE");
        question.setContent(questionJson.getString("content"));
        question.setOptions(questionJson.getString("options"));
        question.setAnswer(questionJson.getString("answer"));
        question.setIsLlmGenerated(1);
        questionMapper.insert(question);
        return question;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Question> generateQuestionsByAi(String prompt, int count, Long courseId) {
        String systemPrompt = String.format(
                "你是一个专业的出题助手。请根据用户需求生成%d道题目，并以JSON数组格式返回。\n" +
                        "每道题必须包含：content(题干), type(题型:SINGLE/MULTI/JUDGE/GAP/ESSAY), answer(答案), options(选项JSON字符串，非选择题可为null)。\n" +
                        "只返回JSON数组，不要其他文字。",
                count);

        String aiRawResult = deepSeekService.generateText(systemPrompt, prompt);
        String json = aiRawResult.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "").replaceAll("```\\s*$", "");
        }

        JSONArray arr = JSON.parseArray(json);
        List<Question> saved = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject item = arr.getJSONObject(i);
            Question question = new Question();
            question.setCourseId(courseId);
            question.setType(item.getString("type") != null ? item.getString("type").toUpperCase() : "SINGLE");
            question.setContent(item.getString("content") != null ? item.getString("content") : item.getString("stem"));
            Object options = item.get("options");
            if (options instanceof String) {
                question.setOptions((String) options);
            } else if (options != null) {
                question.setOptions(JSON.toJSONString(options));
            }
            question.setAnswer(item.getString("answer"));
            question.setIsLlmGenerated(1);
            questionMapper.insert(question);
            saved.add(question);
        }
        return saved;
    }
}
