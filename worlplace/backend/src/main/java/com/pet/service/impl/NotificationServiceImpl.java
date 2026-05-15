package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.entity.Notification;
import com.pet.mapper.NotificationMapper;
import com.pet.service.NotificationService;
import com.pet.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public IPage<Notification> getList(Long userId, Integer page, Integer size) {
        Page<Notification> pageParam = new Page<>(page, size);
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long id, Long userId) {
        return notificationMapper.markAsRead(id, userId) > 0;
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId) > 0;
    }

    @Override
    @Transactional
    public void push(Long userId, Long fromUserId, String fromUsername,
                     String type, String targetType, Long targetId, String content) {
        if (userId.equals(fromUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setFromUsername(fromUsername);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());

        notificationMapper.insert(notification);

        NotificationWebSocketHandler.sendNotification(userId, type, content);
    }
}
