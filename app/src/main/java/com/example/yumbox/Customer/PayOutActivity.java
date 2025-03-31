package com.example.yumbox.Customer;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityPayOutBinding;
import com.example.yumbox.Customer.Fragment.CongratsBottomSheetFragment;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.Utils.LoadingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PayOutActivity extends AppCompatActivity {
    private ActivityPayOutBinding binding;
    private ArrayList<CartItem> orderItems;
    private Dialog loadingDialog;
    private String name, address, phone, totalAmount, ownerUid, userID;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPayOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = LoadingDialog.create(this, "Đang đặt món...");

        setUserData();

        // Get data from Cart
        orderItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("OrderItems");
        if (orderItems != null) {
            ownerUid = orderItems.get(0).getOwnerUid();
        }

        totalAmount = String.valueOf(getTotalAmount());
        binding.totalAmount.setText(FormatString.formatAmountFromString(totalAmount));

        // Place order
        binding.placeMyOrderButton.setOnClickListener(v -> {
            // Get user input
            name = binding.name.getText().toString();
            address = binding.address.getText().toString();
            phone = binding.phone.getText().toString();

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                placeOrder();
            }
        });

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void placeOrder() {
        loadingDialog.show();
        userID = auth.getCurrentUser().getUid();
        Long time = System.currentTimeMillis();
        String itemPushKey = databaseRef.child("OrderDetails").push().getKey();
        OrderDetail orderDetail = new OrderDetail(userID, name, orderItems, address, phone, totalAmount, false, false, itemPushKey, time, ownerUid);

        DatabaseReference orderRef = databaseRef.child("OrderDetails").child(itemPushKey);
        orderRef.setValue(orderDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Đặt món thành công");
                saveUserData();
                BottomSheetDialogFragment congratsBottomSheet = new CongratsBottomSheetFragment();
                congratsBottomSheet.show(getSupportFragmentManager(), "CongratsBottomSheet");
                removeItemFromCart();
                addOrderToHistory(orderDetail);
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Đặt món thất bại");
                loadingDialog.dismiss();
            }
        });
    }

    private void addOrderToHistory(OrderDetail orderDetail) {
        DatabaseReference historyRef = databaseRef.child("Users").child(userID).child("BuyHistory").child(orderDetail.getItemPushKey());
        historyRef.setValue(orderDetail).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void removeItemFromCart() {
        DatabaseReference cartItemRef = databaseRef.child("Users").child(userID).child("CartItems");
        cartItemRef.removeValue();
    }

    private int getTotalAmount() {
        int total = 0;
        if (orderItems != null) {
            for (CartItem item : orderItems) {
                int price = Integer.parseInt(item.getFoodPrice());
                int quantity = item.getFoodQuantity();
                total += price * quantity;
            }
        }
        return total;
    }

    private void setUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            DatabaseReference userRef = databaseRef.child("Users").child(userID);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.child("name").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        phone = snapshot.child("phone").getValue(String.class);
                    }

                    binding.name.setText(name);
                    binding.address.setText(address);
                    binding.phone.setText(phone);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void saveUserData() {
        DatabaseReference userRef = databaseRef.child("Users").child(userID);
        userRef.child("name").setValue(name);
        userRef.child("address").setValue(address);
        userRef.child("phone").setValue(phone);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}