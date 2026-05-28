package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.dto.LeaderMaterialRequest;
import com.example.back.entity.*;
import com.example.back.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;

    @GetMapping("/stats/platform")
    public Result<Map<String, Object>> getPlatformStats() {
        try {
            return Result.success(adminService.getPlatformStats());
        } catch (Exception e) {
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }
    
    //登录
    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            Admin admin = adminService.login(username, password);
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 轮播图管理
    @GetMapping("/carousel/list")
    public Result<List<Carousel>> getAllCarousels(@RequestParam(required = false) String page) {
        return Result.success(adminService.getAllCarousels(page));
    }
    
    @GetMapping("/carousel/active")
    public Result<List<Carousel>> getActiveCarousels(@RequestParam(required = false) String page) {
        return Result.success(adminService.getActiveCarousels(page));
    }
    
    @PostMapping("/carousel/create")
    public Result<Carousel> createCarousel(@RequestBody Carousel carousel) {
        try {
            return Result.success(adminService.createCarousel(carousel));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/carousel/update")
    public Result<Carousel> updateCarousel(@RequestBody Carousel carousel) {
        try {
            return Result.success(adminService.updateCarousel(carousel));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/carousel/{id}")
    public Result<String> deleteCarousel(@PathVariable Long id) {
        try {
            adminService.deleteCarousel(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    //  科学栏目管理
    @GetMapping("/science/list")
    public Result<List<ScienceItem>> getAllScienceItems(@RequestParam(required = false) String module) {
        try {
            return Result.success(adminService.getAllScienceItems(module));
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/science/active")
    public Result<List<ScienceItem>> getActiveScienceItems(@RequestParam(required = false) String module) {
        try {
            return Result.success(adminService.getActiveScienceItems(module));
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @PostMapping("/science/create")
    public Result<ScienceItem> createScienceItem(@RequestBody ScienceItem item) {
        try {
            return Result.success(adminService.createScienceItem(item));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PostMapping("/science/update")
    public Result<ScienceItem> updateScienceItem(@RequestBody ScienceItem item) {
        try {
            return Result.success(adminService.updateScienceItem(item));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/science/{id}")
    public Result<String> deleteScienceItem(@PathVariable Long id) {
        try {
            adminService.deleteScienceItem(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    // 新闻管理
    @GetMapping("/news/list")
    public Result<List<News>> getAllNews(@RequestParam(required = false) String category) {
        return Result.success(adminService.getAllNews(category));
    }
    
    @GetMapping("/news/published")
    public Result<List<News>> getPublishedNews(@RequestParam(required = false) String category) {
        return Result.success(adminService.getPublishedNews(category));
    }
    
    @GetMapping("/news/latest")
    public Result<List<News>> getLatestNews(@RequestParam(defaultValue = "5") int limit,
                                           @RequestParam(required = false) String category) {
        return Result.success(adminService.getLatestNews(limit, category));
    }
    
    @GetMapping("/news/{id}")
    public Result<News> getNewsById(@PathVariable Long id) {
        News news = adminService.getNewsByIdForAdmin(id);
        return news != null ? Result.success(news) : Result.error("新闻不存在");
    }
    
    @PostMapping("/news/create")
    public Result<News> createNews(@RequestBody News news) {
        try {
            return Result.success(adminService.createNews(news));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/news/update")
    public Result<News> updateNews(@RequestBody News news) {
        try {
            return Result.success(adminService.updateNews(news));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/news/{id}")
    public Result<String> deleteNews(@PathVariable Long id) {
        try {
            adminService.deleteNews(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    //  素材审核
    @GetMapping("/material/pending")
    public Result<List<Material>> getPendingMaterials() {
        return Result.success(adminService.getPendingMaterials());
    }
    
    @GetMapping("/material/all")
    public Result<List<Material>> getAllMaterials() {
        return Result.success(adminService.getAllMaterialsForAdmin());
    }
    
    @GetMapping("/material/detail/{id}")
    public Result<Material> getMaterialDetail(@PathVariable Long id) {
        try {
            Material material = adminService.getMaterialDetail(id);
            return Result.success(material);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/material/approve/{id}")
    public Result<String> approveMaterial(@PathVariable Long id) {
        try {
            adminService.approveMaterial(id);
            return Result.success("审核通过");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/material/reject/{id}")
    public Result<String> rejectMaterial(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            adminService.rejectMaterial(id, reason != null ? reason : "不符合要求");
            return Result.success("已拒绝");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/material/revoke/{id}")
    public Result<String> revokeMaterial(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            adminService.revokeMaterial(id, reason != null ? reason : "需要重新审核");
            return Result.success("已撤回");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/shelf/{id}")
    public Result<String> setMaterialShelfStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String shelfStatus = params.get("shelfStatus");
            adminService.setMaterialShelfStatus(id, shelfStatus);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/category/{id}")
    public Result<String> updateMaterialCategory(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String category = params.get("category");
            adminService.updateMaterialCategory(id, category);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/leader-material/create")
    public Result<Material> createLeaderMaterial(@RequestBody LeaderMaterialRequest request) {
        try {
            return Result.success(adminService.createLeaderMaterial(request));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    //  讨论评论管理
    @PostMapping("/discussion/comment/delete/{id}")
    public Result<String> deleteDiscussionComment(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            adminService.deleteDiscussionComment(id, reason != null ? reason : "内容不当");
            return Result.success("删除成功，已通知发言人");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    //  时事论坛话题管理
    @GetMapping("/topic/list")
    public Result<List<ForumTopic>> getAllTopics() {
        return Result.success(adminService.getAllTopics());
    }
    
    @GetMapping("/topic/active")
    public Result<List<ForumTopic>> getActiveTopics() {
        return Result.success(adminService.getActiveTopics());
    }
    
    @GetMapping("/topic/{id}")
    public Result<ForumTopic> getTopicById(@PathVariable Long id) {
        ForumTopic topic = adminService.getTopicById(id);
        return topic != null ? Result.success(topic) : Result.error("话题不存在");
    }
    
    @GetMapping("/topic/code/{code}")
    public Result<ForumTopic> getTopicByCode(@PathVariable String code) {
        ForumTopic topic = adminService.getTopicByCode(code);
        return topic != null ? Result.success(topic) : Result.error("话题不存在");
    }
    
    @PostMapping("/topic/create")
    public Result<ForumTopic> createTopic(@RequestBody Map<String, Object> params) {
        try {
            ForumTopic topic = new ForumTopic();
            topic.setTitle((String) params.get("title"));
            topic.setDescription((String) params.get("description"));
            topic.setAdminId(Long.valueOf(params.get("adminId").toString()));
            
            List<Long> materialIds = new ArrayList<>();
            Object materialIdsObj = params.get("materialIds");
            if (materialIdsObj != null && materialIdsObj instanceof List) {
                for (Object idObj : (List<?>) materialIdsObj) {
                    if (idObj != null) {
                        materialIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }

            List<Long> newsIds = new ArrayList<>();
            Object newsIdsObj = params.get("newsIds");
            if (newsIdsObj != null && newsIdsObj instanceof List) {
                for (Object idObj : (List<?>) newsIdsObj) {
                    if (idObj != null) {
                        newsIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }
            
            return Result.success(adminService.createTopic(topic, materialIds, newsIds));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/topic/update")
    public Result<ForumTopic> updateTopic(@RequestBody Map<String, Object> params) {
        try {
            ForumTopic topic = new ForumTopic();
            topic.setId(Long.valueOf(params.get("id").toString()));
            topic.setTitle((String) params.get("title"));
            topic.setDescription((String) params.get("description"));
            topic.setStatus((String) params.get("status"));
            
            List<Long> materialIds = new ArrayList<>();
            Object materialIdsObj = params.get("materialIds");
            if (materialIdsObj != null && materialIdsObj instanceof List) {
                for (Object idObj : (List<?>) materialIdsObj) {
                    if (idObj != null) {
                        materialIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }

            List<Long> newsIds = new ArrayList<>();
            Object newsIdsObj = params.get("newsIds");
            if (newsIdsObj != null && newsIdsObj instanceof List) {
                for (Object idObj : (List<?>) newsIdsObj) {
                    if (idObj != null) {
                        newsIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }
            
            return Result.success(adminService.updateTopic(topic, materialIds, newsIds));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/topic/{id}")
    public Result<String> deleteTopic(@PathVariable Long id) {
        try {
            adminService.deleteTopic(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    //  话题评论
    @GetMapping("/topic/{topicId}/comments")
    public Result<List<TopicComment>> getTopicComments(@PathVariable Long topicId) {
        return Result.success(adminService.getTopicComments(topicId));
    }
    
    @PostMapping("/topic/comment")
    public Result<TopicComment> addTopicComment(@RequestBody Map<String, Object> params) {
        try {
            TopicComment comment = new TopicComment();
            comment.setTopicId(Long.valueOf(params.get("topicId").toString()));
            comment.setUserId(Long.valueOf(params.get("userId").toString()));
            comment.setUserType((String) params.get("userType"));
            comment.setContent((String) params.get("content"));
            
            if (params.get("parentId") != null) {
                comment.setParentId(Long.valueOf(params.get("parentId").toString()));
            }
            if (params.get("replyToUserId") != null) {
                comment.setReplyToUserId(Long.valueOf(params.get("replyToUserId").toString()));
            }
            if (params.get("replyToUserType") != null) {
                comment.setReplyToUserType((String) params.get("replyToUserType"));
            }
            
            return Result.success(adminService.addTopicComment(comment));
        } catch (Exception e) {
            return Result.error("评论失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/topic/comment/delete/{id}")
    public Result<String> deleteTopicComment(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            adminService.deleteTopicComment(id, reason != null ? reason : "内容不当");
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/topic/comment/{id}/pin")
    public Result<String> toggleTopicCommentPin(@PathVariable Long id, @RequestParam Long topicId) {
        try {
            String result = adminService.toggleTopicCommentPin(id, topicId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    //  通知相关
    @GetMapping("/notification/{userId}/{userType}")
    public Result<List<UserNotification>> getUserNotifications(@PathVariable Long userId, @PathVariable String userType) {
        return Result.success(adminService.getUserNotifications(userId, userType));
    }
    
    @GetMapping("/notification/unread/{userId}/{userType}")
    public Result<Integer> getUnreadCount(@PathVariable Long userId, @PathVariable String userType) {
        return Result.success(adminService.getUnreadCount(userId, userType));
    }
    
    @PostMapping("/notification/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        adminService.markNotificationAsRead(id);
        return Result.success("已读");
    }
    
    @PostMapping("/notification/readAll/{userId}/{userType}")
    public Result<String> markAllAsRead(@PathVariable Long userId, @PathVariable String userType) {
        adminService.markAllNotificationsAsRead(userId, userType);
        return Result.success("全部已读");
    }
}
