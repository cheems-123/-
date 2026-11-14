package com.zhmindnotes.controller.dto;

public class SegmentDTO {
    /** TEXT / IMAGE / AUDIO */
    private String segType;
    private String contentText;
    private String mediaUrl;
    private String caption;

    public String getSegType() { return segType; }
    public void setSegType(String segType) { this.segType = segType; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
