package com.pet.agent.vo;

public class ChatResponse {

    private String reply;
    private String operation;
    private String domain;
    private String agentType;
    private String sessionId;
    private String navigate;
    private String navigateParams;
    private String action;

    public static ChatResponse ok(String reply, String operation, String domain,
                                   String agentType, String sessionId) {
        ChatResponse r = new ChatResponse();
        r.reply = reply;
        r.operation = operation;
        r.domain = domain;
        r.agentType = agentType;
        r.sessionId = sessionId;
        return r;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getNavigate() { return navigate; }
    public void setNavigate(String navigate) { this.navigate = navigate; }
    public String getNavigateParams() { return navigateParams; }
    public void setNavigateParams(String navigateParams) { this.navigateParams = navigateParams; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
