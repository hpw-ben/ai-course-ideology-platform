package com.example.back.dto;

import jakarta.validation.constraints.NotNull;

public class MarkTaskMaterialCompletedRequest {
    @NotNull(message = "studentId不能为空")
    private Long studentId;

    @NotNull(message = "taskId不能为空")
    private Long taskId;

    @NotNull(message = "materialId不能为空")
    private Long materialId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
}
