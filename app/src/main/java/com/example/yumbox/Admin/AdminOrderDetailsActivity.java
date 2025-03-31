package com.example.yumbox.Admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Admin.Adapter.OrderDetailAdapter;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.databinding.ActivityAdminOrderDetailsBinding;

import java.util.ArrayList;

public class AdminOrderDetailsActivity extends AppCompatActivity {
    private ActivityAdminOrderDetailsBinding binding;
    private String userName, address, phoneNumber, totalPrice;
    private ArrayList<String> foodNames = new ArrayList<>();
    private ArrayList<String> foodImages = new ArrayList<>();
    private ArrayList<Integer> foodQuantities = new ArrayList<>();
    private ArrayList<String> foodPrices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getDataFromIntent();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void getDataFromIntent() {
        OrderDetail receivedOrderDetail = (OrderDetail) getIntent().getSerializableExtra("UserOrderDetail");
        if (receivedOrderDetail != null) {
            userName = receivedOrderDetail.getUserName();
            address = receivedOrderDetail.getAddress();
            phoneNumber = receivedOrderDetail.getPhoneNumber();
            totalPrice = receivedOrderDetail.getTotalPrice();

            for (CartItem cartItem : receivedOrderDetail.getOrderItems()) {
                foodNames.add(cartItem.getFoodName());
                foodImages.add(cartItem.getFoodImage());
                foodQuantities.add(cartItem.getFoodQuantity());
                foodPrices.add(cartItem.getFoodPrice());
            }

            setOrderDetail();
            setAdapter();
        }
    }

    private void setOrderDetail() {
        binding.name.setText(userName);
        binding.address.setText(address);
        binding.phone.setText(phoneNumber);
        binding.totalAmount.setText(FormatString.formatAmountFromString(totalPrice));
    }

    private void setAdapter() {
        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(foodNames, foodImages, foodQuantities, foodPrices, this);
        binding.orderDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderDetailsRecyclerView.setAdapter(orderDetailAdapter);
    }
}