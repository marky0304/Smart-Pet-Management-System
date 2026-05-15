package com.pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.entity.LoginBackground;
import com.pet.mapper.LoginBackgroundMapper;
import com.pet.service.LoginBackgroundService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录背景图片服务实现类
 */
@Service
public class LoginBackgroundServiceImpl extends ServiceImpl<LoginBackgroundMapper, LoginBackground> implements LoginBackgroundService {

    @Override
    public List<LoginBackground> getActiveBackgrounds() {
        return baseMapper.selectActiveBackgrounds();
    }

    @Override
    public List<LoginBackground> getAllBackgrounds() {
        return baseMapper.selectAllBackgrounds();
    }

    @Override
    public LoginBackground toggleStatus(Long id) {
        LoginBackground background = getById(id);
        if (background != null) {
            // 切换状态：1变0，0变1
            background.setIsActive(background.getIsActive() == 1 ? 0 : 1);
            updateById(background);
        }
        return background;
    }
}