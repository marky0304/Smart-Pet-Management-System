package com.pet.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.agent.config.LlmConfig;
import com.pet.agent.service.ChatConfigService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final LlmConfig config;
    private final ChatConfigService chatConfigService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LlmClient(LlmConfig config, ChatConfigService chatConfigService, ObjectMapper objectMapper) {
        this.config = config;
        this.chatConfigService = chatConfigService;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .build();
    }

    private String getApiUrl() {
        String dbValue = chatConfigService.getByKey("llm.api_url");
        String apiUrl = (dbValue != null && !dbValue.isEmpty()) ? dbValue : config.getApiUrl();
        return normalizeApiUrl(apiUrl);
    }

    private String normalizeApiUrl(String apiUrl) {
        if (apiUrl == null) {
            return "";
        }
        String trimmedApiUrl = apiUrl.trim();
        if (trimmedApiUrl.endsWith("/")) {
            trimmedApiUrl = trimmedApiUrl.substring(0, trimmedApiUrl.length() - 1);
        }
        if (trimmedApiUrl.endsWith("/anthropic")) {
            return trimmedApiUrl + "/v1/messages";
        }
        return trimmedApiUrl;
    }

    private boolean isAnthropicMessagesApi(String apiUrl) {
        return apiUrl != null && apiUrl.contains("/v1/messages");
    }

    private String getApiKey() {
        String dbValue = chatConfigService.getByKey("llm.api_key");
        return (dbValue != null && !dbValue.isEmpty()) ? dbValue : config.getApiKey();
    }

    public boolean isChatEnabled() {
        String dbValue = chatConfigService.getByKey("chat.enabled");
        if (dbValue != null && !dbValue.isEmpty()) {
            return "true".equalsIgnoreCase(dbValue);
        }
        return true;
    }

    private String getModel() {
        String dbValue = chatConfigService.getByKey("llm.model");
        return (dbValue != null && !dbValue.isEmpty()) ? dbValue : config.getModel();
    }

    private double getTemperature() {
        String dbValue = chatConfigService.getByKey("llm.temperature");
        if (dbValue != null && !dbValue.isEmpty()) {
            try { return Double.parseDouble(dbValue); } catch (NumberFormatException e) { /* fall through */ }
        }
        return config.getTemperature();
    }

    private int getMaxTokens() {
        String dbValue = chatConfigService.getByKey("llm.max_tokens");
        if (dbValue != null && !dbValue.isEmpty()) {
            try { return Integer.parseInt(dbValue); } catch (NumberFormatException e) { /* fall through */ }
        }
        return config.getMaxTokens();
    }

    /**
     * 单轮对话调用
     */
    public String chat(String systemPrompt, String userMessage) {
        return doChat(systemPrompt, null, userMessage);
    }

    /**
     * 带历史记录的多轮对话调用
     */
    public String chatWithHistory(String systemPrompt, List<Map<String, String>> history,
                                   String userMessage) {
        return doChat(systemPrompt, history, userMessage);
    }

    private String doChat(String systemPrompt, List<Map<String, String>> history,
                          String userMessage) {
        try {
            String apiUrl = getApiUrl();
            String requestBody = isAnthropicMessagesApi(apiUrl)
                    ? buildAnthropicRequestBody(systemPrompt, history, userMessage)
                    : buildOpenAiRequestBody(systemPrompt, history, userMessage);
            log.debug("LLM Request: {}", requestBody);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json");

            if (isAnthropicMessagesApi(apiUrl)) {
                requestBuilder.addHeader("x-api-key", getApiKey())
                        .addHeader("anthropic-version", "2023-06-01");
            } else {
                requestBuilder.addHeader("Authorization", "Bearer " + getApiKey());
            }

            Request request = requestBuilder
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                log.debug("LLM Response: {}", body);

                if (!response.isSuccessful()) {
                    log.error("LLM API error: {} {}", response.code(), body);
                    if (response.code() == 401 || response.code() == 403) {
                        return "AI服务认证失败，请管理员在「管理→客服设置」中检查并更新 API Key。";
                    }
                    return "抱歉，AI服务暂时不可用（HTTP " + response.code() + "），请稍后再试。";
                }

                return extractContent(body);
            }
        } catch (IOException e) {
            log.error("LLM call failed", e);
            return "抱歉，AI服务连接失败，请检查网络后重试。";
        }
    }

    private String buildOpenAiRequestBody(String systemPrompt, List<Map<String, String>> history,
                                          String userMessage) throws IOException {
        StringBuilder messages = new StringBuilder();
        messages.append("[");

        messages.append("{\"role\":\"system\",\"content\":")
                .append(objectMapper.writeValueAsString(systemPrompt))
                .append("}");

        if (history != null) {
            for (Map<String, String> msg : history) {
                messages.append(",");
                messages.append("{\"role\":\"")
                        .append(msg.get("role"))
                        .append("\",\"content\":")
                        .append(objectMapper.writeValueAsString(msg.get("content")))
                        .append("}");
            }
        }

        messages.append(",");
        messages.append("{\"role\":\"user\",\"content\":")
                .append(objectMapper.writeValueAsString(userMessage))
                .append("}");

        messages.append("]");

        return "{"
                + "\"model\":\"" + getModel() + "\","
                + "\"messages\":" + messages.toString() + ","
                + "\"temperature\":" + getTemperature() + ","
                + "\"max_tokens\":" + getMaxTokens()
                + "}";
    }

    private String buildAnthropicRequestBody(String systemPrompt, List<Map<String, String>> history,
                                             String userMessage) throws IOException {
        StringBuilder messages = new StringBuilder();
        messages.append("[");

        boolean hasMessages = false;
        if (history != null) {
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    continue;
                }
                if (hasMessages) {
                    messages.append(",");
                }
                messages.append("{\"role\":\"")
                        .append(role)
                        .append("\",\"content\":")
                        .append(objectMapper.writeValueAsString(msg.get("content")))
                        .append("}");
                hasMessages = true;
            }
        }

        if (hasMessages) {
            messages.append(",");
        }
        messages.append("{\"role\":\"user\",\"content\":")
                .append(objectMapper.writeValueAsString(userMessage))
                .append("}");
        messages.append("]");

        return "{"
                + "\"model\":\"" + getModel() + "\","
                + "\"system\":" + objectMapper.writeValueAsString(systemPrompt) + ","
                + "\"messages\":" + messages.toString() + ","
                + "\"temperature\":" + getTemperature() + ","
                + "\"max_tokens\":" + getMaxTokens()
                + "}";
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentBlocks = root.get("content");
        if (contentBlocks != null && contentBlocks.isArray() && contentBlocks.size() > 0) {
            StringBuilder textBuilder = new StringBuilder();
            for (JsonNode block : contentBlocks) {
                JsonNode type = block.get("type");
                JsonNode text = block.get("text");
                if (type != null && "text".equals(type.asText()) && text != null) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(text.asText());
                }
            }
            if (textBuilder.length() > 0) {
                return textBuilder.toString();
            }
        }

        JsonNode choices = root.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null) {
                    return content.asText();
                }
            }
        }
        log.warn("Unexpected LLM response format: {}", responseBody);
        return "抱歉，收到了无法解析的回复。";
    }
}
