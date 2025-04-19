package com.example.utptespam.ui.create; // Use your package name

import android.content.Intent;
import android.net.Uri; // Import Uri
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// ActivityResult imports
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
// Cloudinary Imports
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import com.example.utptespam.R;
import com.example.utptespam.SuccessActivity;
import com.example.utptespam.databinding.FragmentCreateBinding;
import com.example.utptespam.model.Message;

import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map

public class CreateFragment extends Fragment {

    private FragmentCreateBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "CreateFragment";

    // Variables for Image Handling
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedMessageImageUri = null;
    private String uploadedMessageImageUrl = null; // To store the final Cloudinary URL

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            selectedMessageImageUri = uri;
                            // Optional: Show a preview or indicator
                            binding.buttonUploadImage.setText("Image Selected"); // Simple indicator
                            binding.buttonUploadImage.setIcon(null); // Remove upload icon temporarily
                            // binding.imageViewPreview.setImageURI(uri); // If you add an ImageView for preview
                            Toast.makeText(getContext(), "Image selected, ready to upload.", Toast.LENGTH_SHORT).show();
                            // We upload when 'Send' is clicked, using selectedMessageImageUri
                        } else {
                            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setupListeners();
        // Reset button text/icon on view creation
        resetUploadButton();
        return view;
    }

    // Method to start image selection
    private void openImageChooser() {
        imagePickerLauncher.launch("image/*");
    }

    // Method to reset upload button appearance
    private void resetUploadButton() {
        if (binding != null) {
            binding.buttonUploadImage.setText("Unggah Gambar");
            binding.buttonUploadImage.setIconResource(R.drawable.ic_upload); // Set icon back
        }
        selectedMessageImageUri = null;
        uploadedMessageImageUrl = null;
    }

    // Method to upload selected image to Cloudinary
    private void uploadImageAndSendMessage(Uri imageUri, Message messageData) {
        String uploadPreset = "android_unsigned_upload"; // <-- Replace with your preset
        Log.d(TAG, "Uploading image to Cloudinary...");
        binding.buttonSend.setEnabled(false); // Disable send while uploading
        // Add progress indicator if desired

        MediaManager.get().upload(imageUri)
                .unsigned(uploadPreset)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Cloudinary Upload successful!");
                        uploadedMessageImageUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Cloudinary Image URL: " + uploadedMessageImageUrl);

                        // ★ Now save the message WITH the image URL ★
                        saveMessageToFirestore(messageData, uploadedMessageImageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Cloudinary Upload error: " + error.getDescription());
                        Toast.makeText(getContext(), "Image Upload Failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        binding.buttonSend.setEnabled(true); // Re-enable send on error
                        // Reset URIs
                        selectedMessageImageUri = null;
                        uploadedMessageImageUrl = null;
                    }

                    @Override
                    public void onStart(String requestId) { Log.d(TAG, "Cloudinary Upload started..."); }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { /* Optional progress */ }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { Log.w(TAG,"Cloudinary Upload rescheduled"); }
                })
                .dispatch(getContext());
    }

    // Method to save message data (with or without image URL) to Firestore
    private void saveMessageToFirestore(Message messageData, @Nullable String imageUrl) {
        // Add the final image URL to the message object before saving
        if (imageUrl != null) {
            // Need a setter in Message.java or create a new Map here
            // Let's modify messageData directly if we add a setter, or use a Map:
            Map<String, Object> finalMessageData = new HashMap<>();
            finalMessageData.put("senderId", messageData.getSenderId());
            finalMessageData.put("recipient", messageData.getRecipient());
            finalMessageData.put("snippet", messageData.getSnippet());
            finalMessageData.put("imageUrl", imageUrl); // Add the URL
            // Timestamp will be added by server automatically via @ServerTimestamp
            finalMessageData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()); // Alternative if not using annotation

            db.collection("messages").add(finalMessageData) // Save the map
                    .addOnSuccessListener(documentReference -> handleSaveSuccess(documentReference.getId()))
                    .addOnFailureListener(this::handleSaveFailure);

        } else {
            // Save message without image URL (original object)
            db.collection("messages").add(messageData)
                    .addOnSuccessListener(documentReference -> handleSaveSuccess(documentReference.getId()))
                    .addOnFailureListener(this::handleSaveFailure);
        }
    }

    // Helper for successful save
    private void handleSaveSuccess(String messageId) {
        Log.d(TAG, "Message sent successfully with ID: " + messageId);
        Toast.makeText(getContext(), "Message Sent!", Toast.LENGTH_SHORT).show();
        if (binding != null) binding.buttonSend.setEnabled(true);

        Intent intent = new Intent(getActivity(), SuccessActivity.class);
        startActivity(intent);

        // Clear fields and reset button/uris
        if (binding != null) {
            binding.editTextRecipient.setText("");
            binding.editTextMessage.setText("");
        }
        resetUploadButton(); // Reset button and URIs
    }

    // Helper for failed save
    private void handleSaveFailure(Exception e) {
        Log.w(TAG, "Error sending message", e);
        Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        if (binding != null) binding.buttonSend.setEnabled(true);
    }


    private void setupListeners() {
        // Upload button now launches the picker
        binding.buttonUploadImage.setOnClickListener(v -> openImageChooser());

        // Send Button Logic Updated
        binding.buttonSend.setOnClickListener(v -> {
            String recipient = binding.editTextRecipient.getText().toString().trim();
            String messageText = binding.editTextMessage.getText().toString().trim();

            if (TextUtils.isEmpty(recipient) || TextUtils.isEmpty(messageText)) { /* ... validation ... */ return; }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) { /* ... handle not logged in ... */ return; }
            String senderId = currentUser.getUid();

            // Create the basic message data (without image URL yet)
            Message newMessage = new Message(recipient, messageText, null, senderId);

            // Check if an image was selected
            if (selectedMessageImageUri != null) {
                // If image selected, upload it first, then save message with URL
                uploadImageAndSendMessage(selectedMessageImageUri, newMessage);
            } else {
                // If no image selected, save message directly without image URL
                saveMessageToFirestore(newMessage, null);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}