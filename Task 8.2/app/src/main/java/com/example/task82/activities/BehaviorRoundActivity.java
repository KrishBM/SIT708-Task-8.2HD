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
import com.example.task82.models.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class BehaviorRoundActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int TOTAL_QUESTIONS = 3;
    
    private TextView questionText, feedbackText, userAnswerText, recordingStatusText;
    private TextView questionCounterText, overallScoreText, sessionProgressText;
    private Button startRecordingButton, nextQuestionButton, completeSessionButton;
    private CardView feedbackCard, userAnswerCard, analysisCard;
    private ImageView recordingIndicator;
    private ProgressBar recordingProgress, sessionProgress;
    
    private SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;
    private boolean isProcessingResults = false;
    private int currentQuestionIndex = 0;
    private String lastPartialResult = "";
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;
    
    // Enhanced behavioral interview questions
    private List<String> behavioralQuestions = Arrays.asList(
        "Tell me about a time when you had to work with a difficult team member. How did you handle the situation?",
        "Describe a situation where you had to meet a tight deadline. What steps did you take to ensure success?",
        "Give me an example of a time when you had to adapt to a significant change at work. How did you manage it?"
    );
    
    // Session tracking
    private List<String> userAnswers = new ArrayList<>();
    private List<String> aiFeedbackList = new ArrayList<>();
    private List<Double> questionScores = new ArrayList<>();
    private InterviewSession currentSession;
    
    private LlamaAIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavior_round);
        
        initializeViews();
        setupClickListeners();
        checkPermissions();
        initializeSpeechRecognizer();
        
        aiService = new LlamaAIService();
        initializeSession();
        loadFirstQuestion();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.question_text);
        feedbackText = findViewById(R.id.feedback_text);
        userAnswerText = findViewById(R.id.user_answer_text);
        recordingStatusText = findViewById(R.id.recording_status_text);
        questionCounterText = findViewById(R.id.question_counter_text);
        overallScoreText = findViewById(R.id.overall_score_text);
        sessionProgressText = findViewById(R.id.session_progress_text);
        
        startRecordingButton = findViewById(R.id.start_recording_button);
        nextQuestionButton = findViewById(R.id.next_question_button);
        completeSessionButton = findViewById(R.id.complete_session_button);
        
        feedbackCard = findViewById(R.id.feedback_card);
        userAnswerCard = findViewById(R.id.user_answer_card);
        analysisCard = findViewById(R.id.analysis_card);
        recordingIndicator = findViewById(R.id.recording_indicator);
        recordingProgress = findViewById(R.id.recording_progress);
        sessionProgress = findViewById(R.id.session_progress);
    }

    private void setupClickListeners() {
        startRecordingButton.setOnClickListener(v -> toggleRecording());
        nextQuestionButton.setOnClickListener(v -> loadNextQuestion());
        completeSessionButton.setOnClickListener(v -> completeSession());
    }

    private void initializeSession() {
        currentSession = new InterviewSession();
        currentSession.setId("behavior_" + System.currentTimeMillis());
        currentSession.setJobCategory("Behavioral Interview");
        currentSession.setTimestamp(new Date());
        currentSession.setUserAnswers(new ArrayList<>());
        currentSession.setAiFeedback(new ArrayList<>());
        
        updateSessionProgress();
    }

    private void updateSessionProgress() {
        int progress = (int) ((currentQuestionIndex / (float) TOTAL_QUESTIONS) * 100);
        sessionProgress.setProgress(progress);
        questionCounterText.setText("Question " + (currentQuestionIndex + 1) + " of " + TOTAL_QUESTIONS);
        sessionProgressText.setText("Session Progress: " + progress + "%");
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
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onReadyForSpeech ===");
                    updateRecordingUI(true, "Ready to listen... Start speaking now!");
                    cancelTimeout();
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onBeginningOfSpeech ===");
                    updateRecordingUI(true, "Listening... Keep speaking!");
                    cancelTimeout();
                    isProcessingResults = false;
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    if (recordingProgress != null && isRecording) {
                        int progress = (int) Math.min(100, Math.max(0, (rmsdB + 10) * 5));
                        recordingProgress.setProgress(progress);
                    }
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onBufferReceived ===");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onEndOfSpeech ===");
                    if (!isProcessingResults) {
                        updateRecordingUI(false, "Processing your answer...");
                        startTimeout(2000);
                    }
                }

                @Override
                public void onError(int error) {
                    Log.e("BehaviorRoundActivity", "=== SPEECH ERROR: " + getErrorMessage(error) + " ===");
                    runOnUiThread(() -> {
                        isRecording = false;
                        isProcessingResults = false;
                        updateRecordingUI(false, "Error: " + getErrorMessage(error));
                        Toast.makeText(BehaviorRoundActivity.this, 
                            "Speech recognition error: " + getErrorMessage(error), 
                            Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onResults ===");
                    isProcessingResults = true;
                    cancelTimeout();
                    
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        Log.d("BehaviorRoundActivity", "Final result: " + recognizedText);
                        
                        runOnUiThread(() -> {
                            isRecording = false;
                            updateRecordingUI(false, "Processing complete!");
                            processUserAnswer(recognizedText);
                        });
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String partialText = matches.get(0);
                        if (!partialText.equals(lastPartialResult)) {
                            lastPartialResult = partialText;
                            runOnUiThread(() -> {
                                recordingStatusText.setText("Heard: " + partialText);
                            });
                        }
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d("BehaviorRoundActivity", "=== SPEECH: onEvent " + eventType + " ===");
                }
            });
        }
    }

    private void startTimeout(int milliseconds) {
        cancelTimeout();
        timeoutRunnable = () -> {
            Log.d("BehaviorRoundActivity", "=== TIMEOUT: Stopping recording ===");
            if (speechRecognizer != null && isRecording) {
                speechRecognizer.stopListening();
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
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No speech input detected";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Unknown error";
        }
    }

    private void updateRecordingUI(boolean recording, String status) {
        isRecording = recording;
        recordingIndicator.setVisibility(recording ? View.VISIBLE : View.GONE);
        recordingStatusText.setText(status);
        startRecordingButton.setText(recording ? "Stop Recording" : "Start Recording");
        startRecordingButton.setEnabled(true);
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (speechRecognizer == null) {
            Toast.makeText(this, "Speech recognizer not available", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Answer the behavioral question");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);

        updateRecordingUI(true, "Initializing...");
        speechRecognizer.startListening(intent);
        startTimeout(30000); // 30 second maximum recording time
    }

    private void stopRecording() {
        if (speechRecognizer != null && isRecording) {
            speechRecognizer.stopListening();
            cancelTimeout();
        }
    }

    private void processUserAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            Toast.makeText(this, "No answer detected. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store the answer
        userAnswers.add(userAnswer);
        currentSession.getUserAnswers().add(userAnswer);

        // Display the user's answer
        userAnswerText.setText(userAnswer);
        userAnswerCard.setVisibility(View.VISIBLE);

        // Generate AI feedback
        String currentQuestion = getCurrentQuestion();
        aiService.generateBehavioralFeedback(currentQuestion, userAnswer, new LlamaAIService.FeedbackCallback() {
            @Override
            public void onSuccess(String feedback) {
                runOnUiThread(() -> {
                    // Store feedback
                    aiFeedbackList.add(feedback);
                    currentSession.getAiFeedback().add(feedback);
                    
                    // Extract score from feedback (simulated)
                    double score = extractScoreFromFeedback(feedback);
                    questionScores.add(score);
                    
                    // Display feedback
                    feedbackText.setText(feedback);
                    feedbackCard.setVisibility(View.VISIBLE);
                    
                    // Update analysis
                    updateAnalysis();
                    
                    // Show next question button or complete session button
                    if (currentQuestionIndex < TOTAL_QUESTIONS - 1) {
                        nextQuestionButton.setVisibility(View.VISIBLE);
                    } else {
                        completeSessionButton.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    feedbackText.setText("Error generating feedback: " + error);
                    feedbackCard.setVisibility(View.VISIBLE);
                    if (currentQuestionIndex < TOTAL_QUESTIONS - 1) {
                        nextQuestionButton.setVisibility(View.VISIBLE);
                    } else {
                        completeSessionButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
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
        
        if (feedback.toLowerCase().contains("specific examples")) {
            score += 5;
        }
        if (feedback.toLowerCase().contains("clear structure")) {
            score += 5;
        }
        
        return Math.min(100, score);
    }

    private void updateAnalysis() {
        if (questionScores.isEmpty()) return;
        
        double averageScore = questionScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        overallScoreText.setText(String.format("Current Average Score: %.1f/100", averageScore));
        
        // Update session score
        currentSession.setScore(averageScore);
        
        analysisCard.setVisibility(View.VISIBLE);
    }

    private String getCurrentQuestion() {
        if (currentQuestionIndex < behavioralQuestions.size()) {
            return behavioralQuestions.get(currentQuestionIndex);
        }
        return "";
    }

    private void loadFirstQuestion() {
        currentQuestionIndex = 0;
        loadCurrentQuestion();
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex < TOTAL_QUESTIONS - 1) {
            currentQuestionIndex++;
            loadCurrentQuestion();
            hideAnswerAndFeedback();
        }
    }

    private void loadCurrentQuestion() {
        String question = getCurrentQuestion();
        questionText.setText(question);
        updateSessionProgress();
        
        // Reset UI
        startRecordingButton.setEnabled(true);
        recordingStatusText.setText("Tap 'Start Recording' to begin your answer");
    }

    private void hideAnswerAndFeedback() {
        userAnswerCard.setVisibility(View.GONE);
        feedbackCard.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.GONE);
        completeSessionButton.setVisibility(View.GONE);
    }

    private void completeSession() {
        currentSession.setCompleted(true);
        
        // Calculate final analytics
        double finalScore = questionScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        currentSession.setScore(finalScore);
        
        Toast.makeText(this, "Behavioral interview session completed!", Toast.LENGTH_LONG).show();
        
        // Return to main activity with results
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeechRecognizer();
            } else {
                Toast.makeText(this, "Audio permission required for speech recognition", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        cancelTimeout();
    }
} 