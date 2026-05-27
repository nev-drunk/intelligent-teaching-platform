package com.huadi.intelligentteachingplatform.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 核心业务：根据考点生成题目并入库
     */
    @Transactional(rollbackFor = Exception.class) // 涉及数据库操作，建议加上事务
    public Question generateAndSaveAiQuestion(Long courseId, String keyword) {
        // 1. 定义强约束的系统 Prompt
        String systemPrompt = "你是一个高校智能题库出题专家。你必须生成一道关于用户指定考点的单选题(SINGLE)。" +
                "要求返回严格的JSON格式，千万不要包含任何 markdown 标识符（如 ```json 等），格式必须如下：\n" +
                "{\"content\": \"题干\", \"options\": \"[\\\"A.选项1\\\", \\\"B.选项2\\\", \\\"C.选项3\\\", \\\"D.选项4\\\"]\", \"answer\": \"A\"}";

        String userPrompt = "请围绕关键词【" + keyword + "】出题。";

        // 2. 调用通用的 Service 层获取 AI 结果
        String aiRawResult = deepSeekService.generateText(systemPrompt, userPrompt);

        // 3. 解析并落库
        JSONObject questionJson = JSON.parseObject(aiRawResult);

        Question question = new Question();
        question.setCourseId(courseId);
        question.setType("SINGLE");
        question.setContent(questionJson.getString("content"));
        question.setOptions(questionJson.getString("options"));
        question.setAnswer(questionJson.getString("answer"));
        question.setIsLlmGenerated(1); // 💥 标记为 AI 生成

        // 执行入库
        questionMapper.insertQuestion(question);

        return question;
    }
}