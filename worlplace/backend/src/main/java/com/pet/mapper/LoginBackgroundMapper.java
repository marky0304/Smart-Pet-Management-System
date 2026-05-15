package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.LoginBackground;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 登录背景图片Mapper接口
 */
@Mapper
public interface LoginBackgroundMapper extends BaseMapper<LoginBackground> {

    /**
     * 获取所有启用的背景图片，按排序顺序
     */
    @Select("SELECT * FROM login_backgrounds WHERE is_active = 1 ORDER BY sort_order ASC")
    List<LoginBackground> selectActiveBackgrounds();

    /**
     * 获取所有背景图片，按排序顺序
     */
    @Select("SELECT * FROM login_backgrounds ORDER BY sort_order ASC")
    List<LoginBackground> selectAllBackgrounds();
}