package com.example.yumbox.Customer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Model.CustomerModel;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.PasswordToggleHelper;
import com.example.yumbox.databinding.ActivityCustomerSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CustomerSignUpActivity extends AppCompatActivity {
    private ActivityCustomerSignUpBinding binding;
    private Dialog loadingDialog;
    private String email, password, username, address, phone, dateOfBirth;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private FirebaseUser user;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerSignUpBinding.inflate(getLayoutInflater());
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

        // Show/hide password
        PasswordToggleHelper passwordToggleHelper = new PasswordToggleHelper();
        passwordToggleHelper.setupPasswordToggle(binding.password);

        // Button create account
        binding.createAccountButton.setOnClickListener(v -> {
            // Get user input
            username = binding.username.getText().toString().trim();
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            // Check
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else if (!checkEmail(email)) {
                showToast("Email phải là gmail hoặc outlook");
            } else if (!checkPassword(password)) {
                showToast("Mật khẩu phải có ít nhất 6 ký tự và chứa chữ cái và số");
            } else {
                createAccount(email, password);
            }
        });

        // Move to Login
        binding.haveAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerSignUpActivity.this, CustomerLoginActivity.class));
        });
    }

    private void createAccount(String email, String password) {
        loadingDialog.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("Tạo tài khoản thành công");
                saveUserData();
                loadingDialog.dismiss();
                startActivity(new Intent(this, CustomerLoginActivity.class));
                finish();
            } else {
                showToast("Tạo tài khoản thất bại");
                loadingDialog.dismiss();
            }
        });
    }

    private void saveUserData() {
        // Get user input
        username = binding.username.getText().toString().trim();
        email = binding.email.getText().toString().trim();

        if (auth.getCurrentUser() != null) {
            CustomerModel user = new CustomerModel(username, email, address, phone, dateOfBirth);
            String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            databaseRef.child("Users").child(userID).setValue(user);
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