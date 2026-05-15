package com.pet.agent.core;

public enum DomainType {

    PET("宠物档案", "ArchiveAgent"),
    HEALTH("健康管理", "HealthAgent"),
    MALL("商城服务", "MallAgent"),
    GENERAL("宠物咨询", "AdvisorAgent"),
    COMMUNITY("社区互动", "CommunityAgent"),
    CHAT("日常聊天", "GeneralAgent");

    private final String label;
    private final String agentName;

    DomainType(String label, String agentName) {
        this.label = label;
        this.agentName = agentName;
    }

    public String getLabel() { return label; }
    public String getAgentName() { return agentName; }
}
