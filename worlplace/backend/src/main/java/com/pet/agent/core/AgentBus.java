package com.pet.agent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Inter-agent communication bus using the blackboard pattern.
 * Agents publish findings to shared topics; dependent agents read before execution.
 */
@Component
public class AgentBus {

    private static final Logger log = LoggerFactory.getLogger(AgentBus.class);

    /** Shared data blackboard — keyed by topic name */
    private final Map<String, Object> blackboard = new ConcurrentHashMap<>();

    /** Topic -> subscribed agent names */
    private final Map<String, Set<String>> subscriptions = new ConcurrentHashMap<>();

    /** Ordered event log for debugging and auditing */
    private final List<BusEvent> eventLog = Collections.synchronizedList(new ArrayList<>());

    // ── Publish / Read ──────────────────────────────────────────

    public void publish(String topic, Object data, String fromAgent) {
        blackboard.put(topic, data);
        eventLog.add(new BusEvent(topic, fromAgent, data != null ? data.getClass().getSimpleName() : "null",
                System.currentTimeMillis()));
        log.debug("[AgentBus] {} → {}", fromAgent, topic);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(String topic) {
        return (T) blackboard.get(topic);
    }

    public boolean hasTopic(String topic) {
        return blackboard.containsKey(topic);
    }

    public String readString(String topic) {
        Object val = blackboard.get(topic);
        return val != null ? val.toString() : null;
    }

    // ── Subscription ────────────────────────────────────────────

    public void subscribe(String topic, String agentName) {
        subscriptions.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>()).add(agentName);
        log.info("[AgentBus] {} listening on '{}'", agentName, topic);
    }

    public Set<String> getSubscribers(String topic) {
        return subscriptions.getOrDefault(topic, Collections.emptySet());
    }

    public boolean hasSubscribers(String topic) {
        Set<String> subs = subscriptions.get(topic);
        return subs != null && !subs.isEmpty();
    }

    // ── Lifecycle ────────────────────────────────────────────────

    /** Reset all state for a new session */
    public void reset() {
        blackboard.clear();
        eventLog.clear();
    }

    public List<BusEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /** Immutable event record */
    public static class BusEvent {
        private final String topic, agent, dataType;
        private final long timestamp;
        public BusEvent(String topic, String agent, String dataType, long timestamp) {
            this.topic = topic; this.agent = agent; this.dataType = dataType; this.timestamp = timestamp;
        }
        public String getTopic() { return topic; }
        public String getAgent() { return agent; }
        public String getDataType() { return dataType; }
        public long getTimestamp() { return timestamp; }
    }
}
