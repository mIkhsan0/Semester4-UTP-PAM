package com.example.utptespam;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Import MenuItem
import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // Import Fragment
import androidx.fragment.app.FragmentManager; // Import FragmentManager
import androidx.fragment.app.FragmentTransaction; // Import FragmentTransaction

import com.google.android.material.navigation.NavigationBarView; // Import NavigationBarView
import com.example.utptespam.databinding.ActivityMainBinding; // Import your binding class

// Placeholder Fragment classes (we need to create these basic files)
import com.example.utptespam.ui.home.HomeFragment;
import com.example.utptespam.ui.create.CreateFragment; // Placeholder for Create
import com.example.utptespam.ui.profile.ProfileFragment; // Placeholder for Profile

import java.util.HashMap; // Add import
import java.util.Map; // Add import
import com.cloudinary.android.MediaManager; // Add import

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Declare binding variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Cloudinary
        try {
            // Specify the Generic Types: <String, String>
            Map<String, String> config = new HashMap<>(); // Diamond operator <> infers types here
            config.put("cloud_name", "ds7cudqcp");
            // If you were adding other types, you might use Map<String, Object>
            // config.put("secure", true); // If you did this, use Map<String, Object>

            MediaManager.init(this, config); // Pass the correctly typed Map
            Log.i("MainActivity", "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e("MainActivity", "Cloudinary initialization failed", e);
            // Handle initialization error if needed
        }

        // --- Navigation Logic ---

        // Load the default fragment (HomeFragment) when the activity starts
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load HomeFragment initially
        }

        // Set the listener for bottom navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId(); // Get the ID of the clicked item

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_create) {
                    selectedFragment = new CreateFragment(); // Use your CreateFragment
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment(); // Use your ProfileFragment
                } else {
                    // Default case or handle error
                    selectedFragment = new HomeFragment(); // Default to home if ID is unknown
                }

                // Replace the current fragment with the selected one
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true; // Indicate the item selection was handled
                }
                return false; // Indicate the item selection was not handled
            }
        });
    }

    // Helper method to load fragments into the container
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment_container, fragment); // Replace content in the FrameLayout
        // fragmentTransaction.addToBackStack(null); // Optional: Add transaction to back stack
        fragmentTransaction.commit();
    }
}