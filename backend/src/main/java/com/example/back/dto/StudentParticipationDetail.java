package com.example.back.dto;

import java.time.LocalDateTime;

public class StudentParticipationDetail {
    private Long studentId;
    private String studentName;
    private String studentRealName;
    private String studentMajor;
    private LocalDateTime bindTime;

    private Long recordId;
    private String type;
    private String targetCode;
    private String targetTitle;
    private Integer duration;
    private LocalDateTime enterTime;
    private LocalDateTime leaveTime;

    private Boolean checkedIn;
    private LocalDateTime checkinTime;

    private Integer quizTotal;
    private Integer quizCorrect;
    private LocalDateTime quizSubmittedAt;

    private Integer taskTotalMaterials;
    private Integer taskCompletedMaterials;

    private Integer discussionCommentCount;
    private Integer discussionReplyCount;
    private LocalDateTime discussionLastCommentAt;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRealName() {
        return studentRealName;
    }

    public void setStudentRealName(String studentRealName) {
        this.studentRealName = studentRealName;
    }

    public String getStudentMajor() {
        return studentMajor;
    }

    public void setStudentMajor(String studentMajor) {
        this.studentMajor = studentMajor;
    }

    public LocalDateTime getBindTime() {
        return bindTime;
    }

    public void setBindTime(LocalDateTime bindTime) {
        this.bindTime = bindTime;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(LocalDateTime enterTime) {
        this.enterTime = enterTime;
    }

    public LocalDateTime getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(LocalDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public Boolean getCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(LocalDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }

    public Integer getQuizTotal() {
        return quizTotal;
    }

    public void setQuizTotal(Integer quizTotal) {
        this.quizTotal = quizTotal;
    }

    public Integer getQuizCorrect() {
        return quizCorrect;
    }

    public void setQuizCorrect(Integer quizCorrect) {
        this.quizCorrect = quizCorrect;
    }

    public LocalDateTime getQuizSubmittedAt() {
        return quizSubmittedAt;
    }

    public void setQuizSubmittedAt(LocalDateTime quizSubmittedAt) {
        this.quizSubmittedAt = quizSubmittedAt;
    }

    public Integer getTaskTotalMaterials() {
        return taskTotalMaterials;
    }

    public void setTaskTotalMaterials(Integer taskTotalMaterials) {
        this.taskTotalMaterials = taskTotalMaterials;
    }

    public Integer getTaskCompletedMaterials() {
        return taskCompletedMaterials;
    }

    public void setTaskCompletedMaterials(Integer taskCompletedMaterials) {
        this.taskCompletedMaterials = taskCompletedMaterials;
    }

    public Integer getDiscussionCommentCount() {
        return discussionCommentCount;
    }

    public void setDiscussionCommentCount(Integer discussionCommentCount) {
        this.discussionCommentCount = discussionCommentCount;
    }

    public Integer getDiscussionReplyCount() {
        return discussionReplyCount;
    }

    public void setDiscussionReplyCount(Integer discussionReplyCount) {
        this.discussionReplyCount = discussionReplyCount;
    }

    public LocalDateTime getDiscussionLastCommentAt() {
        return discussionLastCommentAt;
    }

    public void setDiscussionLastCommentAt(LocalDateTime discussionLastCommentAt) {
        this.discussionLastCommentAt = discussionLastCommentAt;
    }
}
