package com.pet.agent.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MultiIntentResult {

    private final List<Intent> intents;
    private final String rawUserMessage;
    private final boolean isMultiIntent;

    public MultiIntentResult(List<Intent> intents, String rawUserMessage) {
        this.intents = intents;
        this.rawUserMessage = rawUserMessage;
        this.isMultiIntent = intents.size() > 1;
    }

    public List<Intent> getIntents() { return intents; }
    public String getRawUserMessage() { return rawUserMessage; }
    public boolean isMultiIntent() { return isMultiIntent; }
    public boolean hasConflicts() { return intents.stream().anyMatch(i -> i.hasConflict()); }

    public List<Intent> sorted() {
        List<Intent> sorted = new java.util.ArrayList<>(intents);
        sorted.sort(Comparator.comparingInt(Intent::getPriority).reversed());
        return Collections.unmodifiableList(sorted);
    }

    public static class Intent {
        private final String domain;
        private final String operation;
        private final int priority;
        private final Set<String> dependsOn;
        private final Set<String> potentialConflicts;
        private final double confidence;

        public Intent(String domain, String operation, int priority,
                      Set<String> dependsOn, Set<String> potentialConflicts, double confidence) {
            this.domain = domain;
            this.operation = operation;
            this.priority = priority;
            this.dependsOn = dependsOn != null ? dependsOn : Collections.emptySet();
            this.potentialConflicts = potentialConflicts != null ? potentialConflicts : Collections.emptySet();
            this.confidence = confidence;
        }

        public Intent(String domain, String operation, int priority) {
            this(domain, operation, priority, Collections.emptySet(), Collections.emptySet(), 0.9);
        }

        public String getDomain() { return domain; }
        public String getOperation() { return operation; }
        public int getPriority() { return priority; }
        public Set<String> getDependsOn() { return dependsOn; }
        public double getConfidence() { return confidence; }
        public boolean hasConflict() { return !potentialConflicts.isEmpty(); }
        public boolean conflictsWith(String otherDomain) { return potentialConflicts.contains(otherDomain); }
        public Set<String> getPotentialConflicts() { return potentialConflicts; }
    }
}
