package com.example.yumbox.Customer.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.CartItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Dialog loadingDialog;
    private ArrayList<CartItem> cartItems;
    private Context context;
    private int[] itemQuantities;
    private ArrayList<String> uniqueKeys; // Local position <--> Remote position

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String userID;
    private DatabaseReference cartItemRef;

    // Constructor
    public CartAdapter(ArrayList<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;

        loadingDialog = LoadingDialog.create(context, "Đang tải...");

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userID = auth.getCurrentUser().getUid();
        cartItemRef = database.getReference().child("Users").child(userID).child("CartItems");
        getUniqueKey();

        // Init itemQuantities
        itemQuantities = new int[cartItems.size()];
        for (int i = 0; i < cartItems.size(); i++) {
            itemQuantities[i] = cartItems.get(i).getFoodQuantity();
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert XML to View
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CartItemBinding binding = CartItemBinding.inflate(inflater, parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Bind data to ViewHolder
        // Position is the index of the current item
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public ArrayList<Integer> getUpdatedItemsQuantities() {
        ArrayList<Integer> mItemQuantities = new ArrayList<>();
        for (int quantity : itemQuantities) {
            mItemQuantities.add(quantity);
        }
        return mItemQuantities;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final CartItemBinding binding;

        // Constructor
        public CartViewHolder(CartItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind data to ViewHolder
        public void bind(int position) {
            CartItem cartItem = cartItems.get(position);
            int quantity = itemQuantities[position];

            binding.cartFoodName.setText(cartItem.getFoodName());
            binding.cartItemPrice.setText(FormatString.formatAmountFromString(cartItem.getFoodPrice()));
            binding.quantity.setText(String.valueOf(quantity));

            // Load image using Glide
            Glide.with(context).load(Uri.parse(cartItem.getFoodImage())).into(binding.cartImage);

            // Decrease quantity
            binding.minusButton.setOnClickListener(v -> {
                decreaseQuantity(position);
            });

            // Increase quantity
            binding.plusButton.setOnClickListener(v -> {
                increaseQuantity(position);
            });

            // Delete item
            binding.deleteButton.setOnClickListener(v -> {
                deleteItem(position);
            });
        }

        private void increaseQuantity(int position) {
            // Max quantity is 10
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++;
                cartItems.get(position).setFoodQuantity(itemQuantities[position]);
                binding.quantity.setText(String.valueOf(itemQuantities[position]));
            }
        }

        private void decreaseQuantity(int position) {
            // Min quantity is 1
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--;
                cartItems.get(position).setFoodQuantity(itemQuantities[position]);
                binding.quantity.setText(String.valueOf(itemQuantities[position]));
            }
        }

        private void deleteItem(int position) {
            loadingDialog.show();
            String uniqueKey = uniqueKeys.get(position);
            if (uniqueKey != null) {
                // Remove item from Firebase
                cartItemRef.child(uniqueKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        cartItems.remove(position);
                        removeItemAtPosition(0);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, cartItems.size());
                        showToast("Xoá thành công");
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Xoá thất bại");
                        loadingDialog.dismiss();
                    }
                });
            }
        }
    }

    private void getUniqueKey() {
        uniqueKeys = new ArrayList<>();

        // Fecth data
        cartItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String uniqueKey = null;

                // Get unique key of each item and add to uniqueKeys
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    uniqueKey = dataSnapshot.getKey();
                    uniqueKeys.add(uniqueKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (cartItems.isEmpty()) {
            uniqueKeys.clear();
        }
    }

    public void removeItemAtPosition(int position) {
        if (position < 0 || position >= itemQuantities.length) {
            return;
        }

        int[] newItemQuantities = new int[itemQuantities.length - 1];
        for (int i = 0, j = 0; i < itemQuantities.length; i++) {
            if (i != position) {
                newItemQuantities[j++] = itemQuantities[i];
            }
        }

        itemQuantities = newItemQuantities;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
