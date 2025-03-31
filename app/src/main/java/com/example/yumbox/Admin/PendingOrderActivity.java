package com.example.yumbox.Admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Admin.Adapter.PendingOrderAdapter;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityPendingOrderBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class PendingOrderActivity extends AppCompatActivity implements PendingOrderAdapter.OnItemClicked {
    private ActivityPendingOrderBinding binding;
    private Dialog loadingDialog;

    // Order info
    private ArrayList<String> listOfName = new ArrayList<>();
    private ArrayList<String> listOfTotalPrice = new ArrayList<>();
    private ArrayList<String> listOfImageFirstFoodOrder = new ArrayList<>();
    private ArrayList<OrderDetail> listOfOrderItem = new ArrayList<>();

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference databaseRefOrderDetails;
    private FirebaseAuth auth;
    private String ownerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPendingOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init firebase
        database = FirebaseDatabase.getInstance();
        databaseRefOrderDetails = database.getReference().child("OrderDetails");
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(PendingOrderActivity.this, "Đang tải...");

        // Get data from Intent
        ownerUid = getIntent().getStringExtra("ownerUid");

        retrieveOrdersDetail();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void retrieveOrdersDetail() {
        loadingDialog.show();
        databaseRefOrderDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    OrderDetail orderItem = dataSnapshot.getValue(OrderDetail.class);
                    if (orderItem != null && orderItem.getOwnerUid() != null) {
                        if (orderItem.getOwnerUid().equals(ownerUid)) {
                            listOfOrderItem.add(orderItem);
                        }
                    }
                }

                Collections.reverse(listOfOrderItem);
                addDataToListForRecyclerView();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void addDataToListForRecyclerView() {
        for (OrderDetail orderItem : listOfOrderItem) {
            listOfName.add(orderItem.getUserName());
            listOfTotalPrice.add(orderItem.getTotalPrice());
            if (!orderItem.getOrderItems().isEmpty()) {
                listOfImageFirstFoodOrder.add(orderItem.getOrderItems().get(0).getFoodImage());
//                for (CartItem cartItem : orderItem.getOrderItems()) {
//                    listOfImageFirstFoodOrder.add(cartItem.getFoodImage());
//                }
            }
        }

        setAdapter();
    }

    private void setAdapter() {
        PendingOrderAdapter pendingOrderAdapter = new PendingOrderAdapter(listOfName, listOfTotalPrice, listOfImageFirstFoodOrder, this, this);
        binding.pendingOrderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.pendingOrderRecyclerView.setAdapter(pendingOrderAdapter);
    }

    @Override
    public void onItemClickListener(int position) {
        Intent intent = new Intent(this, AdminOrderDetailsActivity.class);
        OrderDetail userOrderDetail = listOfOrderItem.get(position);
        intent.putExtra("UserOrderDetail", userOrderDetail);
        startActivity(intent);
    }

    @Override
    public void onItemAcceptClickListener(int position) {
        String childItemPushKey = listOfOrderItem.get(position).getItemPushKey();

        DatabaseReference clickedItemOrderRef = databaseRefOrderDetails.child(childItemPushKey);
        clickedItemOrderRef.child("orderAccepted").setValue(true);

        updateOrderAcceptStatus(position);
    }

    @Override
    public void onItemDispatchClickListener(int position) {
        String dispatchItemPushKey = listOfOrderItem.get(position).getItemPushKey();

        DatabaseReference dispatchItemOrderRef = database.getReference().child("CompletedOrders").child(dispatchItemPushKey);
        dispatchItemOrderRef.setValue(listOfOrderItem.get(position)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                String customerName = listOfOrderItem.get(position).getUserName();
                deleteThisItemFromOrderDetails(dispatchItemPushKey, customerName);
            }
        });
    }

    private void updateOrderAcceptStatus(int position) {
        loadingDialog.show();
        String userIdOfClickedItem = listOfOrderItem.get(position).getUserUid();
        String pushKeyOfClickedItem = listOfOrderItem.get(position).getItemPushKey();
        String customerName = listOfOrderItem.get(position).getUserName();

        DatabaseReference buyHistoryRef = database.getReference().child("Users").child(userIdOfClickedItem).child("BuyHistory").child(pushKeyOfClickedItem);
        buyHistoryRef.child("orderAccepted").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                listOfOrderItem.get(position).setOrderAccepted(true);
                showToast("Đã nhận đơn của " + customerName);
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Đã từ chối đơn của " + customerName);
                loadingDialog.dismiss();
            }
        });

        databaseRefOrderDetails.child(pushKeyOfClickedItem).child("orderAccepted").setValue(true);
    }

    private void deleteThisItemFromOrderDetails(String dispatchItemPushKey, String customerName) {
        loadingDialog.show();
        DatabaseReference orderDetailsItemRef = databaseRefOrderDetails.child(dispatchItemPushKey);
        orderDetailsItemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Đơn của " + customerName + " đã được giao");
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Đơn của " + customerName + " chưa được giao");
                loadingDialog.dismiss();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}