package com.example.back.service;

import com.example.back.entity.LearningTask;
import com.example.back.entity.Material;
import com.example.back.entity.News;
import com.example.back.entity.TaskComment;
import com.example.back.entity.User;
import com.example.back.mapper.LearningTaskMapper;
import com.example.back.mapper.MaterialMapper;
import com.example.back.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 学习任务业务服务。
 *
 * <p>负责：任务创建/查询/删除、加载关联素材与新闻、评论（含回复/点赞/置顶）业务处理。</p>
 */
@Service
public class LearningTaskService {
    
    @Autowired
    private LearningTaskMapper taskMapper;
    
    @Autowired
    private MaterialMapper materialMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentModerationService commentModerationService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final long MAX_TASK_COMMENT_IMAGE_BYTES = 2L * 1024 * 1024;
     
    /**
     * 创建任务（仅任务基本字段，兼容旧调用）。
     */
    public LearningTask create(LearningTask task) {
        return create(task, null, null);
    }

    /**
     * 创建任务并关联素材（兼容旧调用）。
     */
    @Transactional
    public LearningTask create(LearningTask task, List<Long> materialIds) {
        return create(task, materialIds, null);
    }

    /**
     * 创建任务：
     * - 生成任务码 code
     * - 插入任务主表
     * - 维护任务-素材/任务-新闻的关联表
     */
    @Transactional
    public LearningTask create(LearningTask task, List<Long> materialIds, List<Long> newsIds) {
        // 任务码用于学生通过 code 进入任务
        String code = "TASK" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        task.setCode(code);
        task.setStatus("IN_PROGRESS");
        try {
            taskMapper.insert(task);
        } catch (Exception e) {
            // 兼容：部分数据库尚未执行增量 SQL，缺少字段时给出更明确提示
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("viewpoint_options_json") && (msg.contains("Unknown column") || msg.contains("doesn't exist"))) {
                throw new RuntimeException("数据库缺少 viewpoint_options_json 字段，请先执行升级SQL: back/src/main/resources/sql/learning_tasks_learning_records_add_viewpoint_note.sql");
            }
            throw e;
        }

        if (materialIds != null && !materialIds.isEmpty()) {
            for (Long materialId : materialIds) {
                if (materialId == null) continue;
                // 任务与素材多对多关联
                taskMapper.insertTaskMaterial(task.getId(), materialId);
            }
        }

