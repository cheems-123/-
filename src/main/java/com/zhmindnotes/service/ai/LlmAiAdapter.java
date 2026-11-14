package com.zhmindnotes.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhmindnotes.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型适配器（预留接口，OpenAI 兼容协议 /chat/completions）。
 * 未启用或未配置密钥时返回 null，由编排层回退到 RuleAiOrganizer；调用异常同样回退，不影响主流程。
 */
@Component
public class LlmAiAdapter {

    private static final Logger log = LoggerFactory.getLogger(LlmAiAdapter.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AiProperties props;

    public LlmAiAdapter(AiProperties props) {
        this.props = props;
    }

    public AiResult tryCall(String title, String fullText) {
        if (!props.isEnabled() || props.getApiKey() == null || props.getApiKey().isBlank()) {
            return null;
        }
        try {
            String body = buildRequestBody(title, fullText);
            URL url = new URL(props.getBaseUrl() + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + props.getApiKey());
            conn.setConnectTimeout(props.getTimeoutSeconds() * 1000);
            conn.setReadTimeout(props.getTimeoutSeconds() * 1000);
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code != 200) {
                log.warn("大模型接口返回码 {}，回退规则整理", code);
                return null;
            }
            StringBuilder resp = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) resp.append(line);
            }
            return parseResult(resp.toString());
        } catch (Exception e) {
            log.warn("大模型调用失败（{}），回退规则整理", e.getMessage());
            return null;
        }
    }

    private String buildRequestBody(String title, String fullText) throws Exception {
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "你是学习笔记整理助手。请阅读学生笔记标题与正文，仅输出严格 JSON：" +
                        "{\"summary\":\"100字以内摘要\",\"keywords\":[\"关键词1\",\"关键词2\"],\"points\":[\"要点1\",\"要点2\"]}，" +
                        "不要输出 JSON 以外的文字或 Markdown 代码块。");
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "标题：" + safe(title) + "\n正文：" + safe(fullText));
        Map<String, Object> req = new HashMap<>();
        req.put("model", props.getModel());
        req.put("messages", List.of(systemMsg, userMsg));
        req.put("temperature", 0.3);
        return mapper.writeValueAsString(req);
    }

    private AiResult parseResult(String responseJson) throws Exception {
        JsonNode root = mapper.readTree(responseJson);
        String content = root.path("choices").get(0).path("message").path("content").asText("");
        content = content.replaceAll("(?s)```json\\s*|\\s*```", "").trim();
        JsonNode data = mapper.readTree(content);
        String summary = data.path("summary").asText("");
        List<String> keywords = new ArrayList<>();
        data.path("keywords").forEach(n -> keywords.add(n.asText()));
        List<String> points = new ArrayList<>();
        data.path("points").forEach(n -> points.add(n.asText()));
        return new AiResult("LLM", summary, keywords, points);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
