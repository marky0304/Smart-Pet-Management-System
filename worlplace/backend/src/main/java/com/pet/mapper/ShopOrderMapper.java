package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.ShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {
    
    /**
     * 获取用户订单列表
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ShopOrder> selectByUserId(Long userId);
    
    /**
     * 获取订单统计
     */
    @Select("SELECT status, COUNT(*) as count, SUM(total_amount) as amount FROM orders GROUP BY status")
    List<Map<String, Object>> getOrderStatistics();
    
    /**
     * 获取总销售额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status IN ('PAID', 'SHIPPED', 'COMPLETED')")
    BigDecimal getTotalSales();
}
