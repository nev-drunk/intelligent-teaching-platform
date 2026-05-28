package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.entity.Submission;
import com.huadi.intelligentteachingplatform.entity.Task;
import com.huadi.intelligentteachingplatform.exception.BusinessException;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import com.huadi.intelligentteachingplatform.mapper.SubmissionMapper;
import com.huadi.intelligentteachingplatform.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * 作业提交服务类
 * 提供作业提交、AI批改、教师复核等核心业务功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final TaskMapper taskMapper;
    private final QuestionMapper questionMapper;
    private final BaiduOcrService baiduOcrService;

    /**
     * 提交状态常量
     */
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_AI_PROCESSED = "AI_PROCESSED";
    public static final String STATUS_GRADED = "GRADED";

    /**
     * 根据教师ID获取提交列表
     */
    public List<Submission> getSubmissionsByTeacherId(Long teacherId) {
        return submissionMapper.selectByTeacherId(teacherId);
    }

    /**
     * 根据ID获取提交记录
     */
    public Submission getSubmissionById(Long id) {
        return submissionMapper.selectById(id);
    }

    /**
     * 教师批改
     */
    public Submission updateComment(Long id, String comment, Integer score) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }
        submission.setTeacherComment(comment);
        if (score != null) {
            submission.setTeacherScore(score);
        }
        submission.setStatus(STATUS_GRADED);
        submissionMapper.updateById(submission);
        return submission;
    }

    /**
     * AI自动批改处理
     *
     * @param submissionId 提交记录ID
     * @return 处理后的提交记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Submission aiBatchProcess(Long submissionId) {
        log.info("开始AI批改处理，提交ID: {}", submissionId);

        // 1. 根据ID查询提交记录
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 2. 检查状态，只能处理 SUBMITTED 状态的提交
        if (!STATUS_SUBMITTED.equals(submission.getStatus())) {
            throw new BusinessException(400, "只能处理待批改状态的提交记录");
        }

        // 3. 获取识别文本（优先使用OCR识别，没有图片则使用提交文本）
        String recognizeText = null;
        String fileUrl = submission.getFileUrl();
        
        if (fileUrl != null && !fileUrl.trim().isEmpty()) {
            // 有图片，进行OCR识别
            try {
                String base64Image = convertFileToBase64(fileUrl);
                recognizeText = baiduOcrService.executeOcr(base64Image);
                log.info("OCR识别完成，提交ID: {}, 识别文本长度: {}", submissionId, 
                        recognizeText != null ? recognizeText.length() : 0);
            } catch (Exception e) {
                log.warn("OCR识别失败，将尝试使用提交文本，提交ID: {}, 原因: {}", submissionId, e.getMessage());
            }
        }
        
        // 如果OCR识别失败或没有图片，使用提交文本
        if (recognizeText == null || recognizeText.trim().isEmpty()) {
            recognizeText = submission.getSubmitText();
            log.info("使用提交文本进行批改，提交ID: {}, 文本长度: {}", submissionId, 
                    recognizeText != null ? recognizeText.length() : 0);
        }

        // 4. 保存识别结果
        submission.setOcrRawText(recognizeText);

        // 5. 根据任务关联的试卷进行客观题批改
        Integer aiScore = 0;
        String aiComment = "";
        
        // 检查是否有识别文本
        if (recognizeText == null || recognizeText.trim().isEmpty()) {
            aiComment = "未检测到作答内容，请教师手动复核";
        } else {
            try {
                aiScore = calculateAiScore(submission.getTaskId(), recognizeText);
                aiComment = generateAiComment(aiScore);
                log.info("AI批改完成，提交ID: {}, 得分: {}", submissionId, aiScore);
            } catch (Exception e) {
                log.warn("AI批改计算异常，提交ID: {}, 原因: {}", submissionId, e.getMessage());
                aiComment = "自动批改异常，请教师手动复核";
            }
        }

        // 6. 更新提交记录
        submission.setAiScore(aiScore);
        submission.setAiComment(aiComment);
        submission.setStatus(STATUS_AI_PROCESSED);
        
        submissionMapper.updateById(submission);

        log.info("AI批改流程完成，提交ID: {}, 状态: {}, AI得分: {}", 
                submissionId, submission.getStatus(), aiScore);

        return submission;
    }

    /**
     * 将文件转换为Base64编码
     */
    private String convertFileToBase64(String fileUrl) {
        try {
            // 处理URL路径，提取本地文件路径
            String filePath = fileUrl;
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                // 如果是URL，提取路径部分
                filePath = fileUrl.replaceFirst("^https?://[^/]+/", "");
            }
            
            // 构建完整路径
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                // 尝试从uploads目录查找
                path = Paths.get("uploads", filePath);
            }
            
            if (!Files.exists(path)) {
                throw new BusinessException(404, "图片文件不存在: " + filePath);
            }

            byte[] fileBytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", fileUrl, e);
            throw new BusinessException(500, "读取图片文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务ID计算AI得分
     */
    private Integer calculateAiScore(Long taskId) {
        return calculateAiScore(taskId, null);
    }

    /**
     * 根据任务ID和OCR文本计算AI得分
     */
    private Integer calculateAiScore(Long taskId, String ocrText) {
        // 1. 获取任务信息
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("任务不存在，任务ID: {}", taskId);
            return 0;
        }

        // 2. 如果任务关联了试卷，根据试卷批改
        if (task.getPaperId() != null) {
            return calculateScoreByPaper(task.getPaperId(), ocrText);
        }

        // 3. 否则返回默认分数（可根据业务需求调整）
        return 0;
    }

    /**
     * 根据试卷计算得分
     */
    private Integer calculateScoreByPaper(Long paperId, String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return 0;
        }

        // 获取试卷关联的题目（需要在QuestionMapper中添加查询方法）
        List<Question> questions = questionMapper.selectByPaperId(paperId);
        if (questions == null || questions.isEmpty()) {
            log.warn("试卷未关联题目，试卷ID: {}", paperId);
            return 0;
        }

        int totalScore = 0;
        int maxScore = 0;

        for (Question question : questions) {
            maxScore += question.getScore() != null ? question.getScore() : 0;
            
            String answer = question.getAnswer();
            if (answer == null || answer.trim().isEmpty()) {
                continue;
            }

            // 根据题目类型进行不同的批改逻辑
            String type = question.getType();
            int questionScore = question.getScore() != null ? question.getScore() : 10;

            if ("SINGLE".equals(type) || "JUDGE".equals(type)) {
                // 单选题和判断题：关键词匹配
                if (matchAnswer(ocrText, answer)) {
                    totalScore += questionScore;
                }
            } else if ("MULTIPLE".equals(type)) {
                // 多选题：匹配多个关键词
                if (matchMultipleAnswer(ocrText, answer)) {
                    totalScore += questionScore;
                }
            } else {
                // 其他类型：简单匹配
                if (ocrText.contains(answer.trim())) {
                    totalScore += questionScore;
                }
            }
        }

        return totalScore;
    }

    /**
     * 单答案匹配
     */
    private boolean matchAnswer(String ocrText, String answer) {
        if (ocrText == null || answer == null) {
            return false;
        }

        // 标准化处理：转小写，去除空格
        String normalizedOcr = ocrText.toLowerCase().replaceAll("\\s+", "");
        String normalizedAnswer = answer.toLowerCase().replaceAll("\\s+", "");

        // 检查是否包含答案（支持多种格式）
        return normalizedOcr.contains(normalizedAnswer)
                || normalizedOcr.contains(normalizedAnswer.replace(".", ""));
    }

    /**
     * 多答案匹配（多选题）
     */
    private boolean matchMultipleAnswer(String ocrText, String answer) {
        if (ocrText == null || answer == null) {
            return false;
        }

        String normalizedOcr = ocrText.toLowerCase().replaceAll("\\s+", "");
        
        // 假设多选题答案格式为 "A,B,C" 或 "ABC"
        String[] answers = answer.toLowerCase().split("[,，]");
        int matchCount = 0;

        for (String ans : answers) {
            String normalizedAns = ans.trim().replaceAll("\\s+", "");
            if (normalizedOcr.contains(normalizedAns) || normalizedOcr.contains(normalizedAns.replace(".", ""))) {
                matchCount++;
            }
        }

        // 全部匹配才算正确
        return matchCount == answers.length;
    }

    /**
     * 生成AI评语
     */
    private String generateAiComment(Integer score) {
        if (score >= 90) {
            return "回答优秀，正确率很高！";
        } else if (score >= 70) {
            return "回答良好，继续加油！";
        } else if (score >= 60) {
            return "回答及格，需要加强练习。";
        } else {
            return "回答较差，请认真复习相关知识。";
        }
    }

    /**
     * 分页查询提交列表（连表查询任务名称）
     */
    public List<Submission> getSubmissionsWithTaskInfo(Long taskId, Integer page, Integer size) {
        return submissionMapper.selectSubmissionsWithTaskInfo(taskId, (page - 1) * size, size);
    }

    /**
     * 统计提交数量
     */
    public long countSubmissions(Long taskId) {
        return submissionMapper.countSubmissions(taskId);
    }

    /**
     * 保存提交记录
     */
    public Submission saveSubmission(Submission submission) {
        if (submission.getId() == null) {
            submissionMapper.insert(submission);
        } else {
            submissionMapper.updateById(submission);
        }
        return submission;
    }

    /**
     * 删除提交记录
     */
    public void deleteSubmission(Long id) {
        submissionMapper.deleteById(id);
    }
}