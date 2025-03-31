package com.example.yumbox.Customer.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.databinding.FragmentCartBinding;
import com.example.yumbox.Customer.Adapter.CartAdapter;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Customer.PayOutActivity;
import com.example.yumbox.Utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private Dialog loadingDialog;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItems;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String userID;
    private DatabaseReference foodRef;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Firebase
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(getContext(), "Đang tải...");

        retrieveCartItems();

        // Move to Pay out
        binding.proceedButton.setOnClickListener(v -> {
            getOrderItemsDetail();
        });
    }

    private void getOrderItemsDetail() {
        DatabaseReference orderItemsRef = database.getReference().child("Users").child(userID).child("CartItems");
        cartItems = new ArrayList<>();
        ArrayList<Integer> foodQuantities = cartAdapter.getUpdatedItemsQuantities();

        orderItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem orderItem = dataSnapshot.getValue(CartItem.class);
                    cartItems.add(orderItem);
                }

                // Join quantities to order items
                joinQuantitiesToOrderItems(cartItems, foodQuantities);
                orderNow(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void joinQuantitiesToOrderItems(ArrayList<CartItem> cartItems, ArrayList<Integer> foodQuantities) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            cartItem.setFoodQuantity(foodQuantities.get(i));
        }
    }

    private void orderNow(ArrayList<CartItem> cartItems) {
        Intent intent = new Intent(getContext(), PayOutActivity.class);
        intent.putExtra("OrderItems", cartItems);
        startActivity(intent);
    }

    private void retrieveCartItems() {
        loadingDialog.show();
        database = FirebaseDatabase.getInstance();
        userID = auth.getCurrentUser().getUid();
        foodRef = database.getReference().child("Users").child(userID).child("CartItems");
        cartItems = new ArrayList<>();

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                    cartItems.add(cartItem);
                }
                setAdapter();

                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setAdapter() {
        cartAdapter = new CartAdapter(cartItems, getContext());
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.cartRecyclerView.setAdapter(cartAdapter);
    }
}