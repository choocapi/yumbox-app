package com.example.yumbox.Customer.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yumbox.databinding.NotificationItemBinding;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private ArrayList<String> notifications;
    private ArrayList<Integer> notificationImages;

    // Constructor
    public NotificationAdapter(ArrayList<String> notifications, ArrayList<Integer> notificationImages) {
        this.notifications = notifications;
        this.notificationImages = notificationImages;
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        NotificationItemBinding binding = NotificationItemBinding.inflate(inflater, parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        // Bind data to the view
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final NotificationItemBinding binding;

        // Constructor
        public NotificationViewHolder(NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind data to the view
        public void bind(int position) {
            binding.notificationTextView.setText(notifications.get(position));
            binding.notificationImageView.setImageResource(notificationImages.get(position));
        }
    }
}
