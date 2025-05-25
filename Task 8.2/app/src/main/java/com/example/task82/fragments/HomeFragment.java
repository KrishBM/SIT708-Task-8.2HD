package com.example.task82.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.task82.R;
import com.example.task82.activities.InterviewActivity;
import com.example.task82.activities.BehaviorRoundActivity;

public class HomeFragment extends Fragment {

    private TextView welcomeTitle, welcomeSubtitle;
    private CardView mockInterviewCard, behaviorInterviewCard, dashboardCard, feedbackCard;
    private Button getStartedButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        welcomeTitle = view.findViewById(R.id.welcome_title);
        welcomeSubtitle = view.findViewById(R.id.welcome_subtitle);
        getStartedButton = view.findViewById(R.id.get_started_button);
        
        mockInterviewCard = view.findViewById(R.id.mock_interview_card);
        behaviorInterviewCard = view.findViewById(R.id.behavior_interview_card);
        dashboardCard = view.findViewById(R.id.dashboard_card);
        feedbackCard = view.findViewById(R.id.feedback_card);
    }

    private void setupClickListeners() {
        getStartedButton.setOnClickListener(v -> {
            // Navigate to interview activity
            Intent intent = new Intent(getActivity(), InterviewActivity.class);
            startActivity(intent);
        });

        mockInterviewCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InterviewActivity.class);
            startActivity(intent);
        });
        
        behaviorInterviewCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BehaviorRoundActivity.class);
            startActivity(intent);
        });

        dashboardCard.setOnClickListener(v -> {
            // Switch to dashboard fragment
            if (getActivity() != null) {
                ((com.example.task82.MainActivity) getActivity()).loadFragment(new DashboardFragment());
            }
        });

        feedbackCard.setOnClickListener(v -> {
            // Switch to feedback fragment
            if (getActivity() != null) {
                ((com.example.task82.MainActivity) getActivity()).loadFragment(new FeedbackFragment());
            }
        });
    }
} 