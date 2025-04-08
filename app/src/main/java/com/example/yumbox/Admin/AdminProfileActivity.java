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
import com.example.yumbox.StartActivity;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityAdminProfileBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    private boolean isEditable = false;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private UserPreferences userPreferences;

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
        binding.phone.setEnabled(false);
        binding.oldpassword.setEnabled(false);
        binding.newpassword.setEnabled(false);
        binding.saveInfoButton.setEnabled(false);

        binding.editInfoButton.setOnClickListener(v -> {
            toggleEditableFields();
        });

        binding.saveInfoButton.setOnClickListener(v -> {
            updateUserPasswordAndInfo();
        });

        retrieveUserData();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateUserPasswordAndInfo() {
        loadingDialog.show();

        String updateName = binding.name.getText().toString().trim();
        String updateAddress = binding.address.getText().toString().trim();
        String updatePhone = binding.phone.getText().toString().trim();
        String oldPassword = binding.oldpassword.getText().toString().trim();
        String newPassword = binding.newpassword.getText().toString().trim();

        if (updateName.isEmpty() || updateAddress.isEmpty() || updatePhone.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ tên, địa chỉ và số điện thoại");
            loadingDialog.dismiss();
            return;
        }

        if (auth.getCurrentUser() == null || currentUserID == null || userRef == null) {
            showToast("Không tìm thấy người dùng");
            loadingDialog.dismiss();
            return;
        }

        // Nếu người dùng muốn đổi mật khẩu
        if (!oldPassword.isEmpty() || !newPassword.isEmpty()) {
            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                showToast("Vui lòng nhập đầy đủ mật khẩu cũ và mới");
                loadingDialog.dismiss();
                return;
            }

            if (newPassword.length() < 6) {
                showToast("Mật khẩu mới phải có ít nhất 6 ký tự");
                loadingDialog.dismiss();
                return;
            }

            String currentEmail = auth.getCurrentUser().getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, oldPassword);

            auth.getCurrentUser().reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        auth.getCurrentUser().updatePassword(newPassword)
                                .addOnSuccessListener(unused1 -> {
                                    updateOtherInfo(updateName, updateAddress, updatePhone);
                                    showToast("Đổi mật khẩu thành công. Đăng nhập lại!");
                                    loadingDialog.dismiss();
                                    logout();
                                })
                                .addOnFailureListener(e -> {
                                    showToast("Cập nhật mật khẩu thất bại: " + e.getMessage());
                                    loadingDialog.dismiss();
                                });
                    })
                    .addOnFailureListener(e -> {
                        showToast("Xác thực không thành công: " + e.getMessage());
                        loadingDialog.dismiss();
                    });

        } else {
            // Không cập nhật mật khẩu, chỉ cập nhật info
            updateOtherInfo(updateName, updateAddress, updatePhone);
        }
    }

    private void updateOtherInfo(String name, String address, String phone) {
        userRef.child("name").setValue(name);
        userRef.child("address").setValue(address);
        userRef.child("phone").setValue(phone);

        toggleEditableFields();
        showToast("Cập nhật thông tin thành công");
        loadingDialog.dismiss();
    }

    private void toggleEditableFields() {
        isEditable = !isEditable;

        binding.name.setEnabled(isEditable);
        binding.address.setEnabled(isEditable);
        binding.phone.setEnabled(isEditable);
        binding.oldpassword.setEnabled(isEditable);
        binding.newpassword.setEnabled(isEditable);
        binding.saveInfoButton.setEnabled(isEditable);

        if (isEditable) {
            binding.name.requestFocus();
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
                        String address = (String) snapshot.child("address").getValue();
                        String phone = (String) snapshot.child("phone").getValue();
                        nameOfRestaurant = (String) snapshot.child("nameOfRestaurant").getValue();

                        setDataToView(name, email, address, phone);
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

    private void logout() {
        auth.signOut();
        userPreferences = new UserPreferences(getApplicationContext());
        userPreferences.clearUserRole();

        Intent intent = new Intent(AdminProfileActivity.this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setDataToView(String name, String email, String address, String phone) {
        binding.name.setText(name);
        binding.email.setText(email);
        binding.address.setText(address);
        binding.phone.setText(phone);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}