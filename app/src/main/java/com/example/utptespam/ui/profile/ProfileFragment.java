package com.example.utptespam.ui.profile; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.DocumentReference; // Import DocumentReference
import com.google.firebase.firestore.DocumentSnapshot; // Import DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import com.google.firebase.firestore.FirebaseFirestoreException; // Import Exception

import com.example.utptespam.LoginActivity;
import com.example.utptespam.R;
import com.example.utptespam.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Declare Firestore instance
    private static final String TAG = "ProfileFragment"; // For logging

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        loadUserProfile(); // Load user data when view is created
        setupListeners(); // Setup button listeners

        return view;
    }

    // Method to load user data
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Set Email from Auth
            String email = currentUser.getEmail();
            binding.textViewEmailValue.setText(email != null ? email : "N/A");

            // Get Username from Firestore
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.get().addOnCompleteListener(task -> {
                if (binding == null) {
                    // The view was destroyed before the task completed. Do nothing or log it.
                    Log.w("ProfileFragment", "View destroyed before profile load completed.");
                    return;
                }

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String username = document.getString("username"); // Key must match Firestore
                        binding.textViewUsernameValue.setText(username != null ? username : "N/A");
                        Log.d(TAG, "Username loaded: " + username);
                        // TODO: Load profile picture URL later from document.getString("profileImageUrl")
                        // --- Load Profile Picture using Glide ---
                        String profileImageUrl = document.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty() && getContext() != null) {
                            Glide.with(getContext()) // Use fragment's context
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person) // Your placeholder drawable
                                    .error(R.drawable.ic_person) // Your error drawable
                                    .circleCrop() // Make it circular
                                    .into(binding.imageViewProfilePic); // Target ImageView
                        } else {
                            // Set placeholder if no URL
                            binding.imageViewProfilePic.setImageResource(R.drawable.ic_person); // Placeholder
                        }
                    } else {
                        Log.d(TAG, "No such user document in Firestore");
                        binding.textViewUsernameValue.setText("N/A");
                    }
                } else {
                    Log.w(TAG, "Error getting user document from Firestore:", task.getException());
                    binding.textViewUsernameValue.setText("Error loading profile");
                }
            });

            // TODO: Load Post Count (requires querying messages/posts collection later)
            binding.textViewPostCount.setText("? Postingan"); // Placeholder

            // TODO: Load Profile Picture using Glide/Picasso from URL stored in Firestore

        } else {
            // No user logged in - this shouldn't happen if MainActivity protects routes,
            // but handle defensively. Maybe navigate to Login?
            Log.w(TAG, "No current user found in ProfileFragment");
            binding.textViewEmailValue.setText("N/A");
            binding.textViewUsernameValue.setText("N/A");
            // Consider navigating back to Login
            // Intent intent = new Intent(getActivity(), LoginActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            // if (getActivity() != null) getActivity().finish();
        }
    }


    private void setupListeners() {
        binding.buttonEditProfile.setOnClickListener(v -> {
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_container, editProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}