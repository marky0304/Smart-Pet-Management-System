package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_reviews")
public class ProductReview {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("user_avatar")
    private String userAvatar;

    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField("images")
    private String images;

    @TableField("is_anonymous")
    private Integer isAnonymous;

    @TableField("likes_count")
    private Integer likesCount;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
