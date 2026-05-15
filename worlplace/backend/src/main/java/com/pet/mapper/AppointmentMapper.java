package com.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
    
    // 根据ID直接查询，不经过逻辑删除过滤
    @Select("SELECT * FROM appointments WHERE id = #{id}")
    Appointment selectByIdRaw(@Param("id") Long id);

    // 直接更新状态，不经过逻辑删除过滤
    @org.apache.ibatis.annotations.Update("UPDATE appointments SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    // 更新评价信息
    @org.apache.ibatis.annotations.Update("UPDATE appointments SET rating = #{rating}, review = #{review}, update_time = NOW() WHERE id = #{id}")
    int updateReviewById(@Param("id") Long id, @Param("rating") Integer rating, @Param("review") String review);

    @Select("SELECT SUM(total_price) FROM appointments WHERE status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Select("SELECT s.category, COUNT(*) as count FROM appointments a " +
            "JOIN services s ON a.service_id = s.id " +
            "GROUP BY s.category")
    List<Map<String, Object>> getCategoryDistribution();
    
    // 自定义查询，不应用逻辑删除（因为appointments表的status不是逻辑删除字段）
    @Select({"<script>",
            "SELECT * FROM appointments WHERE user_id = #{userId}",
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>",
            "<if test='serviceId != null'> AND service_id = #{serviceId} </if>",
            "<if test='startDate != null'> AND appointment_datetime &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND appointment_datetime &lt;= #{endDate} </if>",
            "ORDER BY create_time DESC",
            "</script>"})
    List<Appointment> selectMyAppointments(@Param("userId") Long userId,
                                          @Param("status") String status,
                                          @Param("serviceId") Long serviceId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    @Select({"<script>",
            "SELECT COUNT(*) FROM appointments WHERE user_id = #{userId}",
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>",
            "<if test='serviceId != null'> AND service_id = #{serviceId} </if>",
            "<if test='startDate != null'> AND appointment_datetime &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND appointment_datetime &lt;= #{endDate} </if>",
            "</script>"})
    Long countMyAppointments(@Param("userId") Long userId,
                            @Param("status") String status,
                            @Param("serviceId") Long serviceId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
    
    // 管理员查询所有预约（不应用逻辑删除）
    @Select({"<script>",
            "SELECT * FROM appointments WHERE 1=1",
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>",
            "<if test='serviceId != null'> AND service_id = #{serviceId} </if>",
            "<if test='startDate != null'> AND appointment_datetime &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND appointment_datetime &lt;= #{endDate} </if>",
            "ORDER BY create_time DESC",
            "</script>"})
    List<Appointment> selectAllAppointments(@Param("status") String status,
                                           @Param("serviceId") Long serviceId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    @Select({"<script>",
            "SELECT COUNT(*) FROM appointments WHERE 1=1",
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>",
            "<if test='serviceId != null'> AND service_id = #{serviceId} </if>",
            "<if test='startDate != null'> AND appointment_datetime &gt;= #{startDate} </if>",
            "<if test='endDate != null'> AND appointment_datetime &lt;= #{endDate} </if>",
            "</script>"})
    Long countAllAppointments(@Param("status") String status,
                             @Param("serviceId") Long serviceId,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}

