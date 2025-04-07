package com.example.yumbox.Customer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Customer.Fragment.CongratsBottomSheetFragment;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Zalopay.Api.CreateOrder;
import com.example.yumbox.databinding.ActivityPayOutBinding;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PayOutActivity extends AppCompatActivity {
    private ActivityPayOutBinding binding;
    private ArrayList<CartItem> orderItems;
    private Dialog loadingDialog;
    private String name, address, phone, totalAmount, ownerUid, userID;
    private Integer selectedPayment;
    private final Map<String, Integer> paymentOptionsMap = new LinkedHashMap<>();

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

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        // Init
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = LoadingDialog.create(this, "Đang đặt món...");

        setUserData();

        paymentOptionsMap.put("Trực tiếp", 0);
        paymentOptionsMap.put("Zalopay", 1);
        ArrayAdapter<String> paymentOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(paymentOptionsMap.keySet()));
        binding.paymentOptions.setAdapter(paymentOptionsAdapter);

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
            selectedPayment = paymentOptionsMap.get(binding.paymentOptions.getText().toString());

            if (name.isBlank() || address.isBlank() || phone.isBlank() || selectedPayment == null) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            }

            // Check phone format
            if (!phone.matches("0\\d{9}")) {
                showToast("Số điện thoại không hợp lệ");
                return;
            }

            if (selectedPayment == 1) {
                CreateOrder orderApi = new CreateOrder();
                try {
                    JSONObject data = orderApi.createOrder(totalAmount);
                    String code = data.getString("return_code");

                    if (code.equals("1")) {
                        String token = data.getString("zp_trans_token");
                        ZaloPaySDK.getInstance().payOrder(PayOutActivity.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(String s, String s1, String s2) {
                                placeOrder(true);
                            }

                            @Override
                            public void onPaymentCanceled(String s, String s1) {
                                showToast("Thanh toán bị huỷ");
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                showToast("Thanh toán thất bại");
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (selectedPayment == 0) {
                placeOrder(false);
            }
        });

        // Go back
        binding.backButton.setOnClickListener(v -> { finish(); });
    }

    private void placeOrder(boolean paymentReceived) {
        loadingDialog.show();
        userID = auth.getCurrentUser().getUid();
        Long time = System.currentTimeMillis();
        String itemPushKey = databaseRef.child("OrderDetails").push().getKey();
        OrderDetail orderDetail = new OrderDetail(userID, name, orderItems, address, phone, totalAmount, false, paymentReceived, false, selectedPayment, itemPushKey, time, ownerUid);

        DatabaseReference orderRef = databaseRef.child("OrderDetails").child(itemPushKey);
        orderRef.setValue(orderDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}