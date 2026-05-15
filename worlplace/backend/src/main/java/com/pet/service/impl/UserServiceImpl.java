package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.common.exception.BusinessException;
import com.pet.dto.*;
import com.pet.entity.User;
import com.pet.mapper.UserMapper;
import com.pet.mapper.PetMapper;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.CommunityPostMapper;
import com.pet.mapper.CommunityCommentMapper;
import com.pet.service.UserService;
import com.pet.utils.JwtUtil;
import com.pet.vo.LoginResponse;
import com.pet.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PetMapper petMapper;
    
    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private CommunityPostMapper communityPostMapper;
    
    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${app.captcha.fallback-code:}")
    private String captchaFallbackCode;

    private boolean isCaptchaFallbackValid(String code) {
        return captchaFallbackCode != null
                && !captchaFallbackCode.isEmpty()
                && captchaFallbackCode.equals(code);
    }

    private static final String VERIFY_CODE_PREFIX = "verify:reset:";
    private static final long VERIFY_CODE_EXPIRE = 5; // 5分钟
    
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = getUserByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (StringUtils.hasText(request.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, request.getPhone());
            if (count(wrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail());
            if (count(wrapper) > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 设置默认值
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname("用户" + System.currentTimeMillis());
        }
        user.setRole("USER");
        user.setStatus(1);
        
        // 保存用户
        save(user);
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 支持用户名/手机号/邮箱登录
        User user = getUserByAccount(request.getUsername());
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole()
        );
        
        return new LoginResponse(token, userInfo);
    }
    
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
    
    @Override
    public void updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查手机号是否被其他用户使用
        if (StringUtils.hasText(request.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, request.getPhone());
            wrapper.ne(User::getId, userId);
            if (count(wrapper) > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(request.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail());
            wrapper.ne(User::getId, userId);
            if (count(wrapper) > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(request, user);
        updateById(user);
    }
    
    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
    }
    
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // 根据手机号或邮箱查找用户
        User user = getUserByAccount(request.getAccount());
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        
        // 从 Redis 验证验证码
        String codeKey = VERIFY_CODE_PREFIX + request.getAccount();
        try {
            if (redisTemplate != null) {
                String savedCode = redisTemplate.opsForValue().get(codeKey);
                if (savedCode == null) {
                    throw new BusinessException("验证码已过期，请重新获取");
                }
                if (!savedCode.equals(request.getCode())) {
                    throw new BusinessException("验证码错误");
                }
                redisTemplate.delete(codeKey);
            } else {
                // Redis 不可用降级
                if (!isCaptchaFallbackValid(request.getCode())) {
                    throw new BusinessException("验证码错误（Redis不可用）");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 连接异常降级
            if (!isCaptchaFallbackValid(request.getCode())) {
                throw new BusinessException("验证码错误（Redis不可用）");
            }
        }
        
        // 重置密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
    }

    @Override
    public String sendVerifyCode(String account) {
        User user = getUserByAccount(account);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        try {
            if (redisTemplate != null) {
                String codeKey = VERIFY_CODE_PREFIX + account;
                redisTemplate.opsForValue().set(codeKey, code, VERIFY_CODE_EXPIRE, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // Redis 不可用时降级，验证码仍然返回（开发环境）
        }
        return code;
    }
    
    @Override
    public UserVO getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
    
    @Override
    public IPage<UserVO> getUserList(UserQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), User::getUsername, request.getUsername());
        wrapper.like(StringUtils.hasText(request.getNickname()), User::getNickname, request.getNickname());
        wrapper.like(StringUtils.hasText(request.getPhone()), User::getPhone, request.getPhone());
        wrapper.eq(StringUtils.hasText(request.getRole()), User::getRole, request.getRole());
        wrapper.eq(request.getStatus() != null, User::getStatus, request.getStatus());
        wrapper.orderByDesc(User::getCreateTime);
        
        // 分页查询
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<User> userPage = page(page, wrapper);
        
        // 转换为VO
        IPage<UserVO> voPage = userPage.convert(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        });
        
        return voPage;
    }
    
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能禁用管理员账号
        if ("ADMIN".equals(user.getRole()) && status == 0) {
            throw new BusinessException("不能禁用管理员账号");
        }
        
        user.setStatus(status);
        updateById(user);
    }
    
    @Override
    public void deleteUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除管理员账号
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账号");
        }
        
        removeById(userId);
    }
    
    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取宠物数量
            LambdaQueryWrapper<com.pet.entity.Pet> petWrapper = new LambdaQueryWrapper<>();
            petWrapper.eq(com.pet.entity.Pet::getUserId, userId);
            petWrapper.eq(com.pet.entity.Pet::getStatus, 1); // 只统计正常状态的宠物
            long petCount = petMapper.selectCount(petWrapper);
            stats.put("petCount", petCount);
            
            // 获取预约数量
            LambdaQueryWrapper<com.pet.entity.Appointment> appointmentWrapper = new LambdaQueryWrapper<>();
            appointmentWrapper.eq(com.pet.entity.Appointment::getUserId, userId);
            long appointmentCount = appointmentMapper.selectCount(appointmentWrapper);
            stats.put("appointmentCount", appointmentCount);
            
            // 获取发布动态数量
            LambdaQueryWrapper<com.pet.entity.CommunityPost> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.eq(com.pet.entity.CommunityPost::getUserId, userId);
            long postCount = communityPostMapper.selectCount(postWrapper);
            stats.put("postCount", postCount);
            
            // 获取评论数量
            LambdaQueryWrapper<com.pet.entity.CommunityComment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(com.pet.entity.CommunityComment::getUserId, userId);
            long commentCount = communityCommentMapper.selectCount(commentWrapper);
            stats.put("commentCount", commentCount);
            
        } catch (Exception e) {
            // 如果出现异常，返回默认值
            stats.put("petCount", 0);
            stats.put("appointmentCount", 0);
            stats.put("postCount", 0);
            stats.put("commentCount", 0);
        }
        
        return stats;
    }
    
    /**
     * 根据账号查询用户（支持用户名/手机号/邮箱）
     */
    private User getUserByAccount(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getUsername, account)
                .or().eq(User::getPhone, account)
                .or().eq(User::getEmail, account));
        return getOne(wrapper);
    }
}
