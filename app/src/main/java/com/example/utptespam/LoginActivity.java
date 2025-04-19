package com.example.utptespam; // Use your package name

import android.app.Activity; // Import Activity
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

// Imports for ActivityResultLauncher
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Google Sign In Imports
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

// Firebase Auth Imports
import com.google.firebase.auth.AuthCredential; // Import AuthCredential
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider; // Import GoogleAuthProvider

import com.example.utptespam.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth; // Declare Firebase Auth instance
    private GoogleSignInClient mGoogleSignInClient; // Google Sign In Client
    private ActivityResultLauncher<Intent> googleSignInLauncher; // Launcher for Google Sign In
    private static final String TAG = "LoginActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // --- Configure Google Sign In ---
        // Configure Google Sign In options. Request ID token and basic profile.
        // ID token is obtained using DEFAULT_WEB_CLIENT_ID - IMPORTANT!
        // Ensure you have string resource R.string.default_web_client_id containing your WEB client ID
        // (You might need to add it manually to res/values/strings.xml from your google-services.json)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your Web Client ID here
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // --- End Google Sign In Config ---


        // --- Initialize ActivityResultLauncher for Google Sign In ---
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Google Sign In was successful, authenticate with Firebase
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                                if (account.getIdToken() != null) {
                                    firebaseAuthWithGoogle(account);
                                } else {
                                    Log.w(TAG, "Google ID Token is null");
                                    Toast.makeText(LoginActivity.this, "Google Sign In failed (No ID Token).", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ApiException e) {
                                // Google Sign In failed, update UI appropriately
                                Log.w(TAG, "Google sign in failed", e);
                                Toast.makeText(LoginActivity.this, "Google Sign In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Google Sign In failed or was cancelled by user
                            Log.w(TAG, "Google sign in cancelled or failed with result code: " + result.getResultCode());
                            Toast.makeText(LoginActivity.this, "Google Sign In Cancelled/Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // --- End Launcher Initialization ---


        // Setup other listeners
        setupListeners();

    } // End onCreate


    // --- Add this onStart method ---
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, go directly to MainActivity
            Toast.makeText(this, "User already logged in: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show(); // Optional: for debugging
            goToMainActivity();
        } else {
            // User is signed out, stay on LoginActivity
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show(); // Optional: for debugging
        }
    }
    // --- End of onStart method ---


    // --- Helper method to go to MainActivity ---
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Clear back stack so user cannot press back to get to Login screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close LoginActivity
    }

    // --- Firebase Authentication with Google Credential ---
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // Show progress (optional)
        // binding.progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress (optional)
                        // binding.progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential(Google):success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Google Sign In Successful!", Toast.LENGTH_SHORT).show();
                            // TODO: Check if this is a new user (task.getAdditionalUserInfo().isNewUser())
                            // If new user, might need to save their info (name, email, pic url from acct) to Firestore here.
                            goToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential(Google):failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // --- End firebaseAuthWithGoogle ---

    // --- Method to setup listeners (refactored from onCreate) ---
    private void setupListeners() {
        // --- Update buttonSignIn Listener ---
        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get email and password from EditTexts
                // NOTE: Your XML uses editTextUsername for the email/username field ID
                String email = binding.editTextUsername.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString().trim();

                // --- Input Validation ---
                if (TextUtils.isEmpty(email)) {
                    binding.textFieldUsernameLayout.setError("Email/Username is required.");
                    return;
                } else {
                    binding.textFieldUsernameLayout.setError(null);
                }
                if (TextUtils.isEmpty(password)) {
                    binding.textFieldPasswordLayout.setError("Password is required.");
                    return;
                } else {
                    binding.textFieldPasswordLayout.setError(null);
                }
                // --- End Validation ---

                // Show progress indicator (optional)
                // binding.progressBar.setVisibility(View.VISIBLE);

                // --- Sign In with Firebase Auth ---
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Hide progress indicator (optional)
                                // binding.progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    // Sign in success
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Login Successful!",
                                            Toast.LENGTH_SHORT).show();
                                    // Navigate to MainActivity
                                    goToMainActivity();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    // Consider more specific error checks if needed
                                    // e.g., invalid credentials, user not found
                                }
                            }
                        });
                // --- End Firebase Auth Call ---
            }
        });
        // --- End buttonSignIn Listener ---

        // Google Sign In Button Listener
        binding.buttonSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Google Sign In button clicked");
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent); // Use the launcher
            }
        });

        binding.textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        binding.textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Forgot Password clicked (Not Implemented)", Toast.LENGTH_SHORT).show();
                // TODO: Implement Forgot Password logic
            }
        });
    }

} // End of LoginActivity class