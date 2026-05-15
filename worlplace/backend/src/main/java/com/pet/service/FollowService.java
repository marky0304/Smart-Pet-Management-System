package com.pet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.entity.User;

import java.util.Map;

public interface FollowService {

    boolean follow(Long followerId, Long followeeId);

    boolean unfollow(Long followerId, Long followeeId);

    boolean isFollowing(Long followerId, Long followeeId);

    IPage<User> getFollowers(Long userId, Integer page, Integer size);

    IPage<User> getFollowing(Long userId, Integer page, Integer size);

    Map<String, Integer> getFollowStats(Long userId);
}
