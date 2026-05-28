package com.example.back.dto;

public class UpdateUsernameRequest {
    private Long userId;
    private String newUsername;
    private String role;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
