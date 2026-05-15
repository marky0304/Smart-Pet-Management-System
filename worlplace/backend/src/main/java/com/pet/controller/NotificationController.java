package com.pet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.common.result.Result;
import com.pet.entity.Notification;
import com.pet.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/notification")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<IPage<Notification>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IPage<Notification> result = notificationService.getList(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(Map.of("unreadCount", count));
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean success = notificationService.markAsRead(id, userId);
        return success ? Result.success("已读", null) : Result.error(403, "操作失败");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        notificationService.markAllAsRead(userId);
        return Result.success("全部已读", null);
    }
}
