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

import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.PasswordToggleHelper;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityAdminLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {
    private ActivityAdminLoginBinding binding;
    private Dialog loadingDialog;
    private String username, nameOfRestaurant, address, phone, email, password, userRole, userID;

    // Firebase & Local
    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = LoadingDialog.create(AdminLoginActivity.this, "Đang đăng nhập...");

        // Show/hide password
        PasswordToggleHelper passwordToggleHelper = new PasswordToggleHelper();
        passwordToggleHelper.setupPasswordToggle(binding.password);

        // Button login
        binding.loginButton.setOnClickListener(v -> {
            // Get user input
            email = binding.emailOrPhone.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            // Check
            if (email.isBlank() || password.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                loginUserAccount(email, password);
            }
        });

        // Moving to sign up
        binding.dontHaveAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminSignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUserAccount(String username, String password) {
        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                userID = user != null ? user.getUid() : "";
                checkUserRole(userID);
                loadingDialog.dismiss();
            } else {
                showToast("Email hoặc mật khẩu không đúng");
                loadingDialog.dismiss();
            }
        });
    }

    private void checkUserRole(String userID) {
        databaseRef.child("Users").child(userID).child("role").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.getValue(String.class);
                    if (userRole != null) {
                        userPreferences = new UserPreferences(getApplicationContext());

                        if (userRole.equals("ownerRestaurant")) {
                            // Chỉ lưu role nếu chưa được lưu trong phiên này
                            if (!userPreferences.isSessionActive()) {
                                userPreferences.saveUserRole(userRole);
                                showToast("Đăng nhập thành công");
                                updateUI(user);
                            }
                        } else {
                            auth.signOut();
                            showToast("Phiên đăng nhập không hợp lệ");
                        }
                    }
                } else {
                    auth.signOut();
                    showToast("Tài khoản không hợp lệ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi kết nối dữ liệu");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = auth.getCurrentUser();
        if (user != null) {
            auth.signOut();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            showToast("Lỗi đăng nhập");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}