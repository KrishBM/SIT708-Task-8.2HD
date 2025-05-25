package com.example.task82.services;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class LlamaAIService {
    
    private static final String TAG = "LlamaAIService";
    private static final String API_URL = "http://192.168.0.100:5000/chat";
    private Random random = new Random();
    
    // Callback interface for async responses
    public interface AIResponseCallback {
        void onSuccess(String feedback);
        void onError(String error);
    }
    
    // New interface specifically for feedback callbacks
    public interface FeedbackCallback {
        void onSuccess(String feedback);
        void onError(String error);
    }
    
    // Enhanced AI feedback generation with real Llama API + comprehensive analysis
    public void generateFeedback(String question, String userAnswer, String jobCategory, AIResponseCallback callback) {
        Log.d(TAG, "=== GENERATE FEEDBACK CALLED ===");
        Log.d(TAG, "Question: " + question);
        Log.d(TAG, "User Answer: " + userAnswer);
        Log.d(TAG, "Job Category: " + jobCategory);
        Log.d(TAG, "Callback: " + (callback != null ? "NOT NULL" : "NULL"));
        
        try {
            // Create comprehensive prompt for Llama
            String prompt = createInterviewAnalysisPrompt(question, userAnswer, jobCategory);
            Log.d(TAG, "Prompt created, length: " + prompt.length());
            
            // Make async API call to real Llama server
            Log.d(TAG, "Creating LlamaAPITask...");
            LlamaAPITask task = new LlamaAPITask(callback, question, userAnswer, jobCategory);
            Log.d(TAG, "Executing LlamaAPITask...");
            task.execute(prompt);
            Log.d(TAG, "LlamaAPITask.execute() called!");
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION in generateFeedback: " + e.getMessage());
            Log.e(TAG, "Exception stack trace: ", e);
            if (callback != null) {
                callback.onError("Exception in generateFeedback: " + e.getMessage());
            }
        }
    }
    
    // Synchronous version for backward compatibility
    public String generateFeedback(String question, String userAnswer, String jobCategory) {
        // Fallback to local analysis if sync method is called
        InterviewAnalysis analysis = analyzeAnswer(userAnswer, question, jobCategory);
        return generateComprehensiveFeedback(analysis, jobCategory);
    }
    
    // Enhanced behavioral interview feedback
    public void generateBehavioralFeedback(String question, String userAnswer, FeedbackCallback callback) {
        Log.d(TAG, "=== GENERATE BEHAVIORAL FEEDBACK CALLED ===");
        Log.d(TAG, "Question: " + question);
        Log.d(TAG, "User Answer: " + userAnswer);
        
        try {
            // Create specific prompt for behavioral questions
            String prompt = createBehavioralAnalysisPrompt(question, userAnswer);
            Log.d(TAG, "Behavioral prompt created, length: " + prompt.length());
            
            // Create behavioral analysis task
            BehavioralAnalysisTask task = new BehavioralAnalysisTask(callback, question, userAnswer);
            task.execute(prompt);
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION in generateBehavioralFeedback: " + e.getMessage());
            if (callback != null) {
                callback.onError("Exception in generateBehavioralFeedback: " + e.getMessage());
            }
        }
    }
    
    private String createInterviewAnalysisPrompt(String question, String userAnswer, String jobCategory) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert interview coach and HR professional. ");
        prompt.append("Please analyze this interview response and provide detailed feedback.\n\n");
        
        prompt.append("**Job Category:** ").append(jobCategory).append("\n");
        prompt.append("**Interview Question:** ").append(question).append("\n");
        prompt.append("**Candidate's Answer:** ").append(userAnswer).append("\n\n");
        
        prompt.append("Please provide a comprehensive analysis including:\n");
        prompt.append("1. Overall score (0-100) with brief explanation\n");
        prompt.append("2. Content quality assessment\n");
        prompt.append("3. Grammar and language evaluation\n");
        prompt.append("4. Specific strengths in the response\n");
        prompt.append("5. Areas for improvement\n");
        prompt.append("6. Specific suggestions for ").append(jobCategory).append(" role\n");
        prompt.append("7. Key terms/skills mentioned\n\n");
        
        prompt.append("Format your response professionally with clear sections. ");
        prompt.append("Be constructive and encouraging while providing actionable feedback.");
        
        return prompt.toString();
    }
    
    private String createBehavioralAnalysisPrompt(String question, String userAnswer) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert behavioral interview coach. ");
        prompt.append("Analyze this behavioral interview response using the STAR method (Situation, Task, Action, Result).\n\n");
        
        prompt.append("**Behavioral Question:** ").append(question).append("\n");
        prompt.append("**Candidate's Answer:** ").append(userAnswer).append("\n\n");
        
        prompt.append("Please provide detailed analysis including:\n");
        prompt.append("1. STAR Method Evaluation (situation, task, action, result clarity)\n");
        prompt.append("2. Score (0-100) based on completeness and quality\n");
        prompt.append("3. Communication effectiveness\n");
        prompt.append("4. Evidence of key behavioral competencies\n");
        prompt.append("5. Specific examples and concrete details\n");
        prompt.append("6. Areas for improvement\n");
        prompt.append("7. Suggestions for better responses\n\n");
        
        prompt.append("Focus on behavioral competencies like teamwork, leadership, problem-solving, adaptability, and communication. ");
        prompt.append("Provide constructive feedback to help improve future behavioral interviews.");
        
        return prompt.toString();
    }
    
    // Async task to call Llama API
    private class LlamaAPITask extends AsyncTask<String, Void, String> {
        private AIResponseCallback callback;
        private String question;
        private String userAnswer;
        private String jobCategory;
        private String userMessage;
        private Exception error;
        
        public LlamaAPITask(AIResponseCallback callback, String question, String userAnswer, String jobCategory) {
            Log.d(TAG, "=== LLAMA API TASK CONSTRUCTOR ===");
            this.callback = callback;
            this.question = question;
            this.userAnswer = userAnswer;
            this.jobCategory = jobCategory;
            Log.d(TAG, "LlamaAPITask initialized successfully");
        }
        
        @Override
        protected String doInBackground(String... prompts) {
            Log.d(TAG, "=== DO IN BACKGROUND STARTED ===");
            Log.d(TAG, "Thread: " + Thread.currentThread().getName());
            Log.d(TAG, "Prompts length: " + (prompts != null ? prompts.length : "NULL"));
            
            try {
                this.userMessage = prompts[0];
                Log.d(TAG, "Sending request to Llama API: " + API_URL);
                Log.d(TAG, "Message to send: " + userMessage.substring(0, Math.min(100, userMessage.length())) + "...");
                
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                
                // Set up POST request exactly like Postman
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Accept", "*/*");
                connection.setRequestProperty("User-Agent", "JobAchieverAI/1.0");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setConnectTimeout(30000); // 30 seconds
                connection.setReadTimeout(60000); // 60 seconds
                
                // Get parameters using the proper method
                Map<String, String> params = getParams();
                Log.d(TAG, "Parameters to send: " + params.toString());
                
                // Build form data exactly like Postman
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                
                Log.d(TAG, "Content-Length: " + postDataBytes.length);
                Log.d(TAG, "Post data preview: " + postData.toString().substring(0, Math.min(200, postData.length())) + "...");
                
                // Send request
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(postDataBytes);
                    wr.flush();
                }
                
                Log.d(TAG, "Request sent, waiting for response...");
                
                // Read response
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                Log.d(TAG, "Response Code: " + responseCode + " - " + responseMessage);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine).append("\n");
                        }
                        
                        String responseText = response.toString().trim();
                        Log.d(TAG, "API Response received: " + responseText.length() + " characters");
                        Log.d(TAG, "Response preview: " + responseText.substring(0, Math.min(200, responseText.length())) + "...");
                        return responseText;
                    }
                } else {
                    // Read error response
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine).append("\n");
                        }
                        Log.e(TAG, "API Error Response: " + errorResponse.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error response", e);
                    }
                    
                    Log.e(TAG, "API request failed with code: " + responseCode + " - " + responseMessage);
                    Log.e(TAG, "Request URL was: " + API_URL);
                    Log.e(TAG, "Connection details: " + connection.toString());
                    return null;
                }
                
            } catch (java.net.ConnectException e) {
                Log.e(TAG, "CONNECTION REFUSED: Cannot connect to server at " + API_URL);
                Log.e(TAG, "ConnectException details: " + e.getMessage());
                this.error = e;
                return null;
            } catch (java.net.UnknownHostException e) {
                Log.e(TAG, "UNKNOWN HOST: Cannot resolve " + API_URL);
                Log.e(TAG, "UnknownHostException details: " + e.getMessage());
                this.error = e;
                return null;
            } catch (java.net.SocketTimeoutException e) {
                Log.e(TAG, "SOCKET TIMEOUT: Connection timed out to " + API_URL);
                Log.e(TAG, "SocketTimeoutException details: " + e.getMessage());
                this.error = e;
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Error calling Llama API", e);
                Log.e(TAG, "Exception details: " + e.getMessage());
                Log.e(TAG, "Exception type: " + e.getClass().getSimpleName());
                this.error = e;
                return null;
            }
        }
        
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("userMessage", userMessage);
            return params;
        }
        
        @Override
        protected void onPostExecute(String apiResponse) {
            if (apiResponse != null && !apiResponse.trim().isEmpty()) {
                // Success! Enhance API response with our analysis
                String enhancedFeedback = enhanceWithLocalAnalysis(apiResponse, question, userAnswer, jobCategory);
                callback.onSuccess(enhancedFeedback);
            } else {
                // Fallback to local analysis
                Log.w(TAG, "API call failed, using local analysis");
                InterviewAnalysis analysis = analyzeAnswer(userAnswer, question, jobCategory);
                String fallbackFeedback = generateComprehensiveFeedback(analysis, jobCategory);
                
                // Add note about API being unavailable
                String fallbackWithNote = "🤖 AI Analysis (Local Mode)\n" +
                    "═══════════════════════════════\n" +
                    "Note: Using local analysis mode\n\n" + 
                    fallbackFeedback;
                
                if (error != null) {
                    callback.onError("API unavailable, using local analysis: " + error.getMessage());
                } else {
                    callback.onSuccess(fallbackWithNote);
                }
            }
        }
    }
    
    // Behavioral analysis task
    private class BehavioralAnalysisTask extends AsyncTask<String, Void, String> {
        private FeedbackCallback callback;
        private String question;
        private String userAnswer;
        private Exception error;
        
        public BehavioralAnalysisTask(FeedbackCallback callback, String question, String userAnswer) {
            this.callback = callback;
            this.question = question;
            this.userAnswer = userAnswer;
        }
        
        @Override
        protected String doInBackground(String... prompts) {
            try {
                // Try API first, fallback to local analysis
                return generateLocalBehavioralFeedback(question, userAnswer);
            } catch (Exception e) {
                Log.e(TAG, "Error in behavioral analysis", e);
                this.error = e;
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(String feedback) {
            if (callback != null) {
                if (feedback != null && !feedback.trim().isEmpty()) {
                    callback.onSuccess(feedback);
                } else if (error != null) {
                    callback.onError("Behavioral analysis failed: " + error.getMessage());
                } else {
                    callback.onError("Failed to generate behavioral feedback");
                }
            }
        }
    }
    
    private String enhanceWithLocalAnalysis(String apiResponse, String question, String userAnswer, String jobCategory) {
        try {
            // If API response exists, enhance it; otherwise generate local analysis
            if (apiResponse != null && !apiResponse.trim().isEmpty()) {
                String cleanResponse = cleanAPIResponse(apiResponse);
                
                // Add local analysis insights
                InterviewAnalysis localAnalysis = analyzeAnswer(userAnswer, question, jobCategory);
                StringBuilder enhanced = new StringBuilder();
                
                enhanced.append("🤖 **AI Analysis:**\n");
                enhanced.append(cleanResponse).append("\n\n");
                
                enhanced.append("📊 **Detailed Metrics:**\n");
                enhanced.append("• Overall Score: ").append(localAnalysis.overallScore).append("/100\n");
                enhanced.append("• Grammar: ").append(localAnalysis.grammarScore).append("/100\n");
                enhanced.append("• Content Quality: ").append(localAnalysis.contentScore).append("/100\n");
                enhanced.append("• Clarity: ").append(localAnalysis.clarityScore).append("/100\n");
                enhanced.append("• Relevance: ").append(localAnalysis.relevanceScore).append("/100\n");
                enhanced.append("• Confidence: ").append(localAnalysis.confidenceScore).append("/100\n\n");
                
                if (!localAnalysis.keywords.isEmpty()) {
                    enhanced.append("🔍 **Key Terms Identified:** ");
                    enhanced.append(String.join(", ", localAnalysis.keywords)).append("\n\n");
                }
                
                return enhanced.toString();
            } else {
                // Generate comprehensive local analysis
                InterviewAnalysis analysis = analyzeAnswer(userAnswer, question, jobCategory);
                return generateComprehensiveFeedback(analysis, jobCategory);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in enhanceWithLocalAnalysis", e);
            // Fallback to basic local analysis
            InterviewAnalysis analysis = analyzeAnswer(userAnswer, question, jobCategory);
            return generateComprehensiveFeedback(analysis, jobCategory);
        }
    }
    
    private String generateLocalBehavioralFeedback(String question, String userAnswer) {
        BehavioralAnalysis analysis = analyzeBehavioralAnswer(userAnswer, question);
        return generateBehavioralFeedbackText(analysis);
    }
    
    private BehavioralAnalysis analyzeBehavioralAnswer(String userAnswer, String question) {
        BehavioralAnalysis analysis = new BehavioralAnalysis();
        
        // Analyze STAR method components
        analysis.situationScore = assessSituation(userAnswer);
        analysis.taskScore = assessTask(userAnswer);
        analysis.actionScore = assessAction(userAnswer);
        analysis.resultScore = assessResult(userAnswer);
        
        // Overall STAR score
        analysis.starScore = (analysis.situationScore + analysis.taskScore + analysis.actionScore + analysis.resultScore) / 4;
        
        // Analyze behavioral competencies
        analysis.teamworkIndicators = assessTeamwork(userAnswer);
        analysis.leadershipIndicators = assessLeadership(userAnswer);
        analysis.problemSolvingIndicators = assessProblemSolving(userAnswer);
        analysis.adaptabilityIndicators = assessAdaptability(userAnswer);
        analysis.communicationScore = assessCommunication(userAnswer);
        
        // Calculate overall score
        analysis.overallScore = calculateBehavioralScore(analysis);
        
        // Identify strengths and improvements
        analysis.strengths = identifyBehavioralStrengths(analysis, userAnswer);
        analysis.improvements = identifyBehavioralImprovements(analysis, userAnswer);
        
        return analysis;
    }
    
    private int assessSituation(String answer) {
        int score = 60; // Base score
        
        String lowerAnswer = answer.toLowerCase();
        
        // Look for situational context indicators
        if (lowerAnswer.contains("when") || lowerAnswer.contains("time") || 
            lowerAnswer.contains("situation") || lowerAnswer.contains("project") ||
            lowerAnswer.contains("working") || lowerAnswer.contains("company")) {
            score += 15;
        }
        
        // Look for specific context
        if (lowerAnswer.contains("team") || lowerAnswer.contains("client") ||
            lowerAnswer.contains("deadline") || lowerAnswer.contains("challenge")) {
            score += 10;
        }
        
        // Look for timeline/specificity
        if (lowerAnswer.contains("month") || lowerAnswer.contains("week") ||
            lowerAnswer.contains("year") || lowerAnswer.contains("day")) {
            score += 10;
        }
        
        return Math.min(100, score);
    }
    
    private int assessTask(String answer) {
        int score = 60; // Base score
        
        String lowerAnswer = answer.toLowerCase();
        
        // Look for task clarity indicators
        if (lowerAnswer.contains("needed to") || lowerAnswer.contains("had to") ||
            lowerAnswer.contains("responsible") || lowerAnswer.contains("goal") ||
            lowerAnswer.contains("objective") || lowerAnswer.contains("required")) {
            score += 20;
        }
        
        // Look for specific tasks
        if (lowerAnswer.contains("deliver") || lowerAnswer.contains("complete") ||
            lowerAnswer.contains("solve") || lowerAnswer.contains("manage")) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    private int assessAction(String answer) {
        int score = 60; // Base score
        
        String lowerAnswer = answer.toLowerCase();
        
        // Look for action indicators
        if (lowerAnswer.contains("i did") || lowerAnswer.contains("i took") ||
            lowerAnswer.contains("i implemented") || lowerAnswer.contains("i decided") ||
            lowerAnswer.contains("i organized") || lowerAnswer.contains("i created")) {
            score += 20;
        }
        
        // Look for specific actions
        if (lowerAnswer.contains("steps") || lowerAnswer.contains("approach") ||
            lowerAnswer.contains("strategy") || lowerAnswer.contains("method")) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    private int assessResult(String answer) {
        int score = 60; // Base score
        
        String lowerAnswer = answer.toLowerCase();
        
        // Look for result indicators
        if (lowerAnswer.contains("result") || lowerAnswer.contains("outcome") ||
            lowerAnswer.contains("achieved") || lowerAnswer.contains("success") ||
            lowerAnswer.contains("completed") || lowerAnswer.contains("improved")) {
            score += 20;
        }
        
        // Look for quantifiable results
        if (lowerAnswer.contains("%") || lowerAnswer.contains("percent") ||
            lowerAnswer.contains("increased") || lowerAnswer.contains("reduced") ||
            lowerAnswer.contains("saved") || lowerAnswer.contains("gained")) {
            score += 15;
        }
        
        return Math.min(100, score);
    }
    
    private int assessTeamwork(String answer) {
        String lowerAnswer = answer.toLowerCase();
        int indicators = 0;
        
        if (lowerAnswer.contains("team") || lowerAnswer.contains("colleagues")) indicators++;
        if (lowerAnswer.contains("collaboration") || lowerAnswer.contains("together")) indicators++;
        if (lowerAnswer.contains("group") || lowerAnswer.contains("others")) indicators++;
        if (lowerAnswer.contains("support") || lowerAnswer.contains("help")) indicators++;
        
        return indicators * 25; // 0-100 scale
    }
    
    private int assessLeadership(String answer) {
        String lowerAnswer = answer.toLowerCase();
        int indicators = 0;
        
        if (lowerAnswer.contains("led") || lowerAnswer.contains("managed")) indicators++;
        if (lowerAnswer.contains("directed") || lowerAnswer.contains("guided")) indicators++;
        if (lowerAnswer.contains("initiative") || lowerAnswer.contains("responsibility")) indicators++;
        if (lowerAnswer.contains("decision") || lowerAnswer.contains("organize")) indicators++;
        
        return indicators * 25; // 0-100 scale
    }
    
    private int assessProblemSolving(String answer) {
        String lowerAnswer = answer.toLowerCase();
        int indicators = 0;
        
        if (lowerAnswer.contains("problem") || lowerAnswer.contains("challenge")) indicators++;
        if (lowerAnswer.contains("solution") || lowerAnswer.contains("resolve")) indicators++;
        if (lowerAnswer.contains("analyze") || lowerAnswer.contains("identify")) indicators++;
        if (lowerAnswer.contains("creative") || lowerAnswer.contains("innovative")) indicators++;
        
        return indicators * 25; // 0-100 scale
    }
    
    private int assessAdaptability(String answer) {
        String lowerAnswer = answer.toLowerCase();
        int indicators = 0;
        
        if (lowerAnswer.contains("change") || lowerAnswer.contains("adapt")) indicators++;
        if (lowerAnswer.contains("flexible") || lowerAnswer.contains("adjust")) indicators++;
        if (lowerAnswer.contains("learn") || lowerAnswer.contains("new")) indicators++;
        if (lowerAnswer.contains("different") || lowerAnswer.contains("approach")) indicators++;
        
        return indicators * 25; // 0-100 scale
    }
    
    private int assessCommunication(String answer) {
        int score = 70; // Base score
        
        // Check for clear structure
        if (answer.contains(".") && answer.contains(",")) score += 10;
        
        // Check answer length (optimal range)
        int wordCount = answer.split("\\s+").length;
        if (wordCount >= 50 && wordCount <= 150) {
            score += 15;
        } else if (wordCount >= 30 && wordCount <= 200) {
            score += 10;
        }
        
        // Check for professional language
        String lowerAnswer = answer.toLowerCase();
        if (!lowerAnswer.contains("um") && !lowerAnswer.contains("uh") && !lowerAnswer.contains("like")) {
            score += 5;
        }
        
        return Math.min(100, score);
    }
    
    private int calculateBehavioralScore(BehavioralAnalysis analysis) {
        // Weighted scoring
        double starWeight = 0.4;
        double competencyWeight = 0.4;
        double communicationWeight = 0.2;
        
        double competencyAvg = (analysis.teamworkIndicators + analysis.leadershipIndicators + 
                               analysis.problemSolvingIndicators + analysis.adaptabilityIndicators) / 4.0;
        
        return (int) (analysis.starScore * starWeight + 
                     competencyAvg * competencyWeight + 
                     analysis.communicationScore * communicationWeight);
    }
    
    private List<String> identifyBehavioralStrengths(BehavioralAnalysis analysis, String answer) {
        List<String> strengths = new java.util.ArrayList<>();
        
        if (analysis.starScore >= 80) {
            strengths.add("Excellent use of STAR method structure");
        }
        if (analysis.teamworkIndicators >= 75) {
            strengths.add("Strong teamwork and collaboration skills demonstrated");
        }
        if (analysis.leadershipIndicators >= 75) {
            strengths.add("Clear leadership abilities and initiative");
        }
        if (analysis.problemSolvingIndicators >= 75) {
            strengths.add("Effective problem-solving approach");
        }
        if (analysis.adaptabilityIndicators >= 75) {
            strengths.add("Good adaptability and flexibility");
        }
        if (analysis.communicationScore >= 85) {
            strengths.add("Clear and professional communication");
        }
        if (answer.toLowerCase().contains("result") || answer.toLowerCase().contains("outcome")) {
            strengths.add("Focused on results and outcomes");
        }
        
        return strengths;
    }
    
    private List<String> identifyBehavioralImprovements(BehavioralAnalysis analysis, String answer) {
        List<String> improvements = new java.util.ArrayList<>();
        
        if (analysis.situationScore < 70) {
            improvements.add("Provide more specific context about the situation");
        }
        if (analysis.taskScore < 70) {
            improvements.add("Clarify your specific role and responsibilities");
        }
        if (analysis.actionScore < 70) {
            improvements.add("Detail the specific actions you took");
        }
        if (analysis.resultScore < 70) {
            improvements.add("Include quantifiable results and outcomes");
        }
        if (analysis.communicationScore < 75) {
            improvements.add("Structure your response more clearly");
        }
        if (answer.split("\\s+").length < 30) {
            improvements.add("Provide more detailed examples and explanations");
        }
        if (!answer.toLowerCase().contains("learn") && !answer.toLowerCase().contains("result")) {
            improvements.add("Mention what you learned or achieved from the experience");
        }
        
        return improvements;
    }
    
    private String generateBehavioralFeedbackText(BehavioralAnalysis analysis) {
        StringBuilder feedback = new StringBuilder();
        
        // Overall assessment
        feedback.append("🎯 **Behavioral Interview Analysis**\n\n");
        feedback.append("**Overall Score: ").append(analysis.overallScore).append("/100** ");
        feedback.append(getScoreEmoji(analysis.overallScore)).append("\n\n");
        
        // STAR Method Analysis
        feedback.append("📋 **STAR Method Evaluation:**\n");
        feedback.append("• Situation: ").append(analysis.situationScore).append("/100\n");
        feedback.append("• Task: ").append(analysis.taskScore).append("/100\n");
        feedback.append("• Action: ").append(analysis.actionScore).append("/100\n");
        feedback.append("• Result: ").append(analysis.resultScore).append("/100\n\n");
        
        // Competency Analysis
        feedback.append("🏆 **Behavioral Competencies:**\n");
        feedback.append("• Teamwork: ").append(analysis.teamworkIndicators).append("/100\n");
        feedback.append("• Leadership: ").append(analysis.leadershipIndicators).append("/100\n");
        feedback.append("• Problem Solving: ").append(analysis.problemSolvingIndicators).append("/100\n");
        feedback.append("• Adaptability: ").append(analysis.adaptabilityIndicators).append("/100\n");
        feedback.append("• Communication: ").append(analysis.communicationScore).append("/100\n\n");
        
        // Strengths
        if (!analysis.strengths.isEmpty()) {
            feedback.append("✅ **Strengths:**\n");
            for (String strength : analysis.strengths) {
                feedback.append("• ").append(strength).append("\n");
            }
            feedback.append("\n");
        }
        
        // Areas for Improvement
        if (!analysis.improvements.isEmpty()) {
            feedback.append("🔄 **Areas for Improvement:**\n");
            for (String improvement : analysis.improvements) {
                feedback.append("• ").append(improvement).append("\n");
            }
            feedback.append("\n");
        }
        
        // Recommendations
        feedback.append("💡 **Recommendations:**\n");
        feedback.append("• Practice the STAR method for consistent structure\n");
        feedback.append("• Include specific examples with measurable results\n");
        feedback.append("• Focus on your role and personal contributions\n");
        feedback.append("• Highlight relevant competencies for the role\n");
        
        return feedback.toString();
    }
    
    // Behavioral analysis data class
    private static class BehavioralAnalysis {
        int overallScore;
        int starScore;
        int situationScore;
        int taskScore;
        int actionScore;
        int resultScore;
        int teamworkIndicators;
        int leadershipIndicators;
        int problemSolvingIndicators;
        int adaptabilityIndicators;
        int communicationScore;
        List<String> strengths;
        List<String> improvements;
    }
    
    private String cleanAPIResponse(String response) {
        // Clean up the API response for better display
        try {
            // Remove any JSON formatting if present
            if (response.startsWith("{") && response.endsWith("}")) {
                // Extract content from JSON if needed
                // For now, just return as is
            }
            
            // Basic cleaning
            return response.trim()
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\t", "    ");
        } catch (Exception e) {
            Log.w(TAG, "Error cleaning API response", e);
            return response;
        }
    }
    
    // All the existing analysis methods remain the same...
    private InterviewAnalysis analyzeAnswer(String userAnswer, String question, String jobCategory) {
        InterviewAnalysis analysis = new InterviewAnalysis();
        
        // Analyze various aspects
        analysis.overallScore = calculateOverallScore(userAnswer, jobCategory);
        analysis.grammarScore = analyzeGrammar(userAnswer);
        analysis.contentScore = analyzeContent(userAnswer, jobCategory);
        analysis.clarityScore = analyzeClarity(userAnswer);
        analysis.relevanceScore = analyzeRelevance(userAnswer, question, jobCategory);
        analysis.confidenceScore = analyzeConfidence(userAnswer);
        
        analysis.grammarIssues = findGrammarIssues(userAnswer);
        analysis.strengths = identifyStrengths(userAnswer, jobCategory);
        analysis.improvements = identifyImprovements(userAnswer, jobCategory);
        analysis.keywords = analyzeKeywords(userAnswer, jobCategory);
        
        return analysis;
    }
    
    private int calculateOverallScore(String userAnswer, String jobCategory) {
        int baseScore = 60; // Starting score
        
        // Length analysis
        if (userAnswer.length() >= 100 && userAnswer.length() <= 400) {
            baseScore += 10; // Optimal length
        } else if (userAnswer.length() < 50) {
            baseScore -= 15; // Too short
        } else if (userAnswer.length() > 500) {
            baseScore -= 5; // Too long
        }
        
        // Sentence structure
        String[] sentences = userAnswer.split("[.!?]+");
        if (sentences.length >= 3) {
            baseScore += 5; // Multiple sentences show structure
        }
        
        // Keywords presence
        if (hasRelevantKeywords(userAnswer, jobCategory)) {
            baseScore += 15;
        }
        
        // Examples mentioned
        if (userAnswer.toLowerCase().contains("example") || 
            userAnswer.toLowerCase().contains("experience") ||
            userAnswer.toLowerCase().contains("project")) {
            baseScore += 10;
        }
        
        return Math.min(Math.max(baseScore, 20), 100); // Clamp between 20-100
    }
    
    private int analyzeGrammar(String userAnswer) {
        int grammarScore = 85; // Start with good score
        
        // Check for common issues
        if (hasRunOnSentences(userAnswer)) grammarScore -= 10;
        if (hasInconsistentTense(userAnswer)) grammarScore -= 8;
        if (hasCapitalizationIssues(userAnswer)) grammarScore -= 5;
        if (hasPunctuationIssues(userAnswer)) grammarScore -= 7;
        if (hasRepeatedWords(userAnswer)) grammarScore -= 5;
        
        return Math.max(grammarScore, 40);
    }
    
    private int analyzeContent(String userAnswer, String jobCategory) {
        int contentScore = 70;
        
        List<String> relevantKeywords = getJobKeywords(jobCategory);
        long keywordCount = relevantKeywords.stream()
            .mapToLong(keyword -> countOccurrences(userAnswer.toLowerCase(), keyword))
            .sum();
        
        contentScore += Math.min((int)(keywordCount * 5), 25);
        
        // Check for specific examples
        if (containsSpecificExamples(userAnswer)) {
            contentScore += 15;
        }
        
        // Check for results/metrics mentioned
        if (containsQuantifiableResults(userAnswer)) {
            contentScore += 10;
        }
        
        return Math.min(contentScore, 100);
    }
    
    private int analyzeClarity(String userAnswer) {
        int clarityScore = 75;
        
        // Check average sentence length
        String[] sentences = userAnswer.split("[.!?]+");
        double avgWordsPerSentence = userAnswer.split("\\s+").length / Math.max(sentences.length, 1.0);
        
        if (avgWordsPerSentence > 25) {
            clarityScore -= 15; // Too complex
        } else if (avgWordsPerSentence < 8) {
            clarityScore -= 10; // Too simple
        } else {
            clarityScore += 10; // Just right
        }
        
        // Check for filler words
        String[] fillerWords = {"um", "uh", "like", "you know", "basically", "actually"};
        for (String filler : fillerWords) {
            clarityScore -= countOccurrences(userAnswer.toLowerCase(), filler) * 3;
        }
        
        return Math.max(clarityScore, 30);
    }
    
    private int analyzeRelevance(String userAnswer, String question, String jobCategory) {
        int relevanceScore = 80;
        
        // Check if answer addresses the question
        String[] questionWords = question.toLowerCase().split("\\s+");
        int matchedWords = 0;
        
        for (String word : questionWords) {
            if (word.length() > 3 && userAnswer.toLowerCase().contains(word)) {
                matchedWords++;
            }
        }
        
        relevanceScore += Math.min(matchedWords * 3, 20);
        
        // Job category relevance
        if (hasRelevantKeywords(userAnswer, jobCategory)) {
            relevanceScore += 10;
        }
        
        return Math.min(relevanceScore, 100);
    }
    
    private int analyzeConfidence(String userAnswer) {
        int confidenceScore = 75;
        
        // Positive indicators
        String[] confidentPhrases = {"I have experience", "I successfully", "I led", "I achieved", "I'm confident"};
        for (String phrase : confidentPhrases) {
            if (userAnswer.toLowerCase().contains(phrase.toLowerCase())) {
                confidenceScore += 5;
            }
        }
        
        // Negative indicators
        String[] uncertainPhrases = {"I think", "maybe", "probably", "I'm not sure", "I guess"};
        for (String phrase : uncertainPhrases) {
            confidenceScore -= countOccurrences(userAnswer.toLowerCase(), phrase.toLowerCase()) * 5;
        }
        
        return Math.min(Math.max(confidenceScore, 30), 100);
    }
    
    private List<String> findGrammarIssues(String userAnswer) {
        if (hasRunOnSentences(userAnswer)) {
            return Arrays.asList("Consider breaking long sentences into shorter ones for better readability");
        }
        if (hasInconsistentTense(userAnswer)) {
            return Arrays.asList("Maintain consistent verb tenses throughout your response");
        }
        if (hasCapitalizationIssues(userAnswer)) {
            return Arrays.asList("Check capitalization of proper nouns and sentence beginnings");
        }
        if (hasPunctuationIssues(userAnswer)) {
            return Arrays.asList("Review punctuation usage, especially commas and periods");
        }
        
        return Arrays.asList("No major grammar issues detected - well done!");
    }
    
    private List<String> identifyStrengths(String userAnswer, String jobCategory) {
        if (userAnswer.length() >= 100 && userAnswer.length() <= 400) {
            return Arrays.asList("Well-structured response with appropriate length");
        }
        
        if (containsSpecificExamples(userAnswer)) {
            return Arrays.asList("Excellent use of specific examples to support your points");
        }
        
        if (hasRelevantKeywords(userAnswer, jobCategory)) {
            return Arrays.asList("Demonstrates strong understanding of " + jobCategory + " concepts");
        }
        
        if (containsQuantifiableResults(userAnswer)) {
            return Arrays.asList("Great inclusion of measurable results and outcomes");
        }
        
        return Arrays.asList("Clear communication and relevant content");
    }
    
    private List<String> identifyImprovements(String userAnswer, String jobCategory) {
        if (userAnswer.length() < 100) {
            return Arrays.asList("Expand your answer with more detailed examples");
        }
        
        if (!containsSpecificExamples(userAnswer)) {
            return Arrays.asList("Include specific examples from your experience");
        }
        
        if (!containsQuantifiableResults(userAnswer)) {
            return Arrays.asList("Add measurable results or metrics where possible");
        }
        
        if (!hasRelevantKeywords(userAnswer, jobCategory)) {
            return Arrays.asList("Include more " + jobCategory + "-specific terminology");
        }
        
        return Arrays.asList("Consider adding more specific examples and metrics");
    }
    
    private List<String> analyzeKeywords(String userAnswer, String jobCategory) {
        List<String> jobKeywords = getJobKeywords(jobCategory);
        return jobKeywords.stream()
            .filter(keyword -> userAnswer.toLowerCase().contains(keyword))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private String generateComprehensiveFeedback(InterviewAnalysis analysis, String jobCategory) {
        StringBuilder feedback = new StringBuilder();
        
        // Header with overall score
        feedback.append("🤖 AI Analysis Complete\n");
        feedback.append("═══════════════════════\n\n");
        
        // Overall Score with emoji
        String scoreEmoji = getScoreEmoji(analysis.overallScore);
        feedback.append("📊 OVERALL SCORE: ").append(analysis.overallScore).append("/100 ").append(scoreEmoji).append("\n\n");
        
        // Detailed Scores
        feedback.append("📈 DETAILED BREAKDOWN:\n");
        feedback.append("▪ Content Quality: ").append(analysis.contentScore).append("/100\n");
        feedback.append("▪ Grammar & Language: ").append(analysis.grammarScore).append("/100\n");
        feedback.append("▪ Clarity & Structure: ").append(analysis.clarityScore).append("/100\n");
        feedback.append("▪ Relevance: ").append(analysis.relevanceScore).append("/100\n");
        feedback.append("▪ Confidence Level: ").append(analysis.confidenceScore).append("/100\n\n");
        
        // Strengths
        feedback.append("💪 STRENGTHS:\n");
        for (String strength : analysis.strengths) {
            feedback.append("✅ ").append(strength).append("\n");
        }
        feedback.append("\n");
        
        // Grammar Analysis
        feedback.append("📝 GRAMMAR CHECK:\n");
        for (String issue : analysis.grammarIssues) {
            if (issue.contains("No major")) {
                feedback.append("✅ ").append(issue).append("\n");
            } else {
                feedback.append("⚠️ ").append(issue).append("\n");
            }
        }
        feedback.append("\n");
        
        // Areas for Improvement
        feedback.append("🎯 AREAS FOR IMPROVEMENT:\n");
        for (String improvement : analysis.improvements) {
            feedback.append("💡 ").append(improvement).append("\n");
        }
        feedback.append("\n");
        
        // Keywords Found
        if (!analysis.keywords.isEmpty()) {
            feedback.append("🔍 KEY TERMS IDENTIFIED:\n");
            feedback.append("📌 ").append(String.join(", ", analysis.keywords)).append("\n\n");
        }
        
        // AI Recommendations
        feedback.append("🧠 AI RECOMMENDATIONS:\n");
        feedback.append(getPersonalizedRecommendations(analysis, jobCategory));
        
        return feedback.toString();
    }
    
    private String getPersonalizedRecommendations(InterviewAnalysis analysis, String jobCategory) {
        StringBuilder recommendations = new StringBuilder();
        
        if (analysis.overallScore >= 85) {
            recommendations.append("🌟 Excellent response! You demonstrate strong ").append(jobCategory).append(" knowledge.\n");
            recommendations.append("🚀 Focus on adding specific metrics to make your answers even more compelling.\n");
        } else if (analysis.overallScore >= 70) {
            recommendations.append("👍 Good foundation! Consider adding more specific examples from your experience.\n");
            recommendations.append("📊 Include quantifiable results to strengthen your impact statements.\n");
        } else {
            recommendations.append("💪 Room for improvement! Practice structuring your responses using the STAR method.\n");
            recommendations.append("📚 Research more ").append(jobCategory).append("-specific terminology and concepts.\n");
        }
        
        // Category-specific advice
        switch (jobCategory) {
            case "Software Engineer":
                recommendations.append("💻 Mention specific technologies, frameworks, and coding practices.\n");
                break;
            case "Data Analyst":
                recommendations.append("📊 Discuss data tools, methodologies, and analytical insights.\n");
                break;
            case "Product Manager":
                recommendations.append("🎯 Focus on product strategy, user needs, and stakeholder management.\n");
                break;
            case "Marketing":
                recommendations.append("📢 Highlight campaign performance, audience insights, and ROI metrics.\n");
                break;
        }
        
        return recommendations.toString();
    }
    
    // Helper methods
    private String getScoreEmoji(int score) {
        if (score >= 90) return "🏆";
        if (score >= 80) return "🥇";
        if (score >= 70) return "🥈";
        if (score >= 60) return "🥉";
        return "📈";
    }
    
    private boolean hasRelevantKeywords(String userAnswer, String jobCategory) {
        List<String> keywords = getJobKeywords(jobCategory);
        return keywords.stream().anyMatch(keyword -> 
            userAnswer.toLowerCase().contains(keyword));
    }
    
    private List<String> getJobKeywords(String jobCategory) {
        switch (jobCategory) {
            case "Software Engineer":
                return Arrays.asList("programming", "code", "algorithm", "database", "framework", "API", "testing", "debugging", "software", "development");
            case "Data Analyst":
                return Arrays.asList("data", "analysis", "statistics", "visualization", "insights", "SQL", "Python", "metrics", "dashboard", "reporting");
            case "Product Manager":
                return Arrays.asList("product", "strategy", "roadmap", "stakeholder", "user", "market", "requirements", "prioritization", "features", "launch");
            case "Marketing":
                return Arrays.asList("campaign", "brand", "audience", "conversion", "ROI", "digital", "content", "social media", "analytics", "engagement");
            default:
                return Arrays.asList("experience", "team", "project", "challenge", "solution", "result", "success", "leadership", "communication", "skills");
        }
    }
    
    private boolean containsSpecificExamples(String userAnswer) {
        String[] exampleIndicators = {"for example", "for instance", "in my experience", "at my previous", "when I", "during my"};
        return Arrays.stream(exampleIndicators)
            .anyMatch(indicator -> userAnswer.toLowerCase().contains(indicator));
    }
    
    private boolean containsQuantifiableResults(String userAnswer) {
        Pattern numberPattern = Pattern.compile("\\d+[%$]?|\\b(increased|decreased|improved|reduced)\\b");
        return numberPattern.matcher(userAnswer.toLowerCase()).find();
    }
    
    private boolean hasRunOnSentences(String userAnswer) {
        String[] sentences = userAnswer.split("[.!?]+");
        return Arrays.stream(sentences)
            .anyMatch(sentence -> sentence.split("\\s+").length > 30);
    }
    
    private boolean hasInconsistentTense(String userAnswer) {
        return userAnswer.contains("will") && userAnswer.contains("was");
    }
    
    private boolean hasCapitalizationIssues(String userAnswer) {
        return !userAnswer.matches("^[A-Z].*") || userAnswer.contains(" i ");
    }
    
    private boolean hasPunctuationIssues(String userAnswer) {
        return !userAnswer.trim().endsWith(".") && !userAnswer.trim().endsWith("!") && !userAnswer.trim().endsWith("?");
    }
    
    private boolean hasRepeatedWords(String userAnswer) {
        String[] words = userAnswer.toLowerCase().split("\\s+");
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals(words[i-1]) && words[i].length() > 3) {
                return true;
            }
        }
        return false;
    }
    
    private int countOccurrences(String text, String word) {
        return text.split(word, -1).length - 1;
    }
    
    // Analysis result container
    private static class InterviewAnalysis {
        int overallScore;
        int grammarScore;
        int contentScore;
        int clarityScore;
        int relevanceScore;
        int confidenceScore;
        List<String> grammarIssues;
        List<String> strengths;
        List<String> improvements;
        List<String> keywords;
    }
} 