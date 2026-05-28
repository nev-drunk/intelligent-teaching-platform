package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.Issue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IssueMapper {

    List<Issue> selectList(@Param("courseId") Long courseId);

    Issue selectById(@Param("id") Long id);

    int insert(Issue issue);

    int updateReply(@Param("id") Long id, @Param("teacherReply") String teacherReply, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);
}
