package com.pet.agent.service;

import java.util.Map;

public interface ChatConfigService {

    Map<String, String> getAll();

    String getByKey(String key);

    void batchSave(Map<String, String> configs);
}
