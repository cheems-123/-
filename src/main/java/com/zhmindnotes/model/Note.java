package com.zhmindnotes.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记主表。多格式内容存放在 note_segment（文字 / 图片 / 音频）。
 */
public class Note {
    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    /** 0=私有，1=公开共享（贴吧可见） */
    private Integer shareStatus;
    private Integer viewCount;
    private Integer likeCount;

    // AI 整理结果
    private String aiSummary;
    /** 关键词，英文逗号分隔存储，读取时转列表 */
    private String aiKeywords;
    private String aiPoints;
    private String aiEngine;
    private LocalDateTime aiUpdatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 联表展示字段
    private String courseName;
    private String authorName;
    /** 当前用户是否已点赞（详情接口填充） */
    private Boolean liked;

    // 片段列表（详情接口返回）
    private List<NoteSegment> segments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getShareStatus() { return shareStatus; }
    public void setShareStatus(Integer shareStatus) { this.shareStatus = shareStatus; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public String getAiKeywords() { return aiKeywords; }
    public void setAiKeywords(String aiKeywords) { this.aiKeywords = aiKeywords; }
    public String getAiPoints() { return aiPoints; }
    public void setAiPoints(String aiPoints) { this.aiPoints = aiPoints; }
    public String getAiEngine() { return aiEngine; }
    public void setAiEngine(String aiEngine) { this.aiEngine = aiEngine; }
    public LocalDateTime getAiUpdatedAt() { return aiUpdatedAt; }
    public void setAiUpdatedAt(LocalDateTime aiUpdatedAt) { this.aiUpdatedAt = aiUpdatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public List<NoteSegment> getSegments() { return segments; }
    public void setSegments(List<NoteSegment> segments) { this.segments = segments; }
}
