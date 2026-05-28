package com.example.back.service;

import com.example.back.entity.Material;
import com.example.back.entity.News;
import com.example.back.mapper.MaterialMapper;
import com.example.back.mapper.NewsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class AiQuizService {

    @Autowired
    private BailianClient bailianClient;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private NewsMapper newsMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateQuiz(String title, String description, List<Long> materialIds, List<Long> newsIds, Integer count) {
        int qCount = (count == null) ? 4 : count;
        if (qCount < 3) qCount = 3;
        if (qCount > 5) qCount = 5;

        StringBuilder context = new StringBuilder();
        context.append("任务标题：").append(safe(title)).append("\n");
        if (description != null && !description.trim().isEmpty()) {
            context.append("任务说明：").append(safe(description)).append("\n");
        }

        if (materialIds != null && !materialIds.isEmpty()) {
            context.append("\n学习素材：\n");
            int idx = 1;
            for (Long id : materialIds) {
                if (id == null) continue;
                Material m = materialMapper.findByIdWithoutFile(id);
                if (m == null) continue;
                context.append(idx++).append(". ")
                        .append("[素材]")
                        .append(" 标题=").append(safe(m.getTitle()))
                        .append(" 类型=").append(safe(m.getType()))
                        .append("\n");
                if (m.getDescription() != null && !m.getDescription().isEmpty()) {
                    context.append("简介：").append(truncate(m.getDescription(), 400)).append("\n");
                }
                if (m.getContent() != null && !m.getContent().isEmpty()) {
                    context.append("内容：").append(truncate(m.getContent(), 600)).append("\n");
                }
                context.append("\n");
            }
        }

        if (newsIds != null && !newsIds.isEmpty()) {
            context.append("\n新闻资讯：\n");
            int idx = 1;
            for (Long id : newsIds) {
                if (id == null) continue;
                News n = newsMapper.findById(id);
                if (n == null) continue;
                context.append(idx++).append(". ")
                        .append("[新闻]")
                        .append(" 标题=").append(safe(n.getTitle()))
                        .append("\n");
                if (n.getSummary() != null && !n.getSummary().isEmpty()) {
                    context.append("摘要：").append(truncate(n.getSummary(), 400)).append("\n");
                }
                if (n.getContent() != null && !n.getContent().isEmpty()) {
                    context.append("内容：").append(truncate(n.getContent(), 600)).append("\n");
                }
                context.append("\n");
            }
        }

        String systemPrompt = "你是一名思政教育与工程伦理结合方向的出题助手。你只输出严格JSON，不要包含任何多余文本。";
        String userPrompt = "请根据下面的任务与材料内容，生成" + qCount + "道单选题（每题4个选项A/B/C/D）。要求：\n" +
                "1) 题目用于检验学习效果，突出思政价值、工程伦理、责任担当、法治意识、创新精神、安全生产等；\n" +
                "2) 每题必须有且仅有一个正确答案；\n" +
                "3) 输出必须为严格JSON，结构如下：{\"questions\":[{\"stem\":\"...\",\"options\":{\"A\":\"...\",\"B\":\"...\",\"C\":\"...\",\"D\":\"...\"},\"answer\":\"A\",\"analysis\":\"...\"}]}\n" +
                "4) 不要输出markdown代码块标记。\n\n" +
                "任务与材料：\n" + context;

        try {
            String content = bailianClient.chatCompletions(systemPrompt, userPrompt);
            String json = extractJson(content);

            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.has("questions") || !node.get("questions").isArray()) {
                throw new RuntimeException("题目JSON格式不正确");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.convertValue(node, Map.class);
            return result;
        } catch (Exception e) {
            return fallbackQuiz(title, description, qCount);
        }
    }

    public Map<String, Object> generateMaterialQuizzes(String title, String description, List<Long> materialIds, Integer countPerMaterial) {
        int qCount = (countPerMaterial == null) ? 4 : countPerMaterial;
        if (qCount < 3) qCount = 3;
        if (qCount > 5) qCount = 5;

        List<Map<String, Object>> materials = new ArrayList<>();
        if (materialIds != null && !materialIds.isEmpty()) {
            for (Long id : materialIds) {
                if (id == null) continue;
                Material m = materialMapper.findByIdWithoutFile(id);
                if (m == null) continue;
                Map<String, Object> one = new LinkedHashMap<>();
                one.put("materialId", m.getId());
                one.put("materialTitle", m.getTitle());
                one.put("materialType", m.getType());
                one.put("questions", generateQuizForOneMaterial(title, description, m, qCount));
                materials.add(one);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", "PER_MATERIAL");
        out.put("countPerMaterial", qCount);
        out.put("materials", materials);
        return out;
    }

    private List<Map<String, Object>> generateQuizForOneMaterial(String taskTitle, String taskDescription, Material m, int qCount) {
        StringBuilder context = new StringBuilder();
        context.append("任务标题：").append(safe(taskTitle)).append("\n");
        if (taskDescription != null && !taskDescription.trim().isEmpty()) {
            context.append("任务说明：").append(safe(taskDescription)).append("\n");
        }
        context.append("\n素材信息：\n");
        context.append("标题：").append(safe(m.getTitle())).append("\n");
        context.append("类型：").append(safe(m.getType())).append("\n");
        if (m.getDescription() != null && !m.getDescription().isEmpty()) {
            context.append("简介：").append(truncate(m.getDescription(), 400)).append("\n");
        }
        if (m.getContent() != null && !m.getContent().isEmpty()) {
            context.append("内容：").append(truncate(m.getContent(), 800)).append("\n");
        }

        String systemPrompt = "你是一名思政教育与工程伦理结合方向的出题助手。你只输出严格JSON，不要包含任何多余文本。";
        String userPrompt = "请根据下面的任务与素材内容，仅围绕该素材生成" + qCount + "道单选题（每题4个选项A/B/C/D）。要求：\n" +
                "1) 题目用于检验学习效果，突出思政价值、工程伦理、责任担当、法治意识、创新精神、安全生产等；\n" +
                "2) 每题必须有且仅有一个正确答案；\n" +
                "3) 输出必须为严格JSON，结构如下：{\"questions\":[{\"stem\":\"...\",\"options\":{\"A\":\"...\",\"B\":\"...\",\"C\":\"...\",\"D\":\"...\"},\"answer\":\"A\",\"analysis\":\"...\"}]}\n" +
                "4) 不要输出markdown代码块标记。\n\n" +
                "任务与素材：\n" + context;

        try {
            String content = bailianClient.chatCompletions(systemPrompt, userPrompt);
            String json = extractJson(content);
            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.has("questions") || !node.get("questions").isArray()) {
                throw new RuntimeException("题目JSON格式不正确");
            }
            JsonNode questions = node.get("questions");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = objectMapper.convertValue(questions, List.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            Map<String, Object> fallback = fallbackQuiz(m.getTitle(), taskDescription, qCount);
            Object qs = fallback.get("questions");
            if (qs instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> out = (List<Map<String, Object>>) qs;
                return out;
            }
            return new ArrayList<>();
        }
    }

    private Map<String, Object> fallbackQuiz(String title, String description, int qCount) {
        String t = safe(title);
        if (t.isEmpty()) t = "学习任务";
        String d = safe(description);

        List<Map<String, Object>> questions = new ArrayList<>();
        for (int i = 1; i <= qCount; i++) {
            Map<String, Object> q = new LinkedHashMap<>();
            String stem = "(" + i + ") 根据《" + t + "》相关学习内容，以下哪项最符合思政价值与工程伦理的要求？";
            if (!d.isEmpty() && i == 1) {
                stem = stem + "（任务说明：" + truncate(d, 60) + "）";
            }
            q.put("stem", stem);

            Map<String, String> options = new LinkedHashMap<>();
            options.put("A", "只关注个人利益或短期效果");
            options.put("B", "忽视规范与风险，先做再说");
            options.put("C", "以人民为中心，遵循规范，兼顾安全与责任担当");
            options.put("D", "推卸责任，等待他人处理");
            q.put("options", options);
            q.put("answer", "C");
            q.put("analysis", "该选项体现了以人民为中心、遵守规范、风险意识与责任担当的基本要求。");
            questions.add(q);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("questions", questions);
        return out;
    }

    private String safe(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }

    private String extractJson(String content) {
        if (content == null) throw new RuntimeException("百炼返回为空");
        String t = content.trim();

        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline >= 0) {
                t = t.substring(firstNewline + 1);
            }
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) {
                t = t.substring(0, lastFence);
            }
            t = t.trim();
        }

        int obj = t.indexOf('{');
        int arr = t.indexOf('[');
        int start = -1;
        if (obj >= 0 && arr >= 0) start = Math.min(obj, arr);
        else if (obj >= 0) start = obj;
        else if (arr >= 0) start = arr;
        if (start < 0) return t;

        Deque<Character> stack = new ArrayDeque<>();
        StringBuilder out = new StringBuilder();
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < t.length(); i++) {
            char c = t.charAt(i);

            if (inString) {
                out.append(c);
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }

            if (c == '{' || c == '[') {
                stack.push(c);
                out.append(c);
                continue;
            }

            if (c == '}' || c == ']') {

                while (!stack.isEmpty()) {
                    char open = stack.peek();
                    if ((open == '{' && c == '}') || (open == '[' && c == ']')) {
                        stack.pop();
                        out.append(c);
                        break;
                    }

                    if (open == '[') {
                        stack.pop();
                        out.append(']');
                        continue;
                    }
                    if (open == '{') {
                        stack.pop();
                        out.append('}');
                        continue;
                    }
                    break;
                }
                if (stack.isEmpty()) {

                    return out.toString();
                }

                if (stack.isEmpty()) {
                    return out.toString();
                }
                continue;
            }

            out.append(c);
        }


        while (!stack.isEmpty()) {
            char open = stack.pop();
            out.append(open == '[' ? ']' : '}');
        }
        return out.toString().trim();
    }
}
