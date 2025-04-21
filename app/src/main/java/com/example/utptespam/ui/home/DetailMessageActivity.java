package com.example.utptespam.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.utptespam.R;
import com.example.utptespam.databinding.ActivityDetailMessageBinding;

public class DetailMessageActivity extends AppCompatActivity {

    private ActivityDetailMessageBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil data dari Intent
        Intent intent = getIntent();
        String recipient = intent.getStringExtra("recipient");
        String snippet = intent.getStringExtra("snippet");
        String imageUrl = intent.getStringExtra("imageUrl");

        // Set Text
        binding.textViewRecipient.setText("To: " + (recipient != null ? recipient : "..."));
        binding.textViewMessage.setText(snippet != null ? snippet : "...");

        // Tampilkan gambar (jika ada), jika tidak pakai logo default
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_whisperverse_small)
                    .error(R.drawable.ic_whisperverse_small)
                    .into(binding.imageViewMessage);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_whisperverse_small)
                    .into(binding.imageViewMessage);
        }

        // Tombol kembali
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Tutup activity dan kembali
        });
    }
}
