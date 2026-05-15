package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    @Select("SELECT * FROM product_reviews WHERE product_id = #{productId} AND status = 1 ORDER BY create_time DESC")
    List<ProductReview> selectByProductId(@Param("productId") Long productId);

    @Select("SELECT AVG(rating) FROM product_reviews WHERE product_id = #{productId} AND status = 1")
    Double getAvgRating(@Param("productId") Long productId);

    @Select("SELECT * FROM product_reviews WHERE user_id = #{userId} AND status = 1 ORDER BY create_time DESC")
    List<ProductReview> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM product_reviews WHERE order_id = #{orderId} AND status = 1")
    ProductReview selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) > 0 FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} AND oi.product_id = #{productId} " +
            "AND o.status = 'COMPLETED'")
    boolean hasUserPurchased(@Param("userId") Long userId, @Param("productId") Long productId);
}
