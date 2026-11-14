-- =====================================================================
-- 智记 MindNotes 数据库建表脚本  MySQL 8.0
-- 执行方式（二选一）：
--   1) Navicat Premium 16：新建连接 -> 打开本文件 -> 运行；
--   2) MySQL 8.0 Command Line Client：source 本文件绝对路径
-- 字符集：utf8mb4 / utf8mb4_0900_ai_ci；引擎 InnoDB
-- =====================================================================

CREATE DATABASE IF NOT EXISTS zhi_mindnotes
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE zhi_mindnotes;

-- 若重复执行先清理（注意：会清空数据，演示环境可保留此段）
DROP TABLE IF EXISTS ai_log;
DROP TABLE IF EXISTS contribution_like;
DROP TABLE IF EXISTS contribution;
DROP TABLE IF EXISTS note_like;
DROP TABLE IF EXISTS note_segment;
DROP TABLE IF EXISTS note;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS user;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
CREATE TABLE user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username      VARCHAR(50)  NOT NULL COMMENT '登录名（唯一）',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    nickname      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 2. 课程表（笔记按课程分类）
-- ---------------------------------------------------------------------
CREATE TABLE course (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    code        VARCHAR(40)  NOT NULL COMMENT '课程代码（唯一）',
    description VARCHAR(500) DEFAULT NULL COMMENT '课程简介',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '前端图标标识',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '课程表';

-- ---------------------------------------------------------------------
-- 3. 笔记主表（标题 + AI整理结果 + 统计量）；多格式正文在 note_segment
-- ---------------------------------------------------------------------
CREATE TABLE note (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL COMMENT '作者ID',
    course_id     BIGINT       NOT NULL COMMENT '所属课程ID',
    title         VARCHAR(200) NOT NULL COMMENT '笔记标题',
    share_status  TINYINT      NOT NULL DEFAULT 1 COMMENT '0私有 1公开共享',
    view_count    INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    like_count    INT          NOT NULL DEFAULT 0 COMMENT '点赞量',
    ai_summary    TEXT         COMMENT 'AI 生成摘要',
    ai_keywords   VARCHAR(500) COMMENT 'AI 关键词，逗号分隔',
    ai_points     TEXT         COMMENT 'AI 结构化要点，换行分隔',
    ai_engine     VARCHAR(20)  COMMENT '生成引擎 LLM / RULE',
    ai_updated_at DATETIME     DEFAULT NULL COMMENT 'AI 整理时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_note_course (course_id),
    KEY idx_note_user (user_id),
    KEY idx_note_share (share_status),
    KEY idx_note_views (view_count),
    KEY idx_note_likes (like_count)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '笔记主表';

-- ---------------------------------------------------------------------
-- 4. 笔记片段表：TEXT 文字 / IMAGE 图片 / AUDIO 音频，按 seq 排序
-- ---------------------------------------------------------------------
CREATE TABLE note_segment (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    note_id       BIGINT       NOT NULL,
    seq           INT          NOT NULL DEFAULT 0 COMMENT '片段顺序',
    seg_type      VARCHAR(20)  NOT NULL COMMENT 'TEXT/IMAGE/AUDIO',
    content_text  TEXT         COMMENT '文字内容（TEXT）',
    media_url     VARCHAR(500) DEFAULT NULL COMMENT '媒体文件访问URL（IMAGE/AUDIO）',
    caption       VARCHAR(300) DEFAULT NULL COMMENT '图片/音频说明',
    PRIMARY KEY (id),
    KEY idx_seg_note (note_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '笔记多格式片段表';

-- ---------------------------------------------------------------------
-- 5. 笔记点赞表（联合唯一，防重复点赞）
-- ---------------------------------------------------------------------
CREATE TABLE note_like (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    note_id    BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_user (note_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '笔记点赞表';

-- ---------------------------------------------------------------------
-- 6. 贴吧式补充表（跟帖 / 楼中楼，支持文字、图片、音频补充）
-- ---------------------------------------------------------------------
CREATE TABLE contribution (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    note_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '0=主帖补充；>0=回复某条补充',
    content     TEXT         COMMENT '补充文字',
    media_url   VARCHAR(500) DEFAULT NULL,
    media_type  VARCHAR(20)  NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT/IMAGE/AUDIO',
    like_count  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_contrib_note (note_id),
    KEY idx_contrib_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '共享笔记补充（贴吧跟帖）表';

-- ---------------------------------------------------------------------
-- 7. 补充点赞表
-- ---------------------------------------------------------------------
CREATE TABLE contribution_like (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    contribution_id BIGINT   NOT NULL,
    user_id         BIGINT   NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cu (contribution_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '补充点赞表';

-- ---------------------------------------------------------------------
-- 8. AI 整理调用日志（便于演示整理来源：LLM / RULE）
-- ---------------------------------------------------------------------
CREATE TABLE ai_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    note_id     BIGINT       NOT NULL,
    engine      VARCHAR(20)  NOT NULL COMMENT 'LLM / RULE',
    result_text MEDIUMTEXT   COMMENT '整理结果快照',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ai_note (note_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI整理日志表';
