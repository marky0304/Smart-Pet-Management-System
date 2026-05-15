package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区评论实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_comments")
public class CommunityComment {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 动态ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID（回复功能）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数
     */
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 状态：1-正常，0-删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    /**
     * 评论用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 父评论信息
     */
    @TableField(exist = false)
    private CommunityComment parentComment;

    /**
     * 子评论列表
     */
    @TableField(exist = false)
    private List<CommunityComment> replies;

    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;
}