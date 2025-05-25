package com.example.task82.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.task82.R;

public class FeedbackFragment extends Fragment {

    private EditText commentsEditText;
    private RatingBar experienceRatingBar;
    private RadioGroup satisfactionRadioGroup;
    private CheckBox termsCheckBox;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        
        initializeViews(view);
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        commentsEditText = view.findViewById(R.id.comments_edit_text);
        experienceRatingBar = view.findViewById(R.id.experience_rating_bar);
        satisfactionRadioGroup = view.findViewById(R.id.satisfaction_radio_group);
        termsCheckBox = view.findViewById(R.id.terms_checkbox);
        submitButton = view.findViewById(R.id.submit_feedback_button);
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String comments = commentsEditText.getText().toString().trim();
        float rating = experienceRatingBar.getRating();
        int selectedSatisfactionId = satisfactionRadioGroup.getCheckedRadioButtonId();
        boolean termsAccepted = termsCheckBox.isChecked();

        // Validate input
        if (comments.isEmpty()) {
            Toast.makeText(getContext(), "Please provide your comments", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(getContext(), "Please rate your experience", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSatisfactionId == -1) {
            Toast.makeText(getContext(), "Please select your satisfaction level", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsAccepted) {
            Toast.makeText(getContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Submit feedback (in real app, this would send to Firebase/API)
        submitFeedbackToServer(comments, rating, selectedSatisfactionId);
    }

    private void submitFeedbackToServer(String comments, float rating, int satisfactionId) {
        // Simulate feedback submission
        Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_LONG).show();
        
        // Clear form
        commentsEditText.setText("");
        experienceRatingBar.setRating(0);
        satisfactionRadioGroup.clearCheck();
        termsCheckBox.setChecked(false);
    }
} 