package com.example.utptespam.ui.home; // Use your package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.utptespam.R; // Import your R class
import com.example.utptespam.databinding.ListItemMessageBinding; // Import generated binding for list_item_message.xml
import com.example.utptespam.model.Message; // Import the Message data class
// Import an image loading library like Glide or Picasso later
// import com.bumptech.glide.Glide;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context; // Often needed for things like image loading

    // Constructor
    public MessagesAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    // --- ViewHolder Class ---
    // Holds references to the views in list_item_message.xml
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ListItemMessageBinding binding; // Use ViewBinding for the item layout

        public MessageViewHolder(ListItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Helper method to bind data to views
        void bind(Message message) {
            binding.textViewRecipient.setText(message.getRecipient());
            binding.textViewMessageSnippet.setText(message.getSnippet());

            // Handle optional image
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                binding.imageViewMessage.setVisibility(View.VISIBLE);

                // --- ★ Replace Placeholder with Glide Code ★ ---
                if (binding.imageViewMessage.getContext() != null) { // Ensure context is available
                    Glide.with(binding.imageViewMessage.getContext())
                            .load(message.getImageUrl()) // Load the URL from the Message object
                            .placeholder(R.color.grey_300) // Use a color or drawable placeholder
                            .error(R.drawable.ic_smiley_cloud_2) // Optional: Add an error drawable
                            .into(binding.imageViewMessage); // Target the ImageView in this item
                }
                // --- ★ End Glide Code ★ ---

            } else {
                binding.imageViewMessage.setVisibility(View.GONE); // Hide if no image URL
            }
        }
    }
    // --- End of ViewHolder Class ---


    // --- Adapter Methods ---
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (list_item_message.xml) using ViewBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemMessageBinding itemBinding = ListItemMessageBinding.inflate(inflater, parent, false);
        return new MessageViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Get the data model based on position
        Message message = messageList.get(position);
        // Bind the data to the ViewHolder's views
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return messageList == null ? 0 : messageList.size();
    }

    // Optional: Method to update the list data
    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged(); // TODO: Use DiffUtil for better performance later
    }
    // --- End of Adapter Methods ---
}