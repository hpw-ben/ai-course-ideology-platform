package com.example.back.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitTaskQuizRequest {
    @NotNull(message = "recordId不能为空")
    private Long recordId;

    @NotNull(message = "taskId不能为空")
    private Long taskId;
    private String taskCode;

    @NotNull(message = "answers不能为空")
    private Object answers;
    private String reflection;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Object getAnswers() {
        return answers;
    }

    public void setAnswers(Object answers) {
        this.answers = answers;
    }

    public String getReflection() {
        return reflection;
    }

    public void setReflection(String reflection) {
        this.reflection = reflection;
    }
}
