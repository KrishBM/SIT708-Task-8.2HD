package com.example.task82.models;

public class Question {
    private String id;
    private String text;
    private String category;
    private String difficulty;
    private String suggestedAnswer;
    private String[] keywords;
    
    public Question() {
        // Default constructor required for Firebase
    }
    
    public Question(String id, String text, String category, String difficulty) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getSuggestedAnswer() { return suggestedAnswer; }
    public void setSuggestedAnswer(String suggestedAnswer) { this.suggestedAnswer = suggestedAnswer; }
    
    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }
} 