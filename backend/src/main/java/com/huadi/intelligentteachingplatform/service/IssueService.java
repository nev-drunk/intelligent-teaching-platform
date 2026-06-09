package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.dto.ai.SimilarCandidate;
import com.huadi.intelligentteachingplatform.dto.ai.SimilarItem;
import com.huadi.intelligentteachingplatform.entity.Issue;
import com.huadi.intelligentteachingplatform.mapper.IssueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueMapper issueMapper;
    private final AiServiceClient aiServiceClient;

    /** 查询答疑列表，可按课程ID筛选 */
    public List<Issue> list(Long courseId) {
        return issueMapper.selectList(courseId);
    }

    /** 查询单个答疑 */
    public Issue getById(Long id) {
        return issueMapper.selectById(id);
    }

    /**
     * 【只读】相似问题检索 — 纯查询，绝对不保存任何数据
     * 用于用户在输入框中实时检索相似历史问题
     *
     * @param questionText 用户当前输入的文本
     * @param courseId     课程ID
     * @return Top3 相似问题列表
     */
    public List<SimilarItem> checkSimilar(String questionText, Long courseId) {
        // 获取该课程下所有已有问题文本作为候选
        List<Issue> existingIssues = issueMapper.selectList(courseId);
        List<SimilarCandidate> candidates = new ArrayList<>();
        for (Issue existing : existingIssues) {
            if (existing.getQuestionText() != null
                    && !existing.getQuestionText().isBlank()) {
                candidates.add(new SimilarCandidate(existing.getId(), existing.getQuestionText()));
            }
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 调用 AI 相似度检索 Top3（纯查询，不涉及任何写操作）
        List<SimilarItem> top3 = aiServiceClient.getTop3SimilarWithId(questionText, candidates);

        log.info("[只读] 相似问题检索完成, query='{}', Top3 结果数: {}",
                questionText.length() > 50 ? questionText.substring(0, 50) + "..." : questionText,
                top3.size());
        return top3;
    }

    /**
     * 学生发布问题（仅保存，不返回相似结果）
     */
    public Issue create(Issue issue) {
        issue.setStatus(0);
        issueMapper.insert(issue);
        return issue;
    }

    /**
     * 发布问题并返回 Top3 相似历史问题
     * 注意：此方法会保存问题，仅应在用户点击"发布"时调用
     */
    public IssueCreationResult createWithSimilarityCheck(Issue issue) {
        issue.setStatus(0);
        issueMapper.insert(issue);

        // 获取该课程下所有已有问题文本作为候选
        List<Issue> existingIssues = issueMapper.selectList(issue.getCourseId());
        List<SimilarCandidate> candidates = new ArrayList<>();
        for (Issue existing : existingIssues) {
            if (existing.getQuestionText() != null
                    && !existing.getId().equals(issue.getId())) {
                candidates.add(new SimilarCandidate(existing.getId(), existing.getQuestionText()));
            }
        }

        // 调用 AI 相似度检索 Top3
        List<SimilarItem> top3 = candidates.isEmpty()
                ? List.of()
                : aiServiceClient.getTop3SimilarWithId(issue.getQuestionText(), candidates);

        log.info("问题发布完成，ID: {}, Top3 相似问题数: {}", issue.getId(), top3.size());

        IssueCreationResult result = new IssueCreationResult();
        result.setIssue(issue);
        result.setSimilarQuestions(top3);
        return result;
    }

    /** 教师回复问题，状态置为已解答(1) */
    public boolean reply(Long id, String teacherReply) {
        return issueMapper.updateReply(id, teacherReply, 1) > 0;
    }

    /** 删除问题 */
    public boolean delete(Long id) {
        return issueMapper.deleteById(id) > 0;
    }

    // ── 内部结果类 ─────────────────────────────────────────────────

    @lombok.Data
    public static class IssueCreationResult {
        private Issue issue;
        private List<SimilarItem> similarQuestions;
    }
}
