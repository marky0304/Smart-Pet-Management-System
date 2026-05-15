package com.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.dto.AppointmentQueryRequest;
import com.pet.dto.AppointmentRequest;
import com.pet.dto.ReviewRequest;
import com.pet.vo.AppointmentStatisticsVO;
import com.pet.vo.AppointmentVO;

public interface AppointmentService {
    
    /**
     * 创建预约
     */
    AppointmentVO createAppointment(AppointmentRequest request, Long userId);
    
    /**
     * 取消预约
     */
    void cancelAppointment(Long id, Long userId);
    
    /**
     * 确认预约
     */
    void confirmAppointment(Long id, Long userId);
    
    /**
     * 完成预约
     */
    void completeAppointment(Long id, Long userId);
    
    /**
     * 获取预约详情
     */
    AppointmentVO getAppointmentById(Long id, Long userId);
    
    /**
     * 获取我的预约列表
     */
    Page<AppointmentVO> getMyAppointments(AppointmentQueryRequest request, Long userId);
    
    /**
     * 获取即将到来的预约
     */
    Page<AppointmentVO> getUpcomingAppointments(AppointmentQueryRequest request, Long userId);
    
    /**
     * 获取历史预约
     */
    Page<AppointmentVO> getHistoryAppointments(AppointmentQueryRequest request, Long userId);
    
    /**
     * 添加评价
     */
    void addReview(ReviewRequest request, Long userId);
    
    // ==================== 管理员功能 ====================
    
    /**
     * 管理员：获取所有预约
     */
    Page<AppointmentVO> getAllAppointments(AppointmentQueryRequest request);
    
    /**
     * 管理员：更新预约状态
     */
    void updateAppointmentStatus(Long id, String status);
    
    /**
     * 管理员：删除预约
     */
    void deleteAppointment(Long id);
    
    /**
     * 管理员：获取统计信息
     */
    AppointmentStatisticsVO getStatistics();
}
