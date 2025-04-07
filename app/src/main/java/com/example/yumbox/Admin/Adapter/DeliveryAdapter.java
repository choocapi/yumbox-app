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
import java.util.LinkedHashMap;
import java.util.Map;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {
    private ArrayList<String> customerNames;
    private ArrayList<Boolean> listIsOrderReceived;
    private ArrayList<Integer> paymentMethods;
    private final Map<Integer, String> paymentMethodsMap = new LinkedHashMap<Integer, String>() {{
        put(0, "Trực tiếp");
        put(1, "Zalopay");
    }};

    // Constructor
    public DeliveryAdapter(ArrayList<String> customerNames, ArrayList<Integer> paymentMethods, ArrayList<Boolean> listIsOrderReceived) {
        this.customerNames = customerNames;
        this.paymentMethods = paymentMethods;
        this.listIsOrderReceived = listIsOrderReceived;
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
            if (listIsOrderReceived.get(position)) {
                binding.statusMoney.setText("Đã nhận");
            } else {
                binding.statusMoney.setText("Chưa nhận");
            }

            // Set color for statusMoney and statusColor based on moneyStatus
            int color = colorMap.getOrDefault(listIsOrderReceived.get(position), Color.BLACK);
            binding.statusMoney.setTextColor(color);
            binding.statusColor.setBackgroundTintList(ColorStateList.valueOf(color));

            binding.paymentMethod.setText(
                    paymentMethodsMap.getOrDefault(paymentMethods.get(position), "Không rõ")
            );
        }
    }
}
