package com.example.back.entity;

import java.time.LocalDateTime;

public class LearningRecord {
    private Long id;
    private Long studentId;
    private String type; // 任务或者讨论
    private Long targetId;
    private String targetCode;
    private String targetTitle;
    private Integer duration; // 学习时长（秒）
    private Integer materialDuration; // 素材学习时长（秒）
    private Integer interactionDuration;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;
    private LocalDateTime createdAt;

    private String quizJson;
    private String quizAnswersJson;
    private Integer quizTotal;
    private Integer quizCorrect;
    private LocalDateTime quizSubmittedAt;
    private String reflection;
    private String reportPdfUrl;
    private LocalDateTime reportGeneratedAt;
    private LocalDateTime lastHeartbeatAt;
    private Integer lastHeartbeatDuration;

    private String viewpointChoice;
    private String shortNote;

    private Boolean checkedIn;
    private LocalDateTime checkinTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getTargetTitle() { return targetTitle; }
    public void setTargetTitle(String targetTitle) { this.targetTitle = targetTitle; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getMaterialDuration() { return materialDuration; }
    public void setMaterialDuration(Integer materialDuration) { this.materialDuration = materialDuration; }

    public Integer getInteractionDuration() { return interactionDuration; }
    public void setInteractionDuration(Integer interactionDuration) { this.interactionDuration = interactionDuration; }

    public LocalDateTime getEnterTime() { return enterTime; }
    public void setEnterTime(LocalDateTime enterTime) { this.enterTime = enterTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getQuizJson() { return quizJson; }
    public void setQuizJson(String quizJson) { this.quizJson = quizJson; }

    public String getQuizAnswersJson() { return quizAnswersJson; }
    public void setQuizAnswersJson(String quizAnswersJson) { this.quizAnswersJson = quizAnswersJson; }

    public Integer getQuizTotal() { return quizTotal; }
    public void setQuizTotal(Integer quizTotal) { this.quizTotal = quizTotal; }

    public Integer getQuizCorrect() { return quizCorrect; }
    public void setQuizCorrect(Integer quizCorrect) { this.quizCorrect = quizCorrect; }

    public LocalDateTime getQuizSubmittedAt() { return quizSubmittedAt; }
    public void setQuizSubmittedAt(LocalDateTime quizSubmittedAt) { this.quizSubmittedAt = quizSubmittedAt; }

    public String getReflection() { return reflection; }
    public void setReflection(String reflection) { this.reflection = reflection; }

    public String getReportPdfUrl() { return reportPdfUrl; }
    public void setReportPdfUrl(String reportPdfUrl) { this.reportPdfUrl = reportPdfUrl; }

    public LocalDateTime getReportGeneratedAt() { return reportGeneratedAt; }
    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) { this.reportGeneratedAt = reportGeneratedAt; }

    public LocalDateTime getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(LocalDateTime lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }

    public Integer getLastHeartbeatDuration() { return lastHeartbeatDuration; }
    public void setLastHeartbeatDuration(Integer lastHeartbeatDuration) { this.lastHeartbeatDuration = lastHeartbeatDuration; }

    public String getViewpointChoice() { return viewpointChoice; }
    public void setViewpointChoice(String viewpointChoice) { this.viewpointChoice = viewpointChoice; }

    public String getShortNote() { return shortNote; }
    public void setShortNote(String shortNote) { this.shortNote = shortNote; }

    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }

    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }
}
