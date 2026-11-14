package com.zhmindnotes.service.ai;

/**
 * AI 笔记整理服务：生成便于读者快速选择阅读的摘要、关键词与要点。
 */
public interface AiOrganizeService {

    /**
     * @param title   笔记标题
     * @param fullText 全部文字片段拼接内容（图片/音频无文字，仅用于上下文）
     */
    AiResult organize(String title, String fullText);
}
