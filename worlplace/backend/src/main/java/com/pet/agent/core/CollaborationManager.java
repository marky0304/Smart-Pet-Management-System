package com.pet.agent.core;

import com.pet.agent.dto.MultiIntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
public class CollaborationManager {

    private static final Logger log = LoggerFactory.getLogger(CollaborationManager.class);

    private final AgentBus agentBus;
    private final Map<String, Agent> agentMap;
    private final ExecutorService executor;
    private final Map<String, TaskState> taskStates = new ConcurrentHashMap<>();

    private static final Map<String, Set<String>> DEPENDENCY_MAP = Map.of(
            "MALL", Set.of("HEALTH"),
            "GENERAL", Set.of("PET"),
            "HEALTH", Set.of("PET")
    );

    public CollaborationManager(AgentBus agentBus, List<Agent> agents) {
        this.agentBus = agentBus;
        this.agentMap = new HashMap<>();
        for (Agent agent : agents) {
            this.agentMap.put(agent.getDomain().name(), agent);
        }
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "agent-worker");
            t.setDaemon(true);
            return t;
        });
    }

    public List<AgentResult> execute(MultiIntentResult intentResult, AgentContext ctx,
                                      String userMessage, List<Map<String, String>> history) {
        List<MultiIntentResult.Intent> allIntents = intentResult.sorted();
        Map<String, Set<String>> depGraph = buildDependencyGraph(allIntents);
        List<List<MultiIntentResult.Intent>> batches = topoBatches(allIntents, depGraph);
        Map<String, AgentResult> results = new ConcurrentHashMap<>();

        for (int batchIdx = 0; batchIdx < batches.size(); batchIdx++) {
            List<MultiIntentResult.Intent> batch = batches.get(batchIdx);
            log.info("[Collab] Batch {}: {}", batchIdx,
                    batch.stream().map(MultiIntentResult.Intent::getDomain).collect(Collectors.toList()));

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (MultiIntentResult.Intent intent : batch) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        AgentResult result = executeSingle(intent, ctx, userMessage, history);
                        results.put(intent.getDomain(), result);
                    } catch (Exception e) {
                        log.error("[Collab] Agent {} failed", intent.getDomain(), e);
                        results.put(intent.getDomain(),
                                new AgentResult(intent.getDomain(), intent.getOperation(),
                                        null, 0.0, TaskState.FAILED, e.getMessage()));
                    }
                }, executor);
                futures.add(future);
            }

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("[Collab] Batch {} timed out", batchIdx);
            } catch (Exception e) {
                log.error("[Collab] Batch {} interrupted", batchIdx, e);
            }
        }

        return new ArrayList<>(results.values());
    }

    private AgentResult executeSingle(MultiIntentResult.Intent intent, AgentContext ctx,
                                       String userMessage, List<Map<String, String>> history) {
        DomainType domain = DomainType.valueOf(intent.getDomain());
        OperationType operation = OperationType.valueOf(intent.getOperation());

        Agent agent = agentMap.getOrDefault(intent.getDomain(), agentMap.get(DomainType.CHAT.name()));
        if (agent == null) {
            return new AgentResult(intent.getDomain(), intent.getOperation(),
                    null, 1.0, TaskState.FAILED, "No agent registered");
        }

        // Read dependency data from bus before building prompt
        Set<String> deps = DEPENDENCY_MAP.getOrDefault(intent.getDomain(), Collections.emptySet());
        StringBuilder depContext = new StringBuilder();
        for (String dep : deps) {
            String depOutput = agentBus.readString("agent." + dep + ".output");
            if (depOutput != null) {
                depContext.append("\n[来自").append(dep).append("Agent的信息]: ").append(depOutput);
            }
        }

        String systemPrompt = agent.buildSystemPrompt(ctx, operation);
        String dataContext = agent.buildDataContext(ctx);
        String fullPrompt = systemPrompt + "\n\n" + dataContext;
        if (depContext.length() > 0) {
            fullPrompt += depContext.toString();
        }

        AgentResult result = new AgentResult(intent.getDomain(), intent.getOperation(),
                null, intent.getConfidence(), TaskState.WORKING, null);
        result.setSystemPrompt(fullPrompt);
        result.setAgentName(agent.getName());

        agentBus.publish("agent." + intent.getDomain() + ".prompt", fullPrompt, agent.getName());

        return result;
    }

    private Map<String, Set<String>> buildDependencyGraph(List<MultiIntentResult.Intent> intents) {
        Map<String, Set<String>> graph = new HashMap<>();
        Set<String> present = new HashSet<>();
        for (MultiIntentResult.Intent i : intents) present.add(i.getDomain());

        for (MultiIntentResult.Intent intent : intents) {
            Set<String> deps = new HashSet<>();
            Set<String> staticDeps = DEPENDENCY_MAP.getOrDefault(intent.getDomain(), Collections.emptySet());
            for (String dep : staticDeps) {
                if (present.contains(dep)) deps.add(dep);
            }
            deps.addAll(intent.getDependsOn());
            graph.put(intent.getDomain(), deps);
        }
        return graph;
    }

    private List<List<MultiIntentResult.Intent>> topoBatches(
            List<MultiIntentResult.Intent> intents, Map<String, Set<String>> depGraph) {

        Map<String, MultiIntentResult.Intent> intentMap = new HashMap<>();
        for (MultiIntentResult.Intent i : intents) intentMap.put(i.getDomain(), i);

        Map<String, Integer> inDegree = new HashMap<>();
        for (MultiIntentResult.Intent i : intents) {
            int degree = 0;
            for (String dep : depGraph.getOrDefault(i.getDomain(), Collections.emptySet())) {
                if (intentMap.containsKey(dep)) degree++;
            }
            inDegree.put(i.getDomain(), degree);
        }

        List<List<MultiIntentResult.Intent>> batches = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (processed.size() < intents.size()) {
            List<MultiIntentResult.Intent> currentBatch = new ArrayList<>();
            for (MultiIntentResult.Intent i : intents) {
                if (!processed.contains(i.getDomain()) && inDegree.get(i.getDomain()) == 0) {
                    currentBatch.add(i);
                }
            }
            if (currentBatch.isEmpty() && processed.size() < intents.size()) {
                log.warn("[Collab] Circular dependency, processing remaining as single batch");
                for (MultiIntentResult.Intent i : intents) {
                    if (!processed.contains(i.getDomain())) {
                        currentBatch.add(i);
                        processed.add(i.getDomain());
                    }
                }
                batches.add(currentBatch);
                break;
            }
            batches.add(currentBatch);
            for (MultiIntentResult.Intent i : currentBatch) {
                processed.add(i.getDomain());
                for (MultiIntentResult.Intent other : intents) {
                    if (depGraph.getOrDefault(other.getDomain(), Collections.emptySet()).contains(i.getDomain())) {
                        inDegree.merge(other.getDomain(), 1, (old, val) -> old - 1);
                    }
                }
            }
        }
        return batches;
    }

    public void shutdown() { executor.shutdown(); }

    // ── Result container ─────────────────────────────────────────

    public static class AgentResult {
        private final String domain;
        private final String operation;
        private String output;
        private String systemPrompt;
        private String agentName;
        private final double confidence;
        private final TaskState state;
        private final String error;

        public AgentResult(String domain, String operation, String output,
                           double confidence, TaskState state, String error) {
            this.domain = domain;
            this.operation = operation;
            this.output = output;
            this.confidence = confidence;
            this.state = state;
            this.error = error;
        }

        public String getDomain() { return domain; }
        public String getOperation() { return operation; }
        public String getOutput() { return output; }
        public void setOutput(String v) { this.output = v; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String v) { this.systemPrompt = v; }
        public String getAgentName() { return agentName; }
        public void setAgentName(String v) { this.agentName = v; }
        public double getConfidence() { return confidence; }
        public TaskState getState() { return state; }
        public String getError() { return error; }
        public boolean isFailed() { return state == TaskState.FAILED || error != null; }
    }
}
