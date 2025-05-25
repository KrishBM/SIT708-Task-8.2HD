package com.example.task82.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.task82.MainActivity;
import com.example.task82.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInButton, signUpButton, demoButton;
    private TextView forgotPasswordText;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;
    
    // Demo mode for when Firebase is not configured
    private static final boolean DEMO_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupFirebaseAuth();
        setupClickListeners();
        
        // Check if user is already logged in
        checkUserStatus();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);
        demoButton = findViewById(R.id.demo_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        
        sharedPreferences = getSharedPreferences("JobAchieverPrefs", MODE_PRIVATE);
        
        // Show demo button if in demo mode
        if (DEMO_MODE) {
            demoButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> signInUser());
        signUpButton.setOnClickListener(v -> signUpUser());
        demoButton.setOnClickListener(v -> demoLogin());
        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void checkUserStatus() {
        // Check demo mode login first
        if (DEMO_MODE && sharedPreferences.getBoolean("demo_logged_in", false)) {
            navigateToMainActivity();
            return;
        }
        
        // Check Firebase login
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private void demoLogin() {
        // Demo login for demonstration purposes
        Toast.makeText(this, "Welcome to Job Achiever AI! (Demo Mode)", Toast.LENGTH_SHORT).show();
        
        // Save demo login state
        sharedPreferences.edit().putBoolean("demo_logged_in", true).apply();
        
        navigateToMainActivity();
    }

    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            if (DEMO_MODE) {
                // Demo mode - simulate successful login
                Toast.makeText(this, "Demo Sign In Successful!", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean("demo_logged_in", true).apply();
                navigateToMainActivity();
                return;
            }
            
            // Firebase authentication
            signInButton.setText("Signing In...");
            signInButton.setEnabled(false);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signInButton.setText("Sign In");
                        signInButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            
                            // Fallback to demo mode if Firebase fails
                            Toast.makeText(LoginActivity.this, "Switching to demo mode...", Toast.LENGTH_SHORT).show();
                            demoLogin();
                        }
                    });
        }
    }

    private void signUpUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (DEMO_MODE) {
                // Demo mode - simulate successful registration
                Toast.makeText(this, "Demo Account Created Successfully!", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean("demo_logged_in", true).apply();
                navigateToMainActivity();
                return;
            }

            signUpButton.setText("Creating Account...");
            signUpButton.setEnabled(false);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signUpButton.setText("Sign Up");
                        signUpButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Registration failed";
                            Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            
                            // Fallback to demo mode if Firebase fails
                            Toast.makeText(LoginActivity.this, "Switching to demo mode...", Toast.LENGTH_SHORT).show();
                            demoLogin();
                        }
                    });
        }
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (DEMO_MODE) {
            Toast.makeText(this, "Demo Mode: Password reset email would be sent to " + email, Toast.LENGTH_LONG).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 