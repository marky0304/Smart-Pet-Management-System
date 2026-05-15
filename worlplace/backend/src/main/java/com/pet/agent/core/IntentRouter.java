package com.pet.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.agent.dto.IntentResult;
import com.pet.agent.dto.MultiIntentResult;
import com.pet.agent.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class IntentRouter {

    private static final Logger log = LoggerFactory.getLogger(IntentRouter.class);

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public IntentRouter(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    private static final String ROUTER_PROMPT =
        "分类用户消息，只返回JSON：\n" +
        "operation: QUERY(查/搜索/推荐/浏览)/ADD(增/预约/下单/购买)/MODIFY(改)/DELETE(删/退货/取消)/CHAT(闲聊/问候)\n" +
        "domain: PET(档案/添加宠物)/HEALTH(健康/疫苗/体检/驱虫/生病)/MALL(商城/商品/购物/买/下单/订单/价格/推荐)/COMMUNITY(社区/帖子/动态/话题/宠友分享/晒宠物)/GENERAL(饲养/训练/护理/预约服务)/CHAT(非宠物/问候/无关)\n" +
        "关键：说「买XX」「推荐XX」「搜索XX」→MALL+QUERY 说「下单」「购买」「预约服务」→对应domain+ADD 说「打开XX页面」「去XX」→CHAT+QUERY 说「社区」「帖子」「话题」「动态」「铲屎圈」「晒」→COMMUNITY+QUERY\n" +
        "格式：{\"operation\":\"QUERY\",\"domain\":\"HEALTH\"}";

    private static final String MULTI_ROUTER_PROMPT =
        "分析用户消息，识别所有意图。一条消息可能包含多个意图。\n" +
        "operation: QUERY(查/搜索/推荐/浏览)/ADD(增/预约/下单/购买)/MODIFY(改)/DELETE(删/退货/取消)/CHAT(闲聊/问候)\n" +
        "domain: PET(档案/添加宠物)/HEALTH(健康/疫苗/体检/驱虫/生病)/MALL(商城/商品/购物/买/下单/订单/价格/推荐)/COMMUNITY(社区/帖子/动态/话题/宠友分享)/GENERAL(饲养/训练/护理/预约服务)/CHAT(非宠物/问候/无关)\n" +
        "priority: 1-100的优先级数字，HEALTH=100, GENERAL=80, MALL=60, PET=50, COMMUNITY=40, CHAT=30\n" +
        "dependsOn: 该意图依赖的其他domain列表（如MALL可能依赖HEALTH的健康结论）\n" +
        "仅返回JSON数组，每个元素包含operation/domain/priority/dependsOn。如果只有一个意图，也返回数组。\n" +
        "示例：[{\"operation\":\"QUERY\",\"domain\":\"HEALTH\",\"priority\":100,\"dependsOn\":[]},{\"operation\":\"QUERY\",\"domain\":\"MALL\",\"priority\":60,\"dependsOn\":[\"HEALTH\"]}]";

    // ── Single-intent routing (original, kept for backward compat) ─

    public IntentResult route(String userMessage) {
        try {
            String reply = llmClient.chat(ROUTER_PROMPT, userMessage);
            return parseIntent(reply, userMessage);
        } catch (Exception e) {
            log.warn("Intent routing failed, using fallback", e);
            return fallbackRoute(userMessage);
        }
    }

    private IntentResult parseIntent(String llmReply, String userMessage) {
        try {
            String json = extractJsonObject(llmReply);
            JsonNode root = objectMapper.readTree(json);
            String operation = root.has("operation") ? root.get("operation").asText() : "CHAT";
            String domain = root.has("domain") ? root.get("domain").asText() : "GENERAL";
            return new IntentResult(operation.toUpperCase(), domain.toUpperCase(), 0.9);
        } catch (Exception e) {
            log.warn("Failed to parse intent JSON, using fallback. LLM reply: {}", llmReply);
            return fallbackRoute(userMessage);
        }
    }

    private String extractJsonObject(String llmReply) {
        String normalizedReply = llmReply.trim();
        if (normalizedReply.startsWith("```")) {
            normalizedReply = normalizedReply.replaceAll("```[a-z]*\\n?", "")
                    .replaceAll("```", "")
                    .trim();
        }

        int startIndex = normalizedReply.indexOf('{');
        int endIndex = normalizedReply.lastIndexOf('}');
        if (startIndex >= 0 && endIndex > startIndex) {
            return normalizedReply.substring(startIndex, endIndex + 1);
        }
        return normalizedReply;
    }

    // ── Multi-intent routing ───────────────────────────────────

    public MultiIntentResult routeMulti(String userMessage) {
        try {
            String reply = llmClient.chat(MULTI_ROUTER_PROMPT, userMessage);
            List<MultiIntentResult.Intent> intents = parseMultiIntent(reply, userMessage);
            return new MultiIntentResult(intents, userMessage);
        } catch (Exception e) {
            log.warn("Multi-intent routing failed, falling back to single", e);
            IntentResult single = route(userMessage);
            MultiIntentResult.Intent intent = new MultiIntentResult.Intent(
                    single.getDomain(), single.getOperation(), priorityForDomain(single.getDomain()));
            return new MultiIntentResult(List.of(intent), userMessage);
        }
    }

    private List<MultiIntentResult.Intent> parseMultiIntent(String llmReply, String userMessage) {
        try {
            String json = extractJsonArray(llmReply);
            if (json == null) {
                return fallbackMultiIntent(userMessage);
            }
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray() || root.size() == 0) {
                return fallbackMultiIntent(userMessage);
            }

            List<MultiIntentResult.Intent> intents = new ArrayList<>();
            Set<String> seenDomains = new HashSet<>();
            for (JsonNode node : root) {
                String domain = node.has("domain") ? node.get("domain").asText().toUpperCase() : "CHAT";
                String operation = node.has("operation") ? node.get("operation").asText().toUpperCase() : "CHAT";
                int priority = node.has("priority") ? node.get("priority").asInt() : priorityForDomain(domain);
                Set<String> dependsOn = new HashSet<>();
                if (node.has("dependsOn") && node.get("dependsOn").isArray()) {
                    for (JsonNode dep : node.get("dependsOn")) {
                        dependsOn.add(dep.asText().toUpperCase());
                    }
                }

                // Deduplicate: skip if same domain already parsed with same operation
                String key = domain + ":" + operation;
                if (seenDomains.contains(key)) continue;
                seenDomains.add(key);

                Set<String> conflicts = resolveConflictDomains(domain, seenDomains);
                double confidence = 0.85;

                intents.add(new MultiIntentResult.Intent(domain, operation, priority, dependsOn, conflicts, confidence));
            }

            if (intents.isEmpty()) {
                return fallbackMultiIntent(userMessage);
            }
            return intents;
        } catch (Exception e) {
            log.warn("Failed to parse multi-intent JSON: {}", e.getMessage());
            return fallbackMultiIntent(userMessage);
        }
    }

    private String extractJsonArray(String llmReply) {
        String text = llmReply.trim();
        if (text.startsWith("```")) {
            text = text.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        }
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private List<MultiIntentResult.Intent> fallbackMultiIntent(String message) {
        // Use keyword-based multi-intent fallback
        String msg = message.toLowerCase();
        List<MultiIntentResult.Intent> intents = new ArrayList<>();
        Set<String> added = new HashSet<>();

        // Health keywords
        if ((msg.contains("疫苗") || msg.contains("体检") || msg.contains("生病") || msg.contains("健康")
                || msg.contains("驱虫") || msg.contains("拉肚子") || msg.contains("吐") || msg.contains("不舒服")
                || msg.contains("咳嗽") || msg.contains("感冒")) && added.add("HEALTH")) {
            intents.add(new MultiIntentResult.Intent("HEALTH", "QUERY", 100));
        }

        // Mall keywords
        if ((msg.contains("买") || msg.contains("下单") || msg.contains("商品") || msg.contains("推荐")
                || msg.contains("狗粮") || msg.contains("猫粮") || msg.contains("零食") || msg.contains("价格")
                || msg.contains("购物") || msg.contains("药") || msg.contains("益生菌")) && added.add("MALL")) {
            String op = (msg.contains("下单") || msg.contains("购买")) ? "ADD" : "QUERY";
            intents.add(new MultiIntentResult.Intent("MALL", op, 60));
        }

        // Pet keywords
        if ((msg.contains("添加") || msg.contains("档案") || msg.contains("宠物") || msg.contains("品种")
                || msg.contains("年龄") || msg.contains("体重")) && added.add("PET")) {
            String op = msg.contains("添加") ? "ADD" : "QUERY";
            intents.add(new MultiIntentResult.Intent("PET", op, 50));
        }

        // Appointment keywords
        if ((msg.contains("预约") || msg.contains("寄养") || msg.contains("洗澡") || msg.contains("美容")
                || msg.contains("训练") || msg.contains("兽医") || msg.contains("医生")) && added.add("GENERAL")) {
            String op = msg.contains("预约") ? "ADD" : "QUERY";
            intents.add(new MultiIntentResult.Intent("GENERAL", op, 80));
        }

        // Community keywords
        if ((msg.contains("社区") || msg.contains("帖子") || msg.contains("动态") || msg.contains("话题")
                || msg.contains("铲屎圈") || msg.contains("晒") || msg.contains("分享") || msg.contains("宠友")
                || msg.contains("热门")) && added.add("COMMUNITY")) {
            String op = (msg.contains("发布") || msg.contains("发帖") || msg.contains("晒")) ? "ADD" : "QUERY";
            intents.add(new MultiIntentResult.Intent("COMMUNITY", op, 40));
        }

        if (intents.isEmpty()) {
            intents.add(new MultiIntentResult.Intent("CHAT", "CHAT", 30));
        }

        return intents;
    }

    private Set<String> resolveConflictDomains(String domain, Set<String> seen) {
        // HEALTH and MALL can conflict when both give advice about the same condition
        if ("MALL".equals(domain) && seen.contains("HEALTH:QUERY")) {
            return Set.of("HEALTH");
        }
        if ("HEALTH".equals(domain) && seen.contains("MALL:QUERY")) {
            return Set.of("MALL");
        }
        return Collections.emptySet();
    }

    private int priorityForDomain(String domain) {
        switch (domain) {
            case "HEALTH": return 100;
            case "GENERAL": return 80;
            case "MALL": return 60;
            case "PET": return 50;
            case "COMMUNITY": return 40;
            default: return 30;
        }
    }

    private IntentResult fallbackRoute(String message) {
        String msg = message.toLowerCase();
        String domain = "GENERAL";
        if (msg.contains("疫苗") || msg.contains("体检") || msg.contains("生病") || msg.contains("健康") || msg.contains("提醒") || msg.contains("驱虫")) {
            domain = "HEALTH";
        } else if (msg.contains("商品") || msg.contains("买") || msg.contains("订单") || msg.contains("价格")
                || msg.contains("狗粮") || msg.contains("猫粮") || msg.contains("零食") || msg.contains("玩具")
                || msg.contains("用品") || msg.contains("下单") || msg.contains("购物") || msg.contains("推荐")) {
            domain = "MALL";
        } else if (msg.contains("品种") || msg.contains("年龄") || msg.contains("体重") || msg.contains("档案")
                || msg.contains("宠物") || msg.contains("添加")) {
            domain = "PET";
        } else if (msg.contains("社区") || msg.contains("帖子") || msg.contains("动态") || msg.contains("话题")
                || msg.contains("铲屎圈") || msg.contains("分享") || msg.contains("宠友")) {
            domain = "COMMUNITY";
        }
        String operation = "CHAT";
        if (msg.contains("添加") || msg.contains("预约") || msg.contains("创建") || msg.contains("新增")
                || msg.contains("下单") || msg.contains("购买")) {
            operation = "ADD";
        } else if (msg.contains("修改") || msg.contains("更新") || msg.contains("改成")) {
            operation = "MODIFY";
        } else if (msg.contains("取消") || msg.contains("删除") || msg.contains("退货")) {
            operation = "DELETE";
        } else if (msg.contains("查看") || msg.contains("查询") || msg.contains("是什么") || msg.contains("多少")
                || msg.contains("帮我") || msg.contains("买") || msg.contains("推荐") || msg.contains("搜索")
                || msg.contains("找") || msg.contains("打开") || msg.contains("去")) {
            operation = "QUERY";
        }
        return new IntentResult(operation, domain, 0.6);
    }
}
