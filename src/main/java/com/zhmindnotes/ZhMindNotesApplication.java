package com.zhmindnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智记 MindNotes 启动类。
 * 功能：注册登录、课程分类、多格式(文字/图片/音频)笔记、AI 整理与摘要、
 *       贴吧式共享补充、按阅读量/点赞量排序推荐。
 */
@SpringBootApplication
public class ZhMindNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhMindNotesApplication.class, args);
    }
}
