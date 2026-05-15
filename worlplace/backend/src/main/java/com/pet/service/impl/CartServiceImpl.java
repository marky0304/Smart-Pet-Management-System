package com.pet.service.impl;

import com.pet.common.exception.BusinessException;
import com.pet.entity.CartItem;
import com.pet.entity.Product;
import com.pet.mapper.CartItemMapper;
import com.pet.mapper.ProductMapper;
import com.pet.service.CartService;
import com.pet.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CartItemVO> getCartItems(Long userId) {
        List<CartItem> items = cartItemMapper.selectByUserId(userId);
        List<CartItemVO> vos = new ArrayList<>();
        for (CartItem item : items) {
            CartItemVO vo = toVO(item);
            if (vo != null) {
                vos.add(vo);
            }
        }
        return vos;
    }

    @Override
    public CartItemVO addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() == 0) {
            throw new BusinessException("商品已下架");
        }
        if (product.getStock() == null || product.getStock() < 1) {
            throw new BusinessException("商品已售罄");
        }

        CartItem existing = cartItemMapper.selectByUserIdAndProductId(userId, productId);
        if (existing != null) {
            int newQty = existing.getQuantity() + (quantity != null ? quantity : 1);
            if (newQty > product.getStock()) {
                throw new BusinessException("库存不足，当前库存：" + product.getStock());
            }
            existing.setQuantity(newQty);
            existing.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(existing);
            return toVO(existing);
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity != null ? quantity : 1);
        if (item.getQuantity() > product.getStock()) {
            throw new BusinessException("库存不足，当前库存：" + product.getStock());
        }
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        cartItemMapper.insert(item);
        return toVO(item);
    }

    @Override
    public CartItemVO updateQuantity(Long id, Long userId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("购物车项不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (quantity == null || quantity < 1) {
            throw new BusinessException("数量至少为1");
        }

        Product product = productMapper.selectById(item.getProductId());
        if (product != null && quantity > product.getStock()) {
            throw new BusinessException("库存不足，当前库存：" + product.getStock());
        }

        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(item);
        return toVO(item);
    }

    @Override
    public void removeItem(Long id, Long userId) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) {
            return;
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        cartItemMapper.deleteById(id);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }

    private CartItemVO toVO(CartItem item) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setProductId(item.getProductId());
        vo.setQuantity(item.getQuantity());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());

        Product product = productMapper.selectById(item.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() == 0) {
            return null;
        }
        vo.setProductName(product.getName());
        vo.setProductImage(product.getImage());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setProductStatus(product.getStatus());
        return vo;
    }
}
