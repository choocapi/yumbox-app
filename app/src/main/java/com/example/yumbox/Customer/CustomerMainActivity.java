package com.example.yumbox.Customer;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.yumbox.Customer.Fragment.NotificationBottomFragment;
import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityCustomerMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {
    private ActivityCustomerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bottom navigation
        NavController navController = Navigation.findNavController(CustomerMainActivity.this, R.id.fragmentContainerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Bottom sheet notification
        binding.notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationBottomFragment bottomSheetDialogFragment = new NotificationBottomFragment();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), "NotificationBottomSheetFragment");
            }
        });
    }
}