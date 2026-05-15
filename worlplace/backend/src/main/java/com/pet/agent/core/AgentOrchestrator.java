package com.pet.agent.core;

import com.pet.agent.dto.MultiIntentResult;
import com.pet.agent.llm.LlmClient;
import com.pet.entity.Pet;
import com.pet.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final IntentRouter intentRouter;
    private final LlmClient llmClient;
    private final PetService petService;
    private final ActionExecutor actionExecutor;
    private final AgentBus agentBus;
    private final CollaborationManager collaborationManager;
    private final ConflictResolver conflictResolver;
    private final ResultAggregator resultAggregator;
    private final Map<DomainType, Agent> agentMap = new ConcurrentHashMap<>();

    public AgentOrchestrator(IntentRouter intentRouter, LlmClient llmClient, PetService petService,
                             ActionExecutor actionExecutor, AgentBus agentBus,
                             CollaborationManager collaborationManager, ConflictResolver conflictResolver,
                             ResultAggregator resultAggregator, List<Agent> agents) {
        this.intentRouter = intentRouter;
        this.llmClient = llmClient;
        this.petService = petService;
        this.actionExecutor = actionExecutor;
        this.agentBus = agentBus;
        this.collaborationManager = collaborationManager;
        this.conflictResolver = conflictResolver;
        this.resultAggregator = resultAggregator;
        for (Agent agent : agents) {
            this.agentMap.put(agent.getDomain(), agent);
            log.info("Agent registered: {} -> {}", agent.getDomain(), agent.getName());
        }
    }

    public void registerAgent(Agent agent) {
        agentMap.put(agent.getDomain(), agent);
        log.info("Agent registered: {} -> {}", agent.getDomain(), agent.getName());
    }

    // ── Public API ───────────────────────────────────────────────

    public OrchestratorResult process(Long userId, String username, String userMessage,
                                        List<Map<String, String>> history) {
        return process(userId, username, userMessage, history, null);
    }

    public OrchestratorResult process(Long userId, String username, String userMessage,
                                        List<Map<String, String>> history, String currentPage) {
        agentBus.reset();

        AgentContext ctx = buildContext(userId, username, history);
        if (currentPage != null && !currentPage.isEmpty()) {
            ctx.setCurrentPage(currentPage);
        }

        MultiIntentResult multiIntent = intentRouter.routeMulti(userMessage);

        if (!multiIntent.isMultiIntent()) {
            // Single intent — original flow for full backward compat
            return processSingleIntent(multiIntent.getIntents().get(0), ctx, userMessage, history);
        }

        return processMultiIntent(multiIntent, ctx, userMessage, history);
    }

    // ── Single-intent path (original logic, fully preserved) ─────

    private OrchestratorResult processSingleIntent(MultiIntentResult.Intent intent, AgentContext ctx,
                                                    String userMessage, List<Map<String, String>> history) {
        DomainType domain = DomainType.valueOf(intent.getDomain());
        OperationType operation = OperationType.valueOf(intent.getOperation());

        Agent agent = agentMap.getOrDefault(domain, agentMap.get(DomainType.CHAT));
        if (agent == null) {
            return new OrchestratorResult("抱歉，系统初始化中，请稍后再试。",
                    intent.getOperation(), intent.getDomain(), "NONE");
        }

        String systemPrompt = agent.buildSystemPrompt(ctx, operation);
        String dataContext = agent.buildDataContext(ctx);
        String fullSystemPrompt = systemPrompt + "\n\n" + dataContext;

        String reply;
        if (history != null && !history.isEmpty()) {
            reply = llmClient.chatWithHistory(fullSystemPrompt, history, userMessage);
        } else {
            reply = llmClient.chat(fullSystemPrompt, userMessage);
        }

        ActionExecutor.ActionResult actionResult = actionExecutor.process(reply, ctx.getUserId());
        String confirmation = actionResult.getConfirmation();
        String finalReply;
        String action = null;
        if (confirmation != null) {
            finalReply = actionResult.getCleanReply() + "\n\n" + confirmation;
        } else {
            finalReply = reply;
        }
        if (actionResult.isActionExecuted()) {
            action = extractActionJson(reply);
        }

        return new OrchestratorResult(finalReply, intent.getOperation(), intent.getDomain(),
                agent.getName(), actionResult.getNavigate(), actionResult.getNavigateParams(), action);
    }

    // ── Multi-intent path (new pipeline) ─────────────────────────

    private OrchestratorResult processMultiIntent(MultiIntentResult multiIntent, AgentContext ctx,
                                                   String userMessage, List<Map<String, String>> history) {
        log.info("[Orchestrator] Multi-intent: {} intents, hasConflicts={}",
                multiIntent.getIntents().size(), multiIntent.hasConflicts());

        // Step 1: CollaborationManager schedules and builds prompts
        List<CollaborationManager.AgentResult> agentResults =
                collaborationManager.execute(multiIntent, ctx, userMessage, history);

        // Step 2: Call LLM for each agent with its prepared prompt
        for (CollaborationManager.AgentResult result : agentResults) {
            if (result.getSystemPrompt() == null || result.isFailed()) continue;

            String reply;
            if (history != null && !history.isEmpty()) {
                reply = llmClient.chatWithHistory(result.getSystemPrompt(), history, userMessage);
            } else {
                reply = llmClient.chat(result.getSystemPrompt(), userMessage);
            }

            result.setOutput(reply);
            agentBus.publish("agent." + result.getDomain() + ".output", reply, result.getAgentName());
            log.debug("[Orchestrator] {} output: {} chars", result.getDomain(),
                    reply != null ? reply.length() : 0);
        }

        // Step 3: Conflict resolution
        ConflictResolver.ResolutionResult resolution = conflictResolver.resolve(agentResults);
        log.info("[Orchestrator] Resolution: type={}, escalations={}",
                resolution.getType(), resolution.getEscalations().size());

        // Step 4: Aggregate results
        ResultAggregator.AggregatedResponse aggregated = resultAggregator.aggregate(resolution);

        String finalReply = aggregated.getText();
        String action = null;

        if (aggregated.needsUserInput() && !aggregated.getEscalations().isEmpty()) {
            StringBuilder escText = new StringBuilder(finalReply);
            for (ConflictResolver.Escalation esc : aggregated.getEscalations()) {
                escText.append("\n\n").append(esc.getMessage());
                for (ConflictResolver.Escalation.Choice c : esc.getChoices()) {
                    escText.append("\n  ").append(c.getId()).append(") ").append(c.getLabel());
                }
            }
            finalReply = escText.toString();
        }

        // Collect first action JSON from any agent result
        for (CollaborationManager.AgentResult r : agentResults) {
            if (r.getOutput() != null) {
                String extracted = extractActionJson(r.getOutput());
                if (extracted != null) { action = extracted; break; }
            }
        }

        MultiIntentResult.Intent primary = multiIntent.getIntents().get(0);
        String primaryAgentName = agentMap.containsKey(DomainType.valueOf(primary.getDomain()))
                ? agentMap.get(DomainType.valueOf(primary.getDomain())).getName()
                : "MultiAgent";

        return new OrchestratorResult(finalReply, primary.getOperation(), primary.getDomain(),
                primaryAgentName, null, null, action);
    }

    private AgentContext buildContext(Long userId, String username, List<Map<String, String>> history) {
        AgentContext ctx = new AgentContext();
        ctx.setUserId(userId);
        ctx.setUsername(username);

        List<Pet> pets = petService.lambdaQuery()
                .eq(Pet::getUserId, userId)
                .list();
        ctx.setPets(pets);

        if (history != null && !history.isEmpty()) {
            ctx.setRecentHistory(history);
        }

        return ctx;
    }

    private String extractActionJson(String llmReply) {
        if (llmReply == null) return null;
        String text = llmReply.trim();
        int start = text.lastIndexOf("{\"action\":");
        if (start < 0) return null;
        return extractJsonByBraces(text, start);
    }

    private String extractJsonByBraces(String text, int start) {
        int braceCount = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (ch == '\\') { escaped = true; continue; }
            if (ch == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (ch == '{') { braceCount++; }
            else if (ch == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    public static class OrchestratorResult {
        private final String reply;
        private final String operation;
        private final String domain;
        private final String agentName;
        private final String navigate;
        private final String navigateParams;
        private final String action;

        public OrchestratorResult(String reply, String operation, String domain, String agentName) {
            this(reply, operation, domain, agentName, null, null, null);
        }

        public OrchestratorResult(String reply, String operation, String domain, String agentName,
                                  String navigate, String navigateParams, String action) {
            this.reply = reply;
            this.operation = operation;
            this.domain = domain;
            this.agentName = agentName;
            this.navigate = navigate;
            this.navigateParams = navigateParams;
            this.action = action;
        }

        public String getReply() { return reply; }
        public String getOperation() { return operation; }
        public String getDomain() { return domain; }
        public String getAgentName() { return agentName; }
        public String getNavigate() { return navigate; }
        public String getNavigateParams() { return navigateParams; }
        public String getAction() { return action; }
    }
}