        if (newsIds != null && !newsIds.isEmpty()) {
            try {
                for (Long newsId : newsIds) {
                    if (newsId == null) continue;
                    // 任务与新闻多对多关联
                    taskMapper.insertTaskNews(task.getId(), newsId);
                }
            } catch (Exception e) {
                // 兼容：部分库没有创建关联表（通过异常提示引导执行升级 SQL）
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("task_news") && msg.contains("doesn't exist")) {
                    throw new RuntimeException("数据库缺少 task_news 表，请先执行升级SQL创建 task_news / discussion_news 表后再试");
                }
                throw e;
            }
        }

        return task;
    }

    /**
     * 查询教师创建的任务列表，并加载每个任务关联的素材/新闻等信息。
     */
    public List<LearningTask> getByTeacher(Long teacherId) {
        List<LearningTask> list;
        try {
            list = taskMapper.findByTeacherId(teacherId);
        } catch (Exception e) {
            // 兼容：带统计字段的 SQL 可能因数据库版本/字段差异失败，回退到简单查询
            System.err.println("findByTeacherId(带统计)失败，回退到简单查询。teacherId=" + teacherId + ", err=" + e.getMessage());
            list = taskMapper.findByTeacherIdSimple(teacherId);
        }
        for (LearningTask task : list) {
            loadMaterial(task);
        }
        return list;
    }

    /**
     * 根据任务ID查询任务详情，并加载关联素材/新闻。
     */
    public LearningTask getById(Long id) {
        LearningTask task = taskMapper.findById(id);
        if (task != null) {
            loadMaterial(task);
        }
        return task;
    }

    /**
     * 根据任务码查询任务详情，并加载关联素材/新闻。
     */
    public LearningTask getByCode(String code) {
        LearningTask task = taskMapper.findByCode(code);
        if (task != null) {
            loadMaterial(task);
        }
        return task;
    }

    /**
     * 删除任务：先清理关联表数据，再删除主表。
     */
    @Transactional
    public void delete(Long id) {
        taskMapper.deleteTaskMaterials(id);
        try {
            taskMapper.deleteTaskNews(id);
        } catch (Exception ignored) {
            // ignore
        }
        taskMapper.delete(id);
    }

    private void fillLiked(TaskComment c, Long currentUserId, String currentUserType) {
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

    private Set<String> parseLikeUsers(String json) {
        // 将 likeUsersJson 解析为集合，格式为：STUDENT:1 / TEACHER:2
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

    private String normalizeAndSaveTaskCommentImage(String imageUrl) {
        // 兼容：imageUrl 既可能是已上传的 URL，也可能是 dataURL(base64)
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
            if (bytes.length > MAX_TASK_COMMENT_IMAGE_BYTES) {
                throw new RuntimeException("图片过大，最大支持2MB");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("图片base64不合法");
        }

        return fileStorageService.saveDataUrl(v, "task-comment");
    }
    
    private void loadMaterial(LearningTask task) {
        // 加载任务关联素材与新闻（兼容旧数据：materialId 单素材）
        if (task == null) return;

        List<Material> materials = taskMapper.findMaterialsByTaskId(task.getId());
        if ((materials == null || materials.isEmpty()) && task.getMaterialId() != null) {
            Material m = materialMapper.findByIdWithoutFile(task.getMaterialId());
            if (m != null) {
                materials = new ArrayList<>();
                materials.add(m);
                task.setMaterial(m);
            }
        }
        task.setMaterials(materials);

        try {
            List<News> newsList = taskMapper.findNewsByTaskId(task.getId());
            task.setNewsList(newsList);
        } catch (Exception e) {
            task.setNewsList(new ArrayList<>());
        }
    }

    /**
     * 统计教师任务数量。
     */
    public int countByTeacher(Long teacherId) {
        return taskMapper.countByTeacher(teacherId);
    }
    
    /**
     * 新增评论/回复：
     * - 内容与图片至少存在一个
     * - 若为 dataURL 图片则落盘保存
     * - 文本内容进行敏感词/合规审核（CommentModerationService）
     */
    public TaskComment addComment(TaskComment comment) {
        if (comment == null) {
            throw new RuntimeException("参数错误");
        }

        String content = comment.getContent() == null ? "" : comment.getContent().trim();
        String imageUrl = comment.getImageUrl() == null ? null : comment.getImageUrl().trim();

        if ((content == null || content.isEmpty()) && (imageUrl == null || imageUrl.isEmpty())) {
            throw new RuntimeException("评论内容不能为空");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageUrl = normalizeAndSaveTaskCommentImage(imageUrl);
            comment.setImageUrl(imageUrl);
        }

        if (content != null && !content.isEmpty()) {
            // 评论内容审核（不通过则直接拒绝）
            CommentModerationService.ModerationResult r = commentModerationService.moderateTaskComment(content);
            if (r == null || !r.isPass()) {
                String reason = (r == null) ? "内容可能包含敏感信息" : r.getReason();
                throw new RuntimeException(reason);
            }
        }
        comment.setIsPinned(false);
        try {
            taskMapper.insertComment(comment);
        } catch (Exception e) {
            // 兼容：旧库没有 image_url 字段时回退到 legacy insert
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("image_url") && (msg.contains("Unknown column") || msg.contains("doesn't exist"))) {
                taskMapper.insertCommentLegacy(comment);
            } else {
                throw e;
            }
        }
        return comment;
    }
     
    public List<TaskComment> getComments(Long taskId) {
        return getComments(taskId, null, null);
    }

    public List<TaskComment> getComments(Long taskId, Long currentUserId, String currentUserType) {
        // 查询一级评论，并为每条评论补全用户信息、liked 状态、以及子回复列表
        List<TaskComment> comments = taskMapper.findCommentsByTaskId(taskId);
        for (TaskComment comment : comments) {
            loadCommentUserInfo(comment);
            fillLiked(comment, currentUserId, currentUserType);
            List<TaskComment> replies = taskMapper.findRepliesByParentId(comment.getId());
            for (TaskComment reply : replies) {
                loadCommentUserInfo(reply);
                loadReplyToUserInfo(reply);
                fillLiked(reply, currentUserId, currentUserType);
            }
            comment.setReplies(replies);
        }
        return comments;
    }

    @Transactional
    public boolean toggleLike(Long commentId, Long userId, String userType) {
        // 点赞需要加行级锁，避免并发下 like_count / like_users_json 冲突
        if (commentId == null) throw new IllegalArgumentException("commentId不能为空");
        if (userId == null) throw new IllegalArgumentException("userId不能为空");
        if (userType == null || userType.trim().isEmpty()) throw new IllegalArgumentException("userType不能为空");
        String t = userType.trim().toUpperCase();
        if (!"STUDENT".equals(t) && !"TEACHER".equals(t)) throw new IllegalArgumentException("userType仅支持STUDENT/TEACHER");

        TaskComment c = taskMapper.findCommentByIdForUpdate(commentId);
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
            taskMapper.updateLikeInfo(commentId, set.size(), json);
        } catch (Exception e) {
            // 兼容：旧库缺少 like_count/like_users_json 字段时给出明确提示
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("like_count") && (msg.contains("Unknown column") || msg.contains("doesn't exist"))) {
                throw new RuntimeException("数据库缺少 like_count/like_users_json 字段，请先执行升级SQL");
            }
            throw e;
        }
        return liked;
    }
    
    private void loadCommentUserInfo(TaskComment comment) {
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
    
    private void loadReplyToUserInfo(TaskComment comment) {
        if (comment.getReplyToUserId() != null) {
            User user = null;
            if ("STUDENT".equals(comment.getReplyToUserType())) {
                user = userMapper.findStudentById(comment.getReplyToUserId());
            } else if ("TEACHER".equals(comment.getReplyToUserType())) {
                user = userMapper.findTeacherById(comment.getReplyToUserId());
            }
            if (user != null) {
                comment.setReplyToUserName(user.getUsername());
                comment.setReplyToUserRealName(user.getRealName());
            }
        }
    }
    
    @Transactional
    public void deleteComment(Long id) {
        // 先删除子回复
        taskMapper.deleteRepliesByParentId(id);
        // 再删除评论本身
        taskMapper.deleteComment(id);
    }
    
    /**
     * 置顶/取消置顶评论。
     *
     * <p>目前限制每个任务最多置顶 3 条评论。</p>
     */
    public String togglePinComment(Long commentId, Long taskId) {
        TaskComment comment = taskMapper.findCommentById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        if (comment.getIsPinned() != null && comment.getIsPinned()) {
            // 取消置顶
            taskMapper.updateCommentPinned(commentId, false);
            return "已取消置顶";
        } else {
            // 检查置顶数量
            int pinnedCount = taskMapper.countPinnedComments(taskId);
            if (pinnedCount >= 3) {
                throw new RuntimeException("最多只能置顶3条评论");
            }
            taskMapper.updateCommentPinned(commentId, true);
            return "已置顶";
        }
    }
    
    /**
     * 统计任务评论数量（包含一级评论与回复，具体口径由 SQL 决定）。
     */
    public int countComments(Long taskId) {
        return taskMapper.countCommentsByTaskId(taskId);
    }
}
