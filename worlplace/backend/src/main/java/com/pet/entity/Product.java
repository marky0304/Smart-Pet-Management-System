package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category; // FOOD-食品，TOY-玩具，SUPPLY-用品，MEDICINE-药品
    @TableField("sub_category")
    private String subCategory;
    private String description;
    private BigDecimal price;
    @TableField("original_price")
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    @TableField("pet_type")
    private String petType;
    private String brand;
    private String specifications;
    @TableField("image_url")
    private String image; // 保持原字段名，但映射到数据库的image_url字段
    private String images; // JSON数组
    private Integer status; // 0-下架，1-上架
    @TableField("is_hot")
    private Integer isHot;
    @TableField("is_new")
    private Integer isNew;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("avg_rating")
    private Double avgRating;
    @TableField("review_count")
    private Integer reviewCount;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
