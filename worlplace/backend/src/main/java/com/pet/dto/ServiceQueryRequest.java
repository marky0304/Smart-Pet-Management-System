package com.pet.dto;

import lombok.Data;

@Data
public class ServiceQueryRequest {
    private String category;
    private String name;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
