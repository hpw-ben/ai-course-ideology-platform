package com.example.back.service;

import com.example.back.dto.StudentParticipationDetail;
import com.example.back.entity.Discussion;
import com.example.back.entity.LearningRecord;
import com.example.back.entity.LearningTask;
import com.example.back.mapper.DiscussionMapper;
import com.example.back.mapper.LearningRecordMapper;
import com.example.back.mapper.LearningTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LearningRecordService {
    
    @Autowired
    private LearningRecordMapper recordMapper;

    @Autowired
    private LearningTaskMapper taskMapper;

    @Autowired
    private DiscussionMapper discussionMapper;

    @Autowired
    private PdfReportService pdfReportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TYPE_TASK = "TASK";
    private static final String TYPE_DISCUSSION = "DISCUSSION";
    private static final String TYPE_TASK_MATERIAL = "TASK_MATERIAL";

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    public Map<String, Object> getTeacherBoundStudentParticipationDetails(Long teacherId, String code, String type, Integer page, Integer pageSize) {
        if (teacherId == null) throw new IllegalArgumentException("teacherId不能为空");
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("任务码/讨论码不能为空");

        String c = code.trim();
        String t = type == null ? null : type.trim();
        if (t != null && t.isEmpty()) t = null;

        if (t == null) {
            if (c.toUpperCase().startsWith("TASK")) {
                t = TYPE_TASK;
            } else if (c.toUpperCase().startsWith("DIS")) {
                t = TYPE_DISCUSSION;
            }
        }

        String targetTitle = null;
        if (TYPE_TASK.equalsIgnoreCase(t)) {
            LearningTask task = taskMapper.findByCode(c);
            if (task == null) throw new RuntimeException("任务不存在");
            if (task.getTeacherId() != null && !teacherId.equals(task.getTeacherId())) {
                throw new RuntimeException("无权限查看该任务");
            }
            t = TYPE_TASK;
            targetTitle = task.getTitle();
        } else if (TYPE_DISCUSSION.equalsIgnoreCase(t)) {
            Discussion d = discussionMapper.findByCode(c);
            if (d == null) throw new RuntimeException("讨论不存在");
            if (d.getTeacherId() != null && !teacherId.equals(d.getTeacherId())) {
                throw new RuntimeException("无权限查看该讨论");
            }
            t = TYPE_DISCUSSION;
            targetTitle = d.getTitle();
        } else {
            LearningTask task = taskMapper.findByCode(c);
            if (task != null) {
                if (task.getTeacherId() != null && !teacherId.equals(task.getTeacherId())) {
                    throw new RuntimeException("无权限查看该任务");
                }
                t = TYPE_TASK;
                targetTitle = task.getTitle();
            } else {
                Discussion d = discussionMapper.findByCode(c);
                if (d == null) throw new RuntimeException("任务/讨论不存在");
                if (d.getTeacherId() != null && !teacherId.equals(d.getTeacherId())) {
                    throw new RuntimeException("无权限查看该讨论");
                }
                t = TYPE_DISCUSSION;
                targetTitle = d.getTitle();
            }
        }

        int total = recordMapper.countBoundStudentsByTeacher(teacherId);

        int p = page == null ? 1 : Math.max(page, 1);
        int ps = pageSize == null ? 20 : Math.max(pageSize, 1);
        int offset = Math.max((p - 1) * ps, 0);

        List<StudentParticipationDetail> list;
        if (total <= 0 || offset >= total) {
            list = new ArrayList<>();
        } else {
            list = recordMapper.findTeacherBoundStudentParticipationDetailsPaged(teacherId, c, t, offset, ps);
            if (list == null) list = new ArrayList<>();
        }

        Map<String, Object> r = new HashMap<>();
        r.put("code", c);
        r.put("type", t);
        r.put("targetTitle", targetTitle);
        r.put("total", total);
        r.put("list", list);

        if (TYPE_DISCUSSION.equalsIgnoreCase(t)) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("boundStudentCount", total);

            Map<String, Object> agg = recordMapper.getTeacherBoundDiscussionSummary(teacherId, c);
            int boundStudentParticipantCount = numberToInt(agg == null ? null : agg.get("boundStudentParticipantCount"));
            long boundTopLevelCommentCount = numberToLong(agg == null ? null : agg.get("boundTopLevelCommentCount"));
            long boundReplyCommentCount = numberToLong(agg == null ? null : agg.get("boundReplyCommentCount"));
            LocalDateTime boundLastCommentAt = objectToLocalDateTime(agg == null ? null : agg.get("boundLastCommentAt"));

            summary.put("boundStudentParticipantCount", boundStudentParticipantCount);
            summary.put("boundTopLevelCommentCount", boundTopLevelCommentCount);
            summary.put("boundReplyCommentCount", boundReplyCommentCount);
            summary.put("boundTotalCommentCount", boundTopLevelCommentCount + boundReplyCommentCount);
            summary.put("boundLastCommentAt", boundLastCommentAt);

            try {
                Map<String, Object> allStats = discussionMapper.getDiscussionCommentStatsByCode(c);
                summary.put("allStats", allStats == null ? new HashMap<>() : allStats);
            } catch (Exception e) {
                summary.put("allStats", new HashMap<>());
            }

            r.put("summary", summary);
        } else if (TYPE_TASK.equalsIgnoreCase(t)) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("boundStudentCount", total);

            Map<String, Object> agg = recordMapper.getTeacherBoundTaskSummary(teacherId, c);
            int boundStudentParticipantCount = numberToInt(agg == null ? null : agg.get("boundStudentParticipantCount"));
            int boundStudentCompletedCount = numberToInt(agg == null ? null : agg.get("boundStudentCompletedCount"));
            Integer taskTotalMaterials = agg == null ? null : (agg.get("taskTotalMaterials") == null ? null : numberToInt(agg.get("taskTotalMaterials")));

            summary.put("boundStudentParticipantCount", boundStudentParticipantCount);
            summary.put("boundStudentCompletedCount", boundStudentCompletedCount);
            summary.put("taskTotalMaterials", taskTotalMaterials);

            r.put("summary", summary);
        }
        return r;
    }

    private int numberToInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private long numberToLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private LocalDateTime objectToLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime) return (LocalDateTime) v;
        if (v instanceof java.sql.Timestamp) return ((java.sql.Timestamp) v).toLocalDateTime();
        if (v instanceof java.util.Date) return new java.sql.Timestamp(((java.util.Date) v).getTime()).toLocalDateTime();
        return null;
    }

    public List<Map<String, Object>> listStudentDiscussionParticipation(Long studentId) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        List<Map<String, Object>> list = recordMapper.listStudentDiscussionParticipation(studentId);
        return list == null ? new ArrayList<>() : list;
    }

    public List<Map<String, Object>> listStudentMaterialFootprints(Long studentId) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        List<Map<String, Object>> list = recordMapper.listStudentMaterialFootprints(studentId);
        return list == null ? new ArrayList<>() : list;
    }
    
    // 记录进入学习
    public LearningRecord recordEnter(Long studentId, String type, Long targetId, String targetCode, String targetTitle) {
        LearningRecord record = new LearningRecord();
        record.setStudentId(studentId);
        record.setType(type);
        record.setTargetId(targetId);
        record.setTargetCode(targetCode);
        record.setTargetTitle(targetTitle);
        record.setEnterTime(LocalDateTime.now());
        record.setDuration(0);
        recordMapper.insert(record);
        return record;
    }

    public Map<String, Object> markTaskMaterialCompleted(Long studentId, Long taskId, Long materialId) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        if (taskId == null) throw new IllegalArgumentException("taskId不能为空");
        if (materialId == null) throw new IllegalArgumentException("materialId不能为空");

        LearningTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");

        List<com.example.back.entity.Material> materials = getTaskMaterials(task);
        if (materials != null && !materials.isEmpty()) {
            boolean belongs = false;
            for (com.example.back.entity.Material m : materials) {
                if (m != null && m.getId() != null && m.getId().equals(materialId)) {
                    belongs = true;
                    break;
                }
            }
            if (!belongs) throw new RuntimeException("素材不属于该任务");
        } else if (task.getMaterialId() != null && !task.getMaterialId().equals(materialId)) {
            throw new RuntimeException("素材不属于该任务");
        }

        String materialCode = String.valueOf(materialId);
        int exists = recordMapper.existsTaskMaterialCompletion(studentId, taskId, materialCode);
        if (exists <= 0) {
            String title = findMaterialTitle(materials, materialId);
            if (title == null || title.trim().isEmpty()) title = "素材" + materialId;
            recordMapper.insertTaskMaterialCompletion(studentId, taskId, materialCode, title, LocalDateTime.now());
        }
        return getTaskProgress(studentId, taskId);
    }

    public Map<String, Object> getTaskProgress(Long studentId, Long taskId) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        if (taskId == null) throw new IllegalArgumentException("taskId不能为空");

        LearningTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");

        boolean quizRequired = task.getQuizJson() != null && !task.getQuizJson().trim().isEmpty();

        List<com.example.back.entity.Material> materials = getTaskMaterials(task);
        List<Long> taskMaterialIds = getTaskMaterialIds(task, materials);
        List<String> doneCodes = recordMapper.findCompletedMaterialCodesByStudentAndTask(studentId, taskId);
        List<Long> doneMaterialIds = new ArrayList<>();
        if (doneCodes != null) {
            for (String c : doneCodes) {
                if (c == null) continue;
                try {
                    doneMaterialIds.add(Long.valueOf(c));
                } catch (Exception ignored) {
                }
            }
        }
        int total = taskMaterialIds == null ? 0 : taskMaterialIds.size();
        int done = doneMaterialIds.size();
        boolean quizSubmitted = (!quizRequired) || (recordMapper.countQuizSubmittedByStudentAndTask(studentId, taskId) > 0);
        boolean checkedIn = recordMapper.countCheckedInByStudentAndTask(studentId, taskId) > 0;

        boolean completed = quizSubmitted && checkedIn && (total <= 0 || done >= total);
        String status = completed ? STATUS_COMPLETED : STATUS_IN_PROGRESS;

        Map<String, Object> r = new HashMap<>();
        r.put("taskId", taskId);
        r.put("totalMaterials", total);
        r.put("completedMaterials", done);
        r.put("completedMaterialIds", doneMaterialIds);
        r.put("quizRequired", quizRequired);
        r.put("quizSubmitted", quizSubmitted);
        r.put("checkedIn", checkedIn);
        r.put("status", status);
        r.put("completed", completed);
        return r;
    }

    public List<Map<String, Object>> listStudentTaskCompletions(Long studentId, String status) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        String s = status == null ? STATUS_IN_PROGRESS : status.trim();
        if (s.isEmpty()) s = STATUS_IN_PROGRESS;
        if (!STATUS_IN_PROGRESS.equalsIgnoreCase(s) && !STATUS_COMPLETED.equalsIgnoreCase(s)) {
            throw new IllegalArgumentException("status仅支持IN_PROGRESS/COMPLETED");
        }
        List<Map<String, Object>> touched = recordMapper.listStudentTouchedTasks(studentId);
        List<Map<String, Object>> out = new ArrayList<>();
        if (touched != null) {
            for (Map<String, Object> row : touched) {
                if (row == null) continue;
                Long taskId = row.get("taskId") == null ? null : Long.valueOf(row.get("taskId").toString());
                if (taskId == null) continue;

                Map<String, Object> progress;
                try {
                    progress = getTaskProgress(studentId, taskId);
                } catch (Exception e) {
                    continue;
                }

                String st = progress.get("status") == null ? STATUS_IN_PROGRESS : progress.get("status").toString();
                if (!st.equalsIgnoreCase(s)) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("taskId", taskId);
                item.put("taskCode", row.get("taskCode"));
                item.put("taskTitle", row.get("taskTitle"));
                item.put("status", st.toUpperCase());

                Object quizSubmittedAt = row.get("quizSubmittedAt");
                Object checkinTime = row.get("checkinTime");
                Object completedAt = checkinTime != null ? checkinTime : quizSubmittedAt;
                item.put("completedAt", STATUS_COMPLETED.equalsIgnoreCase(st) ? completedAt : null);
                out.add(item);
            }
        }
        return out;
    }

    private List<com.example.back.entity.Material> getTaskMaterials(LearningTask task) {
        if (task == null || task.getId() == null) return null;
        try {
            return taskMapper.findMaterialsByTaskId(task.getId());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String findMaterialTitle(List<com.example.back.entity.Material> list, Long materialId) {
        if (list == null || materialId == null) return null;
        for (com.example.back.entity.Material m : list) {
            if (m != null && materialId.equals(m.getId())) {
                return m.getTitle();
            }
        }
        return null;
    }

    private List<Long> getTaskMaterialIds(LearningTask task, List<com.example.back.entity.Material> materials) {
        List<Long> ids = new ArrayList<>();
        if (materials != null) {
            for (com.example.back.entity.Material m : materials) {
                if (m != null && m.getId() != null) ids.add(m.getId());
            }
        }
        if ((ids.isEmpty()) && task != null && task.getMaterialId() != null) {
            ids.add(task.getMaterialId());
        }
        return ids;
    }

    public void heartbeat(Long recordId, Integer duration, Integer materialDuration, Integer interactionDuration) {
        if (recordId == null) return;
        int d = duration == null ? 0 : Math.max(duration, 0);
        Integer md = materialDuration == null ? null : Math.max(materialDuration, 0);
        Integer id = interactionDuration == null ? null : Math.max(interactionDuration, 0);
        recordMapper.updateHeartbeat(recordId, d, md, id, LocalDateTime.now());
    }

    public void leave(Long recordId, Integer duration, Integer materialDuration, Integer interactionDuration) {
        if (recordId == null) return;
        int d = duration == null ? 0 : Math.max(duration, 0);
        Integer md = materialDuration == null ? null : Math.max(materialDuration, 0);
        Integer id = interactionDuration == null ? null : Math.max(interactionDuration, 0);
        recordMapper.updateLeave(recordId, d, md, id, LocalDateTime.now());
    }

    public Map<String, Object> getTaskCheckinStatus(Long studentId, Long taskId) {
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        if (taskId == null) throw new IllegalArgumentException("taskId不能为空");
        int cnt = recordMapper.countCheckedInByStudentAndTask(studentId, taskId);
        Map<String, Object> r = new HashMap<>();
        r.put("checkedIn", cnt > 0);
        return r;
    }

    public void checkinTask(Long recordId, Long taskId, Integer duration, Integer materialDuration, Integer interactionDuration) {
        if (recordId == null) throw new IllegalArgumentException("recordId不能为空");
        if (taskId == null) throw new IllegalArgumentException("taskId不能为空");

        LearningRecord record = recordMapper.findById(recordId);
        if (record == null) throw new RuntimeException("学习记录不存在");
        if (record.getStudentId() == null) throw new RuntimeException("学习记录缺少studentId");
        if (record.getTargetId() == null || !record.getTargetId().equals(taskId)) {
            throw new RuntimeException("任务不匹配");
        }
        if (!TYPE_TASK.equals(record.getType())) {
            throw new RuntimeException("仅任务支持打卡");
        }

        // 尝试先用前端传来的最新时长刷新一次，避免“已学够但心跳未到”导致后端判断不通过
        if (duration != null) {
            int d = Math.max(duration, 0);
            Integer md = materialDuration == null ? null : Math.max(materialDuration, 0);
            Integer id = interactionDuration == null ? null : Math.max(interactionDuration, 0);
            recordMapper.updateHeartbeat(recordId, d, md, id, LocalDateTime.now());
            record = recordMapper.findById(recordId);
        }

        int already = recordMapper.countCheckedInByStudentAndTask(record.getStudentId(), taskId);
        if (already > 0) {
            throw new RuntimeException("该任务已打卡");
        }

        LearningTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");
        int required = task.getCheckinRequiredSeconds() == null ? 0 : Math.max(task.getCheckinRequiredSeconds(), 0);
        int d = record.getDuration() == null ? 0 : Math.max(record.getDuration(), 0);
        if (d < required) {
            throw new RuntimeException("学习时长未达标，还需" + (required - d) + "秒");
        }

        recordMapper.updateCheckin(recordId, LocalDateTime.now());
    }

    public LearningRecord saveTaskViewpointAndNote(Long recordId, Long studentId, String viewpointChoice, String shortNote) {
        if (recordId == null) throw new IllegalArgumentException("recordId不能为空");
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");

        String choice = viewpointChoice == null ? null : viewpointChoice.trim();
        String note = shortNote == null ? null : shortNote.trim();
        if (note != null && note.length() > 50) {
            throw new IllegalArgumentException("短笔记最多50字");
        }

        LearningRecord record = recordMapper.findById(recordId);
        if (record == null) throw new RuntimeException("学习记录不存在");
        if (record.getStudentId() == null || !studentId.equals(record.getStudentId())) {
            throw new RuntimeException("无权限修改该学习记录");
        }
        if (!TYPE_TASK.equalsIgnoreCase(record.getType())) {
            throw new RuntimeException("仅任务支持观点打卡/短笔记");
        }

        recordMapper.updateViewpointAndNote(recordId, choice, note);
        return recordMapper.findById(recordId);
    }

    public Map<String, Object> submitTaskQuiz(Long recordId, Long taskId, String taskCode, Object answersObj, String reflection) {
        if (recordId == null) throw new IllegalArgumentException("recordId不能为空");
        if (taskId == null) throw new IllegalArgumentException("taskId不能为空");

        LearningTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");
        if (taskCode != null && task.getCode() != null && !task.getCode().equals(taskCode)) {
            throw new RuntimeException("任务码不匹配");
        }
        if (task.getQuizJson() == null || task.getQuizJson().trim().isEmpty()) {
            throw new RuntimeException("该任务尚未配置测验题");
        }

        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answersObj);
        } catch (Exception e) {
            throw new RuntimeException("答案格式不正确");
        }

        int total;
        int correct;
        try {
            JsonNode quizNode = objectMapper.readTree(task.getQuizJson());
            JsonNode answersNode = objectMapper.readTree(answersJson);

            String mode = quizNode == null || quizNode.get("mode") == null ? null : quizNode.get("mode").asText();
            JsonNode materials = quizNode == null ? null : quizNode.get("materials");
            boolean perMaterial = (mode != null && "PER_MATERIAL".equalsIgnoreCase(mode)) || (materials != null && materials.isArray());

            total = 0;
            correct = 0;

            if (perMaterial) {
                if (materials == null || !materials.isArray()) {
                    throw new RuntimeException("题目格式不正确");
                }

                for (JsonNode m : materials) {
                    if (m == null) continue;
                    JsonNode midNode = m.get("materialId");
                    String mid = midNode == null ? null : midNode.asText();
                    if (mid == null || mid.trim().isEmpty()) continue;

                    JsonNode qs = m.get("questions");
                    if (qs == null || !qs.isArray()) continue;

                    JsonNode mAns = answersNode == null ? null : answersNode.get(mid);
                    for (int i = 0; i < qs.size(); i++) {
                        JsonNode q = qs.get(i);
                        if (q == null) continue;
                        total++;

                        String ans = q.get("answer") == null ? null : q.get("answer").asText();
                        String chosen = null;
                        if (mAns != null) {
                            JsonNode byIndex = mAns.get(String.valueOf(i));
                            if (byIndex != null && !byIndex.isNull()) chosen = byIndex.asText();
                            if (chosen == null) {
                                JsonNode byOneBased = mAns.get(String.valueOf(i + 1));
                                if (byOneBased != null && !byOneBased.isNull()) chosen = byOneBased.asText();
                            }
                        }

                        if (ans != null && chosen != null && ans.equalsIgnoreCase(chosen)) {
                            correct++;
                        }
                    }
                }
            } else {
                JsonNode questions = quizNode.get("questions");
                if (questions == null || !questions.isArray()) {
                    throw new RuntimeException("题目格式不正确");
                }
                total = questions.size();

                for (int i = 0; i < total; i++) {
                    JsonNode q = questions.get(i);
                    String ans = q.get("answer") == null ? null : q.get("answer").asText();
                    String chosen = null;
                    if (answersNode != null) {
                        JsonNode byIndex = answersNode.get(String.valueOf(i));
                        if (byIndex != null && !byIndex.isNull()) chosen = byIndex.asText();
                        if (chosen == null) {
                            JsonNode byOneBased = answersNode.get(String.valueOf(i + 1));
                            if (byOneBased != null && !byOneBased.isNull()) chosen = byOneBased.asText();
                        }
                    }
                    if (ans != null && chosen != null && ans.equalsIgnoreCase(chosen)) {
                        correct++;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("判分失败: " + e.getMessage());
        }

        recordMapper.updateQuizResult(
                recordId,
                task.getQuizJson(),
                answersJson,
                total,
                correct,
                LocalDateTime.now(),
                reflection
        );

        Map<String, Object> result = new HashMap<>();
        result.put("quizTotal", total);
        result.put("quizCorrect", correct);
        result.put("accuracy", total > 0 ? (correct * 1.0 / total) : 0.0);
        return result;
    }

    public byte[] generateReportPdf(Long recordId) {
        if (recordId == null) throw new IllegalArgumentException("recordId不能为空");
        LearningRecord record = recordMapper.findById(recordId);
        if (record == null) throw new RuntimeException("学习记录不存在");
        if (record.getCheckedIn() == null || !record.getCheckedIn()) throw new RuntimeException("请先完成打卡再生成报告");

        // 若任务未配置题目，则无需答题也可生成报告
        if (TYPE_TASK.equalsIgnoreCase(record.getType())) {
            LearningTask task = null;
            if (record.getTargetId() != null) {
                task = taskMapper.findById(record.getTargetId());
            }
            boolean quizRequired = task != null && task.getQuizJson() != null && !task.getQuizJson().trim().isEmpty();
            if (quizRequired && record.getQuizSubmittedAt() == null) {
                throw new RuntimeException("请先完成答题再生成报告");
            }
        } else {
            // 兼容：非任务记录仍按原逻辑
            if (record.getQuizSubmittedAt() == null) throw new RuntimeException("请先完成答题再生成报告");
        }

        byte[] pdf = pdfReportService.generateTaskReportPdf(record);
        recordMapper.updateReportInfo(recordId, "/api/learning-record/report/" + recordId + "/pdf", LocalDateTime.now());
        return pdf;
    }

    public LearningRecord getById(Long id) {
        if (id == null) return null;
        return recordMapper.findById(id);
    }
    
    // 获取学生的学习轨迹
    public List<LearningRecord> getByStudent(Long studentId) {
        return recordMapper.findByStudentId(studentId);
    }
    
    // 获取学生统计数据（从评论表统计）
    public Map<String, Object> getStudentStats(Long studentId) {
        // 参与讨论数 = 只统计讨论（不含任务）
        int discussionCount = recordMapper.countStudentDiscussions(studentId);
        // 参与任务数：改为已打卡任务数（每个任务最多算一次）
        int taskCount = recordMapper.countStudentCheckedInTasks(studentId);
        // 总评论数
        int totalComments = recordMapper.countStudentTotalComments(studentId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("discussionCount", discussionCount);
        stats.put("taskCount", taskCount);
        stats.put("recordCount", totalComments);
        
        return stats;
    }
}
