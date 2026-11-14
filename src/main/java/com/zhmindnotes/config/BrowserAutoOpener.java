package com.zhmindnotes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

/**
 * 启动完成后自动用系统默认浏览器打开首页，省去手动输地址。
 * 开关：application.yml 中 app.auto-open-browser（默认 true，改成 false 即关闭）。
 * 说明：仅在带图形界面的桌面环境生效；服务器/无桌面环境会自动跳过，不影响启动。
 */
@Component
@ConditionalOnProperty(prefix = "app", name = "auto-open-browser", havingValue = "true", matchIfMissing = true)
public class BrowserAutoOpener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(BrowserAutoOpener.class);

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 种子数据、静态资源均已就绪，稍等片刻再打开，保证首屏拿到数据
        Thread t = new Thread(this::openLater, "auto-open-browser");
        t.setDaemon(true);
        t.start();
    }

    private void openLater() {
        String url = "http://localhost:" + port
                + (contextPath == null ? "" : contextPath)
                + "/index.html";
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (openWithDesktop(url)) {
            log.info("已在默认浏览器中打开首页：{}", url);
            return;
        }
        if (openWithCommand(url)) {
            log.info("已通过系统命令打开首页：{}", url);
            return;
        }
        log.warn("未能自动打开浏览器（当前可能是无桌面环境），请手动访问：{}", url);
    }

    /** 通用做法：JDK Desktop API，Windows / macOS / 常见 Linux 桌面均支持 */
    private boolean openWithDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception e) {
            log.debug("Desktop 方式打开浏览器失败：{}", e.getMessage());
        }
        return false;
    }

    /** 兜底做法：直接调系统命令，绕开 Desktop 在部分环境不可用的问题 */
    private boolean openWithCommand(String url) {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
            return true;
        } catch (Exception e) {
            log.debug("系统命令打开浏览器失败：{}", e.getMessage());
            return false;
        }
    }
}
