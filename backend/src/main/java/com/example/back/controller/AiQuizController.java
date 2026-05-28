package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.service.AiQuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/quiz")
public class AiQuizController {

    @Autowired
    private AiQuizService aiQuizService;

    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody Map<String, Object> params) {
        try {
            String title = params.get("title") == null ? null : params.get("title").toString();
            String description = params.get("description") == null ? null : params.get("description").toString();

            List<Long> materialIds = toLongList(params.get("materialIds"));
            List<Long> newsIds = toLongList(params.get("newsIds"));

            Integer count = null;
            if (params.get("count") != null) {
                try {
                    count = Integer.valueOf(params.get("count").toString());
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> quiz = aiQuizService.generateQuiz(title, description, materialIds, newsIds, count);
            return Result.success(quiz);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成题目失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate-material")
    public Result<Map<String, Object>> generateMaterial(@RequestBody Map<String, Object> params) {
        try {
            String title = params.get("title") == null ? null : params.get("title").toString();
            String description = params.get("description") == null ? null : params.get("description").toString();

            List<Long> materialIds = toLongList(params.get("materialIds"));

            Integer count = null;
            if (params.get("count") != null) {
                try {
                    count = Integer.valueOf(params.get("count").toString());
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> quiz = aiQuizService.generateMaterialQuizzes(title, description, materialIds, count);
            return Result.success(quiz);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成题目失败: " + e.getMessage());
        }
    }

    private List<Long> toLongList(Object obj) {
        if (obj == null) return null;
        if (!(obj instanceof List)) return null;
        List<?> list = (List<?>) obj;
        List<Long> out = new ArrayList<>();
        for (Object o : list) {
            if (o == null) continue;
            try {
                out.add(Long.valueOf(o.toString()));
            } catch (Exception ignored) {
            }
        }
        return out;
    }
}
