package com.example.utptespam.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.utptespam.R;
import com.example.utptespam.databinding.ActivityDetailMessageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        String senderId = intent.getStringExtra("senderId");
        String documentId = intent.getStringExtra("documentId"); // 🔥 Ambil documentId juga

        // Tampilkan teks
        binding.textViewRecipient.setText("To: " + (recipient != null ? recipient : "..."));
        binding.textViewMessage.setText(snippet != null ? snippet : "...");

        // Tampilkan gambar (opsional)
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

        // Cek apakah user login adalah pemilik pesan
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (senderId != null && senderId.equals(currentUid)) {
            binding.btnDelete.setVisibility(View.VISIBLE);

            // Jika tombol delete ditekan
            binding.btnDelete.setOnClickListener(v -> {
                if (documentId != null && !documentId.isEmpty()) {
                    FirebaseFirestore.getInstance()
                            .collection("messages")
                            .document(documentId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Pesan berhasil dihapus", Toast.LENGTH_SHORT).show();
                                finish(); // Kembali ke sebelumnya
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal menghapus pesan", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "ID dokumen tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            binding.btnDelete.setVisibility(View.GONE);
        }

        // Tombol kembali
        binding.btnBack.setOnClickListener(v -> finish());
    }
}
