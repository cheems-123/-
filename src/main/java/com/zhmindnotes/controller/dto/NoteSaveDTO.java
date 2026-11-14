package com.zhmindnotes.controller.dto;

import java.util.List;

public class NoteSaveDTO {
    private String title;
    private Long courseId;
    /** 0=私有，1=公开共享 */
    private Integer shareStatus;
    private List<SegmentDTO> segments;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Integer getShareStatus() { return shareStatus; }
    public void setShareStatus(Integer shareStatus) { this.shareStatus = shareStatus; }
    public List<SegmentDTO> getSegments() { return segments; }
    public void setSegments(List<SegmentDTO> segments) { this.segments = segments; }
}
