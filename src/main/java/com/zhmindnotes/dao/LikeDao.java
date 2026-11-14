package com.zhmindnotes.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LikeDao {

    private final JdbcTemplate jdbc;

    public LikeDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------------- 笔记点赞 ----------------

    public boolean noteLiked(Long noteId, Long userId) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM note_like WHERE note_id = ? AND user_id = ?", Long.class, noteId, userId);
        return c != null && c > 0;
    }

    public void addNoteLike(Long noteId, Long userId) {
        jdbc.update("INSERT IGNORE INTO note_like (note_id, user_id) VALUES (?, ?)", noteId, userId);
    }

    public void removeNoteLike(Long noteId, Long userId) {
        jdbc.update("DELETE FROM note_like WHERE note_id = ? AND user_id = ?", noteId, userId);
    }

    // ---------------- 补充点赞 ----------------

    public boolean contribLiked(Long contributionId, Long userId) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM contribution_like WHERE contribution_id = ? AND user_id = ?",
                Long.class, contributionId, userId);
        return c != null && c > 0;
    }

    public void addContribLike(Long contributionId, Long userId) {
        jdbc.update("INSERT IGNORE INTO contribution_like (contribution_id, user_id) VALUES (?, ?)",
                contributionId, userId);
    }

    public void removeContribLike(Long contributionId, Long userId) {
        jdbc.update("DELETE FROM contribution_like WHERE contribution_id = ? AND user_id = ?",
                contributionId, userId);
    }
}
