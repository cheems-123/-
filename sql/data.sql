-- =====================================================================
-- 课程字典种子数据（在 schema.sql 之后执行）
-- 演示用户、示例笔记由应用启动时 DataInitializer 自动幂等写入（密码为 BCrypt）
-- =====================================================================
USE zhi_mindnotes;

INSERT INTO course (name, code, description, icon, sort_no) VALUES
('计算机网络', 'CN-2024', 'OSI/TCP-IP、HTTP、TCP三次握手、UDP、DNS、抓包分析', 'network', 1),
('数据结构',     'DS-2024', '线性表、栈队列、树、图、排序与查找、复杂度分析', 'tree',    2),
('编译原理',     'CP-2024', '词法分析、NFA/DFA、正规文法、语法分析、语义分析', 'compile', 3),
('操作系统',     'OS-2024', '进程线程、同步互斥、内存管理、文件系统、调度算法', 'os',      4),
('Java Web',     'JW-2024', 'Servlet、Spring MVC、JDBC、MySQL、会话与拦截器',  'java',    5),
('高等数学',     'MATH-24', '极限、导数、积分、微分方程、级数与不等式证明',     'math',    6);
