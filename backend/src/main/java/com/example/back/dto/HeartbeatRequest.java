package com.example.back.dto;

import jakarta.validation.constraints.NotNull;

public class HeartbeatRequest {
    @NotNull(message = "recordId不能为空")
    private Long recordId;
    private Integer duration;
    private Integer materialDuration;
    private Integer interactionDuration;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
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
