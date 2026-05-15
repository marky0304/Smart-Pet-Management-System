package com.pet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.dto.*;
import com.pet.entity.User;
import com.pet.vo.LoginResponse;
import com.pet.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    void register(RegisterRequest request);
    
    /**
     * 用户登录（支持用户名/手机号/邮箱）
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UpdateUserRequest request);
    
    /**
     * 修改密码
     */
    void updatePassword(Long userId, UpdatePasswordRequest request);
    
    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * 发送验证码（存入Redis，返回验证码供开发环境展示）
     */
    String sendVerifyCode(String account);
    
    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);
    
    /**
     * 分页查询用户列表（管理员）
     */
    IPage<UserVO> getUserList(UserQueryRequest request);
    
    /**
     * 更新用户状态（管理员）
     */
    void updateUserStatus(Long userId, Integer status);
    
    /**
     * 删除用户（管理员）
     */
    void deleteUser(Long userId);
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats(Long userId);
}
