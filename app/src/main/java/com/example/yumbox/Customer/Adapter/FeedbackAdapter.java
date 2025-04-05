package com.example.yumbox.Customer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yumbox.Model.Feedback;
import com.example.yumbox.databinding.FeedbackItemBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private ArrayList<Feedback> feedbacks;
    private Map<String, String> nameCache = new HashMap<>();
    private Context context;

    public FeedbackAdapter(ArrayList<Feedback> feedbacks, Context context) {
        this.feedbacks = feedbacks;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        FeedbackItemBinding binding = FeedbackItemBinding.inflate(inflater, parent, false);
        return new FeedbackAdapter.FeedbackViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.FeedbackViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return feedbacks.size();
    }

    public class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private final FeedbackItemBinding binding;

        public FeedbackViewHolder(FeedbackItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            Feedback feedback = feedbacks.get(position);
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(feedback.getCurrentTime()));
            binding.contentComment.setText(feedback.getContent());
            binding.customerRating.setText(String.format("%.0f", feedback.getRating()));
            binding.feedbackDate.setText(date);

            String uid = feedback.getUserUid();

            // Nếu tên đã có trong cache thì dùng luôn
            if (nameCache.containsKey(uid)) {
                binding.customerName.setText(nameCache.get(uid));
            } else {
                // Lấy tên từ Firebase
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                userRef.child("name").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String name = task.getResult().getValue(String.class);
                        nameCache.put(uid, name); // lưu cache
                        binding.customerName.setText(name);
                    } else {
                        binding.customerName.setText("Người dùng ẩn danh");
                    }
                }).addOnFailureListener(e -> binding.customerName.setText("Không thể tải tên"));
            }
        }

    }
}
