package com.pet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.entity.LoginBackground;

import java.util.List;

/**
 * 登录背景图片服务接口
 */
public interface LoginBackgroundService extends IService<LoginBackground> {

    /**
     * 获取所有启用的背景图片
     */
    List<LoginBackground> getActiveBackgrounds();

    /**
     * 获取所有背景图片
     */
    List<LoginBackground> getAllBackgrounds();

    /**
     * 切换背景图片启用状态
     */
    LoginBackground toggleStatus(Long id);
}