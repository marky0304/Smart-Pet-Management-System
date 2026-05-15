package com.pet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceRequest {
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String image;
    private Integer status;
}
