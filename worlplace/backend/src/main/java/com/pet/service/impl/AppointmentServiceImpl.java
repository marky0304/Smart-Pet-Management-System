package com.pet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.exception.BusinessException;
import com.pet.dto.AppointmentQueryRequest;
import com.pet.dto.AppointmentRequest;
import com.pet.dto.ReviewRequest;
import com.pet.entity.Appointment;
import com.pet.entity.Pet;
import com.pet.entity.PetService;
import com.pet.entity.User;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.PetMapper;
import com.pet.mapper.ServiceMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.AppointmentService;
import com.pet.vo.AppointmentStatisticsVO;
import com.pet.vo.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentMapper appointmentMapper;
    private final ServiceMapper serviceMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    
    private static final Map<String, String> STATUS_NAMES = Map.of(
        "PENDING", "待确认",
        "CONFIRMED", "已确认",
        "COMPLETED", "已完成",
        "CANCELLED", "已取消"
    );
    
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
        "BATH", "洗澡",
        "GROOM", "美容",
        "BOARD", "寄养",
        "MEDICAL", "医疗",
        "TRAIN", "训练",
        "OTHER", "其他"
    );

    @Override
    public AppointmentVO createAppointment(AppointmentRequest request, Long userId) {
        // 验证宠物
        Pet pet = petMapper.selectById(request.getPetId());
        if (pet == null || !pet.getUserId().equals(userId)) {
            throw new BusinessException("宠物不存在或无权操作");
        }
        
        // 验证服务
        PetService service = serviceMapper.selectById(request.getServiceId());
        if (service == null || service.getStatus() == 0) {
            throw new BusinessException("服务不存在或已下架");
        }
        
        // 验证预约时间
        if (request.getAppointmentDatetime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("预约时间不能早于当前时间");
        }
        
        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setOrderNo("APT" + IdUtil.getSnowflakeNextIdStr());
        appointment.setUserId(userId);
        appointment.setPetId(request.getPetId());
        appointment.setServiceId(request.getServiceId());
        appointment.setServiceType(service.getCategory()); // 从服务获取类型
        appointment.setAppointmentDatetime(request.getAppointmentDatetime());
        appointment.setAppointmentDate(request.getAppointmentDatetime().toLocalDate());
        appointment.setAppointmentTime(request.getAppointmentDatetime().toLocalTime());
        appointment.setStatus("PENDING");
        appointment.setTotalPrice(service.getPrice());
        appointment.setNotes(request.getNotes());
        
        appointmentMapper.insert(appointment);
        
        return convertToVO(appointment, pet, service, null);
    }
    
    @Override
    public void cancelAppointment(Long id, Long userId) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约");
        }
        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new BusinessException("该预约无法取消");
        }
        
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateStatusById(appointment.getId(), "CANCELLED");
    }
    
    @Override
    public void confirmAppointment(Long id, Long userId) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约");
        }
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new BusinessException("只能确认待确认的预约");
        }
        
        appointment.setStatus("CONFIRMED");
        appointmentMapper.updateStatusById(appointment.getId(), "CONFIRMED");
    }
    
    @Override
    public void completeAppointment(Long id, Long userId) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约");
        }
        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new BusinessException("只能完成已确认的预约");
        }
        
        appointment.setStatus("COMPLETED");
        appointmentMapper.updateStatusById(appointment.getId(), "COMPLETED");
    }
    
    @Override
    public AppointmentVO getAppointmentById(Long id, Long userId) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此预约");
        }
        
        Pet pet = petMapper.selectById(appointment.getPetId());
        PetService service = serviceMapper.selectById(appointment.getServiceId());
        User user = userMapper.selectById(appointment.getUserId());
        
        return convertToVO(appointment, pet, service, user);
    }

    @Override
    public Page<AppointmentVO> getMyAppointments(AppointmentQueryRequest request, Long userId) {
        log.debug("getMyAppointments: userId={}, pageNum={}, pageSize={}", userId, request.getPageNum(), request.getPageSize());

        // 使用自定义SQL查询，不应用逻辑删除
        Long total = appointmentMapper.countMyAppointments(
            userId,
            request.getStatus(),
            request.getServiceId(),
            request.getStartDate(),
            request.getEndDate()
        );

        List<Appointment> records = appointmentMapper.selectMyAppointments(
            userId,
            request.getStatus(),
            request.getServiceId(),
            request.getStartDate(),
            request.getEndDate()
        );

        // 手动分页
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, records.size());

        List<Appointment> pageRecords = fromIndex < records.size() ?
            records.subList(fromIndex, toIndex) : new ArrayList<>();

        Page<Appointment> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageRecords);

        return convertPageToVO(page);
    }
    
    @Override
    public void addReview(ReviewRequest request, Long userId) {
        Appointment appointment = appointmentMapper.selectByIdRaw(request.getAppointmentId());
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此预约");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new BusinessException("只能评价已完成的预约");
        }
        if (appointment.getRating() != null) {
            throw new BusinessException("该预约已评价");
        }
        
        appointment.setRating(request.getRating());
        appointment.setReview(request.getReview());
        appointmentMapper.updateReviewById(appointment.getId(), request.getRating(), request.getReview());
    }
    
    @Override
    public Page<AppointmentVO> getAllAppointments(AppointmentQueryRequest request) {
        // 使用自定义SQL查询，避免MyBatis Plus的逻辑删除干扰
        List<Appointment> appointments = appointmentMapper.selectAllAppointments(
            request.getStatus(),
            request.getServiceId(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        Long total = appointmentMapper.countAllAppointments(
            request.getStatus(),
            request.getServiceId(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        // 手动分页
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, appointments.size());
        
        List<Appointment> pageRecords = appointments.subList(start, end);
        
        Page<Appointment> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageRecords);
        
        return convertPageToVO(page);
    }
    
    @Override
    public void updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        appointment.setStatus(status);
        appointmentMapper.updateStatusById(id, status);
    }

    @Override
    public AppointmentStatisticsVO getStatistics() {
        AppointmentStatisticsVO statistics = new AppointmentStatisticsVO();
        
        statistics.setTotalAppointments(appointmentMapper.selectCount(null));
        statistics.setPendingCount(appointmentMapper.selectCount(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "PENDING")
        ));
        statistics.setConfirmedCount(appointmentMapper.selectCount(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "CONFIRMED")
        ));
        statistics.setCompletedCount(appointmentMapper.selectCount(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "COMPLETED")
        ));
        statistics.setCancelledCount(appointmentMapper.selectCount(
            new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "CANCELLED")
        ));
        
        BigDecimal totalRevenue = appointmentMapper.getTotalRevenue();
        statistics.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        
        List<Map<String, Object>> distribution = appointmentMapper.getCategoryDistribution();
        Map<String, Long> categoryMap = distribution.stream()
            .collect(Collectors.toMap(
                m -> CATEGORY_NAMES.getOrDefault(m.get("category").toString(), m.get("category").toString()),
                m -> ((Number) m.get("count")).longValue()
            ));
        statistics.setCategoryDistribution(categoryMap);
        
        return statistics;
    }
    
    @Override
    public Page<AppointmentVO> getUpcomingAppointments(AppointmentQueryRequest request, Long userId) {
        request.setStartDate(LocalDateTime.now());
        request.setStatus(null); // 不限制状态，但只查未来的
        return getMyAppointments(request, userId);
    }
    
    @Override
    public Page<AppointmentVO> getHistoryAppointments(AppointmentQueryRequest request, Long userId) {
        request.setEndDate(LocalDateTime.now());
        return getMyAppointments(request, userId);
    }
    
    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentMapper.selectByIdRaw(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        appointmentMapper.deleteById(id);
    }
    
    private Page<AppointmentVO> convertPageToVO(Page<Appointment> page) {
        Set<Long> petIds = page.getRecords().stream().map(Appointment::getPetId).collect(Collectors.toSet());
        Set<Long> serviceIds = page.getRecords().stream().map(Appointment::getServiceId).collect(Collectors.toSet());
        Set<Long> userIds = page.getRecords().stream().map(Appointment::getUserId).collect(Collectors.toSet());
        
        Map<Long, Pet> petMap = new HashMap<>();
        Map<Long, PetService> serviceMap = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();
        
        if (!petIds.isEmpty()) {
            petMap = petMapper.selectBatchIds(petIds).stream()
                .collect(Collectors.toMap(Pet::getId, p -> p));
        }
        if (!serviceIds.isEmpty()) {
            serviceMap = serviceMapper.selectBatchIds(serviceIds).stream()
                .collect(Collectors.toMap(PetService::getId, s -> s));
        }
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        Page<AppointmentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        Map<Long, Pet> finalPetMap = petMap;
        Map<Long, PetService> finalServiceMap = serviceMap;
        Map<Long, User> finalUserMap = userMap;
        
        voPage.setRecords(page.getRecords().stream()
            .map(appointment -> convertToVO(
                appointment,
                finalPetMap.get(appointment.getPetId()),
                finalServiceMap.get(appointment.getServiceId()),
                finalUserMap.get(appointment.getUserId())
            ))
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    private AppointmentVO convertToVO(Appointment appointment, Pet pet, PetService service, User user) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtil.copyProperties(appointment, vo);
        
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setPetType(pet.getType());
        }
        if (service != null) {
            vo.setServiceName(service.getName());
            vo.setServiceCategory(service.getCategory());
            vo.setServiceTypeName(CATEGORY_NAMES.getOrDefault(service.getCategory(), service.getCategory()));
        }
        if (user != null) {
            vo.setUsername(user.getUsername());
        }
        
        vo.setStatusName(STATUS_NAMES.getOrDefault(appointment.getStatus(), appointment.getStatus()));
        
        return vo;
    }
}
