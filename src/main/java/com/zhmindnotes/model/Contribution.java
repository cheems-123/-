package com.zhmindnotes.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 贴吧式补充（跟帖/楼中楼）：读者在共享笔记基础上补充文字、图片或音频片段。
 */
public class Contribution {
    private Long id;
    private Long noteId;
    private Long userId;
    private Long parentId;
    private String content;
    private String mediaUrl;
    /** TEXT / IMAGE / AUDIO */
    private String mediaType;
    private Integer likeCount;
    private LocalDateTime createdAt;

    // 联表展示
    private String authorName;
    private Boolean liked;
    private List<Contribution> replies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public List<Contribution> getReplies() { return replies; }
    public void setReplies(List<Contribution> replies) { this.replies = replies; }
}
