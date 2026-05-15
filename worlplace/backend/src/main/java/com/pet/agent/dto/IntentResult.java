package com.pet.agent.dto;

public class IntentResult {

    private String operation;
    private String domain;
    private double confidence;

    public IntentResult() {}

    public IntentResult(String operation, String domain, double confidence) {
        this.operation = operation;
        this.domain = domain;
        this.confidence = confidence;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
