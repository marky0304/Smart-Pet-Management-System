package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社区动态实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_posts")
public class CommunityPost {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发布用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 动态内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片列表（JSON格式字符串）
     */
    @TableField("images")
    private String images;

    /**
     * 话题标签，逗号分隔
     */
    @TableField("topic_tags")
    private String topicTags;

    /**
     * 位置信息
     */
    @TableField("location")
    private String location;

    /**
     * 点赞数
     */
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 评论数
     */
    @TableField("comments_count")
    private Integer commentsCount;

    /**
     * 分享数
     */
    @TableField("shares_count")
    private Integer sharesCount;

    /**
     * 是否置顶：1-置顶，0-普通
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 状态：1-正常，0-删除，2-审核中
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
     * 发布用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;

    /**
     * 是否为当前用户发布
     */
    @TableField(exist = false)
    private Boolean isMine;
}