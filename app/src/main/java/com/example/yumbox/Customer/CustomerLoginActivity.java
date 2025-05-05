package com.example.yumbox.Customer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.ForgotPasswordActivity;
import com.example.yumbox.Model.CustomerModel;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.PasswordToggleHelper;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityCustomerLoginBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class CustomerLoginActivity extends AppCompatActivity {
    private ActivityCustomerLoginBinding binding;
    private Dialog loadingDialog;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private String username, email, phone, password, userRole, userId;

    // Firebase & Local
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private FirebaseUser user;
    private UserPreferences userPreferences;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = LoadingDialog.create(this, "Đang đăng nhập...");

        // Init Google Sign-In
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                )
                .setAutoSelectEnabled(true) // Option: choose account automatically
                .build();

        // Show/hide password
        PasswordToggleHelper passwordToggleHelper = new PasswordToggleHelper();
        passwordToggleHelper.setupPasswordToggle(binding.password);

        // Button login
        binding.loginButton.setOnClickListener(v -> {
            // Get user input
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            // Check
            if (email.isBlank() || password.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                loginUserAccount(email, password);
            }
        });

        // Button Google Sign-In
        binding.googleButton.setOnClickListener(v -> {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this, result -> {
                        try {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                            oneTapLauncher.launch(intentSenderRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        showToast("Không thể khởi động One Tap: " + e.getMessage());
                        Log.d("One tap", "OneTapGoogleSignIn: " + e.getMessage());
                    });

        });

        binding.dontHaveAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerLoginActivity.this, CustomerSignUpActivity.class);
            startActivity(intent);
        });

        binding.forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerLoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    // Launcher Google Sign-In
    private final ActivityResultLauncher<IntentSenderRequest> oneTapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                            FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            user = FirebaseAuth.getInstance().getCurrentUser();
                                            userId = Objects.requireNonNull(user).getUid();
                                            email = user.getEmail();
                                            username = user.getDisplayName();
                                            phone = user.getPhoneNumber();
                                            saveUserDataSignInGoogle(email, username, phone);
                                            checkUserRole(userId);
                                        } else {
                                            showToast("Đăng nhập thất bại");
                                        }
                                    });
                        }
                    } catch (ApiException e) {
                        e.printStackTrace();
                        showToast("Lỗi đăng nhập: " + e.getMessage());
                    }
                }
            });


    private void loginUserAccount(String email, String password) {
        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                userId = user != null ? user.getUid() : null;
                checkUserRole(userId);
                loadingDialog.dismiss();
            } else {
                showToast("Email hoặc mật khẩu không đúng");
                loadingDialog.dismiss();
            }
        });
    }

    private void checkUserRole(String userId) {
        databaseRef.child("Users").child(userId).child("role").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.getValue(String.class);
                    if (userRole != null) {
                        userPreferences = new UserPreferences(getApplicationContext());

                        if (userRole.equals("customer")) {
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
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            showToast("Lỗi đăng nhập");
        }
    }

    private void saveUserDataSignInGoogle(String email, String username, String phone) {
        if (auth.getCurrentUser() != null) {
            CustomerModel user = new CustomerModel(username, email, null, phone, null);
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            databaseRef.child("Users").child(userId).setValue(user);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}