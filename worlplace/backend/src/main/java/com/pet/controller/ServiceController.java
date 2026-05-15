package com.pet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.dto.ServiceRequest;
import com.pet.service.ServiceService;
import com.pet.vo.ServiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceController {
    
    private final ServiceService serviceService;
    
    @GetMapping("/list")
    public Result<Page<ServiceVO>> getServiceList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(serviceService.getServiceList(category, status, pageNum, pageSize));
    }
    
    @GetMapping("/active")
    public Result<List<ServiceVO>> getAllActiveServices() {
        return Result.success(serviceService.getAllActiveServices());
    }
    
    @GetMapping("/{id}")
    public Result<ServiceVO> getServiceById(@PathVariable Long id) {
        return Result.success(serviceService.getServiceById(id));
    }
    
    @PostMapping
    public Result<ServiceVO> createService(@RequestBody ServiceRequest request) {
        return Result.success(serviceService.createService(request));
    }
    
    @PutMapping
    public Result<ServiceVO> updateService(@RequestBody ServiceRequest request) {
        return Result.success(serviceService.updateService(request));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return Result.success();
    }
}
