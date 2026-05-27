package com.huadi.intelligentteachingplatform.entity;

import java.util.Date;

public class ExamPaper {
    private Long id;
    private Long courseId;
    private String title;
    private Integer totalScore;
    private Long classId;
    private Long teacherId; // 💥 新增：支持多教师多卷隔离
    private Date createTime;

    // 以下为完整 Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer totalScore() { return totalScore; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}