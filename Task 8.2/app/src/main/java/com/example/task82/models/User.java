package com.example.task82.models;

import java.util.List;

public class User {
    private String id;
    private String email;
    private String name;
    private String profileImageUrl;
    private UserStats stats;
    private List<InterviewSession> recentSessions;
    
    public User() {
        // Default constructor required for Firebase
    }
    
    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.stats = new UserStats();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public UserStats getStats() { return stats; }
    public void setStats(UserStats stats) { this.stats = stats; }
    
    public List<InterviewSession> getRecentSessions() { return recentSessions; }
    public void setRecentSessions(List<InterviewSession> recentSessions) { this.recentSessions = recentSessions; }
} 