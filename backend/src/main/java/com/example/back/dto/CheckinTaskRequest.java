package com.example.back.dto;

import jakarta.validation.constraints.NotNull;

public class CheckinTaskRequest {
    @NotNull(message = "recordId不能为空")
    private Long recordId;

    @NotNull(message = "taskId不能为空")
    private Long taskId;
    private Integer duration;
    private Integer materialDuration;
    private Integer interactionDuration;

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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getMaterialDuration() {
        return materialDuration;
    }

    public void setMaterialDuration(Integer materialDuration) {
        this.materialDuration = materialDuration;
    }

    public Integer getInteractionDuration() {
        return interactionDuration;
    }

    public void setInteractionDuration(Integer interactionDuration) {
        this.interactionDuration = interactionDuration;
    }
}
