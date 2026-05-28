package com.example.back.entity;

import java.time.LocalDateTime;

public class StudentBinding {
    private Long id;
    private Long studentId;
    private Long teacherId;
    private LocalDateTime bindTime;
    
    // 学生关联信息
    private String studentName;
    private String studentRealName;
    private String studentMajor;
    
    // 教师关联信息
    private String teacherName;
    private String teacherRealName;
    private String teacherMajor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public LocalDateTime getBindTime() { return bindTime; }
    public void setBindTime(LocalDateTime bindTime) { this.bindTime = bindTime; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentRealName() { return studentRealName; }
    public void setStudentRealName(String studentRealName) { this.studentRealName = studentRealName; }
    public String getStudentMajor() { return studentMajor; }
    public void setStudentMajor(String studentMajor) { this.studentMajor = studentMajor; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getTeacherRealName() { return teacherRealName; }
    public void setTeacherRealName(String teacherRealName) { this.teacherRealName = teacherRealName; }
    public String getTeacherMajor() { return teacherMajor; }
    public void setTeacherMajor(String teacherMajor) { this.teacherMajor = teacherMajor; }
}
