package com.example.yumbox.Admin.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.databinding.OrderDetailItemBinding;

import java.util.ArrayList;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private ArrayList<String> foodNames, foodImages, foodPrices;
    private ArrayList<Integer> foodQuantities;
    private Context context;

    public OrderDetailAdapter(ArrayList<String> foodNames, ArrayList<String> foodImages, ArrayList<Integer> foodQuantities, ArrayList<String> foodPrices, Context context) {
        this.foodNames = foodNames;
        this.foodImages = foodImages;
        this.foodQuantities = foodQuantities;
        this.foodPrices = foodPrices;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        OrderDetailItemBinding binding = OrderDetailItemBinding.inflate(inflater, parent, false);
        return new OrderDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return foodNames.size();
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private final OrderDetailItemBinding binding;

        public OrderDetailViewHolder(OrderDetailItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.foodName.setText(foodNames.get(position));
            binding.foodQuantity.setText(String.valueOf(foodQuantities.get(position)));
            binding.foodPrice.setText(FormatString.formatAmountFromString(foodPrices.get(position)));

            Uri uri = Uri.parse(foodImages.get(position));
            Glide.with(context).load(uri).into(binding.foodImage);
        }
    }
}
