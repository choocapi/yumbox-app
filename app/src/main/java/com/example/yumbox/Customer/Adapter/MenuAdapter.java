package com.example.yumbox.Customer.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Customer.CustomerFoodDetailsActivity;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.MenuItem;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.MenuItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    private ArrayList<MenuItem> menuItems;
    private Dialog loadingDialog;
    private Context context;
    private String foodName, foodPrice, foodImage, foodDescription, foodIngredients, foodKey, ownerUid, restaurantName, userID; // Food info

    // Firebase
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    // Constructor
    public MenuAdapter(ArrayList<MenuItem> menuItems, Context context) {
        this.menuItems = menuItems;
        this.context = context;

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        userID = auth.getCurrentUser().getUid();

        loadingDialog = LoadingDialog.create(context, "Đang tải...");
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert XML to View
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MenuItemBinding binding = MenuItemBinding.inflate(inflater, parent, false);
        return new MenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder {
        private final MenuItemBinding binding;

        // Constructor
        public MenuViewHolder(MenuItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set up each item move to Details
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position);
                }
            });
        }

        // Bind data to ViewHolder
        public void bind(int position) {
            MenuItem menuItem = menuItems.get(position);

            // Show
            binding.menuFoodName.setText(menuItem.getFoodName());
            binding.menuPrice.setText(FormatString.formatAmountFromString(menuItem.getFoodPrice()));
            binding.menuRestaurantName.setText(menuItem.getNameOfRestaurant());
            Uri uri = Uri.parse(menuItem.getFoodImage());
            Glide.with(context).load(uri).into(binding.menuImage); // Load image using Glide

            // Button add to cart
            binding.menuAddToCart.setOnClickListener(v -> {
                addItemToCart(menuItem);
            });
        }
    }

    private void openDetailsActivity(int position) {
        MenuItem menuItem = menuItems.get(position);

        // Sending data to Intent
        Intent intent = new Intent(context, CustomerFoodDetailsActivity.class);
        intent.putExtra("MenuItem", menuItem);
        context.startActivity(intent);
    }

    private void addItemToCart(MenuItem menuItem) {
        loadingDialog.show();
        foodName = menuItem.getFoodName();
        foodPrice = menuItem.getFoodPrice();
        foodImage = menuItem.getFoodImage();
        foodDescription = menuItem.getFoodDescription();
        foodIngredients = menuItem.getFoodIngredients();
        foodKey = menuItem.getFoodKey();
        ownerUid = menuItem.getOwnerUid();
        restaurantName = menuItem.getNameOfRestaurant();

        // Check second food has same restaurant with first?
        databaseRef.child("Users").child(userID).child("CartItems").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot firstChild : snapshot.getChildren()) {
                        String firstOwnerUid = firstChild.getValue(com.example.yumbox.Model.CartItem.class).getOwnerUid();
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
                CartItem cartItem = new CartItem(foodName, foodPrice, foodImage, foodDescription, foodIngredients, 1, foodKey, ownerUid, restaurantName);

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
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

