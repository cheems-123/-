package com.zhmindnotes.dao;

import com.zhmindnotes.model.Contribution;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class ContributionDao {

    private final JdbcTemplate jdbc;

    public ContributionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final BeanPropertyRowMapper<Contribution> MAPPER = new BeanPropertyRowMapper<>(Contribution.class);

    private static final String BASE =
            "SELECT c.id, c.note_id AS noteId, c.user_id AS userId, c.parent_id AS parentId, c.content, " +
            "c.media_url AS mediaUrl, c.media_type AS mediaType, c.like_count AS likeCount, c.created_at AS createdAt, " +
            "u.nickname AS authorName FROM contribution c JOIN user u ON u.id = c.user_id ";

    /** 主帖补充（parent_id=0），按时间正序，呈现为贴吧楼层 */
    public List<Contribution> listTopByNote(Long noteId) {
        return jdbc.query(BASE + "WHERE c.note_id = ? AND c.parent_id = 0 ORDER BY c.created_at ASC, c.id ASC",
                MAPPER, noteId);
    }

    /** 某条补充下的楼中楼回复 */
    public List<Contribution> listReplies(Long parentId) {
        return jdbc.query(BASE + "WHERE c.parent_id = ? ORDER BY c.created_at ASC, c.id ASC", MAPPER, parentId);
    }

    public Long insert(Long noteId, Long userId, Long parentId, String content,
                       String mediaUrl, String mediaType) {
        String sql = "INSERT INTO contribution (note_id, user_id, parent_id, content, media_url, media_type) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setLong(1, noteId);
            ps.setLong(2, userId);
            ps.setLong(3, parentId == null ? 0L : parentId);
            ps.setString(4, content);
            ps.setString(5, mediaUrl);
            ps.setString(6, mediaType == null ? "TEXT" : mediaType);
            return ps;
        }, holder);
        Number key = holder.getKey();
        return key == null ? null : key.longValue();
    }

    public void incrementLike(Long id, int delta) {
        jdbc.update("UPDATE contribution SET like_count = GREATEST(0, like_count + ?) WHERE id = ?", delta, id);
    }

    public Long findNoteId(Long id) {
        List<Long> list = jdbc.queryForList("SELECT note_id FROM contribution WHERE id = ?", Long.class, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public long countByNote(Long noteId) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM contribution WHERE note_id = ?", Long.class, noteId);
        return c == null ? 0 : c;
    }
}
