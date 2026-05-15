package com.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.entity.Pet;
import com.pet.entity.User;
import com.pet.service.PetService;
import com.pet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠物管理控制器
 */
@RestController
@RequestMapping("/pet")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的宠物列表
     */
    @GetMapping("/list")
    public Result<IPage<Pet>> getPetList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Page<Pet> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        
        queryWrapper.eq("user_id", userId);
        
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
        }
        
        if (type != null && !type.trim().isEmpty()) {
            queryWrapper.eq("type", type.trim());
        }
        
        queryWrapper.orderByDesc("create_time");
        
        IPage<Pet> result = petService.page(page, queryWrapper);
        
        return Result.success(result);
    }

    /**
     * 获取我的宠物列表（简化版，用于下拉选择）
     */
    @GetMapping("/my")
    public Result<List<Pet>> getMyPetList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        
        List<Pet> pets = petService.list(queryWrapper);
        
        return Result.success(pets);
    }

    /**
     * 获取所有宠物列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<IPage<Map<String, Object>>> getAllPetList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限访问");
        }

        Page<Pet> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like("name", name.trim());
        }
        
        if (type != null && !type.trim().isEmpty()) {
            queryWrapper.eq("type", type.trim());
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        } else {
            // 不加 status 条件，让 @TableLogic 自动过滤已删除的
        }
        
        queryWrapper.orderByDesc("create_time");
        
        IPage<Pet> petPage = petService.page(page, queryWrapper);
        
        // 构造返回数据，包含用户信息
        IPage<Map<String, Object>> result = new Page<>();
        result.setCurrent(petPage.getCurrent());
        result.setSize(petPage.getSize());
        result.setTotal(petPage.getTotal());
        result.setPages(petPage.getPages());
        
        List<Map<String, Object>> records = petPage.getRecords().stream().map(pet -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pet.getId());
            map.put("name", pet.getName());
            map.put("type", pet.getType());
            map.put("breed", pet.getBreed());
            map.put("gender", pet.getGender());
            map.put("age", pet.getAge());
            map.put("color", pet.getColor());
            map.put("weight", pet.getWeight());
            map.put("birthDate", pet.getBirthDate());
            map.put("chipNumber", pet.getChipNumber());
            map.put("allergy", pet.getAllergy());
            map.put("specialNotes", pet.getSpecialNotes());
            map.put("status", pet.getStatus());
            map.put("createTime", pet.getCreateTime());
            map.put("updateTime", pet.getUpdateTime());
            
            // 获取用户信息
            if (pet.getUserId() != null) {
                User user = userService.getById(pet.getUserId());
                if (user != null) {
                    map.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                    map.put("userPhone", user.getPhone());
                    map.put("userEmail", user.getEmail());
                }
            }
            
            return map;
        }).toList();
        
        result.setRecords(records);
        
        return Result.success(result);
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{petId}")
    public Result<Pet> getPetDetail(@PathVariable Long petId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        
        // 检查权限：管理员或宠物主人
        if (!"ADMIN".equals(userRole) && !pet.getUserId().equals(userId)) {
            return Result.error(403, "无权限访问");
        }
        
        return Result.success(pet);
    }

    /**
     * 创建宠物
     */
    @PostMapping
    public Result<String> createPet(@RequestBody Pet pet, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        pet.setUserId(userId);
        pet.setStatus(1);
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        
        boolean success = petService.save(pet);
        
        if (success) {
            return Result.success("创建成功", null);
        } else {
            return Result.error(500, "创建失败");
        }
    }

    /**
     * 更新宠物信息
     */
    @PutMapping("/{petId}")
    public Result<String> updatePet(
            @PathVariable Long petId,
            @RequestBody Pet pet,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        Pet existingPet = petService.getById(petId);
        if (existingPet == null) {
            return Result.error(404, "宠物不存在");
        }
        
        // 检查权限：管理员或宠物主人
        if (!"ADMIN".equals(userRole) && !existingPet.getUserId().equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        // 更新字段
        existingPet.setName(pet.getName());
        existingPet.setType(pet.getType());
        existingPet.setBreed(pet.getBreed());
        existingPet.setGender(pet.getGender());
        existingPet.setAge(pet.getAge());
        existingPet.setColor(pet.getColor());
        existingPet.setWeight(pet.getWeight());
        existingPet.setBirthDate(pet.getBirthDate());
        existingPet.setChipNumber(pet.getChipNumber());
        existingPet.setAllergy(pet.getAllergy());
        existingPet.setSpecialNotes(pet.getSpecialNotes());
        if (pet.getAvatar() != null && !pet.getAvatar().isEmpty()) {
            existingPet.setAvatar(pet.getAvatar());
        }
        existingPet.setUpdateTime(LocalDateTime.now());
        
        boolean success = petService.updateById(existingPet);
        
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/{petId}")
    public Result<String> deletePet(@PathVariable Long petId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");
        
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        
        // 检查权限：管理员或宠物主人
        if (!"ADMIN".equals(userRole) && !pet.getUserId().equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        // 软删除
        pet.setStatus(0);
        pet.setUpdateTime(LocalDateTime.now());
        
        boolean success = petService.updateById(pet);
        
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 审核宠物（管理员）
     */
    @PutMapping("/admin/{petId}/audit")
    public Result<String> auditPet(
            @PathVariable Long petId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限操作");
        }
        
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        
        Integer status = (Integer) params.get("status");
        if (status == null) {
            return Result.error(400, "状态参数不能为空");
        }
        
        pet.setStatus(status);
        pet.setUpdateTime(LocalDateTime.now());
        
        boolean success = petService.updateById(pet);
        
        if (success) {
            String message = status == 1 ? "恢复成功" : "删除成功";
            return Result.success(message, null);
        } else {
            return Result.error(500, "操作失败");
        }
    }

    /**
     * 获取宠物统计信息（管理员）
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getPetStatistics(HttpServletRequest request) {
        // 检查管理员权限
        String userRole = (String) request.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return Result.error(403, "无权限访问");
        }
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 总数统计
        long totalCount = petService.count();
        statistics.put("totalCount", totalCount);
        
        // 类型分布
        Map<String, Long> typeDistribution = new HashMap<>();
        typeDistribution.put("DOG", petService.count(new QueryWrapper<Pet>().eq("type", "DOG")));
        typeDistribution.put("CAT", petService.count(new QueryWrapper<Pet>().eq("type", "CAT")));
        typeDistribution.put("BIRD", petService.count(new QueryWrapper<Pet>().eq("type", "BIRD")));
        typeDistribution.put("OTHER", petService.count(new QueryWrapper<Pet>().eq("type", "OTHER")));
        statistics.put("typeDistribution", typeDistribution);
        
        // 性别分布
        Map<String, Long> genderDistribution = new HashMap<>();
        genderDistribution.put("公", petService.count(new QueryWrapper<Pet>().eq("gender", 1)));
        genderDistribution.put("母", petService.count(new QueryWrapper<Pet>().eq("gender", 2)));
        genderDistribution.put("未知", petService.count(new QueryWrapper<Pet>().eq("gender", 0)));
        statistics.put("genderDistribution", genderDistribution);
        
        // 年龄分布
        Map<String, Long> ageDistribution = new HashMap<>();
        ageDistribution.put("幼年(0-1岁)", petService.count(new QueryWrapper<Pet>().between("age", 0, 1)));
        ageDistribution.put("青年(2-5岁)", petService.count(new QueryWrapper<Pet>().between("age", 2, 5)));
        ageDistribution.put("成年(6-10岁)", petService.count(new QueryWrapper<Pet>().between("age", 6, 10)));
        ageDistribution.put("老年(10岁以上)", petService.count(new QueryWrapper<Pet>().gt("age", 10)));
        statistics.put("ageDistribution", ageDistribution);
        
        // 品种分布（简化版）
        Map<String, Long> breedDistribution = new HashMap<>();
        breedDistribution.put("金毛", petService.count(new QueryWrapper<Pet>().like("breed", "金毛")));
        breedDistribution.put("拉布拉多", petService.count(new QueryWrapper<Pet>().like("breed", "拉布拉多")));
        breedDistribution.put("哈士奇", petService.count(new QueryWrapper<Pet>().like("breed", "哈士奇")));
        breedDistribution.put("英短", petService.count(new QueryWrapper<Pet>().like("breed", "英短")));
        breedDistribution.put("美短", petService.count(new QueryWrapper<Pet>().like("breed", "美短")));
        statistics.put("breedDistribution", breedDistribution);
        
        return Result.success(statistics);
    }

    /**
     * 获取宠物品种列表
     */
    @GetMapping("/breeds")
    public Result<List<String>> getPetBreeds(@RequestParam(required = false) String type) {
        List<String> breeds = List.of(
            "金毛寻回犬", "拉布拉多", "哈士奇", "萨摩耶", "边境牧羊犬", "德国牧羊犬",
            "英国短毛猫", "美国短毛猫", "波斯猫", "暹罗猫", "布偶猫", "缅因猫",
            "虎皮鹦鹉", "玄凤鹦鹉", "文鸟", "金丝雀", "其他"
        );
        
        return Result.success(breeds);
    }
}