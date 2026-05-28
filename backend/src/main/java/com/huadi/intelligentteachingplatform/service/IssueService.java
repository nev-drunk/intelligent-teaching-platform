package com.huadi.intelligentteachingplatform.service;

import com.huadi.intelligentteachingplatform.entity.Issue;
import com.huadi.intelligentteachingplatform.mapper.IssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssueService {

    @Autowired
    private IssueMapper issueMapper;

    /** 查询答疑列表，可按课程ID筛选 */
    public List<Issue> list(Long courseId) {
        return issueMapper.selectList(courseId);
    }

    /** 查询单个答疑 */
    public Issue getById(Long id) {
        return issueMapper.selectById(id);
    }

    /** 学生发布问题，默认状态为未解决(0) */
    public Issue create(Issue issue) {
        issue.setStatus(0);
        issueMapper.insert(issue);
        return issue;
    }

    /** 教师回复问题，状态置为已解答(1) */
    public boolean reply(Long id, String teacherReply) {
        return issueMapper.updateReply(id, teacherReply, 1) > 0;
    }

    /** 删除问题 */
    public boolean delete(Long id) {
        return issueMapper.deleteById(id) > 0;
    }
}
