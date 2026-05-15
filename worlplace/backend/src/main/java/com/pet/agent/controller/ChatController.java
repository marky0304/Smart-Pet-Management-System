package com.pet.agent.controller;

import com.pet.agent.dto.ChatRequest;
import com.pet.agent.service.ChatService;
import com.pet.agent.vo.ChatResponse;
import com.pet.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public Result<ChatResponse> send(@RequestBody ChatRequest request, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        String username = (String) req.getAttribute("username");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        ChatResponse response = chatService.sendMessage(userId, username,
                request.getMessage(), request.getSessionId(), request.getCurrentPage());
        return Result.success(response);
    }

    @GetMapping("/history")
    public Result<List<ChatResponse>> history(@RequestParam String sessionId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<ChatResponse> history = chatService.getHistory(userId, sessionId);
        return Result.success(history);
    }
}
