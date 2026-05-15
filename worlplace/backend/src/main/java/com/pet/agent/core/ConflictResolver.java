package com.pet.agent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConflictResolver {

    private static final Logger log = LoggerFactory.getLogger(ConflictResolver.class);

    private static final Map<String, Integer> PRIORITY = new java.util.HashMap<>();
    static {
        PRIORITY.put("HEALTH", 100);
        PRIORITY.put("GENERAL", 80);
        PRIORITY.put("MALL", 60);
        PRIORITY.put("PET", 50);
        PRIORITY.put("COMMUNITY", 40);
        PRIORITY.put("CHAT", 30);
    }

    private static final Map<String, Set<String>> CONFLICT_PAIRS = Map.of(
            "HEALTH", Set.of("MALL"),
            "MALL", Set.of("HEALTH")
    );

    public ResolutionResult resolve(List<CollaborationManager.AgentResult> results) {
        if (results == null || results.isEmpty()) return ResolutionResult.empty();

        List<CollaborationManager.AgentResult> filtered = results.stream()
                .filter(r -> !r.isFailed()).collect(Collectors.toList());

        if (filtered.isEmpty())
            return new ResolutionResult(filtered, Collections.emptyList(), ResolutionType.ALL_FAILED);
        if (filtered.size() == 1)
            return new ResolutionResult(filtered, Collections.emptyList(), ResolutionType.NO_CONFLICT);

        List<Conflict> conflicts = detectConflicts(filtered);
        if (conflicts.isEmpty())
            return new ResolutionResult(filtered, Collections.emptyList(), ResolutionType.NO_CONFLICT);

        List<Escalation> escalations = new ArrayList<>();
        for (Conflict conflict : conflicts) {
            ResolutionAttempt attempt = resolveConflict(conflict);
            if (attempt.type == ResolutionType.ESCALATED) {
                escalations.add(attempt.escalation);
            }
        }

        ResolutionType type = escalations.isEmpty()
                ? ResolutionType.AUTO_RESOLVED : ResolutionType.ESCALATED;
        return new ResolutionResult(filtered, escalations, type);
    }

    private List<Conflict> detectConflicts(List<CollaborationManager.AgentResult> results) {
        List<Conflict> conflicts = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            for (int j = i + 1; j < results.size(); j++) {
                String a = results.get(i).getDomain();
                String b = results.get(j).getDomain();
                if (isConflicting(a, b)) {
                    conflicts.add(new Conflict(a, b,
                            results.get(i).getConfidence(), results.get(j).getConfidence(),
                            PRIORITY.getOrDefault(a, 50), PRIORITY.getOrDefault(b, 50)));
                }
            }
        }
        return conflicts;
    }

    private boolean isConflicting(String a, String b) {
        Set<String> c = CONFLICT_PAIRS.get(a);
        return c != null && c.contains(b);
    }

    private ResolutionAttempt resolveConflict(Conflict c) {
        int prioDiff = c.priorityA - c.priorityB;

        // Layer 1: priority
        if (Math.abs(prioDiff) > 0) {
            String winner = prioDiff > 0 ? c.domainA : c.domainB;
            log.info("[Conflict] L1 priority: {} wins", winner);
            return ResolutionAttempt.auto();
        }

        // Layer 2: confidence
        double confDiff = c.confidenceA - c.confidenceB;
        if (Math.abs(confDiff) > 0.05) {
            String winner = confDiff > 0 ? c.domainA : c.domainB;
            log.info("[Conflict] L2 confidence: {} wins (%.2f vs %.2f)", winner,
                    confDiff > 0 ? c.confidenceA : c.confidenceB,
                    confDiff > 0 ? c.confidenceB : c.confidenceA);
            return ResolutionAttempt.auto();
        }

        // Layer 3: escalate
        log.info("[Conflict] L3 escalation: {} vs {}", c.domainA, c.domainB);
        Escalation esc = new Escalation(c.domainA, c.domainB,
                String.format("%s和%s给出了不同建议，请铲屎官选择：",
                        domainLabel(c.domainA), domainLabel(c.domainB)),
                List.of(
                        new Escalation.Choice("A", domainLabel(c.domainA) + "的建议", c.domainA),
                        new Escalation.Choice("B", domainLabel(c.domainB) + "的建议", c.domainB)
                ));
        return ResolutionAttempt.escalated(esc);
    }

    private String domainLabel(String d) {
        switch (d) {
            case "HEALTH": return "健康专家";
            case "MALL": return "商城导购";
            case "GENERAL": return "宠物顾问";
            case "PET": return "档案管家";
            case "COMMUNITY": return "社区管家";
            default: return "客服助手";
        }
    }

    // ── Types ────────────────────────────────────────────────────

    public enum ResolutionType { NO_CONFLICT, AUTO_RESOLVED, ESCALATED, ALL_FAILED }

    public static class ResolutionResult {
        private final List<CollaborationManager.AgentResult> resolved;
        private final List<Escalation> escalations;
        private final ResolutionType type;

        public ResolutionResult(List<CollaborationManager.AgentResult> resolved,
                                List<Escalation> escalations, ResolutionType type) {
            this.resolved = resolved; this.escalations = escalations; this.type = type;
        }

        static ResolutionResult empty() {
            return new ResolutionResult(List.of(), List.of(), ResolutionType.NO_CONFLICT);
        }

        public List<CollaborationManager.AgentResult> getResolved() { return resolved; }
        public List<Escalation> getEscalations() { return escalations; }
        public ResolutionType getType() { return type; }
        public boolean needsUserInput() { return type == ResolutionType.ESCALATED; }
    }

    static class ResolutionAttempt {
        final ResolutionType type;
        final Escalation escalation;
        ResolutionAttempt(ResolutionType t, Escalation e) { this.type = t; this.escalation = e; }
        static ResolutionAttempt auto() { return new ResolutionAttempt(ResolutionType.AUTO_RESOLVED, null); }
        static ResolutionAttempt escalated(Escalation e) { return new ResolutionAttempt(ResolutionType.ESCALATED, e); }
    }

    static class Conflict {
        final String domainA, domainB;
        final double confidenceA, confidenceB;
        final int priorityA, priorityB;
        Conflict(String domainA, String domainB, double confidenceA, double confidenceB,
                 int priorityA, int priorityB) {
            this.domainA = domainA; this.domainB = domainB;
            this.confidenceA = confidenceA; this.confidenceB = confidenceB;
            this.priorityA = priorityA; this.priorityB = priorityB;
        }
    }

    public static class Escalation {
        private final String domainA, domainB, message;
        private final List<Choice> choices;

        public Escalation(String a, String b, String msg, List<Choice> choices) {
            this.domainA = a; this.domainB = b; this.message = msg; this.choices = choices;
        }

        public String getDomainA() { return domainA; }
        public String getDomainB() { return domainB; }
        public String getMessage() { return message; }
        public List<Choice> getChoices() { return choices; }

        public static class Choice {
            private final String id, label, preferredDomain;
            public Choice(String id, String label, String preferredDomain) {
                this.id = id; this.label = label; this.preferredDomain = preferredDomain;
            }
            public String getId() { return id; }
            public String getLabel() { return label; }
            public String getPreferredDomain() { return preferredDomain; }
        }
    }
}
