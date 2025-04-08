package com.example.yumbox;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth auth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init
        auth = FirebaseAuth.getInstance();

        binding.btnReset.setOnClickListener(v -> {
            email = binding.edtForgotPasswordEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                ResetPassword();
            } else {
                binding.edtForgotPasswordEmail.setError("Email không được để trống");
            }
        });

        binding.btnForgotPasswordBack.setOnClickListener(v -> { finish(); });
    }

    private void ResetPassword() {
        binding.forgetPasswordProgressbar.setVisibility(View.VISIBLE);
        binding.btnReset.setVisibility(View.INVISIBLE);

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ForgotPasswordActivity.this, "Liên kết đặt lại mật khẩu đã được gửi đến Email đã đăng ký của bạn", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi :- " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.forgetPasswordProgressbar.setVisibility(View.INVISIBLE);
                        binding.btnReset.setVisibility(View.VISIBLE);
                    }
                });
    }
}