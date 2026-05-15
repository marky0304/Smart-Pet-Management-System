package com.pet.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductReviewVO {

    private Long id;
    private Long productId;
    private Long orderId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer rating;
    private String content;
    private String images;
    private Boolean isAnonymous;
    private Integer likesCount;
    private LocalDateTime createTime;
}
