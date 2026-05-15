package com.pet.service;

import com.pet.vo.CartItemVO;

import java.util.List;

public interface CartService {

    List<CartItemVO> getCartItems(Long userId);

    CartItemVO addToCart(Long userId, Long productId, Integer quantity);

    CartItemVO updateQuantity(Long id, Long userId, Integer quantity);

    void removeItem(Long id, Long userId);

    void clearCart(Long userId);
}
