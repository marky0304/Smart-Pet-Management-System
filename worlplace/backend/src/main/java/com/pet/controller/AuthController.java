package com.pet.controller;

import com.pet.common.result.Result;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.dto.ResetPasswordRequest;
import com.pet.dto.UpdatePasswordRequest;
import com.pet.service.UserService;
import com.pet.vo.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
    
    /**
     * 发送重置密码验证码
     */
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String account) {
        String code = userService.sendVerifyCode(account);
        // 生产环境不返回验证码，这里开发环境返回方便测试
        return Result.success("验证码已发送（开发环境）", code);
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPassword")
    public Result<String> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }

    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@Validated @RequestBody UpdatePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        userService.updatePassword(userId, request);
        return Result.success("密码修改成功", null);
    }
}
