package com.zhmindnotes.model;

/**
 * 笔记片段：支持 TEXT 文字、IMAGE 图片、AUDIO 音频三种格式，按 seq 顺序拼接成一篇笔记。
 */
public class NoteSegment {
    private Long id;
    private Long noteId;
    private Integer seq;
    /** TEXT / IMAGE / AUDIO */
    private String segType;
    private String contentText;
    private String mediaUrl;
    private String caption;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
    public String getSegType() { return segType; }
    public void setSegType(String segType) { this.segType = segType; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
