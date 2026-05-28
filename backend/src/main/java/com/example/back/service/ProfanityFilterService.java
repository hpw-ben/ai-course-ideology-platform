package com.example.back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProfanityFilterService {

    @Value("${comment.profanity.enabled:true}")
    private boolean enabled;

    private static final List<String> DEFAULT_WORDS = Arrays.asList(
            "傻逼",
            "傻b",
            "sb",
            "操你妈",
            "艹你妈",
            "草你妈",
            "你妈的",
            "妈的",
            "卧槽",
            "我草",
            "滚",
            "垃圾",
            "废物",
            "尼玛",
            "逼"
    );

    public boolean containsProfanity(String text) {
        if (!enabled) return false;
        if (text == null) return false;
        String s = normalize(text);
        if (s.isEmpty()) return false;
        for (String w : DEFAULT_WORDS) {
            if (w == null || w.isEmpty()) continue;
            if (s.contains(normalize(w))) return true;
        }
        return false;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("\\s+", "");
    }
}
