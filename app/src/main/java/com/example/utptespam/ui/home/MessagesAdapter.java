package com.example.utptespam.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.utptespam.R;
import com.example.utptespam.databinding.ListItemMessageBinding;
import com.example.utptespam.model.Message;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context;

    public MessagesAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ListItemMessageBinding binding;

        public MessageViewHolder(ListItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            // Set "To: User"
            binding.textViewRecipient.setText("To: " + message.getRecipient());

            // Set message snippet to new ID (textViewMessage)
            binding.textViewMessage.setText(message.getSnippet());

            // Optional Image
            binding.imageViewMessage.setVisibility(View.VISIBLE); // Selalu tampilkan

            String imageUrl = message.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imageViewMessage.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_small_small) // default loading
                        .error(R.drawable.ic_small_small)        // kalau gagal
                        .into(binding.imageViewMessage);
            } else {
                Glide.with(binding.imageViewMessage.getContext())
                        .load(R.drawable.ic_small_small)         // default image
                        .into(binding.imageViewMessage);
            }

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemMessageBinding binding = ListItemMessageBinding.inflate(inflater, parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);

        // Untuk membuka detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailMessageActivity.class);
            intent.putExtra("recipient", message.getRecipient());
            intent.putExtra("snippet", message.getSnippet());
            intent.putExtra("imageUrl", message.getImageUrl());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return (messageList != null) ? messageList.size() : 0;
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged();
    }
}
