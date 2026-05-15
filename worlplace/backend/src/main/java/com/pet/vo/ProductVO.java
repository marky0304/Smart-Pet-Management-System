package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String category;
    private String categoryName;
    private String subCategory;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String petType;
    private String brand;
    private String specifications;
    private String image;
    private String images;
    private Integer status;
    private String statusName;
    private Integer isHot;
    private Integer isNew;
    private Integer sortOrder;
    private Double avgRating;
    private Integer reviewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
