package com.pet.agent.service.impl;

import com.pet.agent.core.AgentOrchestrator;
import com.pet.agent.entity.ChatMessage;
import com.pet.agent.llm.LlmClient;
import com.pet.agent.mapper.ChatMessageMapper;
import com.pet.agent.service.ChatService;
import com.pet.agent.vo.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final AgentOrchestrator orchestrator;
    private final ChatMessageMapper chatMessageMapper;
    private final LlmClient llmClient;

    public ChatServiceImpl(AgentOrchestrator orchestrator, ChatMessageMapper chatMessageMapper,
                           LlmClient llmClient) {
        this.orchestrator = orchestrator;
        this.chatMessageMapper = chatMessageMapper;
        this.llmClient = llmClient;
    }

    @Override
    public ChatResponse sendMessage(Long userId, String username, String message, String sessionId, String currentPage) {
        if (!llmClient.isChatEnabled()) {
            ChatResponse response = new ChatResponse();
            response.setReply("智能客服功能当前已关闭，请联系管理员开启。");
            response.setOperation("CHAT");
            response.setDomain("GENERAL");
            response.setAgentType("SYSTEM");
            response.setSessionId(sessionId != null ? sessionId : "");
            return response;
        }

        String sid = (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString().substring(0, 8);

        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserId(userId);
        userMsg.setSessionId(sid);
        userMsg.setRole("USER");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(userMsg);

        List<Map<String, String>> history = loadRecentHistory(userId, sid);

        AgentOrchestrator.OrchestratorResult result = orchestrator.process(userId, username, message, history, currentPage);

        String cleanReply = stripMarkdown(result.getReply());

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setUserId(userId);
        assistantMsg.setSessionId(sid);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(cleanReply);
        assistantMsg.setOperation(result.getOperation());
        assistantMsg.setDomain(result.getDomain());
        assistantMsg.setAgentType(result.getAgentName());
        assistantMsg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(assistantMsg);

        ChatResponse response = new ChatResponse();
        response.setReply(cleanReply);
        response.setOperation(result.getOperation());
        response.setDomain(result.getDomain());
        response.setAgentType(result.getAgentName());
        response.setSessionId(sid);
        response.setNavigate(result.getNavigate());
        response.setNavigateParams(result.getNavigateParams());
        response.setAction(result.getAction());
        return response;
    }

    private String stripMarkdown(String text) {
        if (text == null) return "";
        return text
            .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
            .replaceAll("\\*(.+?)\\*", "$1")
            .replaceAll("__([^_]+)__", "$1")
            .replaceAll("_([^_]+)_", "$1")
            .replaceAll("`([^`]+)`", "$1")
            .replaceAll("~~(.+?)~~", "$1")
            .replaceAll("^#{1,6}\\s+", "")
            .replaceAll("^>\\s+", "")
            .replaceAll("^[*-]\\s+", "")
            .replaceAll("^\\d+\\.\\s+", "")
            .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
            .replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "$1")
            .replaceAll("\\n{3,}", "\n")
            .trim();
    }

    private List<Map<String, String>> loadRecentHistory(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Collections.emptyList();
        }
        List<ChatMessage> messages = chatMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreateTime)
                        .last("LIMIT 20"));
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.reverse(messages);
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, String> entry = new HashMap<>();
            entry.put("role", msg.getRole().toLowerCase());
            entry.put("content", msg.getContent());
            history.add(entry);
        }
        return history;
    }

    @Override
    public List<ChatResponse> getHistory(Long userId, String sessionId) {
        List<ChatMessage> messages = chatMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, userId)
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));

        return messages.stream().map(m -> {
            ChatResponse r = new ChatResponse();
            r.setReply(m.getContent());
            r.setOperation(m.getOperation());
            r.setDomain(m.getDomain());
            r.setAgentType(m.getAgentType());
            r.setSessionId(m.getSessionId());
            return r;
        }).collect(Collectors.toList());
    }
}
