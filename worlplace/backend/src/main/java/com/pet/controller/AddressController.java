package com.pet.controller;

import com.pet.common.result.Result;
import com.pet.entity.Address;
import com.pet.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/address")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(addressService.getAddressList(userId));
    }

    @GetMapping("/{id}")
    public Result getAddressById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(addressService.getAddressById(id, userId));
    }

    @PostMapping
    public Result createAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(addressService.createAddress(address, userId));
    }

    @PutMapping("/{id}")
    public Result updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        return Result.success(addressService.updateAddress(id, address, userId));
    }

    @DeleteMapping("/{id}")
    public Result deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        addressService.deleteAddress(id, userId);
        return Result.success("删除成功", null);
    }

    @PutMapping("/default/{id}")
    public Result setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "请先登录");
        addressService.setDefault(id, userId);
        return Result.success("设置成功", null);
    }
}
