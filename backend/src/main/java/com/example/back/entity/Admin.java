package com.example.back.entity;

import java.time.LocalDateTime;

public class Admin {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String role = "ADMIN"; // 用于前端识别

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
