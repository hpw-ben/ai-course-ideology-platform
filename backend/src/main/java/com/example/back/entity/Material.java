package com.example.back.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Material {
    private Long id;
    private String title;
    private String type; // IMAGE/VIDEO/ARTICLE
    private String content;
    private String fileUrl;
    private Integer viewCount;
    private String description;
    private Long teacherId;
    private String status; // PENDING/APPROVED/REJECTED
    private String shelfStatus; // ON/OFF
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Tag> tags;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShelfStatus() { return shelfStatus; }
    public void setShelfStatus(String shelfStatus) { this.shelfStatus = shelfStatus; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}
