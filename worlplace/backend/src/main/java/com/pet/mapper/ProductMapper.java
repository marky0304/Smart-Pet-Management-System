package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 更新商品库存
     */
    @Update("UPDATE products SET stock = stock - #{quantity}, sales = sales + #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int updateStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("UPDATE products SET stock = stock + #{quantity} WHERE id = #{productId}")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 直接更新商品状态（绕过逻辑删除）
     */
    @Update("UPDATE products SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 获取热销商品
     */
    @Select("SELECT * FROM products WHERE status = 1 ORDER BY sales DESC LIMIT #{limit}")
    List<Product> getHotProducts(Integer limit);
    
    /**
     * 获取新品推荐
     */
    @Select("SELECT * FROM products WHERE status = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<Product> getNewProducts(Integer limit);
}
