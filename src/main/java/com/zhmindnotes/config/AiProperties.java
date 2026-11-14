package com.zhmindnotes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 整理配置：预留 OpenAI 兼容大模型接口；未启用/无密钥时回退规则式整理。
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用外部大模型 */
    private boolean enabled = false;
    /** 大模型接口基地址，例如 https://api.openai.com/v1 */
    private String baseUrl;
    /** API 密钥，留空则回退本地规则 */
    private String apiKey;
    /** 模型名 */
    private String model = "gpt-4o-mini";
    /** 调用超时秒数 */
    private int timeoutSeconds = 15;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
