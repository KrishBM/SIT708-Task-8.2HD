package com.example.task82.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.task82.R;
import com.example.task82.services.LlamaAIService;
import com.example.task82.models.InterviewSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class InterviewActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int MAX_QUESTIONS_FOR_ANALYSIS = 3;
    
    private TextView questionText, feedbackText, userAnswerText, recordingStatusText;
    private TextView sessionProgressText, questionsAnsweredText, overallScoreText;
    private Button startRecordingButton, nextQuestionButton, viewAnalysisButton;
    private CardView jobCategoryCard1, jobCategoryCard2, jobCategoryCard3, jobCategoryCard4;
    private CardView feedbackCard, userAnswerCard, sessionAnalysisCard;
    private ImageView recordingIndicator;
    private ProgressBar recordingProgress, sessionProgress;
    
    private SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;
    private boolean isProcessingResults = false;
    private int currentQuestionIndex = 0;
    private String selectedJobCategory = "Software Engineer";
    private String lastPartialResult = "";
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;
    
    // Enhanced session tracking
    private List<String> sessionAnswers = new ArrayList<>();
    private List<String> sessionFeedbacks = new ArrayList<>();
    private List<Double> sessionScores = new ArrayList<>();
    private InterviewSession currentSession;
    
    private List<String> softwareEngineerQuestions = Arrays.asList(
        "Tell me about yourself and your experience in software development.",
        "What programming languages are you most comfortable with?",
        "Describe a challenging project you worked on and how you overcame obstacles.",
        "How do you stay updated with the latest technology trends?",
        "Explain the difference between object-oriented and functional programming."
    );
    
    private List<String> dataAnalystQuestions = Arrays.asList(
        "How do you approach data cleaning and preprocessing?",
        "What statistical methods do you use for data analysis?",
        "Describe your experience with data visualization tools.",
        "How do you handle missing data in your analysis?",
        "Explain a time when your analysis led to important business insights."
    );
    
    private List<String> productManagerQuestions = Arrays.asList(
        "How do you prioritize features for a product roadmap?",
        "Describe your experience working with cross-functional teams.",
        "How do you gather and analyze user feedback?",
        "Tell me about a product you managed from conception to launch.",
        "How do you make data-driven decisions in product management?"
    );
    
    private List<String> marketingQuestions = Arrays.asList(
        "How do you develop a marketing strategy for a new product?",
        "What metrics do you use to measure marketing campaign success?",
        "Describe your experience with digital marketing channels.",
        "How do you identify and target your ideal customer?",
        "Tell me about a successful marketing campaign you led."
    );
    
    private LlamaAIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);
        
        initializeViews();
        setupClickListeners();
        checkPermissions();
        initializeSpeechRecognizer();
        
        aiService = new LlamaAIService();
        loadFirstQuestion();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.question_text);
        feedbackText = findViewById(R.id.feedback_text);
        userAnswerText = findViewById(R.id.user_answer_text);
        recordingStatusText = findViewById(R.id.recording_status_text);
        startRecordingButton = findViewById(R.id.start_recording_button);
        nextQuestionButton = findViewById(R.id.next_question_button);
        viewAnalysisButton = findViewById(R.id.view_analysis_button);
        
        sessionProgressText = findViewById(R.id.session_progress_text);
        questionsAnsweredText = findViewById(R.id.questions_answered_text);
        overallScoreText = findViewById(R.id.overall_score_text);
        
        feedbackCard = findViewById(R.id.feedback_card);
        userAnswerCard = findViewById(R.id.user_answer_card);
        recordingIndicator = findViewById(R.id.recording_indicator);
        recordingProgress = findViewById(R.id.recording_progress);
        sessionProgress = findViewById(R.id.session_progress);
        
        jobCategoryCard1 = findViewById(R.id.job_category_card_1);
        jobCategoryCard2 = findViewById(R.id.job_category_card_2);
        jobCategoryCard3 = findViewById(R.id.job_category_card_3);
        jobCategoryCard4 = findViewById(R.id.job_category_card_4);
        
        sessionAnalysisCard = findViewById(R.id.session_analysis_card);
    }

    private void setupClickListeners() {
        startRecordingButton.setOnClickListener(v -> {
            Log.d("InterviewActivity", "=== RECORDING BUTTON CLICKED ===");
            toggleRecording();
        });
        nextQuestionButton.setOnClickListener(v -> loadNextQuestion());
        viewAnalysisButton.setOnClickListener(v -> viewAnalysis());
        
        jobCategoryCard1.setOnClickListener(v -> selectJobCategory("Software Engineer"));
        jobCategoryCard2.setOnClickListener(v -> selectJobCategory("Data Analyst"));
        jobCategoryCard3.setOnClickListener(v -> selectJobCategory("Product Manager"));
        jobCategoryCard4.setOnClickListener(v -> selectJobCategory("Marketing"));
    }

    private void selectJobCategory(String category) {
        selectedJobCategory = category;
        currentQuestionIndex = 0;
        loadFirstQuestion();
        hideAnswerAndFeedback();
        initializeSession();
        Toast.makeText(this, "Selected: " + category, Toast.LENGTH_SHORT).show();
    }

    private void initializeSession() {
        currentSession = new InterviewSession();
        currentSession.setId("session_" + System.currentTimeMillis());
        currentSession.setJobCategory(selectedJobCategory);
        currentSession.setTimestamp(new java.util.Date());
        
        // Clear previous session data
        sessionAnswers.clear();
        sessionFeedbacks.clear();
        sessionScores.clear();
        
        updateSessionProgress();
    }
    
    private void updateSessionProgress() {
        int answeredQuestions = sessionAnswers.size();
        int progressPercentage = (int) ((answeredQuestions / (float) MAX_QUESTIONS_FOR_ANALYSIS) * 100);
        
        if (sessionProgress != null) {
            sessionProgress.setProgress(progressPercentage);
        }
        
        if (sessionProgressText != null) {
            sessionProgressText.setText("Session Progress: " + progressPercentage + "%");
        }
        
        if (questionsAnsweredText != null) {
            questionsAnsweredText.setText("Questions Answered: " + answeredQuestions + "/" + MAX_QUESTIONS_FOR_ANALYSIS);
        }
        
        // Show analysis card if we have completed the minimum questions
        if (answeredQuestions >= MAX_QUESTIONS_FOR_ANALYSIS && sessionAnalysisCard != null) {
            sessionAnalysisCard.setVisibility(View.VISIBLE);
            calculateAndDisplayOverallScore();
        }
    }
    
    private void calculateAndDisplayOverallScore() {
        if (sessionScores.isEmpty()) return;
        
        double averageScore = sessionScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (overallScoreText != null) {
            overallScoreText.setText(String.format("Overall Score: %.1f/100", averageScore));
        }
        
        // Update session with calculated score
        if (currentSession != null) {
            currentSession.setScore(averageScore);
        }
    }

    private void hideAnswerAndFeedback() {
        userAnswerCard.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.GONE);
        if (sessionAnalysisCard != null) {
            sessionAnalysisCard.setVisibility(View.GONE);
        }
        if (viewAnalysisButton != null) {
            viewAnalysisButton.setVisibility(View.GONE);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.RECORD_AUDIO}, 
                PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d("InterviewActivity", "=== SPEECH: onReadyForSpeech ===");
                    updateRecordingUI(true, "Ready to listen... Start speaking now!");
                    cancelTimeout();
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d("InterviewActivity", "=== SPEECH: onBeginningOfSpeech ===");
                    updateRecordingUI(true, "Listening... Keep speaking!");
                    cancelTimeout();
                    isProcessingResults = false;
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Visual feedback for voice level
                    if (recordingProgress != null && isRecording) {
                        int progress = (int) Math.min(100, Math.max(0, (rmsdB + 10) * 5));
                        recordingProgress.setProgress(progress);
                    }
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d("InterviewActivity", "=== SPEECH: onBufferReceived ===");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d("InterviewActivity", "=== SPEECH: onEndOfSpeech ===");
                    Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
                    if (!isProcessingResults) {
                        updateRecordingUI(false, "Processing your answer...");
                        // Give a bit more time for final results
                        startTimeout(2000);
                    }
                }

                @Override
                public void onError(int error) {
                    Log.d("InterviewActivity", "=== SPEECH: onError ===");
                    Log.e("InterviewActivity", "Speech recognition error: " + error + " - " + getErrorMessage(error));
                    Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
                    Log.d("InterviewActivity", "lastPartialResult: '" + lastPartialResult + "'");
                    
                    if (isProcessingResults) return; // Ignore errors if we're already processing
                    
                    String errorMessage = getErrorMessage(error);
                    
                    // If we have partial results and it's just a timeout, use those
                    if ((error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) 
                        && !lastPartialResult.isEmpty() && lastPartialResult.trim().length() > 0) {
                        
                        Log.d("InterviewActivity", "Using partial result: " + lastPartialResult);
                        updateRecordingUI(false, "Using your speech...");
                        processUserAnswer(lastPartialResult);
                        return;
                    }
                    
                    updateRecordingUI(false, "Ready to record");
                    
                    // Only show error if it's a real issue, not just silence
                    if (error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT && error != SpeechRecognizer.ERROR_NO_MATCH) {
                        Toast.makeText(InterviewActivity.this, "Recording error: " + errorMessage, Toast.LENGTH_LONG).show();
                    } else if (lastPartialResult.isEmpty()) {
                        Toast.makeText(InterviewActivity.this, "No speech detected. Please speak louder and try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d("InterviewActivity", "=== SPEECH: onResults ===");
                    Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
                    
                    if (isProcessingResults) {
                        Log.d("InterviewActivity", "Already processing results, ignoring...");
                        return; // Prevent duplicate processing
                    }
                    
                    cancelTimeout();
                    
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Log.d("InterviewActivity", "Results received, matches: " + (matches != null ? matches.size() : "NULL"));
                    
                    try {
                        if (matches != null && !matches.isEmpty()) {
                            String userAnswer = matches.get(0);
                            Log.d("InterviewActivity", "Final result: '" + userAnswer + "'");
                            updateRecordingUI(false, "Got your answer!");
                            Log.d("InterviewActivity", "About to call processUserAnswer with final result...");
                            processUserAnswer(userAnswer);
                            Log.d("InterviewActivity", "processUserAnswer called successfully with final result!");
                        } else if (!lastPartialResult.isEmpty()) {
                            // Use partial result if no final result
                            Log.d("InterviewActivity", "No final result, using partial: '" + lastPartialResult + "'");
                            updateRecordingUI(false, "Using your speech...");
                            Log.d("InterviewActivity", "About to call processUserAnswer with partial result...");
                            processUserAnswer(lastPartialResult);
                            Log.d("InterviewActivity", "processUserAnswer called successfully with partial result!");
                        } else {
                            Log.d("InterviewActivity", "No results and no partial results");
                            updateRecordingUI(false, "Ready to record");
                            Toast.makeText(InterviewActivity.this, "No speech detected. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("InterviewActivity", "EXCEPTION in onResults: " + e.getMessage());
                        Log.e("InterviewActivity", "Exception stack trace: ", e);
                        
                        // Reset state and show error
                        isProcessingResults = false;
                        updateRecordingUI(false, "Ready to record");
                        Toast.makeText(InterviewActivity.this, "Error processing speech: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    Log.d("InterviewActivity", "=== SPEECH: onPartialResults ===");
                    Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
                    
                    if (isProcessingResults) {
                        Log.d("InterviewActivity", "Already processing, ignoring partial results");
                        return;
                    }
                    
                    ArrayList<String> partialMatches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Log.d("InterviewActivity", "Partial matches: " + (partialMatches != null ? partialMatches.size() : "NULL"));
                    
                    if (partialMatches != null && !partialMatches.isEmpty()) {
                        lastPartialResult = partialMatches.get(0);
                        Log.d("InterviewActivity", "Partial result: '" + lastPartialResult + "'");
                        recordingStatusText.setText("You're saying: " + lastPartialResult);
                        
                        // Reset timeout when we get new speech
                        cancelTimeout();
                        startTimeout(5000); // Give 5 seconds after last speech
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d("InterviewActivity", "=== SPEECH: onEvent ===");
                    Log.d("InterviewActivity", "Event type: " + eventType);
                }
            });
        } else {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_LONG).show();
            startRecordingButton.setEnabled(false);
        }
    }

    private void startTimeout(int milliseconds) {
        cancelTimeout();
        timeoutRunnable = () -> {
            if (isRecording && !lastPartialResult.isEmpty() && !isProcessingResults) {
                // User seems to have finished speaking, use partial result
                updateRecordingUI(false, "Processing your answer...");
                isProcessingResults = true;
                if (speechRecognizer != null) {
                    speechRecognizer.stopListening();
                }
                processUserAnswer(lastPartialResult);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, milliseconds);
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech input matched";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Speech timeout";
            default:
                return "Unknown error";
        }
    }

    private void updateRecordingUI(boolean recording, String status) {
        isRecording = recording;
        recordingStatusText.setText(status);
        
        if (recording) {
            startRecordingButton.setText("🎤 Stop Recording");
            startRecordingButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.error));
            recordingIndicator.setVisibility(View.VISIBLE);
            recordingProgress.setVisibility(View.VISIBLE);
        } else {
            startRecordingButton.setText("🎤 Start Recording");
            startRecordingButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
            recordingIndicator.setVisibility(View.GONE);
            recordingProgress.setVisibility(View.GONE);
            recordingProgress.setProgress(0);
        }
    }

    private void toggleRecording() {
        Log.d("InterviewActivity", "=== TOGGLE RECORDING ===");
        Log.d("InterviewActivity", "Current isRecording: " + isRecording);
        
        if (isRecording) {
            Log.d("InterviewActivity", "Stopping recording...");
            stopRecording();
        } else {
            Log.d("InterviewActivity", "Starting recording...");
            startRecording();
        }
    }

    private void startRecording() {
        Log.d("InterviewActivity", "=== START RECORDING ===");
        Log.d("InterviewActivity", "Permission check...");
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED) {
            
            Log.d("InterviewActivity", "Permission granted, initializing...");
            
            if (speechRecognizer == null) {
                Log.d("InterviewActivity", "speechRecognizer is null, initializing...");
                initializeSpeechRecognizer();
            }
            
            // Reset state
            lastPartialResult = "";
            isProcessingResults = false;
            cancelTimeout();
            
            Log.d("InterviewActivity", "Creating speech intent...");
            android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            
            // Increase timeout values significantly
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 8000); // 8 seconds
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 6000); // 6 seconds
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000); // Minimum 2 seconds
            
            // Additional settings for better recognition
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
            
            Log.d("InterviewActivity", "Starting speech recognition...");
            speechRecognizer.startListening(intent);
            updateRecordingUI(true, "Starting recording...");
            Log.d("InterviewActivity", "speechRecognizer.startListening() called!");
        } else {
            Log.e("InterviewActivity", "Microphone permission NOT granted!");
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
            checkPermissions();
        }
    }

    private void stopRecording() {
        Log.d("InterviewActivity", "=== STOP RECORDING ===");
        Log.d("InterviewActivity", "isRecording: " + isRecording);
        Log.d("InterviewActivity", "lastPartialResult: '" + lastPartialResult + "'");
        Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
        
        cancelTimeout();
        
        if (speechRecognizer != null && isRecording) {
            // If we have partial results, use them
            if (!lastPartialResult.isEmpty() && !isProcessingResults) {
                Log.d("InterviewActivity", "Using partial result to process answer");
                updateRecordingUI(false, "Processing your answer...");
                isProcessingResults = true;
                speechRecognizer.stopListening();
                processUserAnswer(lastPartialResult);
            } else if (!isProcessingResults) {
                Log.d("InterviewActivity", "No partial results, just stopping...");
                speechRecognizer.stopListening();
                updateRecordingUI(false, "Processing...");
            } else {
                Log.d("InterviewActivity", "Already processing results");
            }
        } else {
            Log.d("InterviewActivity", "speechRecognizer is null or not recording");
        }
    }

    private void processUserAnswer(String userAnswer) {
        Log.d("InterviewActivity", "=== PROCESSING USER ANSWER START ===");
        Log.d("InterviewActivity", "Input userAnswer: '" + userAnswer + "'");
        
        try {
            if (isProcessingResults) {
                Log.d("InterviewActivity", "Already processing results, returning...");
                return; // Prevent duplicate processing
            }
            isProcessingResults = true;
            
            Log.d("InterviewActivity", "=== PROCESSING USER ANSWER ===");
            Log.d("InterviewActivity", "isProcessingResults: " + isProcessingResults);
            
            // Clean up the answer
            userAnswer = userAnswer.trim();
            Log.d("InterviewActivity", "Cleaned userAnswer: '" + userAnswer + "'");
            
            if (userAnswer.isEmpty()) {
                Log.d("InterviewActivity", "User answer is empty, resetting...");
                updateRecordingUI(false, "Ready to record");
                Toast.makeText(this, "No speech detected. Please try again.", Toast.LENGTH_SHORT).show();
                isProcessingResults = false;
                return;
            }
            
            Log.d("InterviewActivity", "Setting user answer text...");
            // Display user answer
            userAnswerText.setText(userAnswer);
            userAnswerCard.setVisibility(View.VISIBLE);
            
            // Get current question
            String currentQuestion = getCurrentQuestion();
            Log.d("InterviewActivity", "Current question: " + currentQuestion);
            
            // Generate AI feedback using real Llama API + comprehensive analysis
            updateRecordingUI(false, "🚀 Connecting to Llama AI...");
            
            // Create final variables for lambda/callback access
            final String finalUserAnswer = userAnswer;
            
            Log.d("InterviewActivity", "About to call AI service for question: " + currentQuestion);
            Log.d("InterviewActivity", "User answer: " + finalUserAnswer);
            Log.d("InterviewActivity", "Job category: " + selectedJobCategory);
            Log.d("InterviewActivity", "aiService instance: " + (aiService != null ? "NOT NULL" : "NULL"));
            
            // Check if aiService is null
            if (aiService == null) {
                Log.e("InterviewActivity", "ERROR: aiService is NULL! Initializing...");
                aiService = new LlamaAIService();
            }
            
            Log.d("InterviewActivity", "Calling aiService.generateFeedback...");
            
            aiService.generateFeedback(currentQuestion, finalUserAnswer, selectedJobCategory, new LlamaAIService.AIResponseCallback() {
                @Override
                public void onSuccess(String feedback) {
                    Log.d("InterviewActivity", "=== AI CALLBACK SUCCESS ===");
                    // This runs on the main thread
                    runOnUiThread(() -> {
                        Log.d("InterviewActivity", "AI feedback received successfully, length: " + feedback.length());
                        
                        // Store session data
                        sessionAnswers.add(finalUserAnswer);
                        sessionFeedbacks.add(feedback);
                        
                        // Extract score from feedback (simple scoring based on content)
                        double score = extractScoreFromFeedback(feedback);
                        sessionScores.add(score);
                        
                        // Update session object
                        if (currentSession != null) {
                            currentSession.getUserAnswers().add(finalUserAnswer);
                            currentSession.getAiFeedback().add(feedback);
                        }
                        
                        // Display enhanced feedback (Llama AI + comprehensive analysis)
                        feedbackText.setText(feedback);
                        feedbackCard.setVisibility(View.VISIBLE);
                        nextQuestionButton.setVisibility(View.VISIBLE);
                        
                        // Update session progress
                        updateSessionProgress();
                        
                        updateRecordingUI(false, "✅ AI analysis complete!");
                        
                        Toast.makeText(InterviewActivity.this, "🤖 AI feedback generated successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Reset for next recording
                        lastPartialResult = "";
                        isProcessingResults = false;
                    });
                }
                
                @SuppressLint("SetTextI18n")
                @Override
                public void onError(String error) {
                    Log.d("InterviewActivity", "=== AI CALLBACK ERROR ===");
                    Log.e("InterviewActivity", "AI callback error: " + error);
                    // This runs on the main thread
                    runOnUiThread(() -> {
                        Log.e("InterviewActivity", "AI service error: " + error);
                        updateRecordingUI(false, "⚠️ Using local analysis mode");
                        
                        String feedbackContent;
                        // Still show the feedback even if API failed (local analysis was used)
                        if (error.contains("local analysis")) {
                            // Error message contains the fallback feedback
                            feedbackContent = error.substring(error.indexOf(":") + 1).trim();
                        } else {
                            // Generate pure local analysis
                            feedbackContent = aiService.generateFeedback(currentQuestion, finalUserAnswer, selectedJobCategory);
                            feedbackContent = "🤖 Local AI Analysis\n" +
                                "═══════════════════════\n" +
                                "Note: Network unavailable, using offline mode\n\n" + 
                                feedbackContent;
                        }
                        
                        // Store session data even for local analysis
                        sessionAnswers.add(finalUserAnswer);
                        sessionFeedbacks.add(feedbackContent);
                        
                        // Extract score from feedback
                        double score = extractScoreFromFeedback(feedbackContent);
                        sessionScores.add(score);
                        
                        // Update session object
                        if (currentSession != null) {
                            currentSession.getUserAnswers().add(finalUserAnswer);
                            currentSession.getAiFeedback().add(feedbackContent);
                        }
                        
                        feedbackText.setText(feedbackContent);
                        feedbackCard.setVisibility(View.VISIBLE);
                        nextQuestionButton.setVisibility(View.VISIBLE);
                        
                        // Update session progress
                        updateSessionProgress();
                        
                        Toast.makeText(InterviewActivity.this, "📱 Using local analysis mode", Toast.LENGTH_SHORT).show();
                        
                        // Reset for next recording
                        lastPartialResult = "";
                        isProcessingResults = false;
                    });
                }
            });
            Log.d("InterviewActivity", "aiService.generateFeedback called successfully!");
        } catch (Exception e) {
            Log.e("InterviewActivity", "MAJOR EXCEPTION in processUserAnswer: " + e.getMessage());
            Log.e("InterviewActivity", "Exception stack trace: ", e);
            
            // Fallback to sync method
            try {
                updateRecordingUI(false, "⚠️ Using local analysis mode");
                String localFeedback = aiService.generateFeedback(getCurrentQuestion(), userAnswer, selectedJobCategory);
                feedbackText.setText("🤖 Local AI Analysis\n" +
                    "═══════════════════════\n" +
                    "Note: Using offline analysis\n\n" + 
                    localFeedback);
                
                feedbackCard.setVisibility(View.VISIBLE);
                nextQuestionButton.setVisibility(View.VISIBLE);
                
                Toast.makeText(this, "📱 Using local analysis mode", Toast.LENGTH_SHORT).show();
            } catch (Exception e2) {
                Log.e("InterviewActivity", "Even local fallback failed: " + e2.getMessage());
                updateRecordingUI(false, "Ready to record");
                Toast.makeText(this, "Error processing answer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            
            // Reset for next recording
            lastPartialResult = "";
            isProcessingResults = false;
        }
    }

    private String getCurrentQuestion() {
        List<String> questions = getQuestionsForCategory(selectedJobCategory);
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return "Interview completed! Great job!";
    }

    private List<String> getQuestionsForCategory(String category) {
        switch (category) {
            case "Data Analyst":
                return dataAnalystQuestions;
            case "Product Manager":
                return productManagerQuestions;
            case "Marketing":
                return marketingQuestions;
            default:
                return softwareEngineerQuestions;
        }
    }

    private void loadFirstQuestion() {
        currentQuestionIndex = 0;
        loadCurrentQuestion();
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        loadCurrentQuestion();
        hideAnswerAndFeedback();
        updateRecordingUI(false, "Ready to record");
        lastPartialResult = "";
        isProcessingResults = false;
    }

    private void loadCurrentQuestion() {
        List<String> questions = getQuestionsForCategory(selectedJobCategory);
        
        if (currentQuestionIndex < questions.size()) {
            questionText.setText(questions.get(currentQuestionIndex));
            nextQuestionButton.setText("Next Question (" + (currentQuestionIndex + 1) + "/3)");
        } else {
            questionText.setText("🎉 Congratulations! You've completed all the interview questions for " + selectedJobCategory + ". Great job!");
            nextQuestionButton.setText("Restart Interview");
            nextQuestionButton.setOnClickListener(v -> {
                currentQuestionIndex = 0;
                loadCurrentQuestion();
                hideAnswerAndFeedback();
                updateRecordingUI(false, "Ready to record");
                lastPartialResult = "";
                isProcessingResults = false;
                nextQuestionButton.setOnClickListener(view -> loadNextQuestion());
            });
        }
    }

    private void viewAnalysis() {
        // Implementation of viewAnalysis method
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted!", Toast.LENGTH_SHORT).show();
                initializeSpeechRecognizer();
            } else {
                Toast.makeText(this, "Microphone permission is required for voice recording", Toast.LENGTH_LONG).show();
                startRecordingButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimeout();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private double extractScoreFromFeedback(String feedback) {
        // Simple scoring based on feedback content
        int score = 70; // Base score
        
        if (feedback.toLowerCase().contains("excellent") || feedback.toLowerCase().contains("outstanding")) {
            score += 20;
        } else if (feedback.toLowerCase().contains("good") || feedback.toLowerCase().contains("well")) {
            score += 15;
        } else if (feedback.toLowerCase().contains("satisfactory") || feedback.toLowerCase().contains("adequate")) {
            score += 10;
        }
        
        // Look for specific score mentions
        if (feedback.contains("Score:") || feedback.contains("score:")) {
            try {
                String[] parts = feedback.split("(?i)score:");
                if (parts.length > 1) {
                    String scorePart = parts[1].trim();
                    String[] scoreTokens = scorePart.split("\\s+");
                    if (scoreTokens.length > 0) {
                        String scoreStr = scoreTokens[0].replaceAll("[^0-9.]", "");
                        if (!scoreStr.isEmpty()) {
                            double extractedScore = Double.parseDouble(scoreStr);
                            if (extractedScore <= 100) {
                                return extractedScore;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("InterviewActivity", "Could not extract score from feedback", e);
            }
        }
        
        // Additional scoring based on content quality indicators
        if (feedback.toLowerCase().contains("specific examples")) {
            score += 5;
        }
        if (feedback.toLowerCase().contains("clear structure")) {
            score += 5;
        }
        if (feedback.toLowerCase().contains("professional")) {
            score += 5;
        }
        
        return Math.min(100, score);
    }
} 