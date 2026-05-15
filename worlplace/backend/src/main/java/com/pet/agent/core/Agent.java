package com.pet.agent.core;

public interface Agent {

    DomainType getDomain();

    String getName();

    String buildSystemPrompt(AgentContext ctx, OperationType operation);

    String buildDataContext(AgentContext ctx);
}
