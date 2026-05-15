package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.entity.Notification;
import com.pet.entity.User;
import com.pet.entity.UserFollow;
import com.pet.mapper.UserFollowMapper;
import com.pet.service.FollowService;
import com.pet.service.NotificationService;
import com.pet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public boolean follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            return false;
        }
        if (userFollowMapper.isFollowing(followerId, followeeId) > 0) {
            return false;
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setCreateTime(LocalDateTime.now());
        userFollowMapper.insert(follow);

        User fromUser = userService.getById(followerId);
        String fromUsername = fromUser != null ? fromUser.getUsername() : "宠友";
        notificationService.push(followeeId, followerId, fromUsername,
            Notification.NotifyType.FOLLOW.getCode(), null, null,
            "关注了你");

        return true;
    }

    @Override
    @Transactional
    public boolean unfollow(Long followerId, Long followeeId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId)
               .eq("followee_id", followeeId);
        return userFollowMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        return userFollowMapper.isFollowing(followerId, followeeId) > 0;
    }

    @Override
    public IPage<User> getFollowers(Long userId, Integer page, Integer size) {
        List<UserFollow> follows = userFollowMapper.selectFollowers(userId);
        List<Long> followerIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        if (followerIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("id", followerIds);
        return userService.page(pageParam, wrapper);
    }

    @Override
    public IPage<User> getFollowing(Long userId, Integer page, Integer size) {
        List<UserFollow> follows = userFollowMapper.selectFollowing(userId);
        List<Long> followeeIds = follows.stream()
                .map(UserFollow::getFolloweeId)
                .collect(Collectors.toList());

        if (followeeIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("id", followeeIds);
        return userService.page(pageParam, wrapper);
    }

    @Override
    public Map<String, Integer> getFollowStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("followingCount", userFollowMapper.countFollowing(userId));
        stats.put("followersCount", userFollowMapper.countFollowers(userId));
        return stats;
    }
}
