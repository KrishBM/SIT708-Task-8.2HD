package com.example.task82.models;

public class UserStats {
    private int totalInterviews;
    private int completedInterviews;
    private int achievementsUnlocked;
    private double averageResponseTime;
    private double confidenceLevel;
    private int overallScore;
    private double performancePercentage;
    
    public UserStats() {
        this.totalInterviews = 0;
        this.completedInterviews = 0;
        this.achievementsUnlocked = 0;
        this.averageResponseTime = 0.0;
        this.confidenceLevel = 0.0;
        this.overallScore = 0;
        this.performancePercentage = 0.0;
    }
    
    // Getters and Setters
    public int getTotalInterviews() { return totalInterviews; }
    public void setTotalInterviews(int totalInterviews) { this.totalInterviews = totalInterviews; }
    
    public int getCompletedInterviews() { return completedInterviews; }
    public void setCompletedInterviews(int completedInterviews) { this.completedInterviews = completedInterviews; }
    
    public int getAchievementsUnlocked() { return achievementsUnlocked; }
    public void setAchievementsUnlocked(int achievementsUnlocked) { this.achievementsUnlocked = achievementsUnlocked; }
    
    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    
    public double getPerformancePercentage() { return performancePercentage; }
    public void setPerformancePercentage(double performancePercentage) { this.performancePercentage = performancePercentage; }
} 