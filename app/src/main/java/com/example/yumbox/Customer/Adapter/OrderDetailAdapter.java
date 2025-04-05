package com.example.yumbox.Customer.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.databinding.OrderItemBinding;

import java.util.ArrayList;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private ArrayList<String> foodNames, foodPrices, foodImages;
    private ArrayList<Integer> foodQuantities;
    private Context context;
    private onFeedbackClicked feedbackClicked;

    public interface onFeedbackClicked {
        void OnFeedbackClickListener(int position);
    }

    public OrderDetailAdapter(ArrayList<String> foodNames, ArrayList<String> foodPrices, ArrayList<String> foodImages, ArrayList<Integer> foodQuantities, Context context, onFeedbackClicked feedbackClicked) {
        this.foodNames = foodNames;
        this.foodPrices = foodPrices;
        this.foodImages = foodImages;
        this.foodQuantities = foodQuantities;
        this.context = context;
        this.feedbackClicked = feedbackClicked;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        OrderItemBinding binding = OrderItemBinding.inflate(inflater, parent, false);
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
        private final OrderItemBinding binding;

        public OrderDetailViewHolder(OrderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.foodName.setText(foodNames.get(position));
            binding.foodPrice.setText(FormatString.formatAmountFromString(foodPrices.get(position)));
            binding.foodQuantity.setText(String.valueOf(foodQuantities.get(position)));

            Uri uri = Uri.parse(foodImages.get(position));
            Glide.with(context).load(uri).into(binding.foodImage);

            binding.feedbackButton.setOnClickListener(v -> {
                feedbackClicked.OnFeedbackClickListener(position);
            });
        }
    }
}
