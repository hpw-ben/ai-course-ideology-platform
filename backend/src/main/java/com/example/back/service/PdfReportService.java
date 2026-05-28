package com.example.back.service;

import com.example.back.entity.LearningRecord;
import com.example.back.entity.LearningTask;
import com.example.back.entity.Discussion;
import com.example.back.entity.DiscussionComment;
import com.example.back.entity.User;
import com.example.back.mapper.LearningTaskMapper;
import com.example.back.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PDF 报告生成服务。
 *
 * <p>基于 PDFBox 生成：
 * - 学习任务学习报告 PDF
 * - 讨论报告（教师/学生）PDF
 * - 讨论内容导出/教师发言导出 PDF</p>
 */
@Service
public class PdfReportService {

    private static final Logger log = LoggerFactory.getLogger(PdfReportService.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String PDF_CHINESE_FONT_ENV = "PDF_CHINESE_FONT_PATH";
    private static final String PDF_CHINESE_FONT_PROPERTY = "pdf.chinese.font.path";
    private static final String[] CHINESE_FONT_CANDIDATES = new String[] {
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/msyh.ttc",
            "C:/Windows/Fonts/msyh.ttf",
            "C:/Windows/Fonts/simsun.ttc",
            "C:/Windows/Fonts/simsun.ttf",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJKSC-Regular.otf",
            "/usr/share/fonts/opentype/noto/NotoSerifCJKSC-Regular.otf",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/truetype/arphic/ukai.ttc",
            "/usr/share/fonts/truetype/arphic/uming.ttc",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/noto/NotoSansCJKSC-Regular.otf",
            "/usr/local/share/fonts/NotoSansCJK-Regular.ttc",
            "/usr/local/share/fonts/NotoSansCJKSC-Regular.otf"
    };

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LearningTaskMapper taskMapper;

    /**
     * 生成学习任务学习报告 PDF。
     *
     * <p>报告内容包含：基础信息、学习时长、观点打卡与短笔记、测验得分与（可选）按素材统计。</p>
     */
    public byte[] generateTaskReportPdf(LearningRecord record) {
        if (record == null) throw new IllegalArgumentException("record不能为空");

        LearningTask task = null;
        if (record.getTargetId() != null) {
            task = taskMapper.findById(record.getTargetId());
        }

        User student = null;
        if (record.getStudentId() != null) {
            student = userMapper.findStudentById(record.getStudentId());
        }

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // 尝试加载系统中文字体，避免中文乱码；失败时回退到 Helvetica
            PDFont font = loadChineseFont(doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float y = page.getMediaBox().getHeight() - margin;
                float leading = 18;

                cs.beginText();
                cs.setFont(font, 16);
                cs.newLineAtOffset(margin, y);
                cs.showText("学习报告");
                cs.endText();

                y -= leading * 2;

                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(margin, y);

                writeLine(cs, "【基础信息】");
                writeLine(cs, "任务标题：" + safe(task == null ? record.getTargetTitle() : task.getTitle()));
                writeLine(cs, "任务码：" + safe(record.getTargetCode()));
                writeLine(cs, "学生：" + safe(student == null ? "" : student.getRealName()) + " (" + safe(student == null ? "" : student.getUsername()) + ")");
                writeLine(cs, "专业：" + safe(student == null ? "" : student.getMajor()));
                writeLine(cs, "");

                LocalDateTime enter = record.getEnterTime();
                int durationSec = record.getDuration() == null ? 0 : Math.max(record.getDuration(), 0);
                int materialSec = record.getMaterialDuration() == null ? 0 : Math.max(record.getMaterialDuration(), 0);
                writeLine(cs, "学习日期：" + fmtDate(enter));
                writeLine(cs, "学习时长：" + fmtDuration(durationSec));
                writeLine(cs, "素材学习时长：" + fmtDuration(materialSec));
                writeLine(cs, "");

                String viewpoint = record.getViewpointChoice();
                String shortNote = record.getShortNote();
                if (viewpoint == null) viewpoint = "";
                if (shortNote == null) shortNote = "";
                writeLine(cs, "【互动记录】");
                writeLine(cs, "观点打卡：" + safe(viewpoint));
                writeLine(cs, "短笔记：");
                for (String line : wrap(shortNote, 48)) {
                    writeLine(cs, line);
                }
                writeLine(cs, "");

                int total = record.getQuizTotal() == null ? 0 : record.getQuizTotal();
                int correct = record.getQuizCorrect() == null ? 0 : record.getQuizCorrect();
                String acc = total > 0 ? String.format("%.2f%%", (correct * 100.0 / total)) : "0%";
                writeLine(cs, "【测验结果】");
                writeLine(cs, "得分：" + correct + "/" + total + "    正确率：" + acc);
                writeLine(cs, "提交时间：" + fmtTime(record.getQuizSubmittedAt()));

                try {
                    String quizJson = record.getQuizJson();
                    String ansJson = record.getQuizAnswersJson();
                    if (quizJson != null && !quizJson.trim().isEmpty() && ansJson != null && !ansJson.trim().isEmpty()) {
                        JsonNode quizNode = JSON.readTree(quizJson);
                        JsonNode answersNode = JSON.readTree(ansJson);

                        String mode = quizNode == null || quizNode.get("mode") == null ? null : quizNode.get("mode").asText();
                        JsonNode materials = quizNode == null ? null : quizNode.get("materials");
                        boolean perMaterial = (mode != null && "PER_MATERIAL".equalsIgnoreCase(mode)) || (materials != null && materials.isArray());

                        if (perMaterial && materials != null && materials.isArray()) {
                            // 按素材统计得分：便于教师分析学生对不同素材的掌握情况
                            writeLine(cs, "按素材得分：");
                            for (JsonNode m : materials) {
                                if (m == null) continue;
                                String mid = m.get("materialId") == null ? null : m.get("materialId").asText();
                                String mtitle = m.get("materialTitle") == null ? null : m.get("materialTitle").asText();
                                if (mid == null || mid.trim().isEmpty()) continue;

                                JsonNode qs = m.get("questions");
                                if (qs == null || !qs.isArray() || qs.size() == 0) continue;

                                int mt = qs.size();
                                int mc = 0;

                                JsonNode mAns = answersNode == null ? null : answersNode.get(mid);
                                for (int i = 0; i < mt; i++) {
                                    JsonNode q = qs.get(i);
                                    if (q == null) continue;
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
                                        mc++;
                                    }
                                }

                                String mAcc = mt > 0 ? String.format("%.2f%%", (mc * 100.0 / mt)) : "0%";
                                String line = "- " + safe(mtitle == null || mtitle.trim().isEmpty() ? ("素材" + mid) : mtitle) + "：" + mc + "/" + mt + " (" + mAcc + ")";
                                for (String l : wrap(line, 48)) {
                                    writeLine(cs, l);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }

                writeLine(cs, "");

                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成讨论报告（教师版）PDF。
     */
    public byte[] generateDiscussionTeacherReportPdf(Discussion discussion,
                                                     Map<String, Object> stats,
                                                     List<Map<String, Object>> coreViewpoints,
                                                     List<Map<String, Object>> speechRecords) {
        if (discussion == null) throw new IllegalArgumentException("discussion不能为空");

        List<String> lines = new ArrayList<>();
        lines.add("讨论报告（教师版）");
        lines.add("");
        lines.add("【基础信息】");
        lines.add("讨论标题：" + safe(discussion.getTitle()));
        lines.add("讨论码：" + safe(discussion.getCode()));
        lines.add("发起教师：" + safe(discussion.getTeacherName()));
        lines.add("状态：" + safe(discussion.getStatus()));
        lines.add("开始时间：" + fmtTime(discussion.getStartTime()));
        lines.add("截止时间：" + fmtTime(discussion.getEndTime()));
        lines.add("");

        lines.add("【参与统计】");
        lines.add("参与人数：" + safeNum(stats == null ? null : stats.get("participantCount")));
        lines.add("学生参与人数：" + safeNum(stats == null ? null : stats.get("studentParticipantCount")));
        lines.add("发言总数：" + safeNum(stats == null ? null : stats.get("totalCommentCount")));
        lines.add("主楼数：" + safeNum(stats == null ? null : stats.get("topLevelCommentCount")) + "  回复数：" + safeNum(stats == null ? null : stats.get("replyCommentCount")));
        lines.add("最后发言时间：" + safe(fmtTimeObj(stats == null ? null : stats.get("lastCommentAt"))));
        lines.add("");

        lines.add("【核心观点汇总】");
        if (coreViewpoints == null || coreViewpoints.isEmpty()) {
            lines.add("（无）");
        } else {
            int idx = 1;
            for (Map<String, Object> v : coreViewpoints) {
                String speaker = v == null ? "" : safe(String.valueOf(v.getOrDefault("speaker", "")));
                String content = v == null ? "" : safe(String.valueOf(v.getOrDefault("content", "")));
                String like = v == null ? "" : safeNum(v.get("likeCount"));
                for (String l : wrap(idx + ". " + speaker + "（赞" + like + "）：" + content, 42)) {
                    lines.add(l);
                }
                idx++;
            }
        }
        lines.add("");

        lines.add("【完整发言记录】");
        if (speechRecords == null || speechRecords.isEmpty()) {
            lines.add("（无）");
        } else {
            for (Map<String, Object> r : speechRecords) {
                String user = r == null ? "" : safe(String.valueOf(r.getOrDefault("user", "")));
                String t = r == null ? "" : safe(String.valueOf(r.getOrDefault("userType", "")));
                String time = r == null ? "" : safe(String.valueOf(r.getOrDefault("createdAt", "")));
                String content = r == null ? "" : safe(String.valueOf(r.getOrDefault("content", "")));
                String pinned = (r != null && Boolean.TRUE.equals(r.get("isPinned"))) ? "[置顶]" : "";
                String replyTo = r == null ? "" : safe(String.valueOf(r.getOrDefault("replyTo", "")));
                String prefix = pinned + "[" + t + "]" + user + (replyTo.isEmpty() ? "" : (" 回复 " + replyTo)) + " @" + time + ": ";
                if (r != null && r.get("hasImage") != null && Boolean.TRUE.equals(r.get("hasImage"))) {
                    content = content.isEmpty() ? "[图片]" : (content + " [图片]");
                }
                for (String l : wrap(prefix + content, 42)) {
                    lines.add(l);
                }
                lines.add("");
            }
        }

        return buildPdfFromLines(lines);
    }

    /**
     * 生成讨论内容导出 PDF。
     *
     * <p>用于学生导出自己的讨论学习内容（可包含图片标记）。</p>
     */
    public byte[] generateDiscussionContentExportPdf(Discussion discussion,
                                                     User student,
                                                     List<DiscussionComment> comments) {
        if (discussion == null) throw new IllegalArgumentException("discussion不能为空");

        List<String> lines = new ArrayList<>();
        lines.add("讨论内容导出");
        lines.add("");
        lines.add("【基础信息】");
        lines.add("讨论话题：" + safe(discussion.getTitle()));
        lines.add("讨论码：" + safe(discussion.getCode()));
        if (student != null) {
            lines.add("账号：" + safe(student.getUsername()));
            lines.add("学生姓名：" + safe(student.getRealName()));
            lines.add("学号：" + safe(student.getStudentNo()));
        }
        lines.add("");
        lines.add("【讨论内容】");

        if (comments == null || comments.isEmpty()) {
            lines.add("（无）");
            return buildPdfFromLines(lines);
        }

        for (DiscussionComment c : comments) {
            if (c == null) continue;
            String time = fmtTime(c.getCreatedAt());
            String role = formatRoleLabel(c.getUserType());
            String author = formatAccountWithName(c.getUserName(), c.getUserRealName(), c.getUserId());
            String content = c.getContent() == null ? "" : c.getContent();
            if (c.getImageUrl() != null && !c.getImageUrl().trim().isEmpty()) {
                content = content.isEmpty() ? "[图片]" : (content + " [图片]");
            }

            String prefix = time + " " + role + " " + author + ": ";
            for (String l : wrap(prefix + safe(content), 42)) {
                lines.add(l);
            }
            lines.add("");
        }

        return buildPdfFromLines(lines);
    }

    /**
     * 导出教师发言记录 PDF（仅教师发言）。
     */
    public byte[] generateDiscussionTeacherSpeechOnlyPdf(Discussion discussion,
                                                         LocalDateTime reportDate,
                                                         List<Map<String, Object>> teacherSpeechRecords) {
        if (discussion == null) throw new IllegalArgumentException("discussion不能为空");

        List<String> lines = new ArrayList<>();
        lines.add("讨论教师发言记录");
        lines.add("");
        lines.add("【基础信息】");
        lines.add("讨论题目：" + safe(discussion.getTitle()));
        lines.add("讨论码：" + safe(discussion.getCode()));
        lines.add("教师名称：" + safe(discussion.getTeacherName()));
        lines.add("日期：" + fmtDate(reportDate));
        lines.add("");

        lines.add("【教师发言】");
        if (teacherSpeechRecords == null || teacherSpeechRecords.isEmpty()) {
            lines.add("（无）");
        } else {
            for (Map<String, Object> r : teacherSpeechRecords) {
                String time = r == null ? "" : safe(String.valueOf(r.getOrDefault("createdAt", "")));
                String content = r == null ? "" : safe(String.valueOf(r.getOrDefault("content", "")));
                String replyTo = r == null ? "" : safe(String.valueOf(r.getOrDefault("replyTo", "")));
                String pinned = (r != null && Boolean.TRUE.equals(r.get("isPinned"))) ? "[置顶]" : "";
                String prefix = pinned + "@" + time + (replyTo.isEmpty() ? "" : (" 回复 " + replyTo)) + ": ";
                if (r != null && r.get("hasImage") != null && Boolean.TRUE.equals(r.get("hasImage"))) {
                    content = content.isEmpty() ? "[图片]" : (content + " [图片]");
                }
                for (String l : wrap(prefix + content, 42)) {
                    lines.add(l);
                }
                lines.add("");
            }
        }

        return buildPdfFromLines(lines);
    }

    /**
     * 生成讨论参与报告（学生版）PDF。
     */
    public byte[] generateDiscussionStudentReportPdf(Discussion discussion,
                                                     User student,
                                                     Map<String, Object> stats,
                                                     List<Map<String, Object>> mySpeechRecords) {
        if (discussion == null) throw new IllegalArgumentException("discussion不能为空");

        List<String> lines = new ArrayList<>();
        lines.add("讨论参与报告（学生版）");
        lines.add("");
        lines.add("【基础信息】");
        lines.add("讨论标题：" + safe(discussion.getTitle()));
        lines.add("讨论码：" + safe(discussion.getCode()));
        lines.add("发起教师：" + safe(discussion.getTeacherName()));
        lines.add("开始时间：" + fmtTime(discussion.getStartTime()));
        lines.add("截止时间：" + fmtTime(discussion.getEndTime()));
        lines.add("");

        if (student != null) {
            lines.add("【学生信息】");
            lines.add("学生：" + safe(student.getRealName()) + " (" + safe(student.getUsername()) + ")");
            lines.add("专业：" + safe(student.getMajor()));
            lines.add("");
        }

        lines.add("【我的参与统计】");
        lines.add("发言数：" + safeNum(stats == null ? null : stats.get("myTopLevelCommentCount")) + "  回复数：" + safeNum(stats == null ? null : stats.get("myReplyCommentCount")));
        lines.add("我的发言总数：" + safeNum(stats == null ? null : stats.get("myTotalCommentCount")));
        lines.add("最后发言时间：" + safe(fmtTimeObj(stats == null ? null : stats.get("myLastCommentAt"))));
        lines.add("");

        lines.add("【我的发言记录】");
        if (mySpeechRecords == null || mySpeechRecords.isEmpty()) {
            lines.add("（无）");
        } else {
            for (Map<String, Object> r : mySpeechRecords) {
                String time = r == null ? "" : safe(String.valueOf(r.getOrDefault("createdAt", "")));
                String content = r == null ? "" : safe(String.valueOf(r.getOrDefault("content", "")));
                String replyTo = r == null ? "" : safe(String.valueOf(r.getOrDefault("replyTo", "")));
                String prefix = "@" + time + (replyTo.isEmpty() ? "" : (" 回复 " + replyTo)) + ": ";
                if (r != null && r.get("hasImage") != null && Boolean.TRUE.equals(r.get("hasImage"))) {
                    content = content.isEmpty() ? "[图片]" : (content + " [图片]");
                }
                for (String l : wrap(prefix + content, 42)) {
                    lines.add(l);
                }
                lines.add("");
            }
        }

        return buildPdfFromLines(lines);
    }

    private byte[] buildPdfFromLines(List<String> lines) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 使用中文字体渲染文本，避免乱码
            PDFont font = loadChineseFont(doc);

            float margin = 50;
            float leading = 18;

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float yStart = page.getMediaBox().getHeight() - margin;
            float y = yStart;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            for (String line : (lines == null ? new ArrayList<String>() : lines)) {
                // 分页：到达页底后新建页面继续写
                if (y - leading < margin) {
                    cs.endText();
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    y = yStart;
                    cs = new PDPageContentStream(doc, page);
                    cs.beginText();
                    cs.setFont(font, 12);
                    cs.newLineAtOffset(margin, y);
                }
                cs.showText(line == null ? "" : line);
                cs.newLineAtOffset(0, -leading);
                y -= leading;
            }

            cs.endText();
            cs.close();

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    private void writeLine(PDPageContentStream cs, String text) throws IOException {
        cs.showText(text == null ? "" : text);
        cs.newLineAtOffset(0, -18);
    }

    private PDFont loadChineseFont(PDDocument doc) throws IOException {
        String configuredPath = normalizeFontPath(System.getProperty(PDF_CHINESE_FONT_PROPERTY));
        if (configuredPath == null || configuredPath.trim().isEmpty()) {
            configuredPath = normalizeFontPath(System.getenv(PDF_CHINESE_FONT_ENV));
        }

        List<String> existingCandidates = new ArrayList<>();

        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            log.info("尝试使用配置的 PDF 中文字体: {}", configuredPath);
            PDFont configuredFont = tryLoadFont(doc, configuredPath.trim());
            if (configuredFont != null) {
                log.info("已加载配置的 PDF 中文字体: {}", configuredPath);
                return configuredFont;
            }
            log.warn("配置的 PDF 中文字体加载失败: {}", configuredPath);
        }

        for (String candidate : CHINESE_FONT_CANDIDATES) {
            File candidateFile = new File(candidate);
            if (candidateFile.exists() && candidateFile.isFile()) {
                existingCandidates.add(candidateFile.getAbsolutePath());
            }
            PDFont font = tryLoadFont(doc, candidate);
            if (font != null) {
                log.info("已加载候选 PDF 中文字体: {}", candidate);
                return font;
            }
        }

        log.error("未找到可用的中文字体。配置路径: {}。检测到的候选字体文件: {}",
                configuredPath,
                existingCandidates.isEmpty() ? "无" : String.join(", ", existingCandidates));
        throw new IOException("未找到可用的中文字体，请在服务器安装 Noto Sans CJK 或文泉驿字体，或通过系统属性 pdf.chinese.font.path / 环境变量 PDF_CHINESE_FONT_PATH 指定字体文件路径。当前配置路径："
                + (configuredPath == null ? "空" : configuredPath)
                + "。检测到的候选字体文件："
                + (existingCandidates.isEmpty() ? "无" : String.join(", ", existingCandidates)));
    }

    private PDFont tryLoadFont(PDDocument doc, String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        File fontFile = new File(path);
        if (!fontFile.exists() || !fontFile.isFile()) {
            return null;
        }

        try {
            String lowerCasePath = fontFile.getName().toLowerCase();
            if (lowerCasePath.endsWith(".ttc")) {
                return tryLoadFontFromTtc(doc, fontFile);
            }
            return PDType0Font.load(doc, fontFile);
        } catch (Exception e) {
            log.warn("加载 PDF 中文字体失败: {}", fontFile.getAbsolutePath(), e);
            return null;
        }
    }

    private PDFont tryLoadFontFromTtc(PDDocument doc, File fontFile) {
        try (TrueTypeCollection collection = new TrueTypeCollection(fontFile)) {
            final PDFont[] loadedFontHolder = new PDFont[1];
            collection.processAllFonts(new TrueTypeCollection.TrueTypeFontProcessor() {
                @Override
                public void process(TrueTypeFont trueTypeFont) throws IOException {
                    if (loadedFontHolder[0] == null) {
                        loadedFontHolder[0] = PDType0Font.load(doc, trueTypeFont, true);
                    }
                }
            });
            return loadedFontHolder[0];
        } catch (Exception e) {
            log.warn("加载 TTC 中文字体失败: {}", fontFile.getAbsolutePath(), e);
            return null;
        }
    }

    private String normalizeFontPath(String path) {
        if (path == null) {
            return null;
        }
        String normalized = path.trim();
        if (normalized.length() >= 2) {
            boolean wrappedByDoubleQuotes = normalized.startsWith("\"") && normalized.endsWith("\"");
            boolean wrappedBySingleQuotes = normalized.startsWith("'") && normalized.endsWith("'");
            if (wrappedByDoubleQuotes || wrappedBySingleQuotes) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }
        return normalized;
    }

    private String safe(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private String safeNum(Object n) {
        if (n == null) return "0";
        if (n instanceof Number) return String.valueOf(((Number) n).longValue());
        try {
            return String.valueOf(Long.parseLong(String.valueOf(n)));
        } catch (Exception e) {
            return "0";
        }
    }

    private String fmtTimeObj(Object t) {
        if (t == null) return "";
        if (t instanceof LocalDateTime) return fmtTime((LocalDateTime) t);
        return safe(String.valueOf(t));
    }

    private String fmtTime(LocalDateTime t) {
        if (t == null) return "";
        return t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatAccountWithName(String username, String realName, Long userId) {
        String un = username == null ? "" : username.trim();
        String rn = realName == null ? "" : realName.trim();
        if (!un.isEmpty() && !rn.isEmpty()) return un + "（" + rn + "）";
        if (!un.isEmpty()) return un;
        if (!rn.isEmpty()) return rn;
        return userId == null ? "" : String.valueOf(userId);
    }

    private String formatRoleLabel(String userType) {
        String t = userType == null ? "" : userType.trim();
        if ("STUDENT".equalsIgnoreCase(t)) return "学生";
        if ("TEACHER".equalsIgnoreCase(t)) return "教师";
        if (!t.isEmpty()) return t;
        return "";
    }

    private String fmtDate(LocalDateTime t) {
        if (t == null) return "";
        return t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String fmtDuration(int seconds) {
        int s = Math.max(seconds, 0);
        int h = s / 3600;
        int m = (s % 3600) / 60;
        int sec = s % 60;
        return String.format("%02d:%02d:%02d", h, m, sec);
    }

    private java.util.List<String> wrap(String s, int maxLen) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (s == null || s.isEmpty()) {
            out.add("（无）");
            return out;
        }
        // 简单按字符数换行（不做中英文宽度测量），保证 PDF 不溢出页面宽度
        String t = s.replace("\r", "").trim();
        String[] parts = t.split("\n");
        for (String p : parts) {
            String line = p;
            while (line.length() > maxLen) {
                out.add(line.substring(0, maxLen));
                line = line.substring(maxLen);
            }
            out.add(line);
        }
        return out;
    }
}
