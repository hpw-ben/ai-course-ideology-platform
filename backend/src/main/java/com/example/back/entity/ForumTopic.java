package com.example.back.entity;

import java.time.LocalDateTime;
import java.util.List;

public class ForumTopic {
    private Long id;
    private String title;
    private String description;
    private String code;
    private Long adminId;
    private String newsIdsJson;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联字段
    private String adminName;
    private List<Material> materials;
    private List<Long> newsIds;
    private List<News> relatedNews;
    private Integer commentCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getNewsIdsJson() { return newsIdsJson; }
    public void setNewsIdsJson(String newsIdsJson) { this.newsIdsJson = newsIdsJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }
    public List<Long> getNewsIds() { return newsIds; }
    public void setNewsIds(List<Long> newsIds) { this.newsIds = newsIds; }
    public List<News> getRelatedNews() { return relatedNews; }
    public void setRelatedNews(List<News> relatedNews) { this.relatedNews = relatedNews; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
}
