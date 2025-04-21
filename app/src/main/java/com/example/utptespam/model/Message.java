package com.example.utptespam.model; // Use your package name

import com.google.firebase.Timestamp; // Import Timestamp
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp; // Import ServerTimestamp annotation

public class Message {

    @DocumentId
    private String documentId;
    private String recipient;
    private String snippet;
    private String imageUrl;
    private String senderId; // Added: ID of the user who sent the message
    private @ServerTimestamp Timestamp timestamp; // Added: Firestore server timestamp

    // Default constructor required for Firestore Data Binding (can be empty)
    public Message() {}

    // Constructor for creating a new message
    public Message(String recipient, String snippet, String imageUrl, String senderId) {
        this.recipient = recipient;
        this.snippet = snippet;
        this.imageUrl = imageUrl;
        this.senderId = senderId;
        // Timestamp will be set by Firestore server when writing
    }

    // Getters
    public String getDocumentId() {
        return documentId;
    }
    public String getRecipient() { return recipient; }
    public String getSnippet() { return snippet; }
    public String getImageUrl() { return imageUrl; }
    public String getSenderId() { return senderId; }
    public Timestamp getTimestamp() { return timestamp; } // Getter for timestamp

    // Optional Setters (if needed, but often not for immutable data)
    // public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}