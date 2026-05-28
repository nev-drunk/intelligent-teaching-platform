package com.huadi.intelligentteachingplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.intelligentteachingplatform.dto.PaperDetailsDTO;
import com.huadi.intelligentteachingplatform.dto.PaperSummaryDTO;
import com.huadi.intelligentteachingplatform.dto.paper.PaperQuestionItemDTO;
import com.huadi.intelligentteachingplatform.dto.paper.PublishPaperRequest;
import com.huadi.intelligentteachingplatform.dto.paper.SavePaperRequest;
import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import com.huadi.intelligentteachingplatform.entity.PaperQuestion;
import com.huadi.intelligentteachingplatform.entity.Question;
import com.huadi.intelligentteachingplatform.mapper.ExamPaperMapper;
import com.huadi.intelligentteachingplatform.mapper.PaperQuestionMapper;
import com.huadi.intelligentteachingplatform.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PaperService {

    private final ExamPaperMapper examPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    public List<PaperSummaryDTO> getPapersByTeacherAndCourse(Long teacherId, Long courseId) {
        return examPaperMapper.selectPapersByTeacher(teacherId, courseId);
    }

    public List<PaperDetailsDTO> getAllPapersWithQuestions() {
        return examPaperMapper.selectList(
                new LambdaQueryWrapper<ExamPaper>().orderByDesc(ExamPaper::getId)
        ).stream().map(this::buildPaperDetailsDTO).collect(Collectors.toList());
    }

    public List<ExamPaper> getPapersByCourseId(Long courseId) {
        return examPaperMapper.selectList(
                new LambdaQueryWrapper<ExamPaper>()
                        .eq(ExamPaper::getCourseId, courseId)
                        .orderByDesc(ExamPaper::getId));
    }

    public Optional<PaperDetailsDTO> getPaperDetailsById(Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null) {
            return Optional.empty();
        }
        return Optional.of(buildPaperDetailsDTO(paper));
    }

    public ExamPaper updatePaper(ExamPaper paper) {
        examPaperMapper.updateById(paper);
        return paper;
    }

    public void deletePaperCascade(Long id) {
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        examPaperMapper.deleteById(id);
    }

    public ExamPaper publishPaper(PublishPaperRequest request) {
        int totalScore = request.getItems().stream()
                .mapToInt(PaperQuestionItemDTO::getScore)
                .sum();

        ExamPaper paper = new ExamPaper();
        paper.setCourseId(request.getCourseId());
        paper.setTitle(request.getTitle());
        paper.setTotalScore(totalScore);
        paper.setClassId(request.getClassId());
        paper.setTeacherId(request.getTeacherId());
        examPaperMapper.insert(paper);

        savePaperQuestions(paper.getId(), request.getItems());
        return paper;
    }

    public ExamPaper savePaperWithQuestions(SavePaperRequest request) {
        ExamPaper paper = new ExamPaper();
        paper.setCourseId(request.getCourseId());
        paper.setTitle(request.getTitle());
        paper.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        paper.setClassId(request.getClassId());
        paper.setTeacherId(request.getTeacherId());
        examPaperMapper.insert(paper);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            savePaperQuestions(paper.getId(), request.getQuestions());
        }
        return paper;
    }

    private void savePaperQuestions(Long paperId, List<PaperQuestionItemDTO> items) {
        int sort = 1;
        for (PaperQuestionItemDTO item : items) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(item.getQuestionId());
            pq.setSort(sort++);
            pq.setScore(item.getScore());
            paperQuestionMapper.insert(pq);
        }
    }

    private PaperDetailsDTO buildPaperDetailsDTO(ExamPaper paper) {
        PaperDetailsDTO dto = new PaperDetailsDTO();
        dto.setId(paper.getId());
        dto.setCourseId(paper.getCourseId());
        dto.setTitle(paper.getTitle());
        dto.setTotalScore(paper.getTotalScore());
        dto.setClassId(paper.getClassId());
        dto.setTeacherId(paper.getTeacherId());
        dto.setCreateTime(paper.getCreateTime());

        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paper.getId())
                        .orderByAsc(PaperQuestion::getSort));

        List<PaperDetailsDTO.QuestionInfo> qInfoList = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            if (q != null) {
                PaperDetailsDTO.QuestionInfo qInfo = new PaperDetailsDTO.QuestionInfo();
                qInfo.setQuestionId(q.getId());
                qInfo.setSort(pq.getSort());
                qInfo.setScore(pq.getScore());
                qInfo.setContent(q.getContent());
                qInfo.setType(q.getType());
                qInfo.setOptions(q.getOptions());
                qInfo.setAnswer(q.getAnswer());
                qInfo.setIsLlmGenerated(q.getIsLlmGenerated());
                qInfoList.add(qInfo);
            }
        }
        dto.setQuestions(qInfoList);
        return dto;
    }
}
