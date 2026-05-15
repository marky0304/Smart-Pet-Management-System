package com.pet.dto;

import lombok.Data;

@Data
public class ProductQueryRequest {
    private String keyword;
    private String name;
    private String category;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 12;
    private String sortBy = "create_time"; // create_time, sales, price
    private String sortOrder = "DESC"; // ASC, DESC
}
