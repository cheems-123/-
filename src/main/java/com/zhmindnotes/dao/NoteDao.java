package com.zhmindnotes.dao;

import com.zhmindnotes.model.Note;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NoteDao {

    private final JdbcTemplate jdbc;

    public NoteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final BeanPropertyRowMapper<Note> MAPPER = new BeanPropertyRowMapper<>(Note.class);

    private static final String BASE_COLUMNS =
            "SELECT n.id, n.user_id AS userId, n.course_id AS courseId, n.title, n.share_status AS shareStatus, " +
            "n.view_count AS viewCount, n.like_count AS likeCount, n.ai_summary AS aiSummary, n.ai_keywords AS aiKeywords, " +
            "n.ai_points AS aiPoints, n.ai_engine AS aiEngine, n.ai_updated_at AS aiUpdatedAt, " +
            "n.created_at AS createdAt, n.updated_at AS updatedAt, c.name AS courseName, u.nickname AS authorName " +
            "FROM note n JOIN course c ON c.id = n.course_id JOIN user u ON u.id = n.user_id ";

    /** 公开笔记列表：可按课程、关键词过滤，按阅读量/点赞量/最新排序 */
    public List<Note> listPublic(Long courseId, String sort, String keyword) {
        StringBuilder sql = new StringBuilder(BASE_COLUMNS + "WHERE n.share_status = 1 ");
        List<Object> args = new ArrayList<>();
        if (courseId != null) {
            sql.append("AND n.course_id = ? ");
            args.add(courseId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (n.title LIKE ? OR n.ai_summary LIKE ? OR n.ai_keywords LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            args.add(kw);
            args.add(kw);
            args.add(kw);
        }
        switch (sort == null ? "newest" : sort) {
            case "views":
                sql.append("ORDER BY n.view_count DESC, n.id DESC");
                break;
            case "likes":
                sql.append("ORDER BY n.like_count DESC, n.id DESC");
                break;
            default:
                sql.append("ORDER BY n.created_at DESC, n.id DESC");
        }
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public List<Note> listMine(Long userId) {
        return jdbc.query(BASE_COLUMNS + "WHERE n.user_id = ? ORDER BY n.created_at DESC, n.id DESC",
                MAPPER, userId);
    }

    public Note findDetailById(Long id) {
        List<Note> list = jdbc.query(BASE_COLUMNS + "WHERE n.id = ?", MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Long userId, Long courseId, String title, Integer shareStatus) {
        String sql = "INSERT INTO note (user_id, course_id, title, share_status) VALUES (?, ?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, courseId);
            ps.setString(3, title);
            ps.setInt(4, shareStatus == null ? 1 : shareStatus);
            return ps;
        }, holder);
        Number key = holder.getKey();
        return key == null ? null : key.longValue();
    }

    public void updateBasic(Long id, String title, Long courseId, Integer shareStatus) {
        jdbc.update("UPDATE note SET title = ?, course_id = ?, share_status = ? WHERE id = ?",
                title, courseId, shareStatus, id);
    }

    public void updateShareStatus(Long id, Integer shareStatus) {
        jdbc.update("UPDATE note SET share_status = ? WHERE id = ?", shareStatus, id);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM note WHERE id = ?", id);
    }

    public Long findOwnerId(Long id) {
        List<Long> ids = jdbc.queryForList("SELECT user_id FROM note WHERE id = ?", Long.class, id);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public void incrementView(Long id) {
        jdbc.update("UPDATE note SET view_count = view_count + 1 WHERE id = ?", id);
    }

    public void updateLikeCount(Long id, int delta) {
        jdbc.update("UPDATE note SET like_count = GREATEST(0, like_count + ?) WHERE id = ?", delta, id);
    }

    /** 写回 AI 整理结果 */
    public void updateAiResult(Long id, String summary, String keywords, String points, String engine) {
        jdbc.update("UPDATE note SET ai_summary = ?, ai_keywords = ?, ai_points = ?, ai_engine = ?, ai_updated_at = NOW() WHERE id = ?",
                summary, keywords, points, engine, id);
    }

    public long count() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM note", Long.class);
        return c == null ? 0 : c;
    }
}
