package com.example.yumbox.Customer;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.CustomerMenuItem;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityCustomerFoodDetailsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerFoodDetailsActivity extends AppCompatActivity {
    private ActivityCustomerFoodDetailsBinding binding;
    private Dialog loadingDialog;

    // Food info
    private String foodName, foodPrice, foodImage, foodDescription, foodIngredients, foodType, ownerUid, restaurantName;
    private CustomerMenuItem menuItem;

    // Firebase
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerFoodDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and auth
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(this, "Đang thêm...");

        // Get data from Item ViewHolder
        menuItem = (CustomerMenuItem) getIntent().getSerializableExtra("MenuItem");
        if (menuItem != null) {
            foodName = menuItem.getFoodName();
            foodPrice = menuItem.getFoodPrice();
            foodImage = menuItem.getFoodImage();
            foodDescription = menuItem.getFoodDescription();
            foodIngredients = menuItem.getFoodIngredients();
            foodType = menuItem.getFoodType();
            ownerUid = menuItem.getOwnerUid();
            restaurantName = menuItem.getNameOfRestaurant();
        }

        binding.detailsFoodName.setText(foodName);
        binding.detailsFoodDescription.setText(foodDescription);
        binding.detailsFoodIngredients.setText(foodIngredients);
        binding.restaurantName.setText(restaurantName);
        binding.foodType.setText(foodType);

        Uri uri = Uri.parse(foodImage);
        Glide.with(this).load(uri).into(binding.detailsFoodImage);

        // Go back
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Add item to cart
        binding.addItemButton.setOnClickListener(v -> {
            addItemToCart();
        });
    }

    private void addItemToCart() {
        loadingDialog.show();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        String userID = auth.getCurrentUser().getUid();

        // Check second food has same restaurant with first?
        databaseRef.child("Users").child(userID).child("CartItems").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot firstChild : snapshot.getChildren()) {
                        String firstOwnerUid = firstChild.getValue(CartItem.class).getOwnerUid();
                        firstOwnerUid = firstOwnerUid == null ? "" : firstOwnerUid;
                        if (firstOwnerUid.equals(ownerUid)) {
                            addItem();
                        } else {
                            showToast("Chỉ hỗ trợ đặt nhiều món cùng 1 nhà hàng");
                            loadingDialog.dismiss();
                        }
                    }
                } else {
                    addItem();
                }
            }

            private void addItem() {
                CartItem cartItem = new CartItem(foodName, foodPrice, foodImage, foodDescription, foodIngredients, 1, ownerUid, restaurantName);

                databaseRef.child("Users").child(userID).child("CartItems").push().setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast("Thêm vào giỏ hàng thành công");
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Thêm vào giỏ hàng thất bại");
                        loadingDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}