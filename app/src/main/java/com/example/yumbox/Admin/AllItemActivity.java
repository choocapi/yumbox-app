package com.example.yumbox.Admin;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Admin.Adapter.MenuItemAdapter;
import com.example.yumbox.Model.AdminMenuItem;
import com.example.yumbox.R;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.ActivityAllItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AllItemActivity extends AppCompatActivity {
    private ActivityAllItemBinding binding;
    private Dialog loadingDialog;
    private ArrayList<AdminMenuItem> menuItems = new ArrayList<>();

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String ownerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAllItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        loadingDialog = LoadingDialog.create(AllItemActivity.this, "Đang tải...");

        // Get ownerUid from intent
        ownerUid = getIntent().getStringExtra("ownerUid");

        retrieveMenuItem();

        // Go back
        binding.backButton.setOnClickListener(v -> {finish();});
    }

    private void retrieveMenuItem() {
        loadingDialog.show();
        database = FirebaseDatabase.getInstance();
        DatabaseReference itemsRef = database.getReference().child("MenuItems");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuItems = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AdminMenuItem menuItem = dataSnapshot.getValue(AdminMenuItem.class);
                    if (menuItem != null && menuItem.getOwnerUid() != null) {
                        if (menuItem.getOwnerUid().equals(ownerUid)) {
                            menuItems.add(menuItem);
                        }
                    }
                }
                setAdapter();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setAdapter() {
        MenuItemAdapter adapter = new MenuItemAdapter(menuItems, this, databaseReference, position -> {
            deleteMenuItem(position);
        });
        binding.menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.menuRecyclerView.setAdapter(adapter);
    }

    private void deleteMenuItem(int position) {
        loadingDialog.show();
        AdminMenuItem menuItemToDelete = menuItems.get(position);
        String menuItemKey = menuItemToDelete.getFoodKey();
        DatabaseReference foodItemRef = databaseReference.child("MenuItems").child(menuItemKey);
        foodItemRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    deleteFoodImage(menuItemKey);
                    menuItems.remove(position);
                    binding.menuRecyclerView.getAdapter().notifyItemRemoved(position);
                    binding.menuRecyclerView.getAdapter().notifyItemRangeChanged(position, menuItems.size());
                    loadingDialog.dismiss();
                } else {
                    Toast.makeText(AllItemActivity.this, "Xoá thất bại", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }
        });
    }

    private void deleteFoodImage(String menuItemKey) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference().child("menu_images/" + menuItemKey + ".jpg").delete();
    }
}