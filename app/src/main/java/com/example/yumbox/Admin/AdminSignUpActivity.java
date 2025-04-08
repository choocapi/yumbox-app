package com.example.yumbox.Admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Model.AdminModel;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.PasswordToggleHelper;
import com.example.yumbox.databinding.ActivityAdminSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class AdminSignUpActivity extends AppCompatActivity {
    private ActivityAdminSignUpBinding binding;
    private Dialog loadingDialog;
    private String username, nameOfRestaurant, email, password, address, phone;
    private List<String> locationList;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = LoadingDialog.create(this, "Đang tạo tài khoản...");

        // Location list
        locationList = Arrays.asList("Tp.Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Hải Phòng", "Bình Dương");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
        AutoCompleteTextView autoCompleteTextView = binding.listOfLocation;
        autoCompleteTextView.setAdapter(adapter);

        // Show/hide password
        PasswordToggleHelper passwordToggleHelper = new PasswordToggleHelper();
        passwordToggleHelper.setupPasswordToggle(binding.password);

        // Button create account
        binding.createUserButton.setOnClickListener(v -> {
            // Get user input
            username = binding.nameOwner.getText().toString().trim();
            nameOfRestaurant = binding.nameRestaurant.getText().toString().trim();
            email = binding.emailOrPhone.getText().toString().trim();
            password = binding.password.getText().toString().trim();
            address = binding.listOfLocation.getText().toString().trim();

            // Check
            if (username.isBlank() || nameOfRestaurant.isBlank() || email.isBlank() || password.isBlank() || address.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else if (!checkPassword(password)) {
                showToast("Mật khẩu phải có ít nhất 6 ký tự và chứa chữ cái và số");
            } else if (!checkEmail(email)) {
                showToast("Email phải là gmail hoặc outlook");
            } else {
                createAccount(email, password);
            }
        });

        // Moving to login
        binding.haveAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
        });

    }

    private void createAccount(String email, String password) {
        loadingDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Tạo tài khoản thành công");
                saveUserData();
                loadingDialog.dismiss();
                startActivity(new Intent(this, AdminLoginActivity.class));
                finish();
            } else {
                showToast("Tạo tài khoản thất bại");
                loadingDialog.dismiss();
            }
        });
    }

    private void saveUserData() {
        // Get user input
        username = binding.nameOwner.getText().toString().trim();
        nameOfRestaurant = binding.nameRestaurant.getText().toString().trim();
        email = binding.emailOrPhone.getText().toString().trim();

        // Save user info
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            AdminModel userInfo = new AdminModel(username, userId, nameOfRestaurant, email, address, phone);
            databaseRef.child("Users").child(userId).setValue(userInfo);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkPassword(String password) {
        // Password above 6 characters and contain letter and number
        String regex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$";
        return password.matches(regex);
    }

    private boolean checkEmail(String email) {
        // Must gmail or outlook
        String regex = "^[a-zA-Z0-9._%+-]+@(gmail|outlook)\\.com$";
        return email.matches(regex);
    }
}