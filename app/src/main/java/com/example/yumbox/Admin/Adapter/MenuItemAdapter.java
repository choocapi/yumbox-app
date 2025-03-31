package com.example.yumbox.Admin.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Model.AdminMenuItem;
import com.example.yumbox.Model.CartItem;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.databinding.ItemItemBinding;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.AddItemViewHolder> {
    private Context context;
    private ArrayList<AdminMenuItem> menuList;
    private ArrayList<CartItem> cartItems;
    private int[] itemQuantities;
    private onItemClicked itemClicked;
    private DatabaseReference databaseReference;

    public interface onItemClicked {
        void onItemDeleteClickListener(int position);
    }

    // Constructor
    public MenuItemAdapter(ArrayList<AdminMenuItem> menuList, Context context, DatabaseReference databaseReference, onItemClicked itemClicked) {
        this.menuList = menuList;
        this.context = context;
        this.databaseReference = databaseReference;
        this.itemClicked = itemClicked;

        // Initialize itemQuantities with default value
        itemQuantities = new int[menuList.size()];
        for (int i = 0; i < menuList.size(); i++) {
            itemQuantities[i] = 1;
        }
    }

    @NonNull
    @Override
    public AddItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert xml to java
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemItemBinding binding = ItemItemBinding.inflate(inflater, parent, false);
        return new AddItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddItemViewHolder holder, int position) {
        // Bind data to ViewHolder
        // Position is the index of the current item
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class AddItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemItemBinding binding;

        // Constructor
        public AddItemViewHolder(ItemItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind data to the views
        public void bind(int position) {
            int quatity = itemQuantities[position];
            AdminMenuItem menuItem = menuList.get(position);

            binding.foodName.setText(menuItem.getFoodName());
            binding.shortDesc.setText(menuItem.getFoodDescription());
            binding.foodPrice.setText(FormatString.formatAmountFromString(menuItem.getFoodPrice()));
            binding.quantity.setText(String.valueOf(quatity));

            // Load image using Glide
            Uri uri = Uri.parse(menuItem.getFoodImage());
            Glide.with(context).load(uri).into(binding.foodImage);

            // Handle decrease item when click on minus button
            binding.minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decreaseQuantity(position);
                }
            });

            // Handle increase item when click on plus button
            binding.plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    increaseQuantity(position);
                }
            });

            // Handle delete item when click on delete button
            binding.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClicked.onItemDeleteClickListener(position);
                }
            });
        }

        private void increaseQuantity(int position) {
            // Max quantity is 10
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++;
                binding.quantity.setText(String.valueOf(itemQuantities[position]));
            }
        }

        private void decreaseQuantity(int position) {
            // Min quantity is 1
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--;
                binding.quantity.setText(String.valueOf(itemQuantities[position]));
            }
        }

        private void deleteItem(int position) {
            menuList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, menuList.size());
        }
    }
}
