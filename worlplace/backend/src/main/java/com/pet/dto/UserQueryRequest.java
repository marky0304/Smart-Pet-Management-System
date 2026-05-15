package com.pet.dto;

import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest {
    
    private String username;
    
    private String nickname;
    
    private String phone;
    
    private String role;
    
    private Integer status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
