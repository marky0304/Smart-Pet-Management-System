package com.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.entity.HealthRecord;
import com.pet.entity.Pet;
import com.pet.service.HealthRecordService;
import com.pet.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 健康管理控制器
 */
@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private PetService petService;

    /**
     * 系统健康检查
     */
    @GetMapping("")
    public Result<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "智慧宠物管理系统");
        status.put("timestamp", LocalDateTime.now().toString());
        return Result.success(status);
    }

    /**
     * 获取健康记录列表
     */
    @GetMapping("/records")
    public Result<IPage<HealthRecord>> getHealthRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) String recordType,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Page<HealthRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        
        // 如果不是管理员，只能查看自己宠物的健康记录
        if (!"ADMIN".equals(userRole)) {
            // 获取用户的宠物ID列表
            QueryWrapper<Pet> petWrapper = new QueryWrapper<>();
            petWrapper.eq("user_id", userId).eq("status", 1);
            List<Pet> userPets = petService.list(petWrapper);
            
            if (userPets.isEmpty()) {
                return Result.success(new Page<>());
            }
            
            List<Long> petIds = userPets.stream().map(Pet::getId).toList();
            queryWrapper.in("pet_id", petIds);
        }
        
        if (petId != null) {
            queryWrapper.eq("pet_id", petId);
        }
        
        if (recordType != null && !recordType.trim().isEmpty()) {
            queryWrapper.eq("record_type", recordType.trim());
        }
        
        queryWrapper.orderByDesc("record_date")
                   .orderByDesc("create_time");
        
        IPage<HealthRecord> result = healthRecordService.page(page, queryWrapper);

        // 批量填充宠物名称（避免 N+1 查询）
        if (!result.getRecords().isEmpty()) {
            List<Long> petIds = result.getRecords().stream()
                    .map(HealthRecord::getPetId).distinct().collect(java.util.stream.Collectors.toList());
            Map<Long, String> petNameMap = petService.listByIds(petIds).stream()
                    .collect(java.util.stream.Collectors.toMap(
                            com.pet.entity.Pet::getId,
                            com.pet.entity.Pet::getName));
            result.getRecords().forEach(record ->
                    record.setPetName(petNameMap.getOrDefault(record.getPetId(), "未知")));
        }

        return Result.success(result);
    }

    /**
     * 获取我的健康记录列表（简化版）
     */
    @GetMapping("/my")
    public Result<List<HealthRecord>> getMyHealthRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        // 获取用户的宠物ID列表
        QueryWrapper<Pet> petWrapper = new QueryWrapper<>();
        petWrapper.eq("user_id", userId).eq("status", 1);
        List<Pet> userPets = petService.list(petWrapper);
        
        if (userPets.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<Long> petIds = userPets.stream().map(Pet::getId).toList();
        
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("pet_id", petIds)
                   .orderByDesc("record_date")
                   .orderByDesc("create_time")
                   .last("LIMIT 10"); // 只返回最近10条记录
        
        List<HealthRecord> records = healthRecordService.list(queryWrapper);
        
        return Result.success(records);
    }

    /**
     * 获取健康记录详情
     */
    @GetMapping("/records/{recordId}")
    public Result<HealthRecord> getHealthRecordDetail(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        HealthRecord record = healthRecordService.getById(recordId);
        if (record == null || record.getStatus() == 0) {
            return Result.error(404, "健康记录不存在");
        }
        
        // 检查权限
        if (!"ADMIN".equals(userRole)) {
            Pet pet = petService.getById(record.getPetId());
            if (pet == null || !pet.getUserId().equals(userId)) {
                return Result.error(403, "无权限访问");
            }
        }
        
        return Result.success(record);
    }

    /**
     * 创建健康记录
     */
    @PostMapping("/records")
    public Result<String> createHealthRecord(@RequestBody HealthRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        // 检查宠物权限
        Pet pet = petService.getById(record.getPetId());
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        
        if (!"ADMIN".equals(userRole) && !pet.getUserId().equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        boolean success = healthRecordService.save(record);
        
        if (success) {
            return Result.success("创建成功", null);
        } else {
            return Result.error(500, "创建失败");
        }
    }

    /**
     * 更新健康记录
     */
    @PutMapping("/records/{recordId}")
    public Result<String> updateHealthRecord(
            @PathVariable Long recordId,
            @RequestBody HealthRecord record,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        HealthRecord existingRecord = healthRecordService.getById(recordId);
        if (existingRecord == null || existingRecord.getStatus() == 0) {
            return Result.error(404, "健康记录不存在");
        }
        
        // 检查权限
        if (!"ADMIN".equals(userRole)) {
            Pet pet = petService.getById(existingRecord.getPetId());
            if (pet == null || !pet.getUserId().equals(userId)) {
                return Result.error(403, "无权限操作");
            }
        }
        
        // 更新字段
        existingRecord.setRecordType(record.getRecordType());
        existingRecord.setRecordDate(record.getRecordDate());
        existingRecord.setWeight(record.getWeight());
        existingRecord.setTemperature(record.getTemperature());
        existingRecord.setSymptoms(record.getSymptoms());
        existingRecord.setDiagnosis(record.getDiagnosis());
        existingRecord.setTreatment(record.getTreatment());
        existingRecord.setDoctorName(record.getDoctorName());
        existingRecord.setHospitalName(record.getHospitalName());
        existingRecord.setMedicine(record.getMedicine());
        existingRecord.setCost(record.getCost());
        existingRecord.setNextVisitDate(record.getNextVisitDate());
        existingRecord.setNotes(record.getNotes());
        existingRecord.setUpdateTime(LocalDateTime.now());
        
        boolean success = healthRecordService.updateById(existingRecord);
        
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 删除健康记录（逻辑删除）
     */
    @DeleteMapping("/records/{recordId}")
    public Result<String> deleteHealthRecord(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        HealthRecord record = healthRecordService.getById(recordId);
        if (record == null) {
            return Result.error(404, "健康记录不存在");
        }

        // 检查权限
        if (!"ADMIN".equals(userRole)) {
            Pet pet = petService.getById(record.getPetId());
            if (pet == null || !pet.getUserId().equals(userId)) {
                return Result.error(403, "无权限操作");
            }
        }

        // 使用 removeById 触发 MyBatis-Plus 全局逻辑删除
        boolean success = healthRecordService.removeById(recordId);

        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 获取健康趋势数据（体重/体温折线图）
     */
    @GetMapping("/trend/{petId}")
    public Result<Map<String, Object>> getHealthTrend(@PathVariable Long petId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        // 权限校验
        if (!"ADMIN".equals(userRole)) {
            Pet pet = petService.getById(petId);
            if (pet == null || !pet.getUserId().equals(userId)) {
                return Result.error(403, "无权限访问");
            }
        }

        QueryWrapper<HealthRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("pet_id", petId)
               .isNotNull("record_date")
               .orderByAsc("record_date");

        List<HealthRecord> records = healthRecordService.list(wrapper);

        // 体重趋势
        List<Map<String, Object>> weightTrend = records.stream()
            .filter(r -> r.getWeight() != null)
            .map(r -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", r.getRecordDate().toString());
                point.put("value", r.getWeight());
                return point;
            }).toList();

        // 体温趋势
        List<Map<String, Object>> temperatureTrend = records.stream()
            .filter(r -> r.getTemperature() != null)
            .map(r -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", r.getRecordDate().toString());
                point.put("value", r.getTemperature());
                return point;
            }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("weightTrend", weightTrend);
        result.put("temperatureTrend", temperatureTrend);

        return Result.success(result);
    }

    /**
     * 获取健康统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getHealthStatistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        
        // 如果不是管理员，只统计自己宠物的数据
        if (!"ADMIN".equals(userRole)) {
            QueryWrapper<Pet> petWrapper = new QueryWrapper<>();
            petWrapper.eq("user_id", userId).eq("status", 1);
            List<Pet> userPets = petService.list(petWrapper);
            
            if (userPets.isEmpty()) {
                statistics.put("totalRecords", 0);
                statistics.put("checkupCount", 0);
                statistics.put("vaccineCount", 0);
                statistics.put("treatmentCount", 0);
                return Result.success(statistics);
            }
            
            List<Long> petIds = userPets.stream().map(Pet::getId).toList();
            queryWrapper.in("pet_id", petIds);
        }
        
        // 总记录数
        long totalRecords = healthRecordService.count(queryWrapper);
        statistics.put("totalRecords", totalRecords);
        
        // 各类型记录数
        QueryWrapper<HealthRecord> checkupWrapper = queryWrapper.clone();
        checkupWrapper.eq("record_type", "CHECKUP");
        long checkupCount = healthRecordService.count(checkupWrapper);
        statistics.put("checkupCount", checkupCount);
        
        QueryWrapper<HealthRecord> vaccineWrapper = queryWrapper.clone();
        vaccineWrapper.eq("record_type", "VACCINE");
        long vaccineCount = healthRecordService.count(vaccineWrapper);
        statistics.put("vaccineCount", vaccineCount);
        
        QueryWrapper<HealthRecord> treatmentWrapper = queryWrapper.clone();
        treatmentWrapper.eq("record_type", "ILLNESS");
        long treatmentCount = healthRecordService.count(treatmentWrapper);
        statistics.put("treatmentCount", treatmentCount);
        
        return Result.success(statistics);
    }
}