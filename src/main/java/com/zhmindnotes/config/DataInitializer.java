package com.zhmindnotes.config;

import com.zhmindnotes.controller.dto.NoteSaveDTO;
import com.zhmindnotes.controller.dto.SegmentDTO;
import com.zhmindnotes.dao.CourseDao;
import com.zhmindnotes.dao.NoteDao;
import com.zhmindnotes.dao.UserDao;
import com.zhmindnotes.model.Course;
import com.zhmindnotes.model.User;
import com.zhmindnotes.service.NoteService;
import com.zhmindnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动幂等种子数据：课程字典、演示账号、两篇多格式示例笔记；
 * 已存在则跳过，保证反复启动不会重复插入。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final NoteService noteService;
    private final NoteDao noteDao;
    private final JdbcTemplate jdbc;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public DataInitializer(UserService userService, UserDao userDao, CourseDao courseDao,
                           NoteService noteService, NoteDao noteDao, JdbcTemplate jdbc) {
        this.userService = userService;
        this.userDao = userDao;
        this.courseDao = courseDao;
        this.noteService = noteService;
        this.noteDao = noteDao;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        checkDatabase();
        seedCourses();
        Long adminId = seedUsers();
        seedDemoNotes(adminId);
    }

    /**
     * 启动自检：数据库未就绪时给出可执行的排查提示，避免一大段 Hikari 堆栈看不清根因。
     */
    private void checkDatabase() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            log.info("数据库连接正常，开始初始化种子数据");
        } catch (DataAccessException e) {
            String msg = e.getMostSpecificCause().getMessage();
            log.error("================ 数据库未就绪，应用无法启动 ================");
            log.error("原因：{}", msg);
            if (msg != null && msg.contains("Access denied")) {
                log.error("判断：账号或密码不正确。请把 spring.datasource.username / password 改成本机 MySQL 的账号密码，");
                log.error("      或先执行 setx DB_PASSWORD 你的密码，再重启 IDEA（application.yml 已支持 ${DB_PASSWORD} 占位）。");
            } else if (msg != null && (msg.contains("Unknown database") || msg.contains("doesn't exist"))) {
                log.error("判断：库或表不存在。请先在 Navicat / MySQL CLI 执行 sql/schema.sql 建库建表。");
            } else if (msg != null && (msg.contains("Communications link failure") || msg.contains("Connection refused"))) {
                log.error("判断：连不上 3306 端口。请确认 MySQL80 服务已启动、端口为 3306。");
            } else {
                log.error("判断：请依次核对 MySQL 服务是否启动、账号密码是否正确、是否已执行 sql/schema.sql。");
            }
            log.error("==========================================================");
            throw new IllegalStateException("数据库未就绪：" + msg, e);
        }
    }

    private void seedCourses() {
        String[][] rows = {
                {"计算机网络", "CN-2024", "OSI/TCP-IP、HTTP、TCP三次握手、UDP、DNS、抓包分析", "network", "1"},
                {"数据结构", "DS-2024", "线性表、栈队列、树、图、排序与查找、复杂度分析", "tree", "2"},
                {"编译原理", "CP-2024", "词法分析、NFA/DFA、正规文法、语法分析、语义分析", "compile", "3"},
                {"操作系统", "OS-2024", "进程线程、同步互斥、内存管理、文件系统、调度算法", "os", "4"},
                {"Java Web", "JW-2024", "Servlet、Spring MVC、JDBC、MySQL、会话与拦截器", "java", "5"},
                {"高等数学", "MATH-24", "极限、导数、积分、微分方程、级数与不等式证明", "math", "6"}
        };
        for (String[] r : rows) {
            jdbc.update("INSERT IGNORE INTO course (name, code, description, icon, sort_no) VALUES (?, ?, ?, ?, ?)",
                    r[0], r[1], r[2], r[3], Integer.valueOf(r[4]));
        }
    }

    private Long seedUsers() {
        if (userDao.count() > 0) {
            return userDao.findByUsername("admin").getId();
        }
        User admin = userService.register("admin", "123456", "课程管理员");
        userService.register("student", "123456", "学习委员");
        log.info("已创建演示账号 admin/student，密码均为 123456");
        return admin.getId();
    }

    private void seedDemoNotes(Long adminId) {
        if (noteDao.count() > 0) {
            return;
        }
        writePlaceholderSvg();

        Course network = courseDao.findByCode("CN-2024");
        Course compile = courseDao.findByCode("CP-2024");
        Course javaWeb = courseDao.findByCode("JW-2024");
        if (network == null || compile == null || javaWeb == null) {
            log.warn("课程尚未就绪，跳过示例笔记写入");
            return;
        }

        // 示例一：公开 · 计算机网络（文字 + 图片）
        NoteSaveDTO n1 = new NoteSaveDTO();
        n1.setTitle("TCP/IP 与三次握手复习要点");
        n1.setCourseId(network.getId());
        n1.setShareStatus(1);
        List<SegmentDTO> segs1 = new ArrayList<>();
        segs1.add(text("本笔记用于复习计算机网络 TCP/IP 协议族。OSI 模型分为七层；TCP/IP 模型分为四层：网络接口层、网际层、传输层、应用层。"));
        segs1.add(text("重点：TCP 是面向连接、可靠传输的协议，通过三次握手建立连接。第一次握手客户端发送 SYN 报文；第二次服务端回复 SYN+ACK；第三次客户端回复 ACK，连接建立完成。"));
        segs1.add(text("注意：三次握手可以防止已失效的连接请求报文突然到达而产生错误连接，同时确认双方收发能力正常。"));
        SegmentDTO img = new SegmentDTO();
        img.setSegType("IMAGE");
        img.setMediaUrl("/uploads/demo_network.svg");
        img.setCaption("图1：TCP 三次握手时序图（占位图，发布页可上传真实图片替换）");
        segs1.add(img);
        segs1.add(text("UDP 是无连接、不可靠、低延迟协议，适用于 DNS、视频直播和实时语音。结论：可靠性要求高用 TCP，实时性要求高用 UDP。"));
        n1.setSegments(segs1);
        noteService.createNote(adminId, n1);

        // 示例二：公开 · 编译原理（文字 + 音频占位）
        NoteSaveDTO n2 = new NoteSaveDTO();
        n2.setTitle("从 NFA 到 DFA：子集构造法定化流程");
        n2.setCourseId(compile.getId());
        n2.setShareStatus(1);
        List<SegmentDTO> segs2 = new ArrayList<>();
        segs2.add(text("词法分析用于把源代码字符流切分为 Token。首先根据正规文法或正则表达式构造 NFA；其次使用子集构造法（确定化）把 NFA 转换为 DFA；最后对 DFA 进行最小化，合并等价状态。"));
        segs2.add(text("定义：子集构造法的核心是状态集的 ε-closure（ε闭包）与 move（字母迁移）；初始状态集为起始状态的 ε 闭包。注意：转换后的 DFA 每个状态对每个输入字母最多一条出边。"));
        SegmentDTO audio = new SegmentDTO();
        audio.setSegType("AUDIO");
        audio.setCaption("音频片段：课堂讲解 NFA 与 DFA 的区别（演示占位，可上传 mp3/wav 录音替换）");
        segs2.add(audio);
        n2.setSegments(segs2);
        noteService.createNote(adminId, n2);

        // 示例三：student 私有 · Java Web（仅供“我的笔记”演示）
        Long studentId = userDao.findByUsername("student").getId();
        NoteSaveDTO n3 = new NoteSaveDTO();
        n3.setTitle("Spring MVC 登录拦截器学习笔记");
        n3.setCourseId(javaWeb.getId());
        n3.setShareStatus(0);
        List<SegmentDTO> segs3 = new ArrayList<>();
        segs3.add(text("Spring MVC 拦截器 HandlerInterceptor 在 Controller 执行前后介入。步骤：实现 preHandle、postHandle 方法；在 WebMvcConfigurer 中 addInterceptors 注册；登录校验放在 preHandle 中完成。"));
        segs3.add(text("注意：拦截器默认只拦截 Controller 请求；静态资源映射通过 addResourceHandlers 单独配置。未登录时返回 401 JSON，由前端跳转登录页。"));
        n3.setSegments(segs3);
        noteService.createNote(studentId, n3);

        log.info("已写入 3 篇示例笔记（2 篇公开、1 篇私有）");
    }

    private SegmentDTO text(String content) {
        SegmentDTO seg = new SegmentDTO();
        seg.setSegType("TEXT");
        seg.setContentText(content);
        return seg;
    }

    private void writePlaceholderSvg() {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='640' height='260'>" +
                    "<rect width='100%' height='100%' rx='14' fill='#eef6f6' stroke='#5aa9a0'/>" +
                    "<text x='320' y='130' font-size='20' text-anchor='middle' fill='#27645e'>TCP 三次握手时序示意（占位图）</text>" +
                    "<text x='320' y='170' font-size='14' text-anchor='middle' fill='#5a7a76'>SYN  ──▶  SYN+ACK  ──▶  ACK</text>" +
                    "</svg>";
            Files.write(dir.resolve("demo_network.svg"), svg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("占位图写入失败：{}", e.getMessage());
        }
    }
}
