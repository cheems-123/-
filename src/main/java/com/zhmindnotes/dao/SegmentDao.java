package com.zhmindnotes.dao;

import com.zhmindnotes.model.NoteSegment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SegmentDao {

    private final JdbcTemplate jdbc;

    public SegmentDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final BeanPropertyRowMapper<NoteSegment> MAPPER = new BeanPropertyRowMapper<>(NoteSegment.class);

    public List<NoteSegment> listByNoteId(Long noteId) {
        return jdbc.query(
                "SELECT id, note_id AS noteId, seq, seg_type AS segType, content_text AS contentText, " +
                        "media_url AS mediaUrl, caption FROM note_segment WHERE note_id = ? ORDER BY seq ASC, id ASC",
                MAPPER, noteId);
    }

    public void insert(Long noteId, Integer seq, String segType, String contentText, String mediaUrl, String caption) {
        jdbc.update("INSERT INTO note_segment (note_id, seq, seg_type, content_text, media_url, caption) VALUES (?, ?, ?, ?, ?, ?)",
                noteId, seq, segType, contentText, mediaUrl, caption);
    }

    public void deleteByNoteId(Long noteId) {
        jdbc.update("DELETE FROM note_segment WHERE note_id = ?", noteId);
    }
}
