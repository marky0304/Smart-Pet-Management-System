package com.pet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.common.result.Result;
import com.pet.entity.User;
import com.pet.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/follow")
@CrossOrigin(origins = "*")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{followeeId}")
    public Result<Map<String, Object>> follow(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean result = followService.follow(userId, followeeId);
        return result ? Result.success(Map.of("isFollowed", true, "message", "关注成功"))
                      : Result.error(400, "关注失败，已关注或不能关注自己");
    }

    @DeleteMapping("/{followeeId}")
    public Result<Map<String, Object>> unfollow(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean result = followService.unfollow(userId, followeeId);
        return result ? Result.success(Map.of("isFollowed", false, "message", "已取消关注"))
                      : Result.error(400, "取消关注失败");
    }

    @GetMapping("/check/{followeeId}")
    public Result<Map<String, Object>> checkFollow(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean isFollowing = followService.isFollowing(userId, followeeId);
        return Result.success(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/followers")
    public Result<IPage<User>> getFollowers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IPage<User> result = followService.getFollowers(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/following")
    public Result<IPage<User>> getFollowing(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IPage<User> result = followService.getFollowing(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Integer>> getFollowStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Map<String, Integer> stats = followService.getFollowStats(userId);
        return Result.success(stats);
    }
}
