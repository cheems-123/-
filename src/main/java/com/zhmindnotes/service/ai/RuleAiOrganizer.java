package com.zhmindnotes.service.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则式整理器（离线回退方案，无需任何 API 密钥即可演示）：
 * 中文句切分 -> 词频关键词提取 -> 关键句打分摘要 -> 标记句要点抽取。
 */
@Component
public class RuleAiOrganizer {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "是", "在", "和", "与", "及", "或", "就", "都", "也", "很", "这", "那", "有", "不",
            "人", "上", "中", "为", "以", "于", "对", "把", "被", "让", "向", "从", "到", "我们", "你们",
            "他们", "可以", "一个", "什么", "怎么", "如何", "进行", "需要", "通过", "以及", "但是", "因为",
            "所以", "如果", "对于", "根据", "包括", "使用", "表示", "结果", "过程", "部分", "内容", "其中"));

    private static final Pattern CJK_WORD = Pattern.compile("[\\u4e00-\\u9fa5]{2,6}");
    private static final String[] POINT_MARKERS = {"重点", "注意", "公式", "定义", "概念", "首先", "其次",
            "最后", "因此", "结论", "优点", "缺点", "步骤", "原理", "区别", "总结", "考点", "例如"};

    public AiResult organize(String title, String fullText) {
        String text = fullText == null ? "" : fullText.replaceAll("\\s+", " ").trim();
        List<String> sentences = splitSentences(text);

        // 1) 关键词：标题 + 正文 2~6字中文片段词频
        List<String> keywords = extractKeywords(title, sentences);

        // 2) 摘要：关键句打分取前 3，按原文顺序拼接
        List<String> summarySentences = pickTopSentences(sentences, keywords, 3);
        String summary = String.join("。", summarySentences).replaceAll("。+", "。");
        if (summary.isEmpty()) {
            summary = "本篇以图片、音频等素材为主，文字说明较少；补充文字后可生成更详细摘要。";
        } else if (!summary.endsWith("。")) {
            summary += "。";
        }

        // 3) 结构化要点：优先“重点/定义/步骤”等标记句，不足再用高分短句补齐
        List<String> points = extractPoints(sentences, keywords);

        return new AiResult("RULE", summary, keywords, points);
    }

    private List<String> splitSentences(String text) {
        List<String> result = new ArrayList<>();
        if (text.isEmpty()) {
            return result;
        }
        String[] raw = text.split("[。！？!?；;\n\r\t]");
        for (String s : raw) {
            String t = s.trim();
            if (t.length() >= 8) {
                result.add(t);
            }
        }
        return result;
    }

    private List<String> extractKeywords(String title, List<String> sentences) {
        Map<String, Integer> freq = new HashMap<>();
        countWords(title, freq);
        for (String s : sentences) {
            countWords(s, freq);
        }
        List<String> ranked = new ArrayList<>(freq.keySet());
        ranked.sort((a, b) -> {
            int f = Integer.compare(freq.get(b), freq.get(a));
            if (f != 0) return f;
            return Integer.compare(b.length(), a.length());
        });
        List<String> keywords = new ArrayList<>();
        for (String w : ranked) {
            if (keywords.size() >= 8) break;
            if (!STOP_WORDS.contains(w) && !keywords.contains(w)) {
                keywords.add(w);
            }
        }
        return keywords;
    }

    private void countWords(String sentence, Map<String, Integer> freq) {
        if (sentence == null) return;
        Matcher m = CJK_WORD.matcher(sentence);
        while (m.find()) {
            String w = m.group();
            if (STOP_WORDS.contains(w)) continue;
            freq.merge(w, 1, Integer::sum);
        }
    }

    private List<String> pickTopSentences(List<String> sentences, List<String> keywords, int limit) {
        if (sentences.isEmpty()) return new ArrayList<>();
        List<Map.Entry<Integer, Double>> scored = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            double score = 0;
            for (String k : keywords) {
                if (s.contains(k)) score += 2;
            }
            if (i == 0) score += 1.3;
            int len = s.length();
            if (len >= 18 && len <= 120) score += 1;
            scored.add(new AbstractMap.SimpleEntry<>(i, score));
        }
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Set<Integer> chosen = new HashSet<>();
        for (Map.Entry<Integer, Double> e : scored) {
            if (chosen.size() >= limit) break;
            if (e.getValue() > 0) chosen.add(e.getKey());
        }
        List<Integer> order = new ArrayList<>(chosen);
        Collections.sort(order);
        List<String> out = new ArrayList<>();
        for (Integer idx : order) {
            out.add(sentences.get(idx));
        }
        return out;
    }

    private List<String> extractPoints(List<String> sentences, List<String> keywords) {
        List<String> points = new ArrayList<>();
        for (String s : sentences) {
            if (points.size() >= 4) break;
            for (String marker : POINT_MARKERS) {
                if (s.contains(marker)) {
                    points.add(truncate(s, 80));
                    break;
                }
            }
        }
        if (points.size() < 3) {
            List<String> high = pickTopSentences(sentences, keywords, 4);
            for (String s : high) {
                if (points.size() >= 3) break;
                String p = truncate(s, 80);
                if (!points.contains(p)) points.add(p);
            }
        }
        return points;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
