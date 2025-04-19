package com.example.utptespam.ui.profile; // Adjust package name

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Firebase Imports
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.utptespam.R;
import com.example.utptespam.databinding.FragmentEditProfileBinding;
// import com.bumptech.glide.Glide; // Import Glide/Picasso later

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri = null;
    private String uploadedImageUrl = null; // Will store Cloudinary URL after upload
    private String currentProfileImageUrl = null; // To store existing image URL

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "EditProfileFragment";
    private String originalUsername = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(/* ... keep existing code ... */
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedImageUri = uri;
                            Toast.makeText(getContext(), "Image selected, uploading...", Toast.LENGTH_SHORT).show();
                            binding.imageViewEditProfilePic.setImageURI(selectedImageUri); // Show local preview
                            uploadImageToCloudinary(selectedImageUri);
                        } else {
                            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        loadUserData(); // Load data when view is created
        setupClickListeners();

        return view;
    }

    // --- New Method to Load User Data ---
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Load Email (usually not editable, set as text or hint)
            String email = currentUser.getEmail();
            if (email != null) {
                binding.editTextEditEmail.setText(email); // Set text
                binding.editTextEditEmail.setEnabled(false); // Disable editing email
            }

            // Load Username and Profile Picture URL from Firestore
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Load Username
                        String username = document.getString("username");
                        originalUsername = username != null ? username : ""; // Store original username
                        binding.editTextEditUsername.setText(originalUsername);
                        binding.textViewUsernameDisplay.setText(originalUsername); // Also update text below image

                        // --- Load Profile Picture using Glide ---
                        currentProfileImageUrl = document.getString("profileImageUrl"); // Store original URL
                        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty() && getContext() != null) {
                            Glide.with(getContext()) // Use fragment's context
                                    .load(currentProfileImageUrl)
                                    .placeholder(R.drawable.ic_person) // Your placeholder drawable
                                    .error(R.drawable.ic_person) // Your error drawable
                                    .circleCrop() // Make it circular
                                    .into(binding.imageViewEditProfilePic); // Target ImageView
                            Log.d(TAG, "Loading profile image: " + currentProfileImageUrl);
                        } else {
                            // Set placeholder if no URL
                            binding.imageViewEditProfilePic.setImageResource(R.drawable.ic_person); // Placeholder
                        }
                        // --- End Load Profile Picture ---

                    } else {
                        originalUsername = ""; // Reset if no document found
                        Log.d(TAG, "No profile document found for user " + userId);
                        binding.editTextEditUsername.setText(""); // Clear field if no data
                        binding.textViewUsernameDisplay.setText("...");
                        binding.imageViewEditProfilePic.setImageResource(R.drawable.ic_person); // Default placeholder
                    }
                } else {
                    Log.w(TAG, "Error getting user document:", task.getException());
                    Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                    binding.editTextEditUsername.setText("");
                    binding.textViewUsernameDisplay.setText("Error");
                    binding.imageViewEditProfilePic.setImageResource(R.drawable.ic_person); // Default placeholder
                }
            });

        } else {
            // Handle case where user is somehow null (shouldn't happen if navigation is protected)
            Log.e(TAG, "CurrentUser is null in EditProfileFragment");
            Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            // Optionally navigate back or finish
        }
    }
    // --- End Load User Data ---


    private void uploadImageToCloudinary(Uri imageUri) {
        // ... keep existing upload code ...
        String uploadPreset = "android_unsigned_upload"; // <-- Replace with your actual preset name
        MediaManager.get().upload(imageUri)
                .unsigned(uploadPreset)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CloudinaryUpload", "Upload Started!");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("CloudinaryUpload", "Upload in Progress!");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("CloudinaryUpload", "Upload successful!");
                        uploadedImageUrl = (String) resultData.get("secure_url"); // Store the NEW URL
                        Log.d("CloudinaryUpload", "New Image URL: " + uploadedImageUrl);
                        // Update the ImageView with the uploaded image (using Glide recommended)
                        // Glide.with(requireContext()).load(uploadedImageUrl)...
                        Toast.makeText(getContext(), "Image Upload Success!", Toast.LENGTH_SHORT).show();

                    }
                    // ... onError, onProgress etc. ...
                    @Override
                    public void onError(String requestId, ErrorInfo error){
                        Log.e("CloudinaryUpload", "Upload error: " + error.getDescription());
                        Toast.makeText(getContext(), "Upload Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        // Reset selected URI and potentially the ImageView if upload fails
                        selectedImageUri = null;
                        uploadedImageUrl = null;
                        // Reload original image if needed, or show placeholder
                        // loadUserData(); // Could reload original data
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                    // ...
                })
                .dispatch(getContext());
    }

    private void setupClickListeners() {
        // ... keep existing listeners ...
        binding.imageViewEditIcon.setOnClickListener(v -> openImageChooser());
        binding.imageViewEditProfilePic.setOnClickListener(v -> openImageChooser());
        binding.buttonSaveProfile.setOnClickListener(v -> {
            // Get current input values
            String newUsername = binding.editTextEditUsername.getText().toString().trim();
            String newPassword = binding.editTextEditPassword.getText().toString(); // Don't trim password

            // --- Input Validation (Keep existing validation) ---
            boolean isValid = true;
            // Validate Username
            if (newUsername.isEmpty()) {
                binding.textFieldEditUsernameLayout.setError("Username cannot be empty");
                isValid = false;
            } else {
                binding.textFieldEditUsernameLayout.setError(null);
            }
            // Validate Password (only if field is not empty)
            int minPasswordLength = 6;
            if (!newPassword.isEmpty() && newPassword.length() < minPasswordLength) {
                binding.textFieldEditPasswordLayout.setError("Password must be at least " + minPasswordLength + " characters");
                isValid = false;
            } else {
                binding.textFieldEditPasswordLayout.setError(null);
            }
            if (!isValid) {
                return; // Stop if validation failed
            }
            // --- End Validation ---



            // --- Implement Save Logic ---
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
                return; // Should not happen, but check defensively
            }
            String userId = currentUser.getUid();

            // Show progress indicator (Add a ProgressBar to your XML, e.g., binding.progressBarSave)
            // binding.progressBarSave.setVisibility(View.VISIBLE);
            // Disable button while saving
            binding.buttonSaveProfile.setEnabled(false);


            // --- Determine what changed ---
            boolean usernameChanged = !newUsername.equals(originalUsername);
            boolean passwordChanged = !newPassword.isEmpty();
            boolean profilePicChanged = uploadedImageUrl != null; // Check if a new URL was obtained from Cloudinary upload

            // --- Build Firestore Update Map (Only include changed fields) ---
            Map<String, Object> updates = new HashMap<>();
            if (usernameChanged) {
                updates.put("username", newUsername);
                Log.d(TAG, "Username will be updated.");
            }
            if (profilePicChanged) {
                updates.put("profileImageUrl", uploadedImageUrl);
                Log.d(TAG, "Profile image URL will be updated.");
            }

            // --- Perform Updates ---
            // --- Perform Updates (only if something actually changed) ---
            Task<Void> firestoreTask = null; // Initialize task

            // Check if there are any updates for Firestore
            if (!updates.isEmpty()) {
                Log.d(TAG, "Updating Firestore with: " + updates);
                firestoreTask = db.collection("users").document(userId).update(updates);
            }

            // Check if only password changed (no Firestore updates needed)
            else if (passwordChanged) {
                Log.d(TAG, "Only password changed. Skipping Firestore update.");
                // Create a dummy successful task so the password update logic runs
                firestoreTask = Tasks.forResult(null);
            }

            // Handle case where nothing changed
            else {
                Log.d(TAG, "No changes detected.");
                Toast.makeText(getContext(), "No changes to save.", Toast.LENGTH_SHORT).show();
                allUpdatesFailed(); // Re-enable button etc. (or just navigate back)
                // If navigating back immediately:
                // if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
                return; // Exit if nothing changed
            }

            firestoreTask.addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Firestore update successful (or not needed).");

                // Task 2: Update Password (if needed) - Do this AFTER Firestore potentially succeeds
                if (passwordChanged) {
                    Log.d(TAG, "Updating password in Firebase Auth.");
                    currentUser.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                        if (passwordTask.isSuccessful()) {
                            Log.d(TAG, "Password update successful.");
                            // Both Firestore (if needed) and Password update succeeded
                            allUpdatesSucceeded();
                        } else {
                            // Password update failed
                            Log.w(TAG, "Password update failed.", passwordTask.getException());
                            Toast.makeText(getContext(), "Failed to update password: " + passwordTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            allUpdatesFailed(); // Indicate overall failure
                        }
                    });
                } else {
                    // No password change needed, and Firestore update succeeded (or wasn't needed)
                    allUpdatesSucceeded();
                }

            }).addOnFailureListener(e -> {
                // Firestore update failed
                Log.w(TAG, "Firestore update failed.", e);
                Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                allUpdatesFailed(); // Indicate overall failure
            });
            // --- End Perform Updates ---

        }); // End of Save Button onClick

    }

    // Helper method called when all necessary updates succeed
    private void allUpdatesSucceeded() {
        Toast.makeText(getContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
        // Hide progress indicator
        // if (binding != null) binding.progressBarSave.setVisibility(View.GONE);
        // Navigate back
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    // Helper method called when any update fails
    private void allUpdatesFailed() {
        // Hide progress indicator
        // if (binding != null) binding.progressBarSave.setVisibility(View.GONE);
        // Re-enable save button
        if (binding != null) binding.buttonSaveProfile.setEnabled(true);
    }

    private void openImageChooser() {
        // ... keep existing code ...
        imagePickerLauncher.launch("image/*");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}