package com.example.back.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommentModerationService {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private BailianClient bailianClient;

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Value("${bailian.moderation.enabled:true}")
    private boolean enabled;

    @Value("${bailian.moderation.fail-open:false}")
    private boolean failOpen;

    @Value("${bailian.moderation.topic.enabled:true}")
    private boolean topicEnabled;

    @Value("${bailian.moderation.topic.use-bailian:true}")
    private boolean topicUseBailian;

    @Value("${bailian.moderation.task.enabled:true}")
    private boolean taskEnabled;

    @Value("${bailian.moderation.task.use-bailian:true}")
    private boolean taskUseBailian;

    public ModerationResult moderateStudentComment(String text) {
        if (!enabled) {
            return ModerationResult.pass();
        }
        String content = (text == null) ? "" : text.trim();
        if (content.isEmpty()) {
            return ModerationResult.fail("评论内容不能为空");
        }

        String systemPrompt = "你是内容安全审核助手。你只输出严格JSON，不要输出任何多余文字。";
        String userPrompt = "请对以下用户评论进行政治敏感内容审核（包括但不限于涉政敏感、极端、暴力、煽动、谣言等）。\n" +
                "仅输出严格JSON，格式为：{\"pass\":true/false,\"reason\":\"...\"}。\n" +
                "- pass=true 表示允许发布；\n" +
                "- pass=false 表示不允许发布；reason给出简短原因（不要包含敏感词原文）。\n\n" +
                "评论内容：\n" + content;

        try {
            String resp = bailianClient.chatCompletions(systemPrompt, userPrompt);
            String json = extractJson(resp);
            JsonNode node = JSON.readTree(json);
            boolean pass = node.has("pass") && node.get("pass").asBoolean(false);
            String reason = node.has("reason") ? node.get("reason").asText("") : "";
            if (pass) {
                return ModerationResult.pass();
            }
            if (reason == null || reason.trim().isEmpty()) {
                reason = "内容可能包含敏感信息";
            }
            return ModerationResult.fail(reason.trim());
        } catch (Exception e) {
            if (failOpen) {
                return ModerationResult.pass();
            }
            return ModerationResult.fail("内容审核服务不可用，请稍后再试");
        }
    }

    public ModerationResult moderateTaskComment(String text) {
        if (!taskEnabled) {
            return ModerationResult.pass();
        }
        String content = (text == null) ? "" : text.trim();
        if (content.isEmpty()) {
            return ModerationResult.fail("评论内容不能为空");
        }

        if (profanityFilterService != null && profanityFilterService.containsProfanity(content)) {
            return ModerationResult.fail("内容包含不文明用语");
        }

        if (!taskUseBailian) {
            return ModerationResult.pass();
        }

        String systemPrompt = "你是内容安全审核助手。你只输出严格JSON，不要输出任何多余文字。";
        String userPrompt = "请对以下用户评论进行内容安全审核：重点识别涉政敏感、煽动、极端、暴力、侮辱谩骂、仇恨等。\n" +
                "仅输出严格JSON，格式为：{\"pass\":true/false,\"reason\":\"...\"}。\n" +
                "- pass=true 表示允许发布；\n" +
                "- pass=false 表示不允许发布；reason给出简短原因（不要包含敏感词原文）。\n\n" +
                "评论内容：\n" + content;

        try {
            String resp = bailianClient.chatCompletions(systemPrompt, userPrompt);
            String json = extractJson(resp);
            JsonNode node = JSON.readTree(json);
            boolean pass = node.has("pass") && node.get("pass").asBoolean(false);
            String reason = node.has("reason") ? node.get("reason").asText("") : "";
            if (pass) {
                return ModerationResult.pass();
            }
            if (reason == null || reason.trim().isEmpty()) {
                reason = "内容可能包含敏感信息";
            }
            return ModerationResult.fail(reason.trim());
        } catch (Exception e) {
            if (failOpen) {
                return ModerationResult.pass();
            }
            return ModerationResult.fail("内容审核服务不可用，请稍后再试");
        }
    }

    public ModerationResult moderateTopicComment(String text) {
        if (!topicEnabled) {
            return ModerationResult.pass();
        }
        String content = (text == null) ? "" : text.trim();
        if (content.isEmpty()) {
            return ModerationResult.fail("评论内容不能为空");
        }

        if (profanityFilterService != null && profanityFilterService.containsProfanity(content)) {
            return ModerationResult.fail("内容包含不文明用语");
        }

        if (!topicUseBailian) {
            return ModerationResult.pass();
        }

        String systemPrompt = "你是内容安全审核助手。你只输出严格JSON，不要输出任何多余文字。";
        String userPrompt = "请对以下用户评论进行内容安全审核：重点识别涉政敏感、煽动、极端、暴力、侮辱谩骂、仇恨等。\n" +
                "仅输出严格JSON，格式为：{\"pass\":true/false,\"reason\":\"...\"}。\n" +
                "- pass=true 表示允许发布；\n" +
                "- pass=false 表示不允许发布；reason给出简短原因（不要包含敏感词原文）。\n\n" +
                "评论内容：\n" + content;

        try {
            String resp = bailianClient.chatCompletions(systemPrompt, userPrompt);
            String json = extractJson(resp);
            JsonNode node = JSON.readTree(json);
            boolean pass = node.has("pass") && node.get("pass").asBoolean(false);
            String reason = node.has("reason") ? node.get("reason").asText("") : "";
            if (pass) {
                return ModerationResult.pass();
            }
            if (reason == null || reason.trim().isEmpty()) {
                reason = "内容可能包含敏感信息";
            }
            return ModerationResult.fail(reason.trim());
        } catch (Exception e) {
            if (failOpen) {
                return ModerationResult.pass();
            }
            return ModerationResult.fail("内容审核服务不可用，请稍后再试");
        }
    }

    private String extractJson(String s) {
        if (s == null) return "{}";
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    public static class ModerationResult {
        private final boolean pass;
        private final String reason;

        private ModerationResult(boolean pass, String reason) {
            this.pass = pass;
            this.reason = reason;
        }

        public static ModerationResult pass() {
            return new ModerationResult(true, "");
        }

        public static ModerationResult fail(String reason) {
            return new ModerationResult(false, reason);
        }

        public boolean isPass() {
            return pass;
        }

        public String getReason() {
            return reason;
        }
    }
}
