package com.example.task82.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InterviewSession {
    private String id;
    private String jobCategory;
    private Date timestamp;
    private List<String> userAnswers;
    private List<String> aiFeedback;
    private double score;
    private boolean completed;
    private int questionsAnswered;
    private long duration; // in milliseconds

    public InterviewSession() {
        this.userAnswers = new ArrayList<>();
        this.aiFeedback = new ArrayList<>();
        this.completed = false;
        this.questionsAnswered = 0;
        this.score = 0.0;
    }

    public InterviewSession(String id, String jobCategory) {
        this();
        this.id = id;
        this.jobCategory = jobCategory;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<String> userAnswers) {
        this.userAnswers = userAnswers;
    }

    public List<String> getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(List<String> aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(int questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    // Utility methods
    public void addAnswer(String answer) {
        this.userAnswers.add(answer);
        this.questionsAnswered = this.userAnswers.size();
    }

    public void addFeedback(String feedback) {
        this.aiFeedback.add(feedback);
    }

    public double getAverageScore() {
        return this.score;
    }

    public int getTotalQuestions() {
        return Math.max(userAnswers.size(), aiFeedback.size());
    }

    @Override
    public String toString() {
        return "InterviewSession{" +
                "id='" + id + '\'' +
                ", jobCategory='" + jobCategory + '\'' +
                ", timestamp=" + timestamp +
                ", questionsAnswered=" + questionsAnswered +
                ", score=" + score +
                ", completed=" + completed +
                '}';
    }
} 