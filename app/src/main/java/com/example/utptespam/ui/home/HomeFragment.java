package com.example.utptespam.ui.home; // Use your package name

import android.os.Bundle;
import android.text.Editable; // Import Editable
import android.text.TextWatcher; // Import TextWatcher
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase Imports
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.utptespam.databinding.FragmentHomeBinding;
import com.example.utptespam.model.Message;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MessagesAdapter messagesAdapter;
    private List<Message> messageList;
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;
    private static final String TAG = "HomeFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Simpel: langsung return root
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchListener();
        listenForMessages("");

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int extraTopPadding = (int) (getResources().getDisplayMetrics().density * 16); // +16dp
            view.setPadding(0, topInset + extraTopPadding, 0, 0);
            return insets;
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(getContext(), messageList);
        RecyclerView recyclerView = binding.recyclerViewMessages;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messagesAdapter);
    }

    // --- Add Search Listener Setup ---
    private void setupSearchListener() {
        // Search saat ketik
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                listenForMessages(searchText);
            }
        });

        // Search saat ikon kaca pembesar ditekan
        binding.btnSearchIcon.setOnClickListener(v -> {
            String searchText = binding.editTextSearch.getText().toString().trim();
            listenForMessages(searchText);
            Toast.makeText(getContext(), "Mencari: " + searchText, Toast.LENGTH_SHORT).show();
        });

        // Search saat tombol "Cari" ditekan
        binding.btnCari.setOnClickListener(v -> {
            String searchText = binding.editTextSearch.getText().toString().trim();
            listenForMessages(searchText);
            Toast.makeText(getContext(), "Mencari: " + searchText, Toast.LENGTH_SHORT).show();
        });
    }

    // --- End Search Listener Setup ---


    // --- Modify listenForMessages to accept search text ---
    private void listenForMessages(String searchText) {
        // Remove previous listener if it exists
        if (messagesListener != null) {
            messagesListener.remove();
        }

        Query query;
        if (searchText.isEmpty()) {
            // If search text is empty, load all messages ordered by time (newest first)
            Log.d(TAG, "Listening for all messages (ordered by time)");
            query = db.collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50);
        } else {
            // If search text exists, query for recipients starting with the text
            // The '\uf8ff' character is a high Unicode point ensuring we get all matches starting with searchText
            Log.d(TAG, "Listening for messages where recipient starts with: " + searchText);
            query = db.collection("messages")
                    .orderBy("recipient") // Must order by the field used in range comparisons
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff")
                    .limit(50);
            // Note: You might want secondary sorting (like timestamp) if needed,
            // but Firestore might require a specific composite index for that.
            // Start simple first.
        }

        messagesListener = query.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                // Don't show toast on every transient error, maybe log or show specific errors
                // Toast.makeText(getContext(), "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                messageList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Message message = doc.toObject(Message.class);
                    messageList.add(message);
                }
                messagesAdapter.notifyDataSetChanged(); // TODO: Use DiffUtil later
                Log.d(TAG, "Messages updated/filtered. Count: " + messageList.size());
            } else {
                Log.d(TAG,"Snapshot listener returned null data.");
            }
        });
    }
    // --- End Listen Method Modification ---


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the listener when the view is destroyed
        if (messagesListener != null) {
            messagesListener.remove();
        }
        binding = null;
    }
}