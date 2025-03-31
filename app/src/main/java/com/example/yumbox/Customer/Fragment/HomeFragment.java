package com.example.yumbox.Customer.Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.yumbox.Model.CustomerMenuItem;
import com.example.yumbox.R;
import com.example.yumbox.databinding.FragmentHomeBinding;
import com.example.yumbox.Customer.Adapter.MenuAdapter;
import com.example.yumbox.Utils.LoadingDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Dialog loadingDialog;
    private FirebaseDatabase database;
    private ArrayList<CustomerMenuItem> menuItems;

    public HomeFragment() {
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
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = LoadingDialog.create(getContext(), "Đang tải...");

        // Set up ImageSlider
        ArrayList<SlideModel> imageList = new ArrayList<SlideModel>();
        imageList.add(new SlideModel(R.drawable.banner1, ScaleTypes.FIT));
        imageList.add(new SlideModel(R.drawable.banner2, ScaleTypes.FIT));
        imageList.add(new SlideModel(R.drawable.banner3, ScaleTypes.FIT));
        ImageSlider imageSlider = binding.imageSlider;
        imageSlider.setImageList(imageList);
        imageSlider.setImageList(imageList, ScaleTypes.FIT);
        imageSlider.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                int itemPosition = i;
                String itemMessage = String.format("Đã chọn slide %d", itemPosition);
                Toast.makeText(getContext(), itemMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void doubleClick(int i) {
            }
        });

        // Show all menu item
        binding.viewAllMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment = new MenuBottomSheetFragment();
                bottomSheetDialogFragment.show(getParentFragmentManager(), "MenuBottomSheetFragment");
            }
        });

        retrieveAndShowPopularItems();
    }

    private void retrieveAndShowPopularItems() {
        loadingDialog.show();
        database = FirebaseDatabase.getInstance();
        DatabaseReference foodRef = database.getReference().child("MenuItems");
        menuItems = new ArrayList<>();

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CustomerMenuItem menuItem = dataSnapshot.getValue(CustomerMenuItem.class);
                    menuItems.add(menuItem);
                }
                randomPopularItems();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void randomPopularItems() {
        // List index of menu items
        ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < menuItems.size(); i++) {
            indexList.add(i);
        }
        Collections.shuffle(indexList);

        int numItemToShow = Math.min(6, menuItems.size()); // Number of item to show

        // New menu item list with random index
        ArrayList<CustomerMenuItem> subnetMenuItems = new ArrayList<>();
        for (int i = 0; i < numItemToShow; i++) {
            subnetMenuItems.add(menuItems.get(indexList.get(i)));
        }

        setPopularItemsAdapter(subnetMenuItems);
    }

    private void setPopularItemsAdapter(ArrayList<CustomerMenuItem> subnetMenuItems) {
        MenuAdapter adapter = new MenuAdapter(subnetMenuItems, getContext());
        binding.popularRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.popularRecyclerView.setAdapter(adapter);
    }
}