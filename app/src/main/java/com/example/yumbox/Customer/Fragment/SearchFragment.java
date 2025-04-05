package com.example.yumbox.Customer.Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.Customer.Adapter.MenuAdapter;
import com.example.yumbox.Model.MenuItem;
import com.example.yumbox.Utils.LoadingDialog;
import com.example.yumbox.databinding.FragmentSearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private Dialog loadingDialog;
    private MenuAdapter menuAdapter;

    // Info
    private ArrayList<MenuItem> menuItems, filterMenuItems;
    private ArrayList<String> restaurantList, typeFoodList;

    // Firebase
    private FirebaseDatabase database;

    public SearchFragment() {
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
        binding = FragmentSearchBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = LoadingDialog.create(getContext(), "Đang tải...");

        retrieveMenuItems();
        setupSearchView();
    }

    private void retrieveMenuItems() {
        loadingDialog.show();
        database = FirebaseDatabase.getInstance();
        DatabaseReference menuRef = database.getReference().child("MenuItems");
        menuItems = new ArrayList<>();

        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MenuItem menuItem = dataSnapshot.getValue(MenuItem.class);
                    menuItems.add(menuItem);
                }

                showAllMenuItems();
                getAndDisplayRestaurantList();
                getAndDisplayTypeFoodList();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
            }
        });
    }

    private void showAllMenuItems() {
        filterMenuItems = new ArrayList<>(menuItems);
        setAdapter(filterMenuItems);
    }

    private void setAdapter(ArrayList<MenuItem> filterMenuItems) {
        menuAdapter = new MenuAdapter(filterMenuItems, getContext());
        binding.menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.menuRecyclerView.setAdapter(menuAdapter);
    }

    // Handle SearchView take and filter data
    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMenuItems(query, "search");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMenuItems(newText, "search");
                return true;
            }
        });
    }

    // Filter menu items based on the search query
    private void filterMenuItems(String query, String type) {
        filterMenuItems = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            showAllMenuItems();
        } else {
            for (MenuItem menuItem : menuItems) {
                if (type.equals("search")) {
                    if (menuItem.getFoodName().toLowerCase().contains(query.toLowerCase())) {
                        filterMenuItems.add(menuItem);
                    }
                } else if (type.equals("filter")) {
                    if (menuItem.getNameOfRestaurant().toLowerCase().contains(query.toLowerCase()) || menuItem.getFoodType().toLowerCase().contains(query.toLowerCase())) {
                        filterMenuItems.add(menuItem);
                    }
                }
            }
        }

        setAdapter(filterMenuItems);
    }

    private void getAndDisplayRestaurantList() {
        Set<String> uniqueRestaurants = new HashSet<>();
        for (MenuItem menuItem : menuItems) {
            uniqueRestaurants.add(menuItem.getNameOfRestaurant());
        }
        restaurantList = new ArrayList<>(uniqueRestaurants);
        ArrayAdapter<String> restaurantListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, restaurantList);
        binding.listOfRestaurant.setAdapter(restaurantListAdapter);
        binding.listOfRestaurant.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedRestaurant = parent.getItemAtPosition(position).toString();
                filterMenuItems(selectedRestaurant, "filter");
            }
        });
    }

    private void getAndDisplayTypeFoodList() {
        Set<String> uniqueTypeFood = new HashSet<>();
        for (MenuItem menuItem : menuItems) {
            uniqueTypeFood.add(menuItem.getFoodType());
        }
        typeFoodList = new ArrayList<>(uniqueTypeFood);
        ArrayAdapter<String> typeFoodListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, typeFoodList);
        binding.listOfTypeFood.setAdapter(typeFoodListAdapter);
        binding.listOfTypeFood.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                filterMenuItems(selectedType, "filter");
            }
        });
    }
}