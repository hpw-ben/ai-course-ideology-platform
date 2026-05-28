package com.example.back.entity;

import java.time.LocalDateTime;
import java.util.List;

public class DiscussionComment {
    private Long id;
    private Long discussionId;
    private Long userId;
    private String userType;
    private String content;
    private String imageUrl;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUserType;
    private Boolean isPinned; // 是否置顶
    private Integer likeCount;
    private String likeUsersJson;
    private String status;
    private LocalDateTime createdAt;
    
    // 关联字段
    private String userName;
    private String userRealName;
    private String userAvatar;
    private String replyToUserName;
    private String replyToUserRealName;
    private List<DiscussionComment> replies;
    private Boolean liked;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDiscussionId() { return discussionId; }
    public void setDiscussionId(Long discussionId) { this.discussionId = discussionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserType() { return replyToUserType; }
    public void setReplyToUserType(String replyToUserType) { this.replyToUserType = replyToUserType; }

    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getLikeUsersJson() { return likeUsersJson; }
    public void setLikeUsersJson(String likeUsersJson) { this.likeUsersJson = likeUsersJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRealName() { return userRealName; }
    public void setUserRealName(String userRealName) { this.userRealName = userRealName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }

    public String getReplyToUserRealName() { return replyToUserRealName; }
    public void setReplyToUserRealName(String replyToUserRealName) { this.replyToUserRealName = replyToUserRealName; }

    public List<DiscussionComment> getReplies() { return replies; }
    public void setReplies(List<DiscussionComment> replies) { this.replies = replies; }

    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}
