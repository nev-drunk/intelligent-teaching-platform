package com.huadi.intelligentteachingplatform.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StudentSaveDTO {

    /**
     * 学生ID（修改时使用）
     */
    private Long id;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过50位")
    private String studentName;

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20位")
    private String studentNo;

    /**
     * 班级ID
     */
    @NotNull(message = "班级ID不能为空")
    @Min(value = 1, message = "班级ID必须大于0")
    private Long classId;
}