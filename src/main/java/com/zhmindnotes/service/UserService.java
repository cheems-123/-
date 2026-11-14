package com.zhmindnotes.service;

import com.zhmindnotes.common.BusinessException;
import com.zhmindnotes.dao.UserDao;
import com.zhmindnotes.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDao userDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** 注册：返回不含密码哈希的安全用户信息 */
    public User register(String username, String password, String nickname) {
        if (username == null || !username.matches("[A-Za-z0-9_]{3,20}")) {
            throw new BusinessException(400, "用户名须为 3-20 位字母、数字或下划线");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException(400, "密码长度至少 6 位");
        }
        if (userDao.findByUsername(username) != null) {
            throw new BusinessException(400, "用户名已被注册");
        }
        String nick = (nickname == null || nickname.isBlank()) ? username : nickname.trim();
        Long id = userDao.insert(username, encoder.encode(password), nick);
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setNickname(nick);
        return u;
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }
        User user = userDao.findByUsername(username);
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        user.setPasswordHash(null);
        return user;
    }

    public User me(Long id) {
        User u = userDao.findById(id);
        if (u != null) u.setPasswordHash(null);
        return u;
    }

    public boolean checkPassword(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
