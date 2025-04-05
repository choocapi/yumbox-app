package com.example.yumbox.Admin;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.Model.MenuItem;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityAddItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;

public class AddItemActivity extends AppCompatActivity {
    private ActivityAddItemBinding binding;
    private Dialog loadingDialog;
    private String foodName, foodPrice, foodDescription, foodIngredients, foodType, ownerUid, nameOfRestaurant;
    private Uri foodImageUri = null;

    // Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        loadingDialog = LoadingDialog.create(AddItemActivity.this, "Đang tải...");

        // Get data from Intent
        nameOfRestaurant = getIntent().getStringExtra("nameOfRestaurant");
        ownerUid = getIntent().getStringExtra("ownerUid");

        displayTypeFoodList();

        // Add item
        binding.addItemButton.setOnClickListener(v -> {
            // Get user input
            foodName = binding.enterFoodName.getText().toString().trim();
            foodPrice = binding.enterFoodPrice.getText().toString().trim();
            foodDescription = binding.foodDescription.getText().toString().trim();
            foodIngredients = binding.foodIngredients.getText().toString().trim();

            // Check
            if (foodName.isBlank() || foodPrice.isBlank() || foodDescription.isBlank() || foodIngredients.isBlank() || foodType.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                loadingDialog.show();
                uploadData();
                showToast("Thêm thành công");
                loadingDialog.dismiss();
                finish();
            }
        });

        // Select image
        binding.selectImage.setOnClickListener(v -> {
            pickImage.launch("image/*");
        });

        // Go back
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Launcher for picking image from gallery
    private ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        binding.selectedImage.setImageURI(uri);
                        foodImageUri = uri;
                    }
                }
            });


    private void uploadData() {
        DatabaseReference menuRef = database.getReference("MenuItems");
        // Generate a unique key for the new item with 'push' method
        String newItemKey = menuRef.push().getKey();

        if (foodImageUri != null && ownerUid != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("menu_images/" + newItemKey + ".jpg");

            UploadTask uploadTask = imageRef.putFile(foodImageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get uri uploaded image and put it in item detail
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            MenuItem newItem = new MenuItem(foodName, foodPrice, uri.toString(), foodDescription, foodIngredients, newItemKey, foodType, new ArrayList<>(), ownerUid, nameOfRestaurant);

                            if (newItemKey != null && auth.getCurrentUser() != null) {
                                // Save food info
                                menuRef.child(newItemKey).setValue(newItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        showToast("Tải lên dữ liệu thành công");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showToast("Tải lên dữ liệu thất bại");
                                    }
                                });
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showToast("Tải lên ảnh thất bại");
                }
            });
        } else {
            showToast("Vui lòng chọn ảnh");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayTypeFoodList() {
        ArrayList<String> typeFoodList = new ArrayList<>(Arrays.asList("Món nước", "Món khô", "Món trộn", "Món chiên", "Đồ uống"));
        ArrayAdapter<String> typeFoodListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeFoodList);
        binding.listOfTypeFood.setAdapter(typeFoodListAdapter);
        binding.listOfTypeFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                foodType = parent.getItemAtPosition(position).toString();
            }
        });
    }
}