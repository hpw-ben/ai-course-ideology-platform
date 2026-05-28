package com.example.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BailianClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bailian.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${bailian.api-key:}")
    private String apiKey;

    @Value("${bailian.model:qwen-plus}")
    private String model;

    public String chatCompletions(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("未配置百炼 API Key，请设置环境变量 DASHSCOPE_API_KEY 或 application.properties 的 bailian.api-key");
        }

        String url = baseUrl;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        url = url + "/chat/completions";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.2);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("百炼请求失败: HTTP " + resp.getStatusCode());
            }
            Map<?, ?> body = resp.getBody();
            Object choicesObj = body.get("choices");
            if (!(choicesObj instanceof List) || ((List<?>) choicesObj).isEmpty()) {
                throw new RuntimeException("百炼返回异常：choices为空");
            }
            Object choice0 = ((List<?>) choicesObj).get(0);
            if (!(choice0 instanceof Map)) {
                throw new RuntimeException("百炼返回异常：choices[0]格式不正确");
            }
            Object messageObj = ((Map<?, ?>) choice0).get("message");
            if (!(messageObj instanceof Map)) {
                throw new RuntimeException("百炼返回异常：message格式不正确");
            }
            Object contentObj = ((Map<?, ?>) messageObj).get("content");
            if (contentObj == null) {
                throw new RuntimeException("百炼返回异常：content为空");
            }
            return contentObj.toString();
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            if (body != null && body.length() > 1200) body = body.substring(0, 1200) + "...";
            throw new RuntimeException("百炼请求失败: HTTP " + e.getStatusCode() + (body == null || body.isEmpty() ? "" : ("; body=" + body)), e);
        } catch (RestClientException e) {
            throw new RuntimeException("百炼请求异常: " + e.getMessage(), e);
        }
    }
}
