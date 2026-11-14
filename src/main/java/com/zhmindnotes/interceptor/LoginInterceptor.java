package com.zhmindnotes.interceptor;

import com.zhmindnotes.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截：只读公开接口放行；写操作需登录。
 * 注意：本工程使用 Spring Boot 2.7（javax 命名空间），如后续升级 3.x 请改为 jakarta 包名。
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty()) {
            path = path.substring(ctx.length());
        }

        // 1) 登录 / 注册
        if (("POST".equals(method) && "/api/auth/login".equals(path))
                || ("POST".equals(method) && "/api/auth/register".equals(path))) {
            return true;
        }
        // 2) 只读公开：课程列表、笔记列表/详情、AI结果、补充列表、当前登录态查询
        if ("GET".equals(method) && ("/api/courses".equals(path)
                || "/api/auth/me".equals(path)
                || path.matches("/api/notes(/.*)?"))) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("uid") != null) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(MAPPER.writeValueAsString(Result.fail(401, "请先登录后再操作")));
        return false;
    }
}
