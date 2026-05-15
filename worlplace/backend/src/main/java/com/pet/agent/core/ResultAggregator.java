package com.pet.agent.core;

import com.pet.agent.core.CollaborationManager.AgentResult;
import com.pet.agent.core.ConflictResolver.Escalation;
import com.pet.agent.core.ConflictResolver.ResolutionResult;
import com.pet.agent.core.ConflictResolver.ResolutionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ResultAggregator {

    private static final Logger log = LoggerFactory.getLogger(ResultAggregator.class);

    private static final List<String> DISPLAY_ORDER = java.util.Arrays.asList("HEALTH", "GENERAL", "MALL", "PET", "COMMUNITY", "CHAT");

    public AggregatedResponse aggregate(ResolutionResult resolution) {
        if (resolution.getType() == ResolutionType.ALL_FAILED) {
            return new AggregatedResponse(
                    "抱歉铲屎官，所有服务暂时不可用，请稍后再试或联系人工客服。",
                    null, List.of(), false);
        }

        List<AgentResult> results = new ArrayList<>(resolution.getResolved());
        results.sort(Comparator.comparingInt(r -> {
            int idx = DISPLAY_ORDER.indexOf(r.getDomain());
            return idx >= 0 ? idx : 99;
        }));

        List<AgentResult> deduped = deduplicate(results);
        String text = buildCompositeReply(deduped);
        List<StructuredArtifact> artifacts = collectArtifacts(deduped);
        List<Escalation> escalations = resolution.getEscalations();

        return new AggregatedResponse(text, artifacts, escalations, resolution.needsUserInput());
    }

    private List<AgentResult> deduplicate(List<AgentResult> results) {
        List<AgentResult> deduped = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (AgentResult r : results) {
            if (r.getOutput() == null || r.getOutput().isBlank()) continue;
            String norm = r.getOutput().trim().toLowerCase();
            if (norm.length() < 20) { deduped.add(r); continue; }
            boolean dup = false;
            for (String s : seen) {
                if (similarity(norm, s) > 0.7) { dup = true; break; }
            }
            if (!dup) { seen.add(norm); deduped.add(r); }
            else log.debug("[Aggregator] Deduplicated: {}", r.getDomain());
        }
        return deduped;
    }

    private double similarity(String a, String b) {
        String[] wa = a.split("\\s+"), wb = b.split("\\s+");
        Set<String> sa = Set.of(wa), sb = Set.of(wb);
        long inter = sa.stream().filter(sb::contains).count();
        long union = sa.size() + sb.size() - inter;
        return union == 0 ? 0 : (double) inter / union;
    }

    private String buildCompositeReply(List<AgentResult> results) {
        if (results.isEmpty()) return "抱歉铲屎官，我暂时无法处理这个请求。";
        if (results.size() == 1) return results.get(0).getOutput();

        StringBuilder sb = new StringBuilder();
        for (AgentResult r : results) {
            if (r.getOutput() == null || r.getOutput().isBlank()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(r.getOutput());
        }
        return sb.toString();
    }

    private List<StructuredArtifact> collectArtifacts(List<AgentResult> results) {
        List<StructuredArtifact> list = new ArrayList<>();
        for (AgentResult r : results) {
            if (r.getAgentName() != null) {
                list.add(new StructuredArtifact(r.getDomain(), r.getAgentName(),
                        r.getOutput() != null ? r.getOutput() : "", r.getState().name()));
            }
        }
        return list;
    }

    // ── Response ─────────────────────────────────────────────────

    public static class AggregatedResponse {
        private final String text;
        private final List<StructuredArtifact> artifacts;
        private final List<Escalation> escalations;
        private final boolean needsUserInput;

        public AggregatedResponse(String text, List<StructuredArtifact> artifacts,
                                  List<Escalation> escalations, boolean needsUserInput) {
            this.text = text;
            this.artifacts = artifacts != null ? artifacts : List.of();
            this.escalations = escalations != null ? escalations : List.of();
            this.needsUserInput = needsUserInput;
        }

        public String getText() { return text; }
        public List<StructuredArtifact> getArtifacts() { return artifacts; }
        public List<Escalation> getEscalations() { return escalations; }
        public boolean needsUserInput() { return needsUserInput; }
    }

    public static class StructuredArtifact {
        private final String domain, agentName, content, state;
        public StructuredArtifact(String domain, String agentName, String content, String state) {
            this.domain = domain; this.agentName = agentName; this.content = content; this.state = state;
        }
        public String getDomain() { return domain; }
        public String getAgentName() { return agentName; }
        public String getContent() { return content; }
        public String getState() { return state; }
    }
}
