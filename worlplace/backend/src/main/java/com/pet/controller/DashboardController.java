package com.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pet.common.result.Result;
import com.pet.entity.ShopOrder;
import com.pet.entity.User;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ShopOrderMapper;
import com.pet.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopOrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限访问");
        }

        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalUsers", userMapper.selectCount(new QueryWrapper<User>().eq("status", 1)));

        QueryWrapper<ShopOrder> paidWrapper = new QueryWrapper<ShopOrder>()
            .in("status", "PAID", "SHIPPED", "COMPLETED");
        stats.put("totalOrders", orderMapper.selectCount(paidWrapper));

        BigDecimal totalSales = orderMapper.getTotalSales();
        stats.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);

        stats.put("totalProducts", productMapper.selectCount(
            new QueryWrapper<com.pet.entity.Product>().eq("status", 1)));

        List<Map<String, Object>> orderStats = orderMapper.getOrderStatistics();
        stats.put("orderStats", orderStats);

        QueryWrapper<ShopOrder> todayWrapper = new QueryWrapper<ShopOrder>()
            .apply("DATE(create_time) = CURDATE()");
        stats.put("todayOrders", orderMapper.selectCount(todayWrapper));

        return Result.success(stats);
    }
}
