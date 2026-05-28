package com.example.back.dto;

public class UpdatePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
    private String role;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
