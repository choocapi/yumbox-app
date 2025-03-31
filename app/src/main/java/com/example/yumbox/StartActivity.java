package com.example.yumbox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Admin.AdminLoginActivity;
import com.example.yumbox.Admin.AdminMainActivity;
import com.example.yumbox.Customer.CustomerLoginActivity;
import com.example.yumbox.Customer.CustomerMainActivity;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityStartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private ActivityStartBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private UserPreferences userPreferences;
    private String savedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        binding.nextAdminButton.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, AdminLoginActivity.class));
        });

        binding.nextCustomerButton.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, CustomerLoginActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = auth.getCurrentUser();
        if (user != null) {
            userPreferences = new UserPreferences(getApplicationContext());
            savedRole = userPreferences.getUserRole();
            Log.d("StartActivity", "Saved Role: " + savedRole);

            if (savedRole != null) {
                if (savedRole.equals("ownerRestaurant")) {
                    Intent intent = new Intent(StartActivity.this, AdminMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (savedRole.equals("customer")) {
                    Intent intent = new Intent(StartActivity.this, CustomerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }
}