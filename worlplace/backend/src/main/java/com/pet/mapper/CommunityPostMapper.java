package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社区动态Mapper接口
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {

    /**
     * 分页查询动态列表（包含用户信息）
     */
    @Select("SELECT p.*, u.username, u.nickname, u.avatar " +
            "FROM community_posts p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.is_top DESC, p.create_time DESC")
    IPage<CommunityPost> selectPostsWithUser(Page<CommunityPost> page);

    /**
     * 根据用户ID查询动态列表
     */
    @Select("SELECT p.*, u.username, u.nickname, u.avatar " +
            "FROM community_posts p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC")
    IPage<CommunityPost> selectPostsByUser(Page<CommunityPost> page, @Param("userId") Long userId);

    /**
     * 根据话题查询动态列表
     */
    @Select("SELECT p.*, u.username, u.nickname, u.avatar " +
            "FROM community_posts p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.topic_tags LIKE CONCAT('%', #{topic}, '%') AND p.status = 1 " +
            "ORDER BY p.is_top DESC, p.create_time DESC")
    IPage<CommunityPost> selectPostsByTopic(Page<CommunityPost> page, @Param("topic") String topic);

    /**
     * 增加点赞数
     */
    @Update("UPDATE community_posts SET likes_count = likes_count + 1 WHERE id = #{postId}")
    int increaseLikesCount(@Param("postId") Long postId);

    /**
     * 减少点赞数
     */
    @Update("UPDATE community_posts SET likes_count = likes_count - 1 WHERE id = #{postId} AND likes_count > 0")
    int decreaseLikesCount(@Param("postId") Long postId);

    /**
     * 增加评论数
     */
    @Update("UPDATE community_posts SET comments_count = comments_count + 1 WHERE id = #{postId}")
    int increaseCommentsCount(@Param("postId") Long postId);

    /**
     * 减少评论数
     */
    @Update("UPDATE community_posts SET comments_count = comments_count - 1 WHERE id = #{postId} AND comments_count > 0")
    int decreaseCommentsCount(@Param("postId") Long postId);

    /**
     * 获取热门动态
     */
    @Select("SELECT p.*, u.username, u.nickname, u.avatar " +
            "FROM community_posts p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.status = 1 " +
            "ORDER BY (p.likes_count * 2 + p.comments_count * 3) DESC, p.create_time DESC " +
            "LIMIT #{limit}")
    List<CommunityPost> selectHotPosts(@Param("limit") Integer limit);
}