package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.entity.Discussion;
import com.example.back.entity.DiscussionComment;
import com.example.back.service.DiscussionService;
import com.example.back.service.DiscussionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 讨论模块 API。
 *
 * <p>提供讨论的创建/查询/更新/删除，以及讨论评论（含回复、点赞、置顶）。</p>
 * <p>同时提供讨论报告（教师/学生）JSON 与 PDF 下载接口。</p>
 */
@RestController
@RequestMapping("/api/discussion")
public class DiscussionController {
    
    private static final DateTimeFormatter SPACE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
 
    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private DiscussionReportService discussionReportService;

    /**
     * 兼容两种时间格式：
     * - ISO-8601：yyyy-MM-dd'T'HH:mm:ss
     * - 空格格式：yyyy-MM-dd HH:mm:ss
     *
     * <p>用于兼容前端不同组件/序列化方式导致的时间格式差异。</p>
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
     * 创建讨论。
     *
     * <p>支持 materialIds（多素材）并兼容旧字段 materialId；支持 newsIds（可选）。</p>
     */
    @PostMapping("/create")
    public Result<Discussion> create(@RequestBody Map<String, Object> request) {
        try {
            Discussion discussion = new Discussion();
            discussion.setTitle((String) request.get("title"));
            discussion.setDescription((String) request.get("description"));
            discussion.setTeacherId(Long.valueOf(request.get("teacherId").toString()));
            
            // 处理截止时间
            if (request.get("endTime") != null) {
                String endTimeStr = (String) request.get("endTime");
                try {
                    // 允许 ISO/空格两种格式，提升兼容性
                    discussion.setEndTime(parseLocalDateTimeFlexible(endTimeStr));
                } catch (DateTimeParseException e) {
                    return Result.error("截止时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss");
                }
            }
            
            // 收集素材ID列表
            List<Long> materialIds = new ArrayList<>();
            
            // 兼容旧的单素材字段
            if (request.get("materialId") != null) {
                materialIds.add(Long.valueOf(request.get("materialId").toString()));
            }
            
            // 支持新的多素材字段
            if (request.get("materialIds") != null) {
                @SuppressWarnings("unchecked")
                List<Object> ids = (List<Object>) request.get("materialIds");
                for (Object id : ids) {
                    Long mid = Long.valueOf(id.toString());
                    if (!materialIds.contains(mid)) {
                        materialIds.add(mid);
                    }
                }
            }

            // 收集新闻ID列表
            List<Long> newsIds = new ArrayList<>();
            if (request.get("newsIds") != null) {
                @SuppressWarnings("unchecked")
                List<Object> ids = (List<Object>) request.get("newsIds");
                for (Object id : ids) {
                    Long nid = Long.valueOf(id.toString());
                    if (!newsIds.contains(nid)) {
                        newsIds.add(nid);
                    }
                }
            }

            // 至少选择一个素材或新闻
            if ((materialIds == null || materialIds.isEmpty()) && (newsIds == null || newsIds.isEmpty())) {
                return Result.error("请至少选择一个素材或新闻");
            }
            
            Discussion created = discussionService.create(discussion, materialIds, newsIds);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    // 获取教师的讨论列表
    /**
     * 查询教师发起的讨论列表（教师端常用）。
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Discussion>> getByTeacher(@PathVariable Long teacherId) {
        try {
            List<Discussion> list = discussionService.getByTeacher(teacherId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    // 根据ID获取讨论详情
    /**
     * 根据讨论ID获取讨论详情。
     */
    @GetMapping("/{id}")
    public Result<Discussion> getById(@PathVariable Long id) {
        try {
            Discussion discussion = discussionService.getById(id);
            if (discussion == null) {
                return Result.error("讨论不存在");
            }
            return Result.success(discussion);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    // 根据讨论码获取讨论
    /**
     * 根据讨论码获取讨论详情（学生通过 code 进入讨论时使用）。
     */
    @GetMapping("/code/{code}")
    public Result<Discussion> getByCode(@PathVariable String code) {
        try {
            Discussion discussion = discussionService.getByCode(code);
            if (discussion == null) {
                return Result.error("讨论码无效");
            }
            return Result.success(discussion);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    // 更新讨论
    /**
     * 更新讨论基础信息。
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody Discussion discussion) {
        try {
            discussionService.update(discussion);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    // 删除讨论
    /**
     * 删除讨论（会同时清理讨论与素材/新闻的关联关系）。
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            discussionService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    // 获取统计数据
    /**
     * 教师端统计：讨论数量 + 待审核评论数量。
     */
    @GetMapping("/stats/{teacherId}")
    public Result<Map<String, Integer>> getStats(@PathVariable Long teacherId) {
        try {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("discussionCount", discussionService.countByTeacher(teacherId));
            stats.put("pendingCount", discussionService.countPendingComments(teacherId));
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    // 添加评论
    /**
     * 新增评论/回复（支持图片）。
     */
    @PostMapping("/comment")
    public Result<DiscussionComment> addComment(@RequestBody DiscussionComment comment) {
        try {
            DiscussionComment created = discussionService.addComment(comment);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("评论失败: " + e.getMessage());
        }
    }
    
    // 获取讨论的评论列表
    /**
     * 获取讨论评论列表（包含回复）。
     *
     * <p>支持传入当前用户，用于计算 liked 字段。</p>
     */
    @GetMapping("/{discussionId}/comments")
    public Result<List<DiscussionComment>> getComments(@PathVariable Long discussionId,
                                                      @RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) String userType) {
        try {
            List<DiscussionComment> comments = discussionService.getComments(discussionId, userId, userType);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    // 点赞/取消点赞
    /**
     * 评论点赞/取消点赞。
     */
    @PostMapping("/comment/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
            String userType = body.get("userType") == null ? null : body.get("userType").toString();
            boolean liked = discussionService.toggleLike(id, userId, userType);
            Map<String, Object> r = new HashMap<>();
            r.put("liked", liked);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    // 删除评论
    /**
     * 删除评论。
     */
    @DeleteMapping("/comment/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        try {
            discussionService.deleteComment(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    // 置顶/取消置顶评论
    /**
     * 置顶/取消置顶评论。
     *
     * <p>置顶数量由服务端限制（当前限制最多 3 条）。</p>
     */
    @PostMapping("/comment/{id}/pin")
    public Result<String> togglePin(@PathVariable Long id, @RequestParam Long discussionId) {
        try {
            String result = discussionService.togglePin(id, discussionId);
            if (result.contains("成功") || result.contains("取消")) {
                return Result.success(result);
            } else {
                return Result.error(result);
            }
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    // 讨论报告（教师版）- JSON
    /**
     * 讨论报告（教师版）JSON。
     */
    @GetMapping("/report/teacher")
    public Result<Map<String, Object>> getTeacherReport(@RequestParam Long teacherId, @RequestParam Long discussionId) {
        try {
            return Result.success(discussionReportService.buildTeacherReport(teacherId, discussionId));
        } catch (Exception e) {
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    // 讨论报告（教师版）- PDF
    /**
     * 讨论报告（教师版）PDF 下载。
     */
    @GetMapping("/report/teacher/pdf")
    public ResponseEntity<byte[]> downloadTeacherReportPdf(@RequestParam Long teacherId, @RequestParam Long discussionId) {
        try {
            byte[] pdf = discussionReportService.generateTeacherReportPdf(teacherId, discussionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("discussion-report-teacher-" + discussionId + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            // PDF 下载接口在失败时返回纯文本，便于浏览器直接提示具体错误原因
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            String msg = e.getMessage() == null ? "生成失败" : e.getMessage();
            return new ResponseEntity<>(msg.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }
    }

    // 讨论高频词（教师）
    /**
     * 讨论热词统计（教师端）。
     */
    @GetMapping("/hotwords")
    public Result<List<Map<String, Object>>> getHotWords(@RequestParam Long teacherId,
                                                        @RequestParam Long discussionId,
                                                        @RequestParam(required = false, defaultValue = "10") int limit) {
        try {
            return Result.success(discussionReportService.getHotWords(teacherId, discussionId, limit));
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    // 教师发言PDF（仅教师发言）
    /**
     * 导出教师发言 PDF（仅包含教师发言记录）。
     */
    @GetMapping("/report/teacher-speech/pdf")
    public ResponseEntity<byte[]> downloadTeacherSpeechPdf(@RequestParam Long teacherId, @RequestParam Long discussionId) {
        try {
            byte[] pdf = discussionReportService.generateTeacherSpeechOnlyPdf(teacherId, discussionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("discussion-teacher-speech-" + discussionId + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            String msg = e.getMessage() == null ? "生成失败" : e.getMessage();
            return new ResponseEntity<>(msg.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }
    }

    // 讨论报告（学生版）- JSON
    /**
     * 讨论报告（学生版）JSON。
     */
    @GetMapping("/report/student")
    public Result<Map<String, Object>> getStudentReport(@RequestParam String code, @RequestParam Long studentId) {
        try {
            return Result.success(discussionReportService.buildStudentReport(code, studentId));
        } catch (Exception e) {
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    // 讨论报告（学生版）- PDF
    /**
     * 讨论报告（学生版）PDF 下载。
     */
    @GetMapping("/report/student/pdf")
    public ResponseEntity<byte[]> downloadStudentReportPdf(@RequestParam String code, @RequestParam Long studentId) {
        try {
            byte[] pdf = discussionReportService.generateStudentReportPdf(code, studentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("discussion-report-student-" + studentId + "-" + code + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            String msg = e.getMessage() == null ? "生成失败" : e.getMessage();
            return new ResponseEntity<>(msg.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 导出学生讨论内容 PDF（包含所有已审核讨论内容）。
     */
    @GetMapping("/content/student/pdf")
    public ResponseEntity<byte[]> downloadStudentDiscussionContentPdf(@RequestParam String code, @RequestParam Long studentId) {
        try {
            byte[] pdf = discussionReportService.generateStudentDiscussionContentPdf(code, studentId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("discussion-content-" + studentId + "-" + code + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            String msg = e.getMessage() == null ? "生成失败" : e.getMessage();
            return new ResponseEntity<>(msg.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }
    }
}
