package com.pet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录背景图片实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("login_backgrounds")
public class LoginBackground {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 图片名称
     */
    @TableField("image_name")
    private String imageName;

    /**
     * 图片描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否启用：1-启用，0-禁用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
}