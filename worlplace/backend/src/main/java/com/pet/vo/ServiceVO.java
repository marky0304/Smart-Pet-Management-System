package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceVO {
    private Long id;
    private String name;
    private String category;
    private String categoryName;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String image;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
