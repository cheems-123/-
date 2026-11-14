package com.zhmindnotes.dao;

import com.zhmindnotes.model.Course;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CourseDao {

    private final JdbcTemplate jdbc;

    public CourseDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final BeanPropertyRowMapper<Course> MAPPER = new BeanPropertyRowMapper<>(Course.class);

    public List<Course> listAll() {
        return jdbc.query(
                "SELECT id, name, code, description, icon, sort_no AS sortNo, created_at AS createdAt " +
                        "FROM course ORDER BY sort_no ASC, id ASC", MAPPER);
    }

    public Course findById(Long id) {
        List<Course> list = jdbc.query(
                "SELECT id, name, code, description, icon, sort_no AS sortNo, created_at AS createdAt " +
                        "FROM course WHERE id = ?", MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Course findByCode(String code) {
        List<Course> list = jdbc.query(
                "SELECT id, name, code, description, icon, sort_no AS sortNo, created_at AS createdAt " +
                        "FROM course WHERE code = ?", MAPPER, code);
        return list.isEmpty() ? null : list.get(0);
    }

    public long count() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM course", Long.class);
        return c == null ? 0 : c;
    }
}
