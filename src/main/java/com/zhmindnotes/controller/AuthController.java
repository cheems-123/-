package com.zhmindnotes.controller;

import com.zhmindnotes.common.Result;
import com.zhmindnotes.common.SessionUtil;
import com.zhmindnotes.controller.dto.LoginDTO;
import com.zhmindnotes.controller.dto.RegisterDTO;
import com.zhmindnotes.model.User;
import com.zhmindnotes.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        User u = userService.register(dto.getUsername(), dto.getPassword(), dto.getNickname());
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("username", u.getUsername());
        data.put("nickname", u.getNickname());
        return Result.ok(data);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto, HttpServletRequest request) {
        User u = userService.login(dto.getUsername(), dto.getPassword());
        HttpSession session = request.getSession(true);
        session.setAttribute("uid", u.getId());
        session.setAttribute("username", u.getUsername());
        session.setAttribute("nickname", u.getNickname());
        Map<String, Object> data = new HashMap<>();
        data.put("id", u.getId());
        data.put("username", u.getUsername());
        data.put("nickname", u.getNickname());
        return Result.ok(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        Long id = SessionUtil.uid(request);
        // 未登录不算错误：返回 code=0 + data=null，前端据此渲染游客状态；
        // 若返回 401，前端公共请求 function 会把访客直接跳转到登录页，导致"首页打不开"。
        if (id == null) return Result.ok(null);
        return Result.ok(userService.me(id));
    }
}
