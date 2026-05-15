package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.entity.HealthRecord;
import com.pet.entity.Pet;
import com.pet.service.HealthRecordService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HealthAgent implements Agent {

    private final HealthRecordService healthRecordService;

    public HealthAgent(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @Override
    public DomainType getDomain() {
        return DomainType.HEALTH;
    }

    @Override
    public String getName() {
        return "健康管理Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        String base = PromptTemplates.buildPrompt(PromptTemplates.HEALTH_ROLE, operation.name());
        return base + "\n【商城协作规则】如果铲屎官的健康问题可能需要购买药品、保健品或特殊食品（如益生菌、驱虫药、处方粮等），" +
            "请在回答末尾单独一行输出【推荐商品：关键词1,关键词2】格式的商品搜索关键词，" +
            "商城Agent会自动为铲屎官搜索推荐具体商品。关键词用中文，逗号分隔，控制在1-3个。";
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        if (!ctx.hasPets()) {
            return "用户无宠物，无健康记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("宠物健康摘要：\n");

        for (Pet pet : ctx.getPets()) {
            sb.append(pet.getName()).append("（")
              .append(pet.getBreed() != null ? pet.getBreed() : "?")
              .append(",").append(pet.getAge() != null ? pet.getAge() + "岁" : "?")
              .append(",").append(pet.getWeight() != null ? pet.getWeight() + "kg" : "?")
              .append("）");

            List<HealthRecord> records = healthRecordService.lambdaQuery()
                    .eq(HealthRecord::getPetId, pet.getId())
                    .orderByDesc(HealthRecord::getRecordDate)
                    .last("LIMIT 5")
                    .list();

            if (records.isEmpty()) {
                sb.append(" 无记录\n");
            } else {
                sb.append(":\n");
                for (HealthRecord r : records) {
                    sb.append("  ").append(r.getRecordDate()).append(" ");
                    sb.append(r.getRecordType() != null ? r.getRecordType() : "记录");
                    if (r.getDiagnosis() != null) sb.append(" ").append(r.getDiagnosis());
                    if (r.getMedicine() != null) sb.append(" 用药:").append(r.getMedicine());
                    if (r.getNotes() != null) sb.append(" ").append(r.getNotes());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
