package com.pet.agent.core;

import com.pet.entity.Pet;
import java.util.List;
import java.util.Map;

public class AgentContext {

    private Long userId;
    private String username;
    private List<Pet> pets;
    private String currentPage;
    private List<Map<String, String>> recentHistory;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<Pet> getPets() { return pets; }
    public void setPets(List<Pet> pets) { this.pets = pets; }
    public String getCurrentPage() { return currentPage; }
    public void setCurrentPage(String currentPage) { this.currentPage = currentPage; }
    public List<Map<String, String>> getRecentHistory() { return recentHistory; }
    public void setRecentHistory(List<Map<String, String>> recentHistory) { this.recentHistory = recentHistory; }

    public boolean hasPets() {
        return pets != null && !pets.isEmpty();
    }

    public Pet getFirstPet() {
        return hasPets() ? pets.get(0) : null;
    }
}
