package com.pet.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.agent.entity.ChatConfig;
import com.pet.agent.mapper.ChatConfigMapper;
import com.pet.agent.service.ChatConfigService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatConfigServiceImpl implements ChatConfigService {

    private static final String DEEPSEEK_ANTHROPIC_BASE_URL = "https://api.deepseek.com/anthropic";

    private final ChatConfigMapper mapper;

    public ChatConfigServiceImpl(ChatConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<String, String> getAll() {
        List<ChatConfig> list = mapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> result = new LinkedHashMap<>();
        for (ChatConfig c : list) {
            result.put(c.getConfigKey(), c.getConfigValue());
        }
        return result;
    }

    @Override
    public String getByKey(String key) {
        ChatConfig config = mapper.selectOne(
                new LambdaQueryWrapper<ChatConfig>().eq(ChatConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void batchSave(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configKey = entry.getKey();
            String configValue = normalizeConfigValue(configKey, entry.getValue());
            ChatConfig existing = mapper.selectOne(
                    new LambdaQueryWrapper<ChatConfig>().eq(ChatConfig::getConfigKey, configKey));
            if (existing != null) {
                existing.setConfigValue(configValue);
                mapper.updateById(existing);
            } else {
                ChatConfig config = new ChatConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                mapper.insert(config);
            }
        }
    }

    private String normalizeConfigValue(String configKey, String configValue) {
        if (!"llm.api_url".equals(configKey) || configValue == null) {
            return configValue;
        }
        String trimmedConfigValue = configValue.trim();
        if (trimmedConfigValue.endsWith("/v1/messages")) {
            return DEEPSEEK_ANTHROPIC_BASE_URL;
        }
        return trimmedConfigValue;
    }
}
