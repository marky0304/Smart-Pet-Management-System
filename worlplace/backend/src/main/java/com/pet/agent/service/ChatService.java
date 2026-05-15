package com.pet.agent.service;

import com.pet.agent.vo.ChatResponse;
import java.util.List;

public interface ChatService {

    ChatResponse sendMessage(Long userId, String username, String message, String sessionId, String currentPage);

    List<ChatResponse> getHistory(Long userId, String sessionId);
}
