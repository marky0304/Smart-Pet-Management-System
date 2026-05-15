package com.pet.agent.controller;

import com.pet.agent.service.ChatConfigService;
import com.pet.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/chat-config")
@CrossOrigin(origins = "*")
public class ChatConfigController {

    private final ChatConfigService chatConfigService;

    public ChatConfigController(ChatConfigService chatConfigService) {
        this.chatConfigService = chatConfigService;
    }

    @GetMapping
    public Result<Map<String, String>> getAll(HttpServletRequest req) {
        String userRole = (String) req.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "权限不足，仅管理员可访问");
        }
        return Result.success(chatConfigService.getAll());
    }

    @PutMapping
    public Result<Void> batchSave(@RequestBody Map<String, String> configs, HttpServletRequest req) {
        String userRole = (String) req.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "权限不足，仅管理员可访问");
        }
        chatConfigService.batchSave(configs);
        return Result.success();
    }
}
