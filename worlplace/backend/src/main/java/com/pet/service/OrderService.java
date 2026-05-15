package com.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.dto.CreateOrderByAgentRequest;
import com.pet.dto.OrderQueryRequest;
import com.pet.dto.OrderRequest;
import com.pet.vo.OrderVO;

public interface OrderService {
    
    /**
     * 创建订单
     */
    OrderVO createOrder(OrderRequest request, Long userId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long id, Long userId);
    
    /**
     * 支付订单
     */
    void payOrder(Long id, Long userId);
    
    /**
     * 确认收货
     */
    void confirmOrder(Long id, Long userId);
    
    /**
     * 获取订单详情
     */
    OrderVO getOrderById(Long id, Long userId);
    
    /**
     * 获取我的订单列表
     */
    Page<OrderVO> getMyOrders(OrderQueryRequest request, Long userId);
    
    /**
     * 管理员：获取所有订单
     */
    Page<OrderVO> getAllOrders(OrderQueryRequest request);
    
    /**
     * 管理员：更新订单状态
     */
    void updateOrderStatus(Long id, String status);
    
    /**
     * 管理员：发货
     */
    void shipOrder(Long id, String trackingNumber);

    /**
     * 用户申请退货
     */
    void returnOrder(Long id, Long userId, String reason);

    /**
     * 管理员审批退货
     */
    void approveReturn(Long id, String notes);

    /**
     * 管理员拒绝退货
     */
    void rejectReturn(Long id, String notes);

    /**
     * Agent专用下单（含库存扣减，直接生成已支付订单）
     */
    OrderVO createOrderByAgent(CreateOrderByAgentRequest request);
}
