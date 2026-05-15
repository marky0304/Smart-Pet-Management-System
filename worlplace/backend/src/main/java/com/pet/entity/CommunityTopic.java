package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社区话题实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_topics")
public class CommunityTopic {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 话题名称
     */
    @TableField("name")
    private String name;

    /**
     * 话题描述
     */
    @TableField("description")
    private String description;

    /**
     * 话题封面图
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 动态数量
     */
    @TableField("posts_count")
    private Integer postsCount;

    /**
     * 关注数量
     */
    @TableField("followers_count")
    private Integer followersCount;

    /**
     * 是否热门：1-热门，0-普通
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * 状态：1-正常，0-禁用
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
     * 当前用户是否已关注
     */
    @TableField(exist = false)
    private Boolean isFollowed;
}