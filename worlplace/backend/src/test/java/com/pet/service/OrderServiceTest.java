package com.pet.service;

import com.pet.common.exception.BusinessException;
import com.pet.entity.OrderItem;
import com.pet.entity.ShopOrder;
import com.pet.mapper.OrderItemMapper;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ShopOrderMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceTest {

    @Mock private ShopOrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private ProductMapper productMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ShopOrder pendingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = new ShopOrder();
        pendingOrder.setId(1L);
        pendingOrder.setUserId(1L);
        pendingOrder.setStatus("PENDING");
        pendingOrder.setTotalAmount(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("取消订单：PENDING状态可以取消")
    void cancelOrder_pendingStatus_shouldSucceed() {
        when(orderMapper.selectById(1L)).thenReturn(pendingOrder);
        when(orderMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L, 1L));
        assertEquals("CANCELLED", pendingOrder.getStatus());
    }

    @Test
    @DisplayName("取消订单：非PENDING状态应抛出异常")
    void cancelOrder_nonPendingStatus_shouldThrowException() {
        pendingOrder.setStatus("PAID");
        when(orderMapper.selectById(1L)).thenReturn(pendingOrder);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 1L));
    }

    @Test
    @DisplayName("取消订单：其他用户的订单应抛出异常")
    void cancelOrder_otherUserOrder_shouldThrowException() {
        when(orderMapper.selectById(1L)).thenReturn(pendingOrder);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(1L, 999L));
    }

    @Test
    @DisplayName("支付订单：库存不足应抛出异常并回滚")
    void payOrder_insufficientStock_shouldThrowException() {
        pendingOrder.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(pendingOrder);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setProductName("测试商品");
        item.setQuantity(10);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(List.of(item));
        // 模拟库存不足：updateStock 返回 0
        when(productMapper.updateStock(anyLong(), anyInt())).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.payOrder(1L, 1L));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("支付订单：库存充足应成功")
    void payOrder_sufficientStock_shouldSucceed() {
        pendingOrder.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(pendingOrder);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setProductName("测试商品");
        item.setQuantity(2);
        when(orderItemMapper.selectByOrderId(1L)).thenReturn(List.of(item));
        when(productMapper.updateStock(anyLong(), anyInt())).thenReturn(1);
        when(orderMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> orderService.payOrder(1L, 1L));
        assertEquals("PAID", pendingOrder.getStatus());
    }
}
