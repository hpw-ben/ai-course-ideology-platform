package com.example.back.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Discussion {
    private Long id;
    private String title;
    private String description;
    private String code;
    private Long teacherId;
    private Long materialId;  // 兼容旧数据
    private String status; // NOT_STARTED, IN_PROGRESS, ENDED
    private Integer viewCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联字段
    private Material material;  // 兼容旧数据
    private List<Material> materials;  // 多素材支持
    private List<News> newsList;  // 关联新闻（管理员发布）
    private String teacherName;
    private Integer commentCount;

    private Integer boundStudentCount;
    private Integer boundStudentParticipantCount;
    private Integer boundStudentCommentCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }

    public List<News> getNewsList() { return newsList; }
    public void setNewsList(List<News> newsList) { this.newsList = newsList; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }

    public Integer getBoundStudentCount() { return boundStudentCount; }
    public void setBoundStudentCount(Integer boundStudentCount) { this.boundStudentCount = boundStudentCount; }

    public Integer getBoundStudentParticipantCount() { return boundStudentParticipantCount; }
    public void setBoundStudentParticipantCount(Integer boundStudentParticipantCount) { this.boundStudentParticipantCount = boundStudentParticipantCount; }

    public Integer getBoundStudentCommentCount() { return boundStudentCommentCount; }
    public void setBoundStudentCommentCount(Integer boundStudentCommentCount) { this.boundStudentCommentCount = boundStudentCommentCount; }
}
