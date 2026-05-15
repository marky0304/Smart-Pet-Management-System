package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    @Select("SELECT * FROM cart_items WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<CartItem> selectByUserId(Long userId);

    @Select("SELECT * FROM cart_items WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem selectByUserIdAndProductId(Long userId, Long productId);

    @Delete("DELETE FROM cart_items WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
