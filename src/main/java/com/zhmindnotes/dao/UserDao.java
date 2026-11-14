package com.zhmindnotes.dao;

import com.zhmindnotes.model.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final BeanPropertyRowMapper<User> MAPPER = new BeanPropertyRowMapper<>(User.class);

    public User findByUsername(String username) {
        List<User> list = jdbc.query(
                "SELECT id, username, password_hash, nickname, avatar, role, created_at AS createdAt " +
                        "FROM user WHERE username = ?", MAPPER, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public User findById(Long id) {
        List<User> list = jdbc.query(
                "SELECT id, username, password_hash, nickname, avatar, role, created_at AS createdAt " +
                        "FROM user WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 注意：SELECT 不含 password_hash 时需要单独调整；此处密码哈希仅供服务端比对，不返回前端。 */
    public User findSafeById(Long id) {
        List<User> list = jdbc.query(
                "SELECT id, username, nickname AS nickname, avatar, role, created_at AS createdAt " +
                        "FROM user WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public long count() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM user", Long.class);
        return c == null ? 0 : c;
    }

    public Long insert(String username, String passwordHash, String nickname) {
        String sql = "INSERT INTO user (username, password_hash, nickname, role) VALUES (?, ?, ?, 'USER')";
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, nickname);
            return ps;
        }, holder);
        Number key = holder.getKey();
        return key == null ? null : key.longValue();
    }
}
