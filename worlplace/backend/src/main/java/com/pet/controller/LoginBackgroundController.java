package com.pet.controller;

import com.pet.entity.LoginBackground;
import com.pet.service.LoginBackgroundService;
import com.pet.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录背景图片控制器
 */
@RestController
@RequestMapping("/login-backgrounds")
@CrossOrigin(origins = "*")
public class LoginBackgroundController {

    @Autowired
    private LoginBackgroundService loginBackgroundService;

    /**
     * 获取所有启用的背景图片
     */
    @GetMapping("/active")
    public Result<List<LoginBackground>> getActiveBackgrounds() {
        List<LoginBackground> backgrounds = loginBackgroundService.getActiveBackgrounds();
        return Result.success(backgrounds);
    }

    /**
     * 获取所有背景图片（管理员用）
     */
    @GetMapping("/all")
    public Result<List<LoginBackground>> getAllBackgrounds() {
        List<LoginBackground> backgrounds = loginBackgroundService.getAllBackgrounds();
        return Result.success(backgrounds);
    }

    /**
     * 根据ID获取背景图片
     */
    @GetMapping("/{id}")
    public Result<LoginBackground> getBackgroundById(@PathVariable Long id) {
        LoginBackground background = loginBackgroundService.getById(id);
        return Result.success(background);
    }

    /**
     * 添加背景图片（管理员用）
     */
    @PostMapping
    public Result<LoginBackground> addBackground(@RequestBody LoginBackground background) {
        loginBackgroundService.save(background);
        return Result.success(background);
    }

    /**
     * 更新背景图片（管理员用）
     */
    @PutMapping("/{id}")
    public Result<LoginBackground> updateBackground(@PathVariable Long id, @RequestBody LoginBackground background) {
        background.setId(id);
        loginBackgroundService.updateById(background);
        LoginBackground updatedBackground = loginBackgroundService.getById(id);
        return Result.success(updatedBackground);
    }

    /**
     * 删除背景图片（管理员用）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteBackground(@PathVariable Long id) {
        loginBackgroundService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换背景图片启用状态（管理员用）
     */
    @PutMapping("/{id}/toggle-status")
    public Result<LoginBackground> toggleBackgroundStatus(@PathVariable Long id) {
        LoginBackground background = loginBackgroundService.toggleStatus(id);
        return Result.success(background);
    }
}