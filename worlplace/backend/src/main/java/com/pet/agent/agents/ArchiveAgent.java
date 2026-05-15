package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.entity.Pet;
import com.pet.service.PetService;
import org.springframework.stereotype.Component;

@Component
public class ArchiveAgent implements Agent {

    private final PetService petService;

    public ArchiveAgent(PetService petService) {
        this.petService = petService;
    }

    @Override
    public DomainType getDomain() {
        return DomainType.PET;
    }

    @Override
    public String getName() {
        return "宠物档案Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        return PromptTemplates.buildPrompt(PromptTemplates.ARCHIVE_ROLE, operation.name());
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        if (!ctx.hasPets()) {
            return "当前用户还没有添加宠物档案，请引导用户先添加宠物信息。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("当前用户的宠物档案：\n");
        for (int i = 0; i < ctx.getPets().size(); i++) {
            Pet pet = ctx.getPets().get(i);
            sb.append(i + 1).append(". ");
            sb.append(typeLabel(pet.getType())).append(" - ");
            sb.append("名字：").append(pet.getName());
            if (pet.getBreed() != null) sb.append("，品种：").append(pet.getBreed());
            if (pet.getAge() != null) sb.append("，年龄：").append(pet.getAge()).append("岁");
            if (pet.getWeight() != null) sb.append("，体重：").append(pet.getWeight()).append("kg");
            if (pet.getGender() != null) sb.append("，性别：").append(genderLabel(pet.getGender()));
            if (pet.getColor() != null) sb.append("，毛色：").append(pet.getColor());
            if (pet.getBirthDate() != null) sb.append("，出生日期：").append(pet.getBirthDate());
            if (pet.getAllergy() != null && !pet.getAllergy().isEmpty())
                sb.append("，过敏史：").append(pet.getAllergy());
            if (pet.getSpecialNotes() != null && !pet.getSpecialNotes().isEmpty())
                sb.append("，备注：").append(pet.getSpecialNotes());
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String typeLabel(String type) {
        if ("DOG".equals(type)) return "狗狗";
        if ("CAT".equals(type)) return "猫咪";
        if ("BIRD".equals(type)) return "鸟类";
        return "其他宠物";
    }

    private static String genderLabel(Integer gender) {
        if (gender == null) return "未知";
        if (gender == 1) return "公";
        if (gender == 2) return "母";
        return "未知";
    }
}
