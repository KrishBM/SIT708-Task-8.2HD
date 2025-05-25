package com.example.task82.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.task82.R;
import com.example.task82.activities.InterviewActivity;
import com.example.task82.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private TextView practiceSessionsText, performanceText;
    private CardView practiceCard, performanceCard;
    private Button startPracticeButton, logoutButton;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupClickListeners();
        loadProfileData();
        
        return view;
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        practiceSessionsText = view.findViewById(R.id.practice_sessions_text);
        performanceText = view.findViewById(R.id.performance_text);
        
        practiceCard = view.findViewById(R.id.practice_card);
        performanceCard = view.findViewById(R.id.performance_card);
        
        startPracticeButton = view.findViewById(R.id.start_practice_button);
        logoutButton = view.findViewById(R.id.logout_button);
        
        firebaseAuth = FirebaseAuth.getInstance();
        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences("JobAchieverPrefs", getActivity().MODE_PRIVATE);
        }
    }

    private void setupClickListeners() {
        startPracticeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InterviewActivity.class);
            startActivity(intent);
        });

        practiceCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InterviewActivity.class);
            startActivity(intent);
        });

        performanceCard.setOnClickListener(v -> {
            // Switch to dashboard fragment
            if (getActivity() != null) {
                ((com.example.task82.MainActivity) getActivity()).loadFragment(new DashboardFragment());
            }
        });
        
        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Clear demo login state
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean("demo_logged_in", false).apply();
        }
        
        // Sign out from Firebase
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        
        // Navigate to login screen
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void loadProfileData() {
        // Load sample data - in real app, this would come from Firebase/API
        profileName.setText("Job Achiever AI");
        profileEmail.setText("Interview Skills • Mock Interviews");
        practiceSessionsText.setText("Practice Sessions");
        performanceText.setText("Performance");
    }
} 