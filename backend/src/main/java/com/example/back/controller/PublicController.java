package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.entity.Carousel;
import com.example.back.entity.ForumTopic;
import com.example.back.entity.Material;
import com.example.back.entity.News;
import com.example.back.entity.NewsComment;
import com.example.back.entity.ScienceItem;
import com.example.back.entity.TopicComment;
import com.example.back.entity.User;
import com.example.back.dto.HotTopic;
import com.example.back.entity.Tag;
import com.example.back.mapper.NewsCommentMapper;
import com.example.back.mapper.UserMapper;
import com.example.back.service.AdminService;
import com.example.back.service.FileStorageService;
import com.example.back.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    @Autowired
    private AdminService adminService;

    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private NewsCommentMapper newsCommentMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileStorageService fileStorageService;

    private static final long MAX_NEWS_COMMENT_IMAGE_BYTES = 2L * 1024 * 1024;
    
    // 获取启用的轮播图（公开接口）
    @GetMapping("/carousel/active")
    public Result<List<Carousel>> getActiveCarousels(@RequestParam(required = false) String page) {
        return Result.success(adminService.getActiveCarousels(page));
    }

    @GetMapping("/science/active")
    public Result<List<ScienceItem>> getActiveScienceItems(@RequestParam(required = false) String module) {
        try {
            return Result.success(adminService.getActiveScienceItems(module));
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/science/{id}")
    public Result<ScienceItem> getScienceItemById(@PathVariable Long id) {
        ScienceItem item = adminService.getActiveScienceItemById(id);
        return item != null ? Result.success(item) : Result.error("内容不存在");
    }

    // 获取已发布的新闻（公开接口）
    @GetMapping("/news/published")
    public Result<List<News>> getPublishedNews(@RequestParam(required = false) String category) {
        return Result.success(adminService.getPublishedNews(category));
    }

    // 获取最新的新闻（公开接口）
    @GetMapping("/news/latest")
    public Result<List<News>> getLatestNews(@RequestParam(defaultValue = "5") int limit,
                                           @RequestParam(required = false) String category) {
        return Result.success(adminService.getLatestNews(limit, category));
    }

    // 获取新闻详情（公开接口）
    @GetMapping("/news/{id}")
    public Result<News> getNewsById(@PathVariable Long id) {
        News news = adminService.getNewsById(id);
        return news != null ? Result.success(news) : Result.error("新闻不存在");
    }

    // 获取新闻评论列表
    @GetMapping("/news/{newsId}/comments")
    public Result<List<NewsComment>> getNewsComments(@PathVariable Long newsId,
                                                      @RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) String userType) {
        List<NewsComment> comments = newsCommentMapper.findByNewsId(newsId);
        // 填充用户信息和回复
        for (NewsComment comment : comments) {
            loadCommentUserInfo(comment);
            fillNewsCommentLiked(comment, userId, userType);
            // 加载回复
            List<NewsComment> replies = newsCommentMapper.findRepliesByParentId(comment.getId());
            for (NewsComment reply : replies) {
                loadCommentUserInfo(reply);
                fillNewsCommentLiked(reply, userId, userType);
                // 加载回复目标用户信息
                if (reply.getReplyToUserId() != null) {
                    User replyToUser = null;
                    if ("STUDENT".equals(reply.getReplyToUserType())) {
                        replyToUser = userMapper.findStudentById(reply.getReplyToUserId());
                    } else if ("TEACHER".equals(reply.getReplyToUserType())) {
                        replyToUser = userMapper.findTeacherById(reply.getReplyToUserId());
                    }
                    if (replyToUser != null) {
                        reply.setReplyToUserRealName(replyToUser.getRealName());
                    }
                }
            }
            comment.setReplies(replies);
        }
        return Result.success(comments);
    }
    
    // 添加新闻评论
    @PostMapping("/news/comment")
    public Result<NewsComment> addNewsComment(@RequestBody Map<String, Object> params) {
        try {
            NewsComment comment = new NewsComment();
            comment.setNewsId(Long.valueOf(params.get("newsId").toString()));
            comment.setUserId(Long.valueOf(params.get("userId").toString()));
            comment.setUserType((String) params.get("userType"));
            String content = params.get("content") == null ? "" : String.valueOf(params.get("content"));
            comment.setContent(content);

            String imageUrl = params.get("imageUrl") == null ? null : String.valueOf(params.get("imageUrl"));
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                imageUrl = normalizeAndSaveNewsCommentImage(imageUrl);
                comment.setImageUrl(imageUrl);
            }

            if ((content == null || content.trim().isEmpty()) && (comment.getImageUrl() == null || comment.getImageUrl().trim().isEmpty())) {
                return Result.error("评论内容不能为空");
            }
            
            // 处理回复相关字段
            if (params.get("parentId") != null) {
                comment.setParentId(Long.valueOf(params.get("parentId").toString()));
            }
            if (params.get("replyToUserId") != null) {
                comment.setReplyToUserId(Long.valueOf(params.get("replyToUserId").toString()));
            }
            if (params.get("replyToUserType") != null) {
                comment.setReplyToUserType((String) params.get("replyToUserType"));
            }
            
            try {
                newsCommentMapper.insert(comment);
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("image_url") && (msg.contains("Unknown column") || msg.contains("doesn't exist"))) {
                    if (comment.getImageUrl() != null && !comment.getImageUrl().trim().isEmpty()) {
                        return Result.error("数据库缺少 image_url 字段，请先执行升级SQL");
                    }
                    newsCommentMapper.insertLegacy(comment);
                } else {
                    throw e;
                }
            }
            loadCommentUserInfo(comment);
            return Result.success(comment);
        } catch (Exception e) {
            return Result.error("评论失败: " + e.getMessage());
        }
    }

    // 新闻评论点赞/取消点赞
    @PostMapping("/news/comment/{id}/like")
    public Result<Map<String, Object>> toggleNewsCommentLike(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String userType = body.get("userType") == null ? null : body.get("userType").toString();
            boolean liked = toggleNewsCommentLikeInternal(id, userId, userType);
            Map<String, Object> r = new HashMap<>();
            r.put("liked", liked);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    // 辅助方法：加载评论用户信息
    private void loadCommentUserInfo(NewsComment comment) {
        User user = null;
        if ("STUDENT".equals(comment.getUserType())) {
            user = userMapper.findStudentById(comment.getUserId());
        } else if ("TEACHER".equals(comment.getUserType())) {
            user = userMapper.findTeacherById(comment.getUserId());
        }
        if (user != null) {
            comment.setUserName(user.getUsername());
            comment.setUserRealName(user.getRealName());
            comment.setUserAvatar(user.getAvatar());
        }
    }

    private void fillNewsCommentLiked(NewsComment c, Long currentUserId, String currentUserType) {
        if (c == null) return;
        if (currentUserId == null || currentUserType == null || currentUserType.trim().isEmpty()) {
            c.setLiked(false);
            return;
        }
        String t = currentUserType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t)) {
            c.setLiked(false);
            return;
        }
        String key = t + ":" + currentUserId;
        String json = c.getLikeUsersJson();
        if (json == null || json.trim().isEmpty()) {
            c.setLiked(false);
            return;
        }
        String token = "\"" + key + "\"";
        c.setLiked(json.contains(token));
    }

    private Set<String> parseLikeUsers(String json) {
        Set<String> set = new LinkedHashSet<>();
        if (json == null) return set;
        String t = json.trim();
        if (t.isEmpty()) return set;
        if (t.startsWith("[") && t.endsWith("]")) {
            String inner = t.substring(1, t.length() - 1).trim();
            if (inner.isEmpty()) return set;
            String[] parts = inner.split("\\s*,\\s*");
            for (String p : parts) {
                if (p == null) continue;
                String s = p.trim();
                if (s.startsWith("\"") && s.endsWith("\"")) {
                    s = s.substring(1, s.length() - 1);
                }
                if (!s.isEmpty()) set.add(s);
            }
            return set;
        }
        set.add(t);
        return set;
    }

    private String toLikeUsersJson(Set<String> set) {
        if (set == null || set.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (String s : set) {
            if (s == null) continue;
            String v = s.replace("\\", "\\\\").replace("\"", "\\\"");
            if (!first) sb.append(',');
            sb.append('"').append(v).append('"');
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    private boolean toggleNewsCommentLikeInternal(Long commentId, Long userId, String userType) {
        if (commentId == null) throw new IllegalArgumentException("commentId不能为空");
        if (userId == null) throw new IllegalArgumentException("userId不能为空");
        if (userType == null || userType.trim().isEmpty()) throw new IllegalArgumentException("userType不能为空");
        String t = userType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t)) throw new IllegalArgumentException("userType仅支持STUDENT/TEACHER");

        NewsComment c = newsCommentMapper.findCommentByIdForUpdate(commentId);
        if (c == null) throw new RuntimeException("评论不存在");

        String key = t + ":" + userId;
        Set<String> set = parseLikeUsers(c.getLikeUsersJson());
        boolean liked;
        if (set.contains(key)) {
            set.remove(key);
            liked = false;
        } else {
            set.add(key);
            liked = true;
        }
        String json = toLikeUsersJson(set);
        try {
            newsCommentMapper.updateLikeInfo(commentId, set.size(), json);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("like_count") && (msg.contains("Unknown column") || msg.contains("doesn't exist"))) {
                throw new RuntimeException("数据库缺少 like_count/like_users_json 字段，请先执行升级SQL");
            }
            throw e;
        }
        return liked;
    }

    private String normalizeAndSaveNewsCommentImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return imageUrl;
        String v = imageUrl.trim();
        if (fileStorageService == null) return v;
        if (!fileStorageService.isDataUrl(v)) return v;

        int headerEnd = v.indexOf(";base64,");
        if (headerEnd < 0) throw new RuntimeException("图片格式不正确");
        String mime = v.substring(5, headerEnd).toLowerCase();
        if (!(mime.startsWith("image/"))) {
            throw new RuntimeException("仅支持图片类型上传");
        }
        if (!(mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/png") || mime.equals("image/gif") || mime.equals("image/webp"))) {
            throw new RuntimeException("仅支持jpg/png/gif/webp图片");
        }

        String base64 = v.substring(v.indexOf(',') + 1);
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes.length > MAX_NEWS_COMMENT_IMAGE_BYTES) {
                throw new RuntimeException("图片过大，最大支持2MB");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("图片base64不合法");
        }

        return fileStorageService.saveDataUrl(v, "news-comment");
    }
    
    // 获取活跃的论坛话题（公开接口）
    @GetMapping("/topic/active")
    public Result<List<ForumTopic>> getActiveTopics() {
        return Result.success(adminService.getActiveTopics());
    }

    @GetMapping("/hot-topics")
    public Result<List<HotTopic>> getHotTopics(@RequestParam(defaultValue = "5") int limit) {
        try {
            return Result.success(adminService.getHotTopics(limit));
        } catch (Exception e) {
            return Result.error("获取热门话题失败");
        }
    }

    @GetMapping("/leader/tags")
    public Result<List<Tag>> getLeaderTags() {
        try {
            return Result.success(materialService.getAllTags());
        } catch (Exception e) {
            return Result.error("获取标签失败");
        }
    }

    @GetMapping("/leader/list")
    public Result<List<Material>> getLeaderList(@RequestParam(required = false) String tag) {
        try {
            return Result.success(materialService.getLeaderArticles(tag));
        } catch (Exception e) {
            return Result.error("获取人物事迹失败");
        }
    }

    @GetMapping("/leader/{id}")
    public Result<Material> getLeaderDetail(@PathVariable Long id) {
        try {
            Material m = materialService.getLeaderArticleDetail(id);
            return m != null ? Result.success(m) : Result.error("内容不存在");
        } catch (Exception e) {
            return Result.error("获取详情失败");
        }
    }
    
    // 获取话题详情（公开接口）
    @GetMapping("/topic/code/{code}")
    public Result<ForumTopic> getTopicByCode(@PathVariable String code) {
        ForumTopic topic = adminService.getTopicByCode(code);
        return topic != null ? Result.success(topic) : Result.error("话题不存在");
    }
    
    // 获取话题评论（公开接口）
    @GetMapping("/topic/{topicId}/comments")
    public Result<List<TopicComment>> getTopicComments(@PathVariable Long topicId,
                                                      @RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) String userType) {
        return Result.success(adminService.getTopicComments(topicId, userId, userType));
    }
    
    // 添加话题评论（公开接口）
    @PostMapping("/topic/comment")
    public Result<TopicComment> addTopicComment(@RequestBody Map<String, Object> params) {
        try {
            TopicComment comment = new TopicComment();
            comment.setTopicId(Long.valueOf(params.get("topicId").toString()));
            comment.setUserId(Long.valueOf(params.get("userId").toString()));
            comment.setUserType((String) params.get("userType"));
            comment.setContent((String) params.get("content"));
            if (params.get("imageUrl") != null) {
                comment.setImageUrl((String) params.get("imageUrl"));
            }
            
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

    // 话题评论点赞/取消点赞（公开接口）
    @PostMapping("/topic/comment/{id}/like")
    public Result<Map<String, Object>> toggleTopicCommentLike(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String userType = body.get("userType") == null ? null : body.get("userType").toString();
            boolean liked = adminService.toggleTopicCommentLike(id, userId, userType);
            Map<String, Object> r = new java.util.HashMap<>();
            r.put("liked", liked);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    // 获取单个素材的文件URL
    @GetMapping("/material/{id}/file")
    public Result<Material> getMaterialFile(@PathVariable Long id) {
        try {
            Long len = adminService.getMaterialFileUrlLength(id);
            if (len != null && len > 3L * 1024 * 1024) {
                return Result.error("FILE_URL_TOO_LARGE");
            }
            Material material = adminService.getMaterialFile(id);
            return Result.success(material);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取素材文件失败,请稍后重试");
        }
    }

    @GetMapping("/material/{id}/file/length")
    public Result<Long> getMaterialFileLength(@PathVariable Long id) {
        try {
            Long len = adminService.getMaterialFileUrlLength(id);
            return Result.success(len == null ? 0L : len);
        } catch (Exception e) {
            return Result.error("获取素材大小失败");
        }
    }

    @GetMapping("/material/{id}/file/chunk")
    public Result<String> getMaterialFileChunk(@PathVariable Long id, @RequestParam(defaultValue = "1") long offset) {
        try {
            if (offset < 1) offset = 1;
            String chunk = adminService.getMaterialFileUrlChunk(id, offset);
            return Result.success(chunk == null ? "" : chunk);
        } catch (Exception e) {
            return Result.error("获取素材分片失败");
        }
    }
    
    // 批量获取素材文件URL（最多10个）
    @PostMapping("/material/files")
    public Result<List<Material>> getMaterialFiles(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) params.get("ids");
            if (ids == null) {
                return Result.error("参数错误: ids 不能为空");
            }
            List<Material> materials = adminService.getMaterialFiles(ids);
            return Result.success(materials);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取素材文件失败,请稍后重试");
        }
    }
}
