package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.entity.LearningTask;
import com.example.back.entity.TaskComment;
import com.example.back.service.LearningTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习任务模块 API。
 *
 * <p>提供任务创建/查询/删除、以及任务评论（含回复、点赞、置顶）相关接口。</p>
 */
@RestController
@RequestMapping("/api/task")
public class LearningTaskController {
    
    private static final DateTimeFormatter SPACE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private LearningTaskService taskService;

    /**
     * 兼容两种时间格式：
     * - ISO-8601：yyyy-MM-dd'T'HH:mm:ss
     * - 空格格式：yyyy-MM-dd HH:mm:ss
     *
     * <p>前端不同组件/序列化方式可能会产生不同格式，这里统一做容错解析。</p>
     */
    private static LocalDateTime parseLocalDateTimeFlexible(String text) {
        String value = text == null ? null : text.trim();
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        return LocalDateTime.parse(value, SPACE_DATE_TIME_FORMATTER);
    }
    
    /**
     * 创建学习任务。
     *
     * <p>支持：
     * - 任务基础字段（title/description/endTime等）
     * - 关联多素材 materialIds（也兼容旧字段 materialId）
     * - 关联新闻 newsIds（可选）
     * - quizJson/viewpointOptionsJson/checkinRequiredSeconds 等扩展字段</p>
     */
    @PostMapping("/create")
    public Result<LearningTask> create(@RequestBody Map<String, Object> params) {
        try {
            System.out.println("收到创建任务请求: " + params);
            LearningTask task = new LearningTask();
            task.setTitle((String) params.get("title"));
            task.setDescription((String) params.get("description"));
            Object quizObj = params.get("quizJson");
            if (quizObj != null) {
                task.setQuizJson(quizObj.toString());
            }
            Object viewpointOptionsObj = params.get("viewpointOptionsJson");
            if (viewpointOptionsObj != null) {
                task.setViewpointOptionsJson(viewpointOptionsObj.toString());
            }
            Object checkinReqObj = params.get("checkinRequiredSeconds");
            if (checkinReqObj != null && !checkinReqObj.toString().trim().isEmpty()) {
                try {
                    task.setCheckinRequiredSeconds(Integer.valueOf(checkinReqObj.toString()));
                } catch (Exception ignored) {
                }
            }
            Object teacherIdObj = params.get("teacherId");
            if (teacherIdObj == null) {
                return Result.error("教师ID不能为空");
            }
            task.setTeacherId(Long.valueOf(teacherIdObj.toString()));
            // 处理多素材ID
            List<Long> materialIds = new ArrayList<>();
            Object materialIdsObj = params.get("materialIds");
            if (materialIdsObj != null && materialIdsObj instanceof List) {
                List<?> idList = (List<?>) materialIdsObj;
                for (Object idObj : idList) {
                    if (idObj != null) {
                        materialIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }
            // 兼容单素材ID参数
            if (materialIds.isEmpty()) {
                Object materialIdObj = params.get("materialId");
                if (materialIdObj != null && !materialIdObj.toString().isEmpty() && !"null".equals(materialIdObj.toString())) {
                    task.setMaterialId(Long.valueOf(materialIdObj.toString()));
                    materialIds.add(task.getMaterialId());
                }
            }
            // 截止时间（可选）
            Object endTimeObj = params.get("endTime");
            if (endTimeObj != null && !endTimeObj.toString().isEmpty() && !"null".equals(endTimeObj.toString())) {
                try {
                    task.setEndTime(parseLocalDateTimeFlexible(endTimeObj.toString()));
                } catch (DateTimeParseException e) {
                    return Result.error("截止时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss");
                }
            }
            // 处理多新闻ID
            List<Long> newsIds = new ArrayList<>();
            Object newsIdsObj = params.get("newsIds");
            if (newsIdsObj != null && newsIdsObj instanceof List) {
                List<?> idList = (List<?>) newsIdsObj;
                for (Object idObj : idList) {
                    if (idObj != null) {
                        newsIds.add(Long.valueOf(idObj.toString()));
                    }
                }
            }
            System.out.println("准备创建任务: title=" + task.getTitle() + ", teacherId=" + task.getTeacherId() + ", materialIds=" + materialIds + ", newsIds=" + newsIds);
            LearningTask created = taskService.create(task, materialIds, newsIds);
            System.out.println("任务创建成功: code=" + created.getCode());
            return Result.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("创建任务失败: " + e.getMessage());
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    /**
     按教师查询任务列表（教师端用）。
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<LearningTask>> getByTeacher(@PathVariable Long teacherId) {
        try {
            return Result.success(taskService.getByTeacher(teacherId));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据任务ID获取任务详情。
     */
    @GetMapping("/detail/{id}")
    public Result<LearningTask> getById(@PathVariable Long id) {
        try {
            LearningTask task = taskService.getById(id);
            if (task == null) {
                return Result.error("任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据任务码获取任务详情（学生通过任务码进入任务时使用）。
     */
    @GetMapping("/code/{code}")
    public Result<LearningTask> getByCode(@PathVariable String code) {
        try {
            LearningTask task = taskService.getByCode(code);
            if (task == null) {
                return Result.error("任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除任务（会级联删除任务与素材/新闻的关联关系）。
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            taskService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 教师端统计：获取该教师创建的任务数量。
     */
    @GetMapping("/stats/{teacherId}")
    public Result<Map<String, Object>> getStats(@PathVariable Long teacherId) {
        try {
            int count = taskService.countByTeacher(teacherId);
            HashMap<String, Object> result = new HashMap<>();
            result.put("taskCount", count);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 新增评论/回复。
     *
     * <p>当 parentId 存在时表示“回复某条评论”；replyToUserId/replyToUserType 表示回复目标用户。</p>
     */
    @PostMapping("/comment")
    public Result<TaskComment> addComment(@RequestBody Map<String, Object> params) {
        try {
            TaskComment comment = new TaskComment();
            comment.setTaskId(Long.valueOf(params.get("taskId").toString()));
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
            
            TaskComment created = taskService.addComment(comment);
            return Result.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务评论列表（支持传入当前用户，用于计算 liked 字段）。
     */
    @GetMapping("/{taskId}/comments")
    public Result<List<TaskComment>> getComments(@PathVariable Long taskId,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) String userType) {
        try {
            return Result.success(taskService.getComments(taskId, userId, userType));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    /**
     * 评论点赞/取消点赞。
     */
    @PostMapping("/comment/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String userType = body.get("userType") == null ? null : body.get("userType").toString();
            boolean liked = taskService.toggleLike(id, userId, userType);
            Map<String, Object> r = new HashMap<>();
            r.put("liked", liked);
            return Result.success(r);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评论（服务端会先删除该评论的子回复）。
     */
    @DeleteMapping("/comment/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        try {
            taskService.deleteComment(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 评论置顶/取消置顶。
     *
     * <p>置顶数量由服务端限制（当前限制最多 3 条）。</p>
     */
    @PostMapping("/comment/{id}/pin")
    public Result<String> togglePinComment(@PathVariable Long id, @RequestParam Long taskId) {
        try {
            String result = taskService.togglePinComment(id, taskId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
