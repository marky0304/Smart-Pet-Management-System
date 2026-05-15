package com.pet.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;

/**
 * 更新用户信息请求
 */
@Data
public class UpdateUserRequest {
    
    private String nickname;
    
    private String avatar;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确")
    private String email;
    
    private Integer gender;
}
