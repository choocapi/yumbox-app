package com.example.yumbox.Admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Model.AdminModel;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.StartActivity;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityAdminMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity {
    private ActivityAdminMainBinding binding;
    private Dialog loadingDialog;
    private String nameOfRestaurant, ownerUid;

    // Firebase & Local
    private FirebaseDatabase database;
    private DatabaseReference completeOrderRef;
    private FirebaseAuth auth;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(AdminMainActivity.this, "Đang tải...");
        retrieveRestaurantInfoAndUpdate();

        // Pending order
        binding.pendingOrderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, PendingOrderActivity.class);
                intent.putExtra("ownerUid", ownerUid);
                startActivity(intent);
            }
        });

        // Show all menu item
        binding.showAllMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, AllItemActivity.class);
                intent.putExtra("ownerUid", ownerUid);
                startActivity(intent);
            }
        });

        // Profile
        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, AdminProfileActivity.class);
                startActivity(intent);
            }
        });

        // Create user
        binding.statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            }
        });

        // Order dispatch
        binding.outForDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMainActivity.this, OutForDeliveryActivity.class);
                intent.putExtra("ownerUid", ownerUid);
                startActivity(intent);
            }
        });

        // Add menu item
        binding.addMenu.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddItemActivity.class);
            intent.putExtra("nameOfRestaurant", nameOfRestaurant);
            intent.putExtra("ownerUid", ownerUid);
            startActivity(intent);
        });

        binding.logout.setOnClickListener(v -> {
            auth.signOut();
            userPreferences = new UserPreferences(getApplicationContext());
            userPreferences.clearUserRole();

            Intent intent = new Intent(AdminMainActivity.this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            retrieveRestaurantInfoAndUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            retrieveRestaurantInfoAndUpdate();
        }
    }

    private void retrieveRestaurantInfoAndUpdate() {
        loadingDialog.show();
        String userID = auth.getCurrentUser().getUid();
        database.getReference().child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AdminModel userModel = snapshot.getValue(AdminModel.class);
                    if (userModel != null) {
                        nameOfRestaurant = userModel.getNameOfRestaurant();
                        ownerUid = userModel.getOwnerUid();

                        // Update display data
                        updatePendingOrders();
                        updateCompleteOrders();
                        updateWholeTimeEarning();
                        loadingDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void updateWholeTimeEarning() {
        ArrayList<Integer> listOfTotalPay = new ArrayList<>();
        completeOrderRef = database.getReference().child("CompletedOrders");
        completeOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot completeOrderSnapshot : snapshot.getChildren()) {
                    OrderDetail completeOrder = completeOrderSnapshot.getValue(OrderDetail.class);
                    if (completeOrder != null && completeOrder.getOwnerUid() != null) {
                        if (completeOrder.getOwnerUid().equals(ownerUid)) {
                            int totalPay = Integer.parseInt(completeOrder.getTotalPrice());
                            listOfTotalPay.add(totalPay);
                        }
                    }
                }

                int totalEarning = 0;
                for (int totalPay : listOfTotalPay) {
                    totalEarning += totalPay;
                }

                binding.totalEarning.setText(FormatString.formatAmountFromNumber(totalEarning));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateCompleteOrders() {
        completeOrderRef = database.getReference().child("CompletedOrders");
        completeOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot completeOrderSnapshot : snapshot.getChildren()) {
                        OrderDetail completeOrder = completeOrderSnapshot.getValue(OrderDetail.class);
                        if (completeOrder != null && completeOrder.getOwnerUid() != null) {
                            if (completeOrder.getOwnerUid().equals(ownerUid)) {
                                count++;
                            }
                        }
                    }
                }
                binding.completeOrdersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatePendingOrders() {
        DatabaseReference pendingOrderRef = database.getReference().child("OrderDetails");
        pendingOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot pendingOrderSnapshot : snapshot.getChildren()) {
                        OrderDetail pendingOrder = pendingOrderSnapshot.getValue(OrderDetail.class);
                        if (pendingOrder != null && !pendingOrder.getOrderAccepted() && pendingOrder.getOwnerUid() != null) {
                            if (pendingOrder.getOwnerUid().equals(ownerUid)) {
                                count++;
                            }
                        }
                    }
                }
                binding.pendingOrdersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}