package com.pet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.dto.CreateOrderByAgentRequest;
import com.pet.dto.OrderQueryRequest;
import com.pet.dto.OrderRequest;
import com.pet.service.OrderService;
import com.pet.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(orderService.createOrder(request, userId));
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        orderService.cancelOrder(id, userId);
        return Result.success();
    }
    
    /**
     * 支付订单
     */
    @PutMapping("/pay/{id}")
    public Result<Void> payOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        orderService.payOrder(id, userId);
        return Result.success();
    }
    
    /**
     * 确认收货
     */
    @PutMapping("/confirm/{id}")
    public Result<Void> confirmOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        orderService.confirmOrder(id, userId);
        return Result.success();
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(orderService.getOrderById(id, userId));
    }
    
    /**
     * 获取我的订单列表
     */
    @GetMapping("/my-list")
    public Result<Page<OrderVO>> getMyOrders(OrderQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(orderService.getMyOrders(request, userId));
    }
    
    /**
     * Agent专用下单（无需登录，直接扣库存生成已支付订单）
     */
    @PostMapping("/createByAgent")
    public Result<OrderVO> createOrderByAgent(@Valid @RequestBody CreateOrderByAgentRequest request) {
        return Result.success(orderService.createOrderByAgent(request));
    }

    // ==================== 管理员功能 ====================

    /**
     * 获取所有订单
     */
    @GetMapping("/admin/list")
    public Result<Page<OrderVO>> getAllOrders(OrderQueryRequest request) {
        return Result.success(orderService.getAllOrders(request));
    }
    
    /**
     * 更新订单状态
     */
    @PutMapping("/admin/status/{id}")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return Result.success();
    }
    
    /**
     * 发货
     */
    @PutMapping("/admin/ship/{id}")
    public Result<Void> shipOrder(@PathVariable Long id, @RequestParam String trackingNumber) {
        orderService.shipOrder(id, trackingNumber);
        return Result.success();
    }

    /**
     * 用户申请退货
     */
    @PutMapping("/return/{id}")
    public Result<Void> returnOrder(@PathVariable Long id, @RequestParam String reason, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        orderService.returnOrder(id, userId, reason);
        return Result.success();
    }

    /**
     * 管理员审批退货
     */
    @PutMapping("/admin/approve-return/{id}")
    public Result<Void> approveReturn(@PathVariable Long id, @RequestParam(required = false) String notes) {
        orderService.approveReturn(id, notes);
        return Result.success();
    }

    /**
     * 管理员拒绝退货
     */
    @PutMapping("/admin/reject-return/{id}")
    public Result<Void> rejectReturn(@PathVariable Long id, @RequestParam(required = false) String notes) {
        orderService.rejectReturn(id, notes);
        return Result.success();
    }
}
