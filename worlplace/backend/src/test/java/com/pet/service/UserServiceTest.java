package com.pet.service;

import cn.hutool.crypto.digest.BCrypt;
import com.pet.common.exception.BusinessException;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.entity.User;
import com.pet.mapper.UserMapper;
import com.pet.service.impl.UserServiceImpl;
import com.pet.utils.JwtUtil;
import com.pet.vo.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "passwordEncoder", new BCryptPasswordEncoder());

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword(BCrypt.hashpw("password123"));
        mockUser.setRole("USER");
        mockUser.setStatus(1);
    }

    @Test
    @DisplayName("登录：正确账号密码应成功")
    void login_correctCredentials_shouldSucceed() {
        when(userMapper.selectOne(any())).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("mock-token");

        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        LoginResponse response = userService.login(req);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
    }

    @Test
    @DisplayName("登录：错误密码应抛出异常")
    void login_wrongPassword_shouldThrowException() {
        when(userMapper.selectOne(any())).thenReturn(mockUser);

        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> userService.login(req));
    }

    @Test
    @DisplayName("登录：账号不存在应抛出异常")
    void login_userNotFound_shouldThrowException() {
        when(userMapper.selectOne(any())).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setUsername("nonexistent");
        req.setPassword("password123");

        assertThrows(BusinessException.class, () -> userService.login(req));
    }

    @Test
    @DisplayName("登录：禁用账号应抛出异常")
    void login_disabledUser_shouldThrowException() {
        mockUser.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(mockUser);

        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(req));
        assertTrue(ex.getMessage().contains("禁用"));
    }
}
