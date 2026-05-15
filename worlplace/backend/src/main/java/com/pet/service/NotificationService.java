package com.pet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.entity.Notification;

public interface NotificationService {

    IPage<Notification> getList(Long userId, Integer page, Integer size);

    Integer getUnreadCount(Long userId);

    boolean markAsRead(Long id, Long userId);

    boolean markAllAsRead(Long userId);

    void push(Long userId, Long fromUserId, String fromUsername,
              String type, String targetType, Long targetId, String content);
}
