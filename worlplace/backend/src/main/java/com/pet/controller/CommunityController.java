package com.pet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.common.result.Result;
import com.pet.entity.CommunityComment;
import com.pet.entity.CommunityPost;
import com.pet.entity.CommunityTopic;
import com.pet.service.CommunityService;
import com.pet.service.impl.CommunityServiceImpl;
import com.pet.vo.CommunityCommentVO;
import com.pet.vo.CommunityPostVO;
import com.pet.vo.CommunityTopicVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private CommunityServiceImpl communityServiceImpl;

    @PostMapping("/posts")
    public Result<CommunityPostVO> publishPost(@RequestBody CommunityPost post, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        CommunityPost result = communityService.publishPost(post, userId);
        return Result.success(communityServiceImpl.toPostVO(result));
    }

    @GetMapping("/posts")
    public Result<IPage<CommunityPostVO>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String topic,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");

        IPage<CommunityPost> result;
        if (userId != null) {
            result = communityService.getPostsByUser(page, size, userId, currentUserId);
        } else if (topic != null && !topic.isEmpty()) {
            result = communityService.getPostsByTopic(page, size, topic, currentUserId);
        } else {
            result = communityService.getPostList(page, size, currentUserId);
        }

        IPage<CommunityPostVO> voPage = result.convert(communityServiceImpl::toPostVO);
        return Result.success(voPage);
    }

    @GetMapping("/posts/{postId}")
    public Result<CommunityPostVO> getPostDetail(@PathVariable Long postId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        CommunityPost post = communityService.getPostDetail(postId, currentUserId);

        if (post == null) {
            return Result.error(404, "动态不存在");
        }

        return Result.success(communityServiceImpl.toPostVO(post));
    }

    @DeleteMapping("/posts/{postId}")
    public Result<String> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean success = communityService.deletePost(postId, userId);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(403, "无权限删除或动态不存在");
        }
    }

    @PostMapping("/posts/{postId}/like")
    public Result<Map<String, Object>> togglePostLike(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean isLiked = communityService.togglePostLike(postId, userId);

        Map<String, Object> result = Map.of(
            "isLiked", isLiked,
            "message", isLiked ? "点赞成功" : "取消点赞"
        );

        return Result.success(result);
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<CommunityCommentVO> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String content = (String) params.get("content");
        Long parentId = params.get("parentId") != null ?
            Long.valueOf(params.get("parentId").toString()) : null;

        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }

        CommunityComment comment = communityService.addComment(postId, content, parentId, userId);
        return Result.success(communityServiceImpl.toCommentVO(comment));
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<IPage<CommunityCommentVO>> getCommentList(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        IPage<CommunityComment> result = communityService.getCommentList(postId, page, size, currentUserId);
        IPage<CommunityCommentVO> voPage = result.convert(communityServiceImpl::toCommentVO);
        return Result.success(voPage);
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<String> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean success = communityService.deleteComment(commentId, userId);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(403, "无权限删除或评论不存在");
        }
    }

    @PostMapping("/comments/{commentId}/like")
    public Result<Map<String, Object>> toggleCommentLike(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean isLiked = communityService.toggleCommentLike(commentId, userId);

        Map<String, Object> result = Map.of(
            "isLiked", isLiked,
            "message", isLiked ? "点赞成功" : "取消点赞"
        );

        return Result.success(result);
    }

    @GetMapping("/topics/hot")
    public Result<List<CommunityTopicVO>> getHotTopics(@RequestParam(defaultValue = "10") Integer limit) {
        List<CommunityTopic> topics = communityService.getHotTopics(limit);
        List<CommunityTopicVO> vos = topics.stream()
                .map(communityServiceImpl::toTopicVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @GetMapping("/topics/search")
    public Result<List<CommunityTopicVO>> searchTopics(@RequestParam String keyword) {
        List<CommunityTopic> topics = communityService.searchTopics(keyword);
        List<CommunityTopicVO> vos = topics.stream()
                .map(communityServiceImpl::toTopicVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @GetMapping("/posts/hot")
    public Result<List<CommunityPostVO>> getHotPosts(
            @RequestParam(defaultValue = "5") Integer limit,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        List<CommunityPost> posts = communityService.getHotPosts(limit, currentUserId);
        List<CommunityPostVO> vos = posts.stream()
                .map(communityServiceImpl::toPostVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @GetMapping("/users/recommend")
    public Result<List<Object>> getRecommendUsers(
            @RequestParam(defaultValue = "5") Integer limit,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        List<Object> users = communityService.getRecommendUsers(currentUserId, limit);
        return Result.success(users);
    }
}
