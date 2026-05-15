package com.pet.controller;

import com.pet.common.result.Result;
import com.pet.service.CartService;
import com.pet.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> getCartItems(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartItemVO> items = cartService.getCartItems(userId);
        return Result.success(items);
    }

    @PostMapping
    public Result<CartItemVO> addToCart(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = body.containsKey("quantity")
                ? Integer.valueOf(body.get("quantity").toString()) : 1;
        CartItemVO vo = cartService.addToCart(userId, productId, quantity);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<CartItemVO> updateQuantity(@PathVariable Long id,
                                              @RequestParam Integer quantity,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CartItemVO vo = cartService.updateQuantity(id, userId, quantity);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> removeItem(@PathVariable Long id,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.removeItem(id, userId);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.success();
    }
}
