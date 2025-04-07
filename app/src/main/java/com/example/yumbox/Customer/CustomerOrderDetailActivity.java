package com.example.yumbox.Customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Customer.Adapter.OrderDetailAdapter;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityCustomerOrderDetailBinding;

import java.util.ArrayList;

public class CustomerOrderDetailActivity extends AppCompatActivity {
    private ActivityCustomerOrderDetailBinding binding;

    // Order info
    private OrderDetail recentOrderItems;
    private ArrayList<String> foodNames, foodPrices, foodImages;
    private ArrayList<Integer> foodQuantities;
    private boolean isOrderReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from History
        isOrderReceived = getIntent().getBooleanExtra("isOrderReceived", false);

        recentOrderItems = (OrderDetail) getIntent().getSerializableExtra("OrderItems");
        if (recentOrderItems != null) {
            foodNames = new ArrayList<>();
            foodPrices = new ArrayList<>();
            foodQuantities = new ArrayList<>();
            foodImages = new ArrayList<>();

            for (CartItem cartItem : recentOrderItems.getOrderItems()) {
                foodNames.add(cartItem.getFoodName());
                foodPrices.add(cartItem.getFoodPrice());
                foodQuantities.add(cartItem.getFoodQuantity());
                foodImages.add(cartItem.getFoodImage());
            }
        }

        setAdapter();

        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void setAdapter() {
        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(foodNames, foodPrices, foodImages, foodQuantities, isOrderReceived, this, position -> {
            CartItem foodItem = recentOrderItems.getOrderItems().get(position);
            Intent intent = new Intent(this, OrderFeedbackActivity.class);
            intent.putExtra("FoodItem", foodItem);
            startActivity(intent);
        });
        binding.orderDetailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderDetailRecyclerView.setAdapter(orderDetailAdapter);
    }

    // Data remote
//    private void getFoodInfo(ArrayList<OrderDetail> recentOrderItems) {
//        foodNames = new ArrayList<>();
//        foodPrices = new ArrayList<>();
//        foodQuantities = new ArrayList<>();
//        foodImages = new ArrayList<>();
//
//        for (OrderDetail item : recentOrderItems) {
//            for (CartItem cartItem : item.getOrderItems()) {
//                foodNames.add(cartItem.getFoodName());
//                foodPrices.add(cartItem.getFoodPrice());
//                foodQuantities.add(cartItem.getFoodQuantity());
//                foodImages.add(cartItem.getFoodImage());
//            }
//        }
//    }
}