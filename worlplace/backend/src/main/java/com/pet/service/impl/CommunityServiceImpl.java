package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.entity.*;
import com.pet.mapper.*;
import com.pet.service.CommunityService;
import com.pet.service.FollowService;
import com.pet.service.NotificationService;
import com.pet.service.UserService;
import com.pet.vo.CommunityCommentVO;
import com.pet.vo.CommunityPostVO;
import com.pet.vo.CommunityTopicVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityPostMapper postMapper;

    @Autowired
    private CommunityCommentMapper commentMapper;

    @Autowired
    private CommunityLikeMapper likeMapper;

    @Autowired
    private CommunityTopicMapper topicMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowService followService;

    @Override
    @Transactional
    public CommunityPost publishPost(CommunityPost post, Long userId) {
        post.setUserId(userId);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setSharesCount(0);
        post.setIsTop(0);
        post.setStatus(1);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        
        postMapper.insert(post);

        // 丰富返回数据
        enrichPostInfo(post, userId);

        // 更新话题动态数（如果有话题标签）
        if (post.getTopicTags() != null && !post.getTopicTags().isEmpty()) {
            String[] topics = post.getTopicTags().split(",");
            for (String topic : topics) {
                topic = topic.trim();
                if (!topic.isEmpty()) {
                    // 查找或创建话题
                    QueryWrapper<CommunityTopic> wrapper = new QueryWrapper<>();
                    wrapper.eq("name", topic);
                    CommunityTopic existingTopic = topicMapper.selectOne(wrapper);
                    
                    if (existingTopic != null) {
                        topicMapper.increasePostsCount(existingTopic.getId());
                    } else {
                        // 创建新话题
                        CommunityTopic newTopic = new CommunityTopic();
                        newTopic.setName(topic);
                        newTopic.setDescription("关于" + topic + "的讨论");
                        newTopic.setPostsCount(1);
                        newTopic.setFollowersCount(0);
                        newTopic.setIsHot(0);
                        newTopic.setStatus(1);
                        newTopic.setCreateTime(LocalDateTime.now());
                        newTopic.setUpdateTime(LocalDateTime.now());
                        topicMapper.insert(newTopic);
                    }
                }
            }
        }
        
        return post;
    }

    @Override
    public IPage<CommunityPost> getPostList(Integer page, Integer size, Long currentUserId) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        IPage<CommunityPost> result = postMapper.selectPostsWithUser(pageParam);
        
        // 设置额外信息
        for (CommunityPost post : result.getRecords()) {
            enrichPostInfo(post, currentUserId);
        }
        
        return result;
    }

    @Override
    public IPage<CommunityPost> getPostsByUser(Integer page, Integer size, Long userId, Long currentUserId) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        IPage<CommunityPost> result = postMapper.selectPostsByUser(pageParam, userId);
        
        for (CommunityPost post : result.getRecords()) {
            enrichPostInfo(post, currentUserId);
        }
        
        return result;
    }

    @Override
    public IPage<CommunityPost> getPostsByTopic(Integer page, Integer size, String topic, Long currentUserId) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        IPage<CommunityPost> result = postMapper.selectPostsByTopic(pageParam, topic);
        
        for (CommunityPost post : result.getRecords()) {
            enrichPostInfo(post, currentUserId);
        }
        
        return result;
    }

    @Override
    public CommunityPost getPostDetail(Long postId, Long currentUserId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post != null && post.getStatus() == 1) {
            enrichPostInfo(post, currentUserId);
            return post;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post != null && post.getUserId().equals(userId)) {
            post.setStatus(0);
            post.setUpdateTime(LocalDateTime.now());
            return postMapper.updateById(post) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean togglePostLike(Long postId, Long userId) {
        // 检查是否已点赞
        boolean isLiked = likeMapper.checkUserLiked(userId, CommunityLike.TargetType.POST.getCode(), postId);
        
        if (isLiked) {
            // 取消点赞
            likeMapper.deleteLike(userId, CommunityLike.TargetType.POST.getCode(), postId);
            postMapper.decreaseLikesCount(postId);
            return false;
        } else {
            // 添加点赞
            CommunityLike like = new CommunityLike();
            like.setUserId(userId);
            like.setTargetType(CommunityLike.TargetType.POST.getCode());
            like.setTargetId(postId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            postMapper.increaseLikesCount(postId);
            // 推送点赞通知
            CommunityPost likedPost = postMapper.selectById(postId);
            if (likedPost != null) {
                User fromUser = userService.getById(userId);
                String fromUsername = fromUser != null ? fromUser.getUsername() : "宠友";
                notificationService.push(likedPost.getUserId(), userId, fromUsername,
                    Notification.NotifyType.LIKE.getCode(),
                    Notification.TargetType.POST.getCode(), postId,
                    "赞了你的动态");
            }
            return true;
        }
    }

    @Override
    @Transactional
    public CommunityComment addComment(Long postId, String content, Long parentId, Long userId) {
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setLikesCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);

        // 丰富返回数据
        enrichCommentInfo(comment, userId);

        // 增加动态评论数
        postMapper.increaseCommentsCount(postId);

        // 推送评论通知
        User fromUser = userService.getById(userId);
        String fromUsername = fromUser != null ? fromUser.getUsername() : "宠友";
        CommunityPost targetPost = postMapper.selectById(postId);
        if (targetPost != null) {
            String notifyContent = parentId != null ? "回复了你的评论" : "评论了你的动态";
            notificationService.push(targetPost.getUserId(), userId, fromUsername,
                Notification.NotifyType.COMMENT.getCode(),
                Notification.TargetType.POST.getCode(), postId,
                notifyContent);
        }
        if (parentId != null) {
            CommunityComment parentComment = commentMapper.selectById(parentId);
            if (parentComment != null) {
                notificationService.push(parentComment.getUserId(), userId, fromUsername,
                    Notification.NotifyType.COMMENT.getCode(),
                    Notification.TargetType.COMMENT.getCode(), parentId,
                    "回复了你的评论");
            }
        }

        return comment;
    }

    @Override
    public IPage<CommunityComment> getCommentList(Long postId, Integer page, Integer size, Long currentUserId) {
        Page<CommunityComment> pageParam = new Page<>(page, size);
        IPage<CommunityComment> result = commentMapper.selectCommentsByPost(pageParam, postId);
        
        for (CommunityComment comment : result.getRecords()) {
            enrichCommentInfo(comment, currentUserId);
            
            // 获取回复列表
            List<CommunityComment> replies = commentMapper.selectRepliesByParent(comment.getId());
            for (CommunityComment reply : replies) {
                enrichCommentInfo(reply, currentUserId);
            }
            comment.setReplies(replies);
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment != null && comment.getUserId().equals(userId)) {
            comment.setStatus(0);
            comment.setUpdateTime(LocalDateTime.now());
            commentMapper.updateById(comment);
            
            // 减少动态评论数
            postMapper.decreaseCommentsCount(comment.getPostId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        boolean isLiked = likeMapper.checkUserLiked(userId, CommunityLike.TargetType.COMMENT.getCode(), commentId);
        
        if (isLiked) {
            likeMapper.deleteLike(userId, CommunityLike.TargetType.COMMENT.getCode(), commentId);
            commentMapper.decreaseLikesCount(commentId);
            return false;
        } else {
            CommunityLike like = new CommunityLike();
            like.setUserId(userId);
            like.setTargetType(CommunityLike.TargetType.COMMENT.getCode());
            like.setTargetId(commentId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            commentMapper.increaseLikesCount(commentId);
            // 推送点赞通知
            CommunityComment likedComment = commentMapper.selectById(commentId);
            if (likedComment != null) {
                User fromUser = userService.getById(userId);
                String fromUsername = fromUser != null ? fromUser.getUsername() : "宠友";
                notificationService.push(likedComment.getUserId(), userId, fromUsername,
                    Notification.NotifyType.LIKE.getCode(),
                    Notification.TargetType.COMMENT.getCode(), commentId,
                    "赞了你的评论");
            }
            return true;
        }
    }

    @Override
    public List<CommunityTopic> getHotTopics(Integer limit) {
        return topicMapper.selectHotTopics(limit);
    }

    @Override
    public List<CommunityTopic> searchTopics(String keyword) {
        return topicMapper.searchTopicsByName(keyword);
    }

    @Override
    public List<CommunityPost> getHotPosts(Integer limit, Long currentUserId) {
        List<CommunityPost> posts = postMapper.selectHotPosts(limit);
        for (CommunityPost post : posts) {
            enrichPostInfo(post, currentUserId);
        }
        return posts;
    }

    @Override
    public List<Object> getRecommendUsers(Long currentUserId, Integer limit) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.ne("id", currentUserId)
               .eq("status", 1)
               .orderByDesc("create_time")
               .last("LIMIT " + (limit * 2)); // fetch extra to account for filtering

        List<User> users = userService.list(wrapper);
        List<Object> result = new ArrayList<>();

        for (User user : users) {
            if (result.size() >= limit) break;
            if (followService.isFollowing(currentUserId, user.getId())) continue;

            Object userInfo = new Object() {
                public Long id = user.getId();
                public String username = user.getUsername();
                public String nickname = user.getNickname();
                public String avatar = user.getAvatar();
                public String desc = "新用户";
            };
            result.add(userInfo);
        }

        return result;
    }

    /**
     * 丰富动态信息
     */
    private void enrichPostInfo(CommunityPost post, Long currentUserId) {
        // 设置用户信息
        if (post.getUserId() != null) {
            User user = userService.getById(post.getUserId());
            post.setUser(user);
        }
        
        // 设置是否已点赞
        if (currentUserId != null) {
            boolean isLiked = likeMapper.checkUserLiked(currentUserId, CommunityLike.TargetType.POST.getCode(), post.getId());
            post.setIsLiked(isLiked);
            
            // 设置是否为当前用户发布
            post.setIsMine(post.getUserId().equals(currentUserId));
        } else {
            post.setIsLiked(false);
            post.setIsMine(false);
        }
        
        // 格式化时间显示
        // 这里可以添加时间格式化逻辑
    }

    /**
     * 丰富评论信息
     */
    private void enrichCommentInfo(CommunityComment comment, Long currentUserId) {
        // 设置用户信息
        if (comment.getUserId() != null) {
            User user = userService.getById(comment.getUserId());
            comment.setUser(user);
        }

        // 设置是否已点赞
        if (currentUserId != null) {
            boolean isLiked = likeMapper.checkUserLiked(currentUserId, CommunityLike.TargetType.COMMENT.getCode(), comment.getId());
            comment.setIsLiked(isLiked);
        } else {
            comment.setIsLiked(false);
        }
    }

    // ── VO 转换方法 ──────────────────────────────────────────

    /**
     * Entity → VO 转换（帖子）
     */
    public CommunityPostVO toPostVO(CommunityPost post) {
        if (post == null) return null;
        CommunityPostVO vo = new CommunityPostVO();
        BeanUtils.copyProperties(post, vo, "isTop");
        vo.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
        if (post.getUser() != null) {
            vo.setUsername(post.getUser().getUsername());
            vo.setUserAvatar(post.getUser().getAvatar());
        }
        return vo;
    }

    /**
     * Entity → VO 转换（评论，含嵌套回复）
     */
    public CommunityCommentVO toCommentVO(CommunityComment comment) {
        if (comment == null) return null;
        CommunityCommentVO vo = new CommunityCommentVO();
        BeanUtils.copyProperties(comment, vo, "replies");
        if (comment.getUser() != null) {
            vo.setUsername(comment.getUser().getUsername());
            vo.setUserAvatar(comment.getUser().getAvatar());
        }
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommunityCommentVO> replyVOs = comment.getReplies().stream()
                    .map(this::toCommentVO)
                    .collect(Collectors.toList());
            vo.setReplies(replyVOs);
        }
        return vo;
    }

    /**
     * Entity → VO 转换（话题）
     */
    public CommunityTopicVO toTopicVO(CommunityTopic topic) {
        if (topic == null) return null;
        CommunityTopicVO vo = new CommunityTopicVO();
        BeanUtils.copyProperties(topic, vo, "isHot");
        vo.setIsHot(topic.getIsHot() != null && topic.getIsHot() == 1);
        vo.setIsFollowed(topic.getIsFollowed());
        return vo;
    }
}