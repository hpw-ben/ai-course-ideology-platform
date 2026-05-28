package com.example.back.service;

import com.example.back.entity.Discussion;
import com.example.back.entity.DiscussionComment;
import com.example.back.entity.User;
import com.example.back.mapper.DiscussionMapper;
import com.example.back.mapper.LearningRecordMapper;
import com.example.back.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 讨论报告服务。
 *
 * <p>负责：
 * - 教师/学生讨论报告数据组装（JSON）
 * - 报告 PDF 生成（委托 {@link PdfReportService}）
 * - 讨论热词统计</p>
 */
@Service
public class DiscussionReportService {

    @Autowired
    private DiscussionMapper discussionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LearningRecordMapper recordMapper;

    @Autowired
    private PdfReportService pdfReportService;

    private static final Pattern EN_WORD = Pattern.compile("[A-Za-z]{2,}");
    private static final Pattern CN_SEQ = Pattern.compile("[\\u4e00-\\u9fff]{2,}");

    /**
     * 构建教师版讨论报告（JSON 数据结构）。
     *
     * <p>包含：
     * - 讨论基础信息
     * - 参与/发言统计
     * - 核心观点（按点赞排序）
     * - 发言记录（含回复关系与置顶标记）</p>
     */
    public Map<String, Object> buildTeacherReport(Long teacherId, Long discussionId) {
        if (teacherId == null) throw new IllegalArgumentException("teacherId不能为空");
        if (discussionId == null) throw new IllegalArgumentException("discussionId不能为空");

        Discussion discussion = discussionMapper.findById(discussionId);
        if (discussion == null) throw new RuntimeException("讨论不存在");
        if (discussion.getTeacherId() == null || !discussion.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权限查看");
        }

        // 仅允许对“已结束”的讨论生成报告，避免讨论中数据不断变化
        ensureEnded(discussion);

        List<DiscussionComment> flat = discussionMapper.findApprovedCommentsFlatByDiscussion(discussionId);
        fillUserInfo(flat);
        fillReplyToInfo(flat);

        Map<String, Object> stats = buildStats(flat);
        List<Map<String, Object>> coreViewpoints = buildCoreViewpoints(flat, 5);
        List<Map<String, Object>> speechRecords = buildSpeechRecords(flat, false, null);

        Map<String, Object> r = new HashMap<>();
        r.put("discussion", discussion);
        r.put("stats", stats);
        r.put("coreViewpoints", coreViewpoints);
        r.put("speechRecords", speechRecords);
        return r;
    }

    /**
     * 构建学生版讨论报告（JSON 数据结构）。
     *
     * <p>仅关注学生自己的参与：统计我的发言/回复数量与时间，并输出我的发言记录。</p>
     */
    public Map<String, Object> buildStudentReport(String code, Long studentId) {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("code不能为空");
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");

        Discussion discussion = discussionMapper.findByCode(code.trim());
        if (discussion == null) throw new RuntimeException("讨论不存在");

        // 仅允许对“已结束”的讨论生成报告
        ensureEnded(discussion);

        List<DiscussionComment> flat = discussionMapper.findApprovedCommentsFlatByDiscussion(discussion.getId());
        fillUserInfo(flat);
        fillReplyToInfo(flat);

        List<DiscussionComment> mine = new ArrayList<>();
        for (DiscussionComment c : flat) {
            if (c == null) continue;
            if ("STUDENT".equalsIgnoreCase(c.getUserType()) && studentId.equals(c.getUserId())) {
                mine.add(c);
            }
        }

        Map<String, Object> stats = buildMyStats(mine);
        List<Map<String, Object>> mySpeechRecords = buildSpeechRecords(mine, true, studentId);

        User student = userMapper.findStudentById(studentId);

        Map<String, Object> r = new HashMap<>();
        r.put("discussion", discussion);
        r.put("student", student);
        r.put("stats", stats);
        r.put("mySpeechRecords", mySpeechRecords);
        return r;
    }

    /**
     * 生成教师版讨论报告 PDF。
     */
    public byte[] generateTeacherReportPdf(Long teacherId, Long discussionId) {
        Map<String, Object> d = buildTeacherReport(teacherId, discussionId);
        Discussion discussion = (Discussion) d.get("discussion");
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) d.get("stats");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> coreViewpoints = (List<Map<String, Object>>) d.get("coreViewpoints");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> speechRecords = (List<Map<String, Object>>) d.get("speechRecords");
        return pdfReportService.generateDiscussionTeacherReportPdf(discussion, stats, coreViewpoints, speechRecords);
    }

    /**
     * 生成学生版讨论报告 PDF。
     */
    public byte[] generateStudentReportPdf(String code, Long studentId) {
        Map<String, Object> d = buildStudentReport(code, studentId);
        Discussion discussion = (Discussion) d.get("discussion");
        User student = (User) d.get("student");
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) d.get("stats");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mySpeechRecords = (List<Map<String, Object>>) d.get("mySpeechRecords");
        return pdfReportService.generateDiscussionStudentReportPdf(discussion, student, stats, mySpeechRecords);
    }

    /**
     * 导出学生可见的讨论内容 PDF。
     *
     * <p>会校验该学生是否拥有对应的学习记录，防止越权导出。</p>
     */
    public byte[] generateStudentDiscussionContentPdf(String code, Long studentId) {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("code不能为空");
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");

        String c = code.trim();
        int ok = recordMapper == null ? 0 : recordMapper.countStudentDiscussionRecord(studentId, c);
        if (ok <= 0) throw new RuntimeException("无权限导出：该讨论不在学习记录中");

        Discussion discussion = discussionMapper.findByCode(c);
        if (discussion == null) throw new RuntimeException("讨论不存在");

        List<DiscussionComment> flat = discussionMapper.findApprovedCommentsFlatByDiscussion(discussion.getId());
        fillUserInfo(flat);

        User student = userMapper.findStudentById(studentId);
        return pdfReportService.generateDiscussionContentExportPdf(discussion, student, flat);
    }

    /**
     * 讨论热词统计（教师端）。
     */
    public List<Map<String, Object>> getHotWords(Long teacherId, Long discussionId, int limit) {
        if (teacherId == null) throw new IllegalArgumentException("teacherId不能为空");
        if (discussionId == null) throw new IllegalArgumentException("discussionId不能为空");

        Discussion discussion = discussionMapper.findById(discussionId);
        if (discussion == null) throw new RuntimeException("讨论不存在");
        if (discussion.getTeacherId() == null || !discussion.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权限查看");
        }

        ensureEnded(discussion);

        List<DiscussionComment> flat = discussionMapper.findApprovedCommentsFlatByDiscussion(discussionId);
        // 热词从“已审核评论”中抽取，避免未审核内容进入统计
        return buildHotWords(flat, limit <= 0 ? 10 : limit);
    }

    /**
     * 导出教师发言记录 PDF（仅教师发言）。
     */
    public byte[] generateTeacherSpeechOnlyPdf(Long teacherId, Long discussionId) {
        if (teacherId == null) throw new IllegalArgumentException("teacherId不能为空");
        if (discussionId == null) throw new IllegalArgumentException("discussionId不能为空");

        Discussion discussion = discussionMapper.findById(discussionId);
        if (discussion == null) throw new RuntimeException("讨论不存在");
        if (discussion.getTeacherId() == null || !discussion.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权限查看");
        }

        ensureEnded(discussion);

        List<DiscussionComment> flat = discussionMapper.findApprovedCommentsFlatByDiscussion(discussionId);
        fillUserInfo(flat);
        fillReplyToInfo(flat);

        List<Map<String, Object>> speechRecords = buildTeacherSpeechRecords(flat, teacherId);
        return pdfReportService.generateDiscussionTeacherSpeechOnlyPdf(discussion, LocalDateTime.now(), speechRecords);
    }

    private void ensureEnded(Discussion discussion) {
        if (discussion == null) return;
        LocalDateTime end = discussion.getEndTime();
        if (end != null && LocalDateTime.now().isAfter(end)) {
            // 若超时未更新状态，这里做一次纠正（并同步数据库）
            if (!"ENDED".equalsIgnoreCase(discussion.getStatus())) {
                discussion.setStatus("ENDED");
                discussionMapper.updateStatus(discussion.getId(), "ENDED");
            }
        }
        if (!"ENDED".equalsIgnoreCase(discussion.getStatus())) {
            throw new RuntimeException("讨论未结束，暂不可生成报告");
        }
    }

    private List<Map<String, Object>> buildTeacherSpeechRecords(List<DiscussionComment> flat, Long teacherId) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (flat == null || teacherId == null) return out;
        for (DiscussionComment c : flat) {
            if (c == null) continue;
            if (!"TEACHER".equalsIgnoreCase(c.getUserType())) continue;
            if (!teacherId.equals(c.getUserId())) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("user", formatUser(c));
            m.put("userType", c.getUserType());
            m.put("createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
            m.put("content", c.getContent() == null ? "" : c.getContent());
            m.put("isPinned", Boolean.TRUE.equals(c.getIsPinned()));
            m.put("replyTo", formatReplyToUser(c));
            m.put("hasImage", c.getImageUrl() != null && !c.getImageUrl().trim().isEmpty());
            out.add(m);
        }
        return out;
    }

    private List<Map<String, Object>> buildHotWords(List<DiscussionComment> flat, int limit) {
        // 统计词频后按次数倒序输出 topN
        LinkedHashMap<String, Integer> freq = new LinkedHashMap<>();
        if (flat != null) {
            for (DiscussionComment c : flat) {
                if (c == null) continue;
                String text = c.getContent();
                if (text == null || text.trim().isEmpty()) continue;
                addTokensFromText(freq, text);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(freq.entrySet());
        entries.sort((a, b) -> {
            int ca = a == null || a.getValue() == null ? 0 : a.getValue();
            int cb = b == null || b.getValue() == null ? 0 : b.getValue();
            if (cb != ca) return Integer.compare(cb, ca);
            String ka = a == null ? "" : a.getKey();
            String kb = b == null ? "" : b.getKey();
            return ka.compareTo(kb);
        });

        int n = Math.max(0, limit);
        if (entries.size() > n) entries = entries.subList(0, n);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : entries) {
            Map<String, Object> m = new HashMap<>();
            m.put("word", e.getKey());
            m.put("count", e.getValue());
            out.add(m);
        }
        return out;
    }

    private void addTokensFromText(Map<String, Integer> freq, String text) {
        if (freq == null || text == null) return;
        String t = text.replaceAll("\\s+", " ");

        // 英文：按连续字母序列统计（统一转小写）
        Matcher em = EN_WORD.matcher(t);
        while (em.find()) {
            String w = em.group();
            if (w == null) continue;
            String token = w.toLowerCase();
            if (token.length() < 2) continue;
            if (isStopWord(token)) continue;
            freq.put(token, (freq.getOrDefault(token, 0) + 1));
        }

        // 中文：按连续中文序列，再用 2 字片段做近似分词
        Matcher cm = CN_SEQ.matcher(t);
        while (cm.find()) {
            String seq = cm.group();
            if (seq == null) continue;
            String s = seq.trim();
            if (s.length() < 2) continue;

            // 用2字词片段做近似分词
            for (int i = 0; i + 2 <= s.length(); i++) {
                String token = s.substring(i, i + 2);
                if (token.trim().isEmpty()) continue;
                if (isStopWord(token)) continue;
                freq.put(token, (freq.getOrDefault(token, 0) + 1));
            }
        }
    }

    private boolean isStopWord(String token) {
        if (token == null) return true;
        String t = token.trim();
        if (t.isEmpty()) return true;
        // 英文停用词
        if ("the".equals(t) || "and".equals(t) || "for".equals(t) || "with".equals(t) || "that".equals(t) || "this".equals(t) || "from".equals(t)) return true;
        // 中文常见停用词（2字为主；与2字切分策略匹配）
        if ("我们".equals(t) || "你们".equals(t) || "他们".equals(t) || "老师".equals(t) || "同学".equals(t) || "这个".equals(t) || "那个".equals(t) || "可以".equals(t) || "就是".equals(t) || "如果".equals(t) || "因为".equals(t) || "所以".equals(t) || "然后".equals(t) || "但是".equals(t) || "一个".equals(t) || "一样".equals(t) || "什么".equals(t) || "怎么".equals(t) || "觉得".equals(t) || "问题".equals(t)) return true;
        return false;
    }

    private void fillUserInfo(List<DiscussionComment> list) {
        if (list == null) return;
        for (DiscussionComment c : list) {
            if (c == null) continue;
            if (c.getUserId() == null || c.getUserType() == null) continue;
            if ("STUDENT".equalsIgnoreCase(c.getUserType())) {
                DiscussionComment info = discussionMapper.findStudentInfo(c.getUserId());
                if (info != null) {
                    c.setUserName(info.getUserName());
                    c.setUserRealName(info.getUserRealName());
                    c.setUserAvatar(info.getUserAvatar());
                }
            } else if ("TEACHER".equalsIgnoreCase(c.getUserType())) {
                DiscussionComment info = discussionMapper.findTeacherInfo(c.getUserId());
                if (info != null) {
                    c.setUserName(info.getUserName());
                    c.setUserRealName(info.getUserRealName());
                    c.setUserAvatar(info.getUserAvatar());
                }
            }
        }
    }

    private void fillReplyToInfo(List<DiscussionComment> list) {
        if (list == null) return;
        for (DiscussionComment c : list) {
            if (c == null) continue;
            if (c.getReplyToUserId() == null || c.getReplyToUserType() == null) continue;
            if ("STUDENT".equalsIgnoreCase(c.getReplyToUserType())) {
                DiscussionComment info = discussionMapper.findStudentInfo(c.getReplyToUserId());
                if (info != null) {
                    c.setReplyToUserName(info.getUserName());
                    c.setReplyToUserRealName(info.getUserRealName());
                }
            } else if ("TEACHER".equalsIgnoreCase(c.getReplyToUserType())) {
                DiscussionComment info = discussionMapper.findTeacherInfo(c.getReplyToUserId());
                if (info != null) {
                    c.setReplyToUserName(info.getUserName());
                    c.setReplyToUserRealName(info.getUserRealName());
                }
            }
        }
    }

    private Map<String, Object> buildStats(List<DiscussionComment> flat) {
        Map<String, Object> stats = new HashMap<>();
        int total = flat == null ? 0 : flat.size();
        int topLevel = 0;
        int reply = 0;
        LocalDateTime last = null;
        Set<String> participants = new HashSet<>();
        Set<Long> studentParticipants = new HashSet<>();

        if (flat != null) {
            for (DiscussionComment c : flat) {
                if (c == null) continue;
                if (c.getParentId() == null) topLevel++; else reply++;
                if (c.getCreatedAt() != null && (last == null || c.getCreatedAt().isAfter(last))) {
                    last = c.getCreatedAt();
                }
                if (c.getUserId() != null && c.getUserType() != null) {
                    String key = c.getUserType().toUpperCase() + ":" + c.getUserId();
                    participants.add(key);
                    if ("STUDENT".equalsIgnoreCase(c.getUserType())) {
                        studentParticipants.add(c.getUserId());
                    }
                }
            }
        }

        stats.put("participantCount", participants.size());
        stats.put("studentParticipantCount", studentParticipants.size());
        stats.put("totalCommentCount", total);
        stats.put("topLevelCommentCount", topLevel);
        stats.put("replyCommentCount", reply);
        stats.put("lastCommentAt", last);
        return stats;
    }

    private Map<String, Object> buildMyStats(List<DiscussionComment> mine) {
        Map<String, Object> stats = new HashMap<>();
        int topLevel = 0;
        int reply = 0;
        LocalDateTime last = null;
        if (mine != null) {
            for (DiscussionComment c : mine) {
                if (c == null) continue;
                if (c.getParentId() == null) topLevel++; else reply++;
                if (c.getCreatedAt() != null && (last == null || c.getCreatedAt().isAfter(last))) {
                    last = c.getCreatedAt();
                }
            }
        }
        stats.put("myTopLevelCommentCount", topLevel);
        stats.put("myReplyCommentCount", reply);
        stats.put("myTotalCommentCount", topLevel + reply);
        stats.put("myLastCommentAt", last);
        return stats;
    }

    private List<Map<String, Object>> buildCoreViewpoints(List<DiscussionComment> flat, int limit) {
        List<DiscussionComment> top = new ArrayList<>();
        if (flat != null) {
            for (DiscussionComment c : flat) {
                if (c == null) continue;
                if (c.getParentId() == null) top.add(c);
            }
        }

        top.sort((a, b) -> {
            int la = a == null || a.getLikeCount() == null ? 0 : a.getLikeCount();
            int lb = b == null || b.getLikeCount() == null ? 0 : b.getLikeCount();
            if (lb != la) return Integer.compare(lb, la);
            LocalDateTime ta = a == null ? null : a.getCreatedAt();
            LocalDateTime tb = b == null ? null : b.getCreatedAt();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return ta.compareTo(tb);
        });

        int n = Math.max(0, limit);
        if (top.size() > n) top = top.subList(0, n);

        List<Map<String, Object>> out = new ArrayList<>();
        for (DiscussionComment c : top) {
            Map<String, Object> m = new HashMap<>();
            m.put("speaker", formatUser(c));
            m.put("content", c == null ? "" : (c.getContent() == null ? "" : c.getContent()));
            m.put("likeCount", c == null ? 0 : (c.getLikeCount() == null ? 0 : c.getLikeCount()));
            out.add(m);
        }
        return out;
    }

    private List<Map<String, Object>> buildSpeechRecords(List<DiscussionComment> flat, boolean onlyMe, Long studentId) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (flat == null) return out;
        for (DiscussionComment c : flat) {
            if (c == null) continue;
            if (onlyMe) {
                if (studentId == null) continue;
                if (!"STUDENT".equalsIgnoreCase(c.getUserType()) || !studentId.equals(c.getUserId())) continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("user", formatUser(c));
            m.put("userType", c.getUserType());
            m.put("createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
            m.put("content", c.getContent() == null ? "" : c.getContent());
            m.put("isPinned", Boolean.TRUE.equals(c.getIsPinned()));
            m.put("replyTo", formatReplyToUser(c));
            m.put("hasImage", c.getImageUrl() != null && !c.getImageUrl().trim().isEmpty());
            out.add(m);
        }
        return out;
    }

    private String formatReplyToUser(DiscussionComment c) {
        if (c == null) return "";
        String rn = c.getReplyToUserRealName() == null ? "" : c.getReplyToUserRealName();
        String un = c.getReplyToUserName() == null ? "" : c.getReplyToUserName();
        if (!un.isEmpty() && !rn.isEmpty()) return un + "（" + rn + "）";
        if (!un.isEmpty()) return un;
        if (!rn.isEmpty()) return rn;
        return "";
    }

    private String formatUser(DiscussionComment c) {
        if (c == null) return "";
        String rn = c.getUserRealName() == null ? "" : c.getUserRealName();
        String un = c.getUserName() == null ? "" : c.getUserName();
        if (!un.isEmpty() && !rn.isEmpty()) return un + "（" + rn + "）";
        if (!un.isEmpty()) return un;
        if (!rn.isEmpty()) return rn;
        return c.getUserId() == null ? "" : String.valueOf(c.getUserId());
    }
}
