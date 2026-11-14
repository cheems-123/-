package com.zhmindnotes.controller;

import com.zhmindnotes.common.Result;
import com.zhmindnotes.dao.CourseDao;
import com.zhmindnotes.model.Course;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseDao courseDao;

    public CourseController(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @GetMapping
    public Result<List<Course>> list() {
        return Result.ok(courseDao.listAll());
    }
}
