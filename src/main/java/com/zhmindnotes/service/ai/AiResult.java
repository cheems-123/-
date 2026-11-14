package com.zhmindnotes.service.ai;

import java.util.List;

/**
 * AI 整理结果：摘要 + 关键词 + 结构化要点。
 */
public class AiResult {

    /** LLM 或 RULE */
    private String engine;
    private String summary;
    private List<String> keywords;
    private List<String> points;

    public AiResult() {
    }

    public AiResult(String engine, String summary, List<String> keywords, List<String> points) {
        this.engine = engine;
        this.summary = summary;
        this.keywords = keywords;
        this.points = points;
    }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public List<String> getPoints() { return points; }
    public void setPoints(List<String> points) { this.points = points; }
}
