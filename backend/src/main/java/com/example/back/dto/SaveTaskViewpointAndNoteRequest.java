package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SaveTaskViewpointAndNoteRequest {
    @NotNull(message = "recordId不能为空")
    private Long recordId;

    @NotNull(message = "studentId不能为空")
    private Long studentId;
    private String viewpointChoice;

    @Size(max = 50, message = "短笔记最多50字")
    private String shortNote;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getViewpointChoice() {
        return viewpointChoice;
    }

    public void setViewpointChoice(String viewpointChoice) {
        this.viewpointChoice = viewpointChoice;
    }

    public String getShortNote() {
        return shortNote;
    }

    public void setShortNote(String shortNote) {
        this.shortNote = shortNote;
    }
}
