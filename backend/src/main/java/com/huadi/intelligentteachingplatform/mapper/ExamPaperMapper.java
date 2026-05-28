package com.huadi.intelligentteachingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.intelligentteachingplatform.dto.PaperSummaryDTO;
import com.huadi.intelligentteachingplatform.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {

    List<PaperSummaryDTO> selectPapersByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId
    );
}
