package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.dto.AppointmentQueryRequest;
import com.pet.entity.Appointment;
import com.pet.entity.Pet;
import com.pet.service.AppointmentService;
import com.pet.vo.AppointmentVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdvisorAgent implements Agent {

    private final AppointmentService appointmentService;

    public AdvisorAgent(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    public DomainType getDomain() {
        return DomainType.GENERAL;
    }

    @Override
    public String getName() {
        return "宠物专家顾问Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        return PromptTemplates.buildPrompt(PromptTemplates.ADVISOR_ROLE, operation.name());
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        StringBuilder sb = new StringBuilder();

        if (!ctx.hasPets()) {
            sb.append("当前用户还没有添加宠物，可建议用户先添加宠物信息以获得更个性化的建议。\n");
        } else {
            sb.append("用户宠物概况（用于个性化建议）：\n");
            for (Pet pet : ctx.getPets()) {
                sb.append("- ").append(pet.getName()).append("：");
                if (pet.getBreed() != null) sb.append(pet.getBreed()).append("，");
                if (pet.getAge() != null) sb.append(pet.getAge()).append("岁，");
                if (pet.getWeight() != null) sb.append(pet.getWeight()).append("kg，");
                if (pet.getAllergy() != null && !pet.getAllergy().isEmpty())
                    sb.append("过敏：").append(pet.getAllergy()).append("，");
                sb.append("\n");
            }
        }

        try {
            AppointmentQueryRequest queryReq = new AppointmentQueryRequest();
            queryReq.setPageNum(1);
            queryReq.setPageSize(10);
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<AppointmentVO> page =
                    appointmentService.getUpcomingAppointments(queryReq, ctx.getUserId());
            List<AppointmentVO> voList = page.getRecords();

            if (voList != null && !voList.isEmpty()) {
                sb.append("\n用户近期预约记录：\n");
                for (AppointmentVO vo : voList) {
                    String petName = ctx.getPets().stream()
                            .filter(p -> p.getId().equals(vo.getPetId()))
                            .findFirst()
                            .map(Pet::getName)
                            .orElse("未知宠物");
                    sb.append("- 预约号：").append(vo.getOrderNo())
                            .append("，宠物：").append(petName)
                            .append("，服务：").append(serviceTypeLabel(vo.getServiceType()))
                            .append("，时间：").append(vo.getAppointmentDatetime())
                            .append("，状态：").append(statusLabel(vo.getStatus()))
                            .append("，金额：¥").append(vo.getTotalPrice())
                            .append("\n");
                }
                sb.append("\n提示：可结合用户的预约记录给出个性化建议，");
                sb.append("如提醒即将到来的预约、推荐相关服务或护理方案。\n");
            }
        } catch (Exception e) {
            sb.append("\n（预约数据暂时不可用）\n");
        }

        return sb.toString();
    }

    private String serviceTypeLabel(String type) {
        if (type == null) return "未知";
        switch (type) {
            case "BATH": return "洗澡";
            case "GROOM": return "美容";
            case "BOARD": return "寄养";
            case "MEDICAL": return "医疗";
            case "TRAIN": return "训练";
            default: return type;
        }
    }

    private String statusLabel(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "PENDING": return "待确认";
            case "CONFIRMED": return "已确认";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}
