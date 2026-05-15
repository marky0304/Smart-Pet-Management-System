package com.pet.agent.core;

public enum OperationType {

    QUERY("查询"),
    ADD("新增"),
    MODIFY("修改"),
    DELETE("删除"),
    CHAT("闲聊");

    private final String label;

    OperationType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
