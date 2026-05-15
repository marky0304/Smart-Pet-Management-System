package com.pet.dto;

import lombok.Data;

@Data
public class OrderQueryRequest {
    private String orderNo;
    private String status;
    private Long userId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
