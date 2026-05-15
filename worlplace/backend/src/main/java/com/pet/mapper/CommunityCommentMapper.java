package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社区评论Mapper接口
 */
@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {

    /**
     * 根据动态ID查询评论列表（包含用户信息）
     */
    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM community_comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 AND c.parent_id IS NULL " +
            "ORDER BY c.create_time ASC")
    IPage<CommunityComment> selectCommentsByPost(Page<CommunityComment> page, @Param("postId") Long postId);

    /**
     * 根据父评论ID查询回复列表
     */
    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM community_comments c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC")
    List<CommunityComment> selectRepliesByParent(@Param("parentId") Long parentId);

    /**
     * 增加点赞数
     */
    @Update("UPDATE community_comments SET likes_count = likes_count + 1 WHERE id = #{commentId}")
    int increaseLikesCount(@Param("commentId") Long commentId);

    /**
     * 减少点赞数
     */
    @Update("UPDATE community_comments SET likes_count = likes_count - 1 WHERE id = #{commentId} AND likes_count > 0")
    int decreaseLikesCount(@Param("commentId") Long commentId);

    /**
     * 根据动态ID统计评论数
     */
    @Select("SELECT COUNT(*) FROM community_comments WHERE post_id = #{postId} AND status = 1")
    int countByPostId(@Param("postId") Long postId);
}