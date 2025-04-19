package com.example.utptespam; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.utptespam.databinding.ActivitySuccessBinding; // Import generated binding

public class SuccessActivity extends AppCompatActivity {

    private ActivitySuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Listener for the "Halaman utama" button
        binding.buttonGoToHome.setOnClickListener(v -> {
            // Navigate back to MainActivity (assuming it's the home host)
            Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
            // Clear the activity stack so pressing back doesn't come back here
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close this activity
        });
    }

    // Optional: Override back button press if you don't want users going back
    // @Override
    // public void onBackPressed() {
    //     // super.onBackPressed(); // Disable default back press behavior
    //     // Or navigate home like the button does
    //     binding.buttonGoToHome.performClick();
    // }
}