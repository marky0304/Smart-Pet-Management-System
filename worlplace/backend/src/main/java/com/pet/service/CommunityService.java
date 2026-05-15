package com.pet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.entity.CommunityComment;
import com.pet.entity.CommunityPost;
import com.pet.entity.CommunityTopic;

import java.util.List;

/**
 * 社区服务接口
 */
public interface CommunityService {

    /**
     * 发布动态
     */
    CommunityPost publishPost(CommunityPost post, Long userId);

    /**
     * 分页查询动态列表
     */
    IPage<CommunityPost> getPostList(Integer page, Integer size, Long currentUserId);

    /**
     * 根据用户ID查询动态列表
     */
    IPage<CommunityPost> getPostsByUser(Integer page, Integer size, Long userId, Long currentUserId);

    /**
     * 根据话题查询动态列表
     */
    IPage<CommunityPost> getPostsByTopic(Integer page, Integer size, String topic, Long currentUserId);

    /**
     * 获取动态详情
     */
    CommunityPost getPostDetail(Long postId, Long currentUserId);

    /**
     * 删除动态
     */
    boolean deletePost(Long postId, Long userId);

    /**
     * 点赞/取消点赞动态
     */
    boolean togglePostLike(Long postId, Long userId);

    /**
     * 添加评论
     */
    CommunityComment addComment(Long postId, String content, Long parentId, Long userId);

    /**
     * 获取评论列表
     */
    IPage<CommunityComment> getCommentList(Long postId, Integer page, Integer size, Long currentUserId);

    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 点赞/取消点赞评论
     */
    boolean toggleCommentLike(Long commentId, Long userId);

    /**
     * 获取热门话题
     */
    List<CommunityTopic> getHotTopics(Integer limit);

    /**
     * 搜索话题
     */
    List<CommunityTopic> searchTopics(String keyword);

    /**
     * 获取热门动态
     */
    List<CommunityPost> getHotPosts(Integer limit, Long currentUserId);

    /**
     * 获取推荐用户
     */
    List<Object> getRecommendUsers(Long currentUserId, Integer limit);
}