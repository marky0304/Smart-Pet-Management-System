package com.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.entity.User;
import com.pet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "未登录");
        User user = userService.getById(userId);
        if (user == null) return Result.error(404, "用户不存在");
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {
        
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限访问");
        }

        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username.trim())
                       .or()
                       .like("nickname", username.trim());
        }
        
        if (role != null && !role.trim().isEmpty()) {
            queryWrapper.eq("role", role.trim());
        }
        
        queryWrapper.orderByDesc("create_time");
        
        IPage<User> result = userService.page(page, queryWrapper);
        
        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId, HttpServletRequest request) {
        // 检查权限：管理员或本人
        Long currentUserId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (!"ADMIN".equals(userRole) && !currentUserId.equals(userId)) {
            return Result.error(403, "无权限访问");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 清除密码字段
        user.setPassword(null);
        
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public Result<String> updateUser(
            @PathVariable Long userId,
            @RequestBody User user,
            HttpServletRequest request) {
        
        // 检查权限：管理员或本人
        Long currentUserId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (!"ADMIN".equals(userRole) && !currentUserId.equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 更新允许的字段
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 只有管理员可以修改角色和状态
        if ("ADMIN".equals(userRole)) {
            if (user.getRole() != null) {
                existingUser.setRole(user.getRole());
            }
            if (user.getStatus() != null) {
                existingUser.setStatus(user.getStatus());
            }
        }
        
        boolean success = userService.updateById(existingUser);
        
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/{userId}/status")
    public Result<String> toggleUserStatus(@PathVariable Long userId, HttpServletRequest request) {
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限操作");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 不能禁用自己
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId.equals(userId)) {
            return Result.error(400, "不能禁用自己");
        }
        
        // 切换状态
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(user);
        
        if (success) {
            String message = user.getStatus() == 1 ? "启用成功" : "禁用成功";
            return Result.success(message, null);
        } else {
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/password")
    public Result<String> resetPassword(@PathVariable Long userId, HttpServletRequest request) {
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限操作");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 重置为默认密码 123456
        String defaultPassword = "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success("密码重置成功，新密码为：123456", null);
        } else {
            return Result.error(500, "重置失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限操作");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 不能删除自己
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId.equals(userId)) {
            return Result.error(400, "不能删除自己");
        }
        
        // 软删除：设置状态为0
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 清除密码字段
        user.setPassword(null);
        
        return Result.success(user);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/current")
    public Result<String> updateCurrentUser(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 更新允许的字段
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setUpdateTime(LocalDateTime.now());
        
        boolean success = userService.updateById(existingUser);
        
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/{userId}/stats")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long userId, HttpServletRequest request) {
        // 检查权限：管理员或本人
        Long currentUserId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (!"ADMIN".equals(userRole) && !currentUserId.equals(userId)) {
            return Result.error(403, "无权限访问");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 获取统计数据
        Map<String, Object> stats = userService.getUserStats(userId);
        
        return Result.success(stats);
    }

    /**
     * 获取当前用户统计信息
     */
    @GetMapping("/current/stats")
    public Result<Map<String, Object>> getCurrentUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        // 获取统计数据
        Map<String, Object> stats = userService.getUserStats(userId);
        
        return Result.success(stats);
    }
}