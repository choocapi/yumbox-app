package com.example.yumbox.Admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityAdminProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminProfileActivity extends AppCompatActivity {
    private ActivityAdminProfileBinding binding;
    private Dialog loadingDialog;
    private String nameOfRestaurant, currentUserID;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loadingDialog = LoadingDialog.create(AdminProfileActivity.this, "Đang tải...");

        binding.name.setEnabled(false);
        binding.address.setEnabled(false);
        binding.email.setEnabled(false);
        binding.phone.setEnabled(false);
        binding.password.setEnabled(false);
        binding.saveInfoButton.setEnabled(false);

        final boolean[] isEnable = {false};
        binding.editInfoButton.setOnClickListener(v -> {
            isEnable[0] = !isEnable[0];
            binding.name.setEnabled(isEnable[0]);
            binding.address.setEnabled(isEnable[0]);
            binding.email.setEnabled(isEnable[0]);
            binding.phone.setEnabled(isEnable[0]);
            binding.password.setEnabled(isEnable[0]);
            binding.saveInfoButton.setEnabled(isEnable[0]);

            if (isEnable[0]) {
                binding.name.requestFocus();
            }
        });

        binding.saveInfoButton.setOnClickListener(v -> {
            updateUserInfo();
        });

        retrieveUserData();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateUserInfo() {
        loadingDialog.show();
        String updateName = binding.name.getText().toString().trim();
        String updateAddress = binding.address.getText().toString().trim();
        String updateEmail = binding.email.getText().toString().trim();
        String updatePhone = binding.phone.getText().toString().trim();
        String updatePassword = binding.password.getText().toString().trim();

        if (!updateName.isBlank() && !updateAddress.isBlank() && !updateEmail.isBlank() && !updatePhone.isBlank() && !updatePassword.isBlank()) {
            if (!currentUserID.isBlank() && userRef != null) {
                userRef.child("name").setValue(updateName);
                userRef.child("address").setValue(updateAddress);
                userRef.child("email").setValue(updateEmail);
                userRef.child("phone").setValue(updatePhone);
                userRef.child("password").setValue(updatePassword);

                auth.getCurrentUser().updateEmail(updateEmail);
                auth.getCurrentUser().updatePassword(updatePassword);

                binding.editInfoButton.performClick();
                showToast("Cập nhật thông tin thành công");
                loadingDialog.dismiss();
            } else {
                showToast("Cập nhật thông tin thất bại");
                loadingDialog.dismiss();
            }
        } else {
            showToast("Vui lòng nhập đầy đủ thông tin");
            loadingDialog.dismiss();
        }
    }

    private void retrieveUserData() {
        loadingDialog.show();
        currentUserID = auth.getCurrentUser().getUid();
        if (!currentUserID.isBlank()) {
            userRef = database.getReference().child("Users").child(currentUserID);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = (String) snapshot.child("name").getValue();
                        String email = (String) snapshot.child("email").getValue();
                        String password = (String) snapshot.child("password").getValue();
                        String address = (String) snapshot.child("address").getValue();
                        String phone = (String) snapshot.child("phone").getValue();
                        nameOfRestaurant = (String) snapshot.child("nameOfRestaurant").getValue();

                        setDataToView(name, email, password, address, phone);
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingDialog.dismiss();
                }
            });
        }
    }

    private void setDataToView(String name, String email, String password, String address, String phone) {
        binding.name.setText(name);
        binding.email.setText(email);
        binding.password.setText(password);
        binding.address.setText(address);
        binding.phone.setText(phone);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}