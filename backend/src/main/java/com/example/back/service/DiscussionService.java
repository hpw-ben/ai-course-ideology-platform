package com.example.back.service;

import com.example.back.entity.Discussion;
import com.example.back.entity.DiscussionComment;
import com.example.back.entity.Material;
import com.example.back.entity.News;
import com.example.back.mapper.DiscussionMapper;
import com.example.back.mapper.MaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 讨论模块业务服务。
 *
 * <p>负责讨论创建/查询/删除、加载关联素材与新闻、评论（含回复/点赞/置顶）、以及状态自动更新等逻辑。</p>
 */
@Service
public class DiscussionService {
    
    @Autowired
    private DiscussionMapper discussionMapper;
    
    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private CommentModerationService commentModerationService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final long MAX_COMMENT_IMAGE_BYTES = 2L * 1024 * 1024;
    
    /**
     * 创建讨论（支持多素材）。
     *
     * @param discussion 讨论实体
     * @param materialIds 素材 ID 列表
     * @return 创建后的讨论实体
     */
    @Transactional
    public Discussion create(Discussion discussion, List<Long> materialIds) {
        return create(discussion, materialIds, null);
    }

    /**
     * 填充点赞状态。
     *
     * @param c 当前评论
     * @param currentUserId 当前用户 ID
     * @param currentUserType 当前用户类型
     */
    private void fillLiked(DiscussionComment c, Long currentUserId, String currentUserType) {
        // 根据 likeUsersJson 判断当前用户是否已点赞（用于前端展示）
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

    /**
     * 规范化并保存评论图片。
     *
     * @param imageUrl 图片 URL
     * @return 规范化后的图片 URL
     */
    private String normalizeAndSaveCommentImage(String imageUrl) {
        // 兼容：imageUrl 既可能是已上传的 URL，也可能是 dataURL(base64)
        if (imageUrl == null || imageUrl.trim().isEmpty()) return imageUrl;
        String v = imageUrl.trim();
        if (fileStorageService == null) return v;
        if (!fileStorageService.isDataUrl(v)) return v;

        // data:<mime>;base64,<payload>
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
            if (bytes.length > MAX_COMMENT_IMAGE_BYTES) {
                throw new RuntimeException("图片过大，最大支持2MB");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("图片base64不合法");
        }

        return fileStorageService.saveDataUrl(v, "discussion-comment");
    }

    /**
     * 创建讨论（支持多素材 + 多新闻）。
     *
     * @param discussion 讨论实体
     * @param materialIds 素材 ID 列表
     * @param newsIds 新闻 ID 列表
     * @return 创建后的讨论实体
     */
    @Transactional
    public Discussion create(Discussion discussion, List<Long> materialIds, List<Long> newsIds) {
        // 讨论码用于学生通过 code 进入讨论
        // 生成讨论码：DIS + 6位随机字符
        String code = "DIS" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        discussion.setCode(code);
        discussion.setStatus("IN_PROGRESS"); // 发布即为进行中状态
        discussion.setStartTime(java.time.LocalDateTime.now()); // 设置开始时间为当前时间
        discussionMapper.insert(discussion);

        // 保存素材关联
        if (materialIds != null && !materialIds.isEmpty()) {
            for (Long materialId : materialIds) {
                if (materialId == null) continue;
                discussionMapper.insertDiscussionMaterial(discussion.getId(), materialId);
            }
        }

        // 保存新闻关联
        if (newsIds != null && !newsIds.isEmpty()) {
            for (Long newsId : newsIds) {
                if (newsId == null) continue;
                discussionMapper.insertDiscussionNews(discussion.getId(), newsId);
            }
        }

        return discussion;
    }

    /**
     * 兼容旧的创建方法。
     *
     * @param discussion 讨论实体
     * @return 创建后的讨论实体
     */
    public Discussion create(Discussion discussion) {
        return create(discussion, null, null);
    }

    /**
     * 根据截止时间更新讨论状态。
     *
     * @param d 讨论实体
     */
    private void updateStatusByEndTime(Discussion d) {
        // 若已超过截止时间，则将讨论状态置为 ENDED（并同步入库）
        if (d != null && d.getEndTime() != null) {
            if (java.time.LocalDateTime.now().isAfter(d.getEndTime())) {
                // 已过截止时间，更新为已结束
                if (!"ENDED".equals(d.getStatus())) {
                    d.setStatus("ENDED");
                    discussionMapper.updateStatus(d.getId(), "ENDED");
                }
            }
        }
    }

    /**
     * 加载讨论的素材列表。
     *
     * @param d 讨论实体
     */
    private void loadMaterials(Discussion d) {
        if (d == null) return;

        // 从关联表加载素材（不含file_url）
        List<Material> materials = discussionMapper.findMaterialsByDiscussionId(d.getId());

        // 如果关联表没有数据，尝试从旧的 material_id 字段加载（兼容旧数据）
        if ((materials == null || materials.isEmpty()) && d.getMaterialId() != null) {
            // 使用不返回file_url的查询，避免大数据量传输
            Material m = materialMapper.findByIdWithoutFile(d.getMaterialId());
            if (m != null) {
                materials = new ArrayList<>();
                materials.add(m);
                d.setMaterial(m); // 兼容旧字段
            }
        }

        // 为VIDEO和IMAGE类型的素材单独加载file_url
        if (materials != null && !materials.isEmpty()) {
            // 对于视频/图片类素材，需要 file_url 才能播放/展示；文本类素材不需要加载该字段
            List<Long> needFileUrlIds = new ArrayList<>();
            for (Material m : materials) {
                if ("VIDEO".equals(m.getType()) || "IMAGE".equals(m.getType())) {
                    needFileUrlIds.add(m.getId());
                }
            }

            // 批量加载file_url
            if (!needFileUrlIds.isEmpty()) {
                try {
                    List<Material> fileUrls = materialMapper.findFileUrlsByIdsLimited(needFileUrlIds);
                    // 将file_url填充到对应的素材对象
                    for (Material m : materials) {
                        for (Material fu : fileUrls) {
                            if (m.getId().equals(fu.getId())) {
                                m.setFileUrl(fu.getFileUrl());
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }

        d.setMaterials(materials);

        // 加载关联新闻
        try {
            List<News> newsList = discussionMapper.findNewsByDiscussionId(d.getId());
            d.setNewsList(newsList);
        } catch (Exception e) {
            d.setNewsList(new ArrayList<>());
        }
    }

    /**
     * 获取教师的讨论列表。
     *
     * @param teacherId 教师 ID
     * @return 讨论列表
     */
    public List<Discussion> getByTeacher(Long teacherId) {
        List<Discussion> list = discussionMapper.findByTeacherId(teacherId);
        // 加载关联素材并更新状态
        for (Discussion d : list) {
            updateStatusByEndTime(d); // 检查并更新状态
            loadMaterials(d);
        }
        return list;
    }

    /**
     * 根据 ID 获取讨论。
     *
     * @param id 讨论 ID
     * @return 讨论实体
     */
    public Discussion getById(Long id) {
        Discussion d = discussionMapper.findById(id);
        if (d != null) {
            // 浏览量统计：用于热度/后台统计展示，失败不影响主流程
            try {
                int updated = discussionMapper.incrementViewCount(id);
                if (updated <= 0) {
                    System.err.println("incrementDiscussionViewCount 未更新任何行, id=" + id);
                }
            } catch (Exception e) {
                System.err.println("incrementDiscussionViewCount 失败, id=" + id + ", err=" + e.getMessage());
            }
            updateStatusByEndTime(d); // 检查并更新状态
            loadMaterials(d);
        }
        return d;
    }

    /**
     * 根据讨论码获取讨论。
     *
     * @param code 讨论码
     * @return 讨论实体
     */
    public Discussion getByCode(String code) {
        Discussion d = discussionMapper.findByCode(code);
        if (d != null) {
            // 浏览量统计：用于热度/后台统计展示，失败不影响主流程
            try {
                int updated = discussionMapper.incrementViewCount(d.getId());
                if (updated <= 0) {
                    System.err.println("incrementDiscussionViewCount 未更新任何行, id=" + d.getId() + ", code=" + code);
                }
            } catch (Exception e) {
                System.err.println("incrementDiscussionViewCount 失败, id=" + d.getId() + ", code=" + code + ", err=" + e.getMessage());
            }
            updateStatusByEndTime(d); // 检查并更新状态
            loadMaterials(d);
        }
        return d;
    }

    /**
     * 更新讨论。
     *
     * @param discussion 讨论实体
     */
    public void update(Discussion discussion) {
        discussionMapper.update(discussion);
    }

    /**
     * 删除讨论。
     *
     * @param id 讨论 ID
     */
    @Transactional
    public void delete(Long id) {
        // 删除讨论前先清理关联表，避免残留脏数据
        discussionMapper.deleteDiscussionMaterials(id);
        try {
            discussionMapper.deleteDiscussionNews(id);
        } catch (Exception ignored) {
            // ignore
        }
        discussionMapper.delete(id);
    }

    /**
     * 获取讨论数量。
     *
     * @param teacherId 教师 ID
     * @return 讨论数量
     */
    public int countByTeacher(Long teacherId) {
        return discussionMapper.countByTeacher(teacherId);
    }

    /**
     * 添加评论（支持图片）。
     *
     * @param comment 评论实体
     * @return 添加后的评论实体
     */
    public DiscussionComment addComment(DiscussionComment comment) {
        if (comment == null) {
            throw new RuntimeException("参数错误");
        }

        if (comment.getDiscussionId() == null) {
            throw new RuntimeException("discussionId不能为空");
        }
        Discussion d = discussionMapper.findById(comment.getDiscussionId());
        if (d == null) {
            throw new RuntimeException("讨论不存在");
        }

        updateStatusByEndTime(d);
        if ("ENDED".equalsIgnoreCase(d.getStatus())) {
            throw new RuntimeException("讨论已截止，无法发表评论");
        }

        String content = comment.getContent() == null ? "" : comment.getContent().trim();
        String imageUrl = comment.getImageUrl() == null ? null : comment.getImageUrl().trim();

        if ((content == null || content.isEmpty()) && (imageUrl == null || imageUrl.isEmpty())) {
            throw new RuntimeException("评论内容不能为空");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 若为 dataURL 图片则落盘保存，返回可访问的 URL
            imageUrl = normalizeAndSaveCommentImage(imageUrl);
            comment.setImageUrl(imageUrl);
        }

        if ("STUDENT".equalsIgnoreCase(comment.getUserType()) && content != null && !content.isEmpty()) {
            // 学生评论做内容审核（不通过则直接拒绝）
            CommentModerationService.ModerationResult r = commentModerationService.moderateStudentComment(content);
            if (r == null || !r.isPass()) {
                String reason = (r == null) ? "内容可能包含敏感信息" : r.getReason();
                throw new RuntimeException(reason);
            }
        }

        comment.setStatus("APPROVED");
        discussionMapper.insertComment(comment);
        return comment;
    }

    /**
     * 获取讨论的评论列表（包含回复）。
     *
     * @param discussionId 讨论 ID
     * @return 评论列表
     */
    public List<DiscussionComment> getComments(Long discussionId) {
        return getComments(discussionId, null, null);
    }

    /**
     * 获取讨论的评论列表（包含回复）。
     *
     * @param discussionId 讨论 ID
     * @param currentUserId 当前用户 ID
     * @param currentUserType 当前用户类型
     * @return 评论列表
     */
    public List<DiscussionComment> getComments(Long discussionId, Long currentUserId, String currentUserType) {
        // 查询一级评论，并为每条评论补全用户信息、liked 状态、以及子回复列表
        List<DiscussionComment> comments = discussionMapper.findCommentsByDiscussion(discussionId);
        for (DiscussionComment c : comments) {
            fillUserInfo(c);
            fillLiked(c, currentUserId, currentUserType);

            List<DiscussionComment> replies = discussionMapper.findRepliesByParent(c.getId());
            for (DiscussionComment reply : replies) {
                fillUserInfo(reply);
                fillReplyToUserInfo(reply);
                fillLiked(reply, currentUserId, currentUserType);
            }
            c.setReplies(replies);
        }
        return comments;
    }

    /**
     * 切换评论点赞状态。
     *
     * @param commentId 评论 ID
     * @param userId 用户 ID
     * @param userType 用户类型
     * @return 是否点赞
     */
    @Transactional
    public boolean toggleLike(Long commentId, Long userId, String userType) {
        // 点赞需要加行级锁，避免并发下 like_count / like_users_json 冲突
        if (commentId == null) throw new IllegalArgumentException("commentId不能为空");
        if (userId == null) throw new IllegalArgumentException("userId不能为空");
        if (userType == null || userType.trim().isEmpty()) throw new IllegalArgumentException("userType不能为空");
        String t = userType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t)) throw new IllegalArgumentException("userType仅支持STUDENT/TEACHER");

        DiscussionComment c = discussionMapper.findCommentByIdForUpdate(commentId);
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
        discussionMapper.updateLikeInfo(commentId, set.size(), json);
        return liked;
    }

    /**
     * 解析点赞用户集合。
     *
     * @param json 点赞用户 JSON
     * @return 点赞用户集合
     */
    private Set<String> parseLikeUsers(String json) {
        // 将 likeUsersJson 解析为集合，格式为：STUDENT:1 / TEACHER:2
        Set<String> set = new LinkedHashSet<>();
        if (json == null) return set;
        String t = json.trim();
        if (t.isEmpty()) return set;
        // expected format: ["STUDENT:1","TEACHER:2"]
        if (t.startsWith("[") && t.endsWith("]")) {
            String inner = t.substring(1, t.length() - 1).trim();
            if (inner.isEmpty()) return set;
            // naive parse: split by "," between quoted strings
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
        // fallback: treat as single token
        set.add(t);
        return set;
    }

    /**
     * 将点赞用户集合序列化为 JSON。
     *
     * @param set 点赞用户集合
     * @return 点赞用户 JSON
     */
    private String toLikeUsersJson(Set<String> set) {
        // 将点赞集合序列化为 JSON 数组字符串
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

    /**
     * 填充用户信息。
     *
     * @param c 评论实体
     */
    private void fillUserInfo(DiscussionComment c) {
        // 根据 userType 查询学生/教师信息，填充昵称、真实姓名、头像
        if (c == null) return;
        if ("STUDENT".equals(c.getUserType())) {
            DiscussionComment info = discussionMapper.findStudentInfo(c.getUserId());
            if (info != null) {
                c.setUserName(info.getUserName());
                c.setUserRealName(info.getUserRealName());
                c.setUserAvatar(info.getUserAvatar());
            }
        } else if ("TEACHER".equals(c.getUserType())) {
            DiscussionComment info = discussionMapper.findTeacherInfo(c.getUserId());
            if (info != null) {
                c.setUserName(info.getUserName());
                c.setUserRealName(info.getUserRealName());
                c.setUserAvatar(info.getUserAvatar());
            }
        }
    }

    /**
     * 填充被回复用户信息。
     *
     * @param c 评论实体
     */
    private void fillReplyToUserInfo(DiscussionComment c) {
        // 回复信息用于前端展示 “回复 @xxx”
        if (c.getReplyToUserId() != null && c.getReplyToUserType() != null) {
            if ("STUDENT".equals(c.getReplyToUserType())) {
                DiscussionComment info = discussionMapper.findStudentInfo(c.getReplyToUserId());
                if (info != null) {
                    c.setReplyToUserName(info.getUserName());
                    c.setReplyToUserRealName(info.getUserRealName());
                }
            } else if ("TEACHER".equals(c.getReplyToUserType())) {
                DiscussionComment info = discussionMapper.findTeacherInfo(c.getReplyToUserId());
                if (info != null) {
                    c.setReplyToUserName(info.getUserName());
                    c.setReplyToUserRealName(info.getUserRealName());
                }
            }
        }
    }

    /**
     * 获取待审核评论数。
     *
     * @param teacherId 教师 ID
     * @return 待审核评论数
     */
    public int countPendingComments(Long teacherId) {
        return discussionMapper.countPendingCommentsByTeacher(teacherId);
    }

    /**
     * 审核评论。
     *
     * @param commentId 评论 ID
     * @param status 审核状态
     */
    public void reviewComment(Long commentId, String status) {
        discussionMapper.updateCommentStatus(commentId, status);
    }

    /**
     * 删除评论。
     *
     * @param commentId 评论 ID
     */
    public void deleteComment(Long commentId) {
        // 注意：此处只删除单条评论，是否级联删除回复由 SQL/Mapper 实现决定
        discussionMapper.deleteComment(commentId);
    }

    /**
     * 置顶/取消置顶评论。
     *
     * @param commentId 评论 ID
     * @param discussionId 讨论 ID
     * @return 操作结果
     */
    public String togglePin(Long commentId, Long discussionId) {
        // 置顶用于教师端突出优质发言；当前限制每个讨论最多置顶 3 条
        // 获取当前评论
        List<DiscussionComment> comments = discussionMapper.findCommentsByDiscussion(discussionId);
        DiscussionComment target = null;
        for (DiscussionComment c : comments) {
            if (c.getId().equals(commentId)) {
                target = c;
                break;
            }
        }
        if (target == null) {
            return "评论不存在";
        }
        
        // 如果当前是置顶状态，取消置顶
        if (Boolean.TRUE.equals(target.getIsPinned())) {
            discussionMapper.updatePinStatus(commentId, false);
            return "已取消置顶";
        }
        
        // 检查置顶数量是否已达上限
        int pinnedCount = discussionMapper.countPinnedComments(discussionId);
        if (pinnedCount >= 3) {
            return "置顶数量已达上限(最多3条)";
        }
        
        discussionMapper.updatePinStatus(commentId, true);
        return "置顶成功";
    }
}
