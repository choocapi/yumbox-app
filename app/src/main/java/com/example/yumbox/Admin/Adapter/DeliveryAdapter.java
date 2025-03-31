package com.example.yumbox.Admin.Adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yumbox.databinding.DeliveryItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {
    private ArrayList<String> customerNames;
    private ArrayList<Boolean> moneyStatus;

    // Constructor
    public DeliveryAdapter(ArrayList<String> customerNames, ArrayList<Boolean> moneyStatus) {
        this.customerNames = customerNames;
        this.moneyStatus = moneyStatus;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert xml to java
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        DeliveryItemBinding binding = DeliveryItemBinding.inflate(inflater, parent, false);
        return new DeliveryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        // Bind data to ViewHolder
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return customerNames.size();
    }

    public class DeliveryViewHolder extends RecyclerView.ViewHolder {
        private final DeliveryItemBinding binding;

        // Constructor
        public DeliveryViewHolder(DeliveryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            Map<Boolean, Integer> colorMap = new HashMap<>();
            colorMap.put(true, Color.GREEN);
            colorMap.put(false, Color.RED);

            binding.customerName.setText(customerNames.get(position));
            if (moneyStatus.get(position)) {
                binding.statusMoney.setText("Đã nhận");
            } else {
                binding.statusMoney.setText("Chưa nhận");
            }

            // Set color for statusMoney and statusColor based on moneyStatus
            int color = colorMap.getOrDefault(moneyStatus.get(position), Color.BLACK);
            binding.statusMoney.setTextColor(color);
            binding.statusColor.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}
