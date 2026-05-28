package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecordEnterRequest {
    @NotNull(message = "studentId不能为空")
    private Long studentId;

    @NotBlank(message = "type不能为空")
    private String type;

    @NotNull(message = "targetId不能为空")
    private Long targetId;

    @NotBlank(message = "targetCode不能为空")
    private String targetCode;

    @NotBlank(message = "targetTitle不能为空")
    private String targetTitle;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }
}
