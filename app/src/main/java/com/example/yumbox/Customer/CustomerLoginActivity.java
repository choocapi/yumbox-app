package com.example.yumbox.Customer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.ForgotPasswordActivity;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.Utils.PasswordToggleHelper;
import com.example.yumbox.Utils.UserPreferences;
import com.example.yumbox.databinding.ActivityCustomerLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerLoginActivity extends AppCompatActivity {
    private ActivityCustomerLoginBinding binding;
    private Dialog loadingDialog;
    private GoogleSignInClient googleSignInClient;
    private String username, email, address, phone, password, userRole, userID;

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
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

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
            Intent signIntent = googleSignInClient.getSignInIntent();
            launcher.launch(signIntent);
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
    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(credential).addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        user = auth.getCurrentUser();
                        userID = user != null ? user.getUid() : null;
                        checkUserRole(userID);
                    } else {
                        showToast("Đăng nhập thất bại");
                    }
                });
            }
        }
    });

    private void loginUserAccount(String email, String password) {
        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                userID = user != null ? user.getUid() : null;
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}