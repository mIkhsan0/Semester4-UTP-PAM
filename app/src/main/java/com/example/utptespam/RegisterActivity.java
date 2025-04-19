package com.example.utptespam; // Make sure this matches your package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.util.Log; // Import Log
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener; // Import OnCompleteListener
import com.google.android.gms.tasks.Task; // Import Task
import com.google.firebase.auth.AuthResult; // Import AuthResult
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map


import com.example.utptespam.databinding.ActivityRegisterBinding; // Import ViewBinding

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth; // Declare Firebase Auth instance
    private FirebaseFirestore db; // Declare Firestore instance
    private static final String TAG = "RegisterActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // ... setupListeners() method remains the same ...
        setupListeners();
    }

    private void setupListeners() {
        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextEmail.getText().toString().trim();
                String username = binding.editTextUsername.getText().toString().trim(); // Get username
                String password = binding.editTextPassword.getText().toString();

                // --- Validation (keep existing validation) ---
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || password.length() < 6) {
                    // Handle validation errors as before...
                    if (TextUtils.isEmpty(email)) binding.textFieldEmailLayout.setError("Email is required."); else binding.textFieldEmailLayout.setError(null);
                    if (TextUtils.isEmpty(username)) binding.textFieldUsernameLayout.setError("Username is required."); else binding.textFieldUsernameLayout.setError(null);
                    if (TextUtils.isEmpty(password)) binding.textFieldPasswordLayout.setError("Password is required."); else binding.textFieldPasswordLayout.setError(null);
                    if (!TextUtils.isEmpty(password) && password.length() < 6) binding.textFieldPasswordLayout.setError("Password must be >= 6 characters."); else if (!TextUtils.isEmpty(password)) binding.textFieldPasswordLayout.setError(null);
                    return;
                } else {
                    // Clear all errors if input seems initially okay
                    binding.textFieldEmailLayout.setError(null);
                    binding.textFieldUsernameLayout.setError(null);
                    binding.textFieldPasswordLayout.setError(null);
                }
                // --- End Validation ---


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        // --- Save username to Firestore ---
                                        String userId = user.getUid();
                                        // Create a new user data map
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("username", username);
                                        userData.put("email", email);
                                        // Add other fields later if needed (e.g., profileImageUrl: null)

                                        db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "User profile created in Firestore for " + userId);
                                                    // Proceed only after Firestore write is successful
                                                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                    navigateToLogin();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w(TAG, "Error writing user profile to Firestore", e);
                                                    Toast.makeText(RegisterActivity.this, "Registration failed (database error).", Toast.LENGTH_SHORT).show();
                                                    // Optional: Delete the auth user if DB write fails? (more complex)
                                                });
                                        // --- End Firestore Save ---
                                    } else {
                                        // This case should ideally not happen if task was successful
                                        Toast.makeText(RegisterActivity.this, "Registration failed (user null).", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    // Registration failed
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                                    Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        binding.textViewSignInLink.setOnClickListener(v -> finish());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close RegisterActivity
    }
}