package com.pet.dto;

import lombok.Data;

/**
 * 宠物查询请求
 */
@Data
public class PetQueryRequest {
    
    private String name;
    
    private String type;
    
    private String breed;
    
    private Integer status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
