package com.example.yumbox.Customer.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.yumbox.Model.CustomerModel;
import com.example.yumbox.StartActivity;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private Dialog loadingDialog;
    private int dayOfMonth, month, year;

    // User info
    private CustomerModel userProfile;
    private String name, address, phone, dateOfBirth, userID;

    // Firebase & Local
    private FirebaseAuth auth;
    private DatabaseReference databaseRef, userRef;
    private UserPreferences userPreferences;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadingDialog = LoadingDialog.create(getContext(), "Đang tải...");

        disableEditText();
        retrieveAndShowUserInfo();

        binding.saveInfoButton.setOnClickListener(v -> {
            name = binding.name.getText().toString().trim();
            address = binding.address.getText().toString().trim();
            phone = binding.phone.getText().toString().trim();
            dateOfBirth = binding.dateOfBirth.getText().toString().trim();
            String oldPass = binding.oldpassword.getText().toString().trim();
            String newPass = binding.newpassword.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || dateOfBirth.isEmpty()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
                return;
            }

            updateUserData(name, address, phone, dateOfBirth);
            disableEditText();

            if (!oldPass.isEmpty() || !newPass.isEmpty()) {
                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    showToast("Vui lòng nhập đầy đủ mật khẩu cũ và mới");
                } else if (newPass.length() < 6) {
                    showToast("Mật khẩu mới phải từ 6 ký tự trở lên");
                } else {
                    updatePassword(oldPass, newPass);
                }
            }
        });

        binding.editButton.setOnClickListener(v -> {
            toggleEditableFields();
        });

        binding.dateOfBirth.setOnClickListener(v -> {
            if (binding.dateOfBirth.isEnabled()) {
                pickDateOfBirth();
            }
        });

        binding.logoutButton.setOnClickListener(v -> {
            logout();
        });
    }

    private void disableEditText() {
        binding.name.setEnabled(false);
        binding.address.setEnabled(false);
        binding.phone.setEnabled(false);
        binding.dateOfBirth.setEnabled(false);
        binding.oldpassword.setEnabled(false);
        binding.newpassword.setEnabled(false);
    }

    private void toggleEditableFields() {
        boolean isEditable = !binding.name.isEnabled();

        binding.name.setEnabled(isEditable);
        binding.address.setEnabled(isEditable);
        binding.phone.setEnabled(isEditable);
        binding.dateOfBirth.setEnabled(isEditable);
        binding.oldpassword.setEnabled(isEditable);
        binding.newpassword.setEnabled(isEditable);
    }

    private void updatePassword(String oldPassword, String newPassword) {
        loadingDialog.show();
        auth = FirebaseAuth.getInstance();

        auth.getCurrentUser().reauthenticate(
                com.google.firebase.auth.EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), oldPassword)
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                auth.getCurrentUser().updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            loadingDialog.dismiss();
                            if (updateTask.isSuccessful()) {
                                showToast("Đổi mật khẩu thành công. Đăng nhập lại!");
                                logout();
                            } else {
                                showToast("Đổi mật khẩu thất bại");
                            }
                        });
            } else {
                loadingDialog.dismiss();
                showToast("Mật khẩu cũ không đúng");
            }
        });
    }

    private void updateUserData(String name, String address, String phone, String dateOfBirth) {
        loadingDialog.show();
        userID = auth.getCurrentUser().getUid();
        if (userID != null) {
            userRef = databaseRef.child("Users").child(userID);

            userRef.child("name").setValue(name);
            userRef.child("address").setValue(address);
            userRef.child("phone").setValue(phone);
            userRef.child("dateOfBirth").setValue(dateOfBirth);

            showToast("Cập nhật thông tin cá nhân thành công");
            loadingDialog.dismiss();
        }
    }

    private void retrieveAndShowUserInfo() {
        loadingDialog.show();
        userID = auth.getCurrentUser().getUid();
        if (userID != null) {
            userRef = databaseRef.child("Users").child(userID);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userProfile = snapshot.getValue(CustomerModel.class);
                        if (userProfile != null) {
                            binding.name.setText(userProfile.getName());
                            binding.address.setText(userProfile.getAddress());
                            binding.phone.setText(userProfile.getPhone());
                            binding.email.setText(userProfile.getEmail());
                            binding.dateOfBirth.setText(userProfile.getDateOfBirth());
                        }

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
        userPreferences = new UserPreferences(requireContext());
        userPreferences.clearUserRole();

        Intent intent = new Intent(getContext(), StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pickDateOfBirth() {
        dateOfBirth = binding.dateOfBirth.getText().toString().trim();
        if (!dateOfBirth.isEmpty()) {
            String[] date = dateOfBirth.split("/");
            dayOfMonth = Integer.parseInt(date[0]);
            month = Integer.parseInt(date[1]);
            year = Integer.parseInt(date[2]);
        } else {
            Calendar calendar = Calendar.getInstance();
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                binding.dateOfBirth.setText(String.format("%02d/%02d/%d", dayOfMonth, month, year));
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), callback, year, month, dayOfMonth);
        datePickerDialog.updateDate(year, month, dayOfMonth);
        datePickerDialog.show();
    }
}