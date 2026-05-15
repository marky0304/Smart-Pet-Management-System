package com.pet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.exception.BusinessException;
import com.pet.dto.CreateOrderByAgentRequest;
import com.pet.dto.OrderQueryRequest;
import com.pet.dto.OrderRequest;
import com.pet.entity.OrderItem;
import com.pet.entity.Product;
import com.pet.entity.Address;
import com.pet.entity.ShopOrder;
import com.pet.entity.User;
import com.pet.mapper.OrderItemMapper;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ProductReviewMapper;
import com.pet.mapper.ShopOrderMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.OrderService;
import com.pet.vo.OrderVO;
import com.pet.websocket.OrderWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final ShopOrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductReviewMapper productReviewMapper;
    private final UserMapper userMapper;
    private final com.pet.mapper.AddressMapper addressMapper;
    
    private static final Map<String, String> STATUS_NAMES = new HashMap<String, String>() {{
        put("PENDING", "待支付");
        put("PAID", "已支付");
        put("SHIPPED", "已发货");
        put("COMPLETED", "已完成");
        put("CANCELLED", "已取消");
        put("RETURNING", "退货中");
        put("RETURNED", "已退货");
    }};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderRequest request, Long userId) {
        // 创建订单
        ShopOrder order = new ShopOrder();
        order.setOrderNo("ORD" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setPaymentMethod(request.getPaymentMethod());

        if (request.getAddressId() != null) {
            Address address = addressMapper.selectById(request.getAddressId());
            if (address != null && address.getUserId().equals(userId)) {
                order.setShippingName(address.getReceiverName());
                order.setShippingPhone(address.getReceiverPhone());
                order.setShippingAddress(address.getProvince() + " " + address.getCity() + " "
                        + address.getDistrict() + " " + address.getDetailAddress());
            }
        }
        if (order.getShippingName() == null) {
            order.setShippingName(request.getShippingName());
        }
        if (order.getShippingPhone() == null) {
            order.setShippingPhone(request.getShippingPhone());
        }
        if (order.getShippingAddress() == null) {
            order.setShippingAddress(request.getShippingAddress());
        }
        order.setNotes(request.getNotes());
        
        // 计算总金额并创建订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在：" + itemRequest.getProductId());
            }
            if (product.getStatus() == 0) {
                throw new BusinessException("商品已下架：" + product.getName());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException("商品库存不足：" + product.getName()
                        + "（库存：" + product.getStock() + "，需要：" + itemRequest.getQuantity() + "）");
            }
            
            if (product.getPrice() == null) {
                throw new BusinessException("商品价格异常，请联系管理员：" + product.getName());
            }
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(subtotal);
            
            orderItems.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount); // actual_amount NOT NULL，设为与total_amount相同
        orderMapper.insert(order);
        
        // 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        
        return getOrderById(order.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id, Long userId) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付的订单");
        }
        
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id, Long userId) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }
        
        // 扣减库存（原子操作：WHERE stock >= quantity，防止超卖）
        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        for (OrderItem item : items) {
            int updated = productMapper.updateStock(item.getProductId(), item.getQuantity());
            if (updated == 0) {
                throw new BusinessException("商品库存不足，支付失败：" + item.getProductName());
            }
        }
        
        order.setStatus("PAID");
        order.setPaymentTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public void confirmOrder(Long id, Long userId) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException("只能确认已发货的订单");
        }
        
        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public OrderVO getOrderById(Long id, Long userId) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // userId 为 null 时（管理员场景）跳过权限校验
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }
        return convertToVO(order);
    }

    @Override
    public Page<OrderVO> getMyOrders(OrderQueryRequest request, Long userId) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrder::getUserId, userId);
        
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.like(ShopOrder::getOrderNo, request.getOrderNo());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ShopOrder::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        
        Page<ShopOrder> page = orderMapper.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()),
            wrapper
        );
        
        return convertPageToVO(page);
    }

    @Override
    public Page<OrderVO> getAllOrders(OrderQueryRequest request) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.like(ShopOrder::getOrderNo, request.getOrderNo());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ShopOrder::getStatus, request.getStatus());
        }
        if (request.getUserId() != null) {
            wrapper.eq(ShopOrder::getUserId, request.getUserId());
        }
        
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        
        Page<ShopOrder> page = orderMapper.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()),
            wrapper
        );
        
        return convertPageToVO(page);
    }

    @Override
    public void updateOrderStatus(Long id, String status) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    @Override
    public void shipOrder(Long id, String trackingNumber) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("只能发货已支付的订单");
        }
        
        order.setStatus("SHIPPED");
        order.setTrackingNumber(trackingNumber);
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnOrder(Long id, Long userId, String reason) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!"COMPLETED".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException("仅已完成或已发货的订单可以申请退货");
        }

        order.setStatus("RETURNING");
        order.setReturnReason(reason);
        order.setReturnTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveReturn(Long id, String notes) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"RETURNING".equals(order.getStatus())) {
            throw new BusinessException("只能审批退货中的订单");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        for (OrderItem item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus("RETURNED");
        order.setRefundAmount(order.getActualAmount());
        order.setRefundTime(LocalDateTime.now());
        order.setRefundNotes(notes);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReturn(Long id, String notes) {
        ShopOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"RETURNING".equals(order.getStatus())) {
            throw new BusinessException("只能处理退货中的订单");
        }

        String previousStatus = order.getCompleteTime() != null ? "COMPLETED" : "SHIPPED";
        order.setStatus(previousStatus);
        order.setReturnReason(null);
        order.setReturnTime(null);
        order.setRefundNotes(notes);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrderByAgent(CreateOrderByAgentRequest request) {
        Product product = productMapper.selectById(request.getGoodsId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == 0) {
            throw new BusinessException("商品已下架：" + product.getName());
        }
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("商品库存不足：" + product.getName()
                    + "（库存：" + product.getStock() + "，需要：" + request.getQuantity() + "）");
        }

        // 扣减库存
        int updated = productMapper.updateStock(product.getId(), request.getQuantity());
        if (updated == 0) {
            throw new BusinessException("商品库存不足，下单失败：" + product.getName());
        }

        // 创建订单
        ShopOrder order = new ShopOrder();
        order.setOrderNo("ORD" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(request.getUserId());
        order.setStatus("PAID");
        order.setPaymentMethod("AI_AGENT");
        order.setPaymentTime(LocalDateTime.now());

        // 填充收货地址
        if (request.getAddressId() != null) {
            Address address = addressMapper.selectById(request.getAddressId());
            if (address != null && address.getUserId().equals(request.getUserId())) {
                order.setShippingName(address.getReceiverName());
                order.setShippingPhone(address.getReceiverPhone());
                order.setShippingAddress(address.getProvince() + " " + address.getCity() + " "
                        + address.getDistrict() + " " + address.getDetailAddress());
            }
        }

        if (product.getPrice() == null) {
            throw new BusinessException("商品价格异常，请联系管理员：" + product.getName());
        }
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(request.getQuantity()));
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        orderMapper.insert(order);

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getImage());
        orderItem.setPrice(product.getPrice());
        orderItem.setQuantity(request.getQuantity());
        orderItem.setSubtotal(totalAmount);
        orderItemMapper.insert(orderItem);

        // WebSocket 推送
        OrderWebSocketHandler.sendOrderNotification(request.getUserId(), "NEW_ORDER:" + order.getId());

        return getOrderById(order.getId(), null);
    }

    private Page<OrderVO> convertPageToVO(Page<ShopOrder> page) {
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));
        return voPage;
    }
    
    private OrderVO convertToVO(ShopOrder order) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);
        vo.setStatusName(STATUS_NAMES.getOrDefault(order.getStatus(), order.getStatus()));
        
        // 获取用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }
        
        // 获取订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        vo.setItems(items.stream().map(item -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            // 兜底：若 subtotal 为 null，用 price × quantity 计算
            if (itemVO.getSubtotal() == null && item.getPrice() != null && item.getQuantity() != null) {
                itemVO.setSubtotal(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
            }
            // 仅已完成的订单可评价，且需检查是否已评价过
            if ("COMPLETED".equals(order.getStatus())) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.pet.entity.ProductReview> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("user_id", order.getUserId())
                       .eq("product_id", item.getProductId())
                       .eq("order_id", order.getId())
                       .eq("status", 1);
                itemVO.setReviewed(productReviewMapper.selectCount(wrapper) > 0);
            } else {
                itemVO.setReviewed(true);
            }
            return itemVO;
        }).collect(Collectors.toList()));
        
        return vo;
    }
}
