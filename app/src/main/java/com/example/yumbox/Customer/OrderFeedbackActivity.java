package com.example.yumbox.Customer;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.Feedback;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityOrderFeedbackBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderFeedbackActivity extends AppCompatActivity {
    private ActivityOrderFeedbackBinding binding;
    private Dialog loadingDialog;
    private CartItem foodItem;

    // Info
    private String foodName, foodImage, nameOfRestaurant, foodKey, userUid, content;
    private float rating;
    private Long time;

    // Firebase
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrderFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init
        databaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        loadingDialog = LoadingDialog.create(this, "Đang xử lý...");

        // Get data from Item ViewHolder
        foodItem = (CartItem) getIntent().getSerializableExtra("FoodItem");
        if (foodItem != null) {
            foodName = foodItem.getFoodName();
            foodImage = foodItem.getFoodImage();
            nameOfRestaurant = foodItem.getNameOfRestaurant();
            foodKey = foodItem.getFoodKey();
            userUid = auth.getCurrentUser().getUid();
        }

        binding.foodName.setText(foodName);
        binding.restaurantName.setText(nameOfRestaurant);

        Uri uri = Uri.parse(foodImage);
        Glide.with(this).load(uri).into(binding.foodImage);

        binding.backButton.setOnClickListener(v -> {
            finish();
        });

        binding.submitFeedbackButton.setOnClickListener(v -> {
            submitFeedback();
        });
    }

    private void submitFeedback() {
        loadingDialog.show();
        content = binding.content.getText().toString().trim();
        rating = binding.ratingBar.getRating();
        time = System.currentTimeMillis();

        DatabaseReference feedbackRef = databaseRef.child("MenuItems").child(foodKey).child("feedbacks");

        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Feedback existingFeedback = child.getValue(Feedback.class);

                    if (existingFeedback != null && userUid.equals(existingFeedback.getUserUid())) {
                        // Update feedback cũ
                        String existingKey = child.getKey();
                        Feedback updatedFeedback = new Feedback(userUid, rating, content, time);

                        feedbackRef.child(existingKey).setValue(updatedFeedback)
                                .addOnCompleteListener(task -> {
                                    loadingDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(OrderFeedbackActivity.this, "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(OrderFeedbackActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // Thêm feedback mới
                    String newKey = feedbackRef.push().getKey();
                    Feedback newFeedback = new Feedback(userUid, rating, content, time);

                    if (newKey != null) {
                        feedbackRef.child(newKey).setValue(newFeedback)
                                .addOnCompleteListener(task -> {
                                    loadingDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(OrderFeedbackActivity.this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(OrderFeedbackActivity.this, "Gửi đánh giá thất bại!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(OrderFeedbackActivity.this, "Lỗi xử lý!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(OrderFeedbackActivity.this, "Lỗi dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}