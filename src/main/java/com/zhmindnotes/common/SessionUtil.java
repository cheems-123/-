package com.zhmindnotes.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class SessionUtil {

    private SessionUtil() {
    }

    public static Long uid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("uid");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public static Long requireUid(HttpServletRequest request) {
        Long id = uid(request);
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        return id;
    }
}
