package com.pet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.dto.AppointmentQueryRequest;
import com.pet.dto.AppointmentRequest;
import com.pet.dto.ReviewRequest;
import com.pet.service.AppointmentService;
import com.pet.vo.AppointmentStatisticsVO;
import com.pet.vo.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    /**
     * 创建预约
     */
    @PostMapping
    public Result<AppointmentVO> createAppointment(@Valid @RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.createAppointment(request, userId));
    }
    
    /**
     * 创建预约（别名）
     */
    @PostMapping("/create")
    public Result<AppointmentVO> createAppointmentAlias(@Valid @RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.createAppointment(request, userId));
    }
    
    /**
     * 取消预约
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelAppointment(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.cancelAppointment(id, userId);
        return Result.success();
    }
    
    /**
     * 确认预约（用户确认）
     */
    @PutMapping("/confirm/{id}")
    public Result<Void> confirmAppointment(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.confirmAppointment(id, userId);
        return Result.success();
    }
    
    /**
     * 完成预约（标记为已完成）
     */
    @PutMapping("/complete/{id}")
    public Result<Void> completeAppointment(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.completeAppointment(id, userId);
        return Result.success();
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public Result<AppointmentVO> getAppointmentById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.getAppointmentById(id, userId));
    }
    
    /**
     * 获取我的预约列表
     */
    @GetMapping("/my-list")
    public Result<Page<AppointmentVO>> getMyAppointments(AppointmentQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.getMyAppointments(request, userId));
    }
    
    /**
     * 添加评价
     */
    @PostMapping("/review")
    public Result<Void> addReview(@Valid @RequestBody ReviewRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        appointmentService.addReview(request, userId);
        return Result.success();
    }
    
    /**
     * 获取即将到来的预约
     */
    @GetMapping("/upcoming")
    public Result<Page<AppointmentVO>> getUpcomingAppointments(AppointmentQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.getUpcomingAppointments(request, userId));
    }
    
    /**
     * 获取历史预约
     */
    @GetMapping("/history")
    public Result<Page<AppointmentVO>> getHistoryAppointments(AppointmentQueryRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(appointmentService.getHistoryAppointments(request, userId));
    }
    
    // ==================== 管理员功能 ====================
    
    /**
     * 管理员：获取所有预约列表
     */
    @GetMapping("/admin/list")
    public Result<Page<AppointmentVO>> getAllAppointments(AppointmentQueryRequest request) {
        return Result.success(appointmentService.getAllAppointments(request));
    }
    
    /**
     * 管理员：更新预约状态
     */
    @PutMapping("/admin/status/{id}")
    public Result<Void> updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        appointmentService.updateAppointmentStatus(id, status);
        return Result.success();
    }
    
    /**
     * 管理员：获取预约统计
     */
    @GetMapping("/admin/statistics")
    public Result<AppointmentStatisticsVO> getStatistics() {
        return Result.success(appointmentService.getStatistics());
    }
    
    /**
     * 管理员：删除预约
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return Result.success();
    }
}
