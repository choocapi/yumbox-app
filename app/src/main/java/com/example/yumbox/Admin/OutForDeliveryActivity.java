package com.example.yumbox.Admin;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Admin.Adapter.DeliveryAdapter;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityOutForDeliveryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class OutForDeliveryActivity extends AppCompatActivity {
    private ActivityOutForDeliveryBinding binding;
    private Dialog loadingDialog;
    private ArrayList<OrderDetail> listOfCompleteOrder;

    // Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String ownerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOutForDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(OutForDeliveryActivity.this, "Đang tải...");

        // Get ownerUid from intent
        ownerUid = getIntent().getStringExtra("ownerUid");

        retrieveCompleteOrders();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void retrieveCompleteOrders() {
        loadingDialog.show();
        Query completeOrderQuery = database.getReference("CompletedOrders").orderByChild("currentTime");
        listOfCompleteOrder = new ArrayList<>();

        completeOrderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot completeOrderSnapshot : snapshot.getChildren()) {
                    OrderDetail completeOrder = completeOrderSnapshot.getValue(OrderDetail.class);
                    if (completeOrder != null && completeOrder.getOwnerUid() != null) {
                        if (completeOrder.getOwnerUid().equals(ownerUid)) {
                            listOfCompleteOrder.add(completeOrder);
                        }
                    }
                }
                Collections.reverse(listOfCompleteOrder);
                setDateIntoRecyclerView();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setDateIntoRecyclerView() {
        ArrayList<String> customerNames = new ArrayList<>();
        ArrayList<Boolean> moneyStatus = new ArrayList<>();

        for (OrderDetail completeOrder : listOfCompleteOrder) {
            customerNames.add(completeOrder.getUserName());
            moneyStatus.add(completeOrder.getPaymentReceived());
        }

        DeliveryAdapter deliveryAdapter = new DeliveryAdapter(customerNames, moneyStatus);
        binding.deliveryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.deliveryRecyclerView.setAdapter(deliveryAdapter);
    }
}