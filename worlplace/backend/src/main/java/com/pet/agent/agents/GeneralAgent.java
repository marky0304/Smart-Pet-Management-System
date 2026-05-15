package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.entity.Pet;
import org.springframework.stereotype.Component;

@Component
public class GeneralAgent implements Agent {

    @Override
    public DomainType getDomain() {
        return DomainType.CHAT;
    }

    @Override
    public String getName() {
        return "通用聊天Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        return PromptTemplates.buildPrompt(PromptTemplates.GENERAL_CHAT_ROLE, operation.name());
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        if (!ctx.hasPets()) {
            return "当前用户还没有添加宠物，但可以自由进行日常聊天和通识知识问答。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("用户宠物背景（仅作了解，非宠物话题无需提及）：\n");
        for (Pet pet : ctx.getPets()) {
            sb.append("- ").append(pet.getName()).append("：");
            if (pet.getBreed() != null) sb.append(pet.getBreed()).append("，");
            if (pet.getAge() != null) sb.append(pet.getAge()).append("岁");
            sb.append("\n");
        }
        sb.append("\n注意：当前对话是日常聊天/通识问答模式，不是宠物咨询。");
        sb.append("如果用户聊非宠物话题，正常回应即可，无需强行关联宠物。");
        sb.append("只有当用户主动提起宠物相关话题时，才结合以上宠物背景给出简单建议。");
        return sb.toString();
    }
}
