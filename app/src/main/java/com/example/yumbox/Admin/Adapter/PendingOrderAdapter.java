package com.example.yumbox.Admin.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.Model.OrderDetail;
import com.example.yumbox.Utils.FormatString;
import com.example.yumbox.databinding.PendingOrderItemBinding;

import java.util.ArrayList;

public class PendingOrderAdapter extends RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder> {
    private ArrayList<String> customerNames, orderItemsTotalPrice, foodImages;
    private ArrayList<OrderDetail> orderItems;
    private Context context;
    private OnItemClicked itemClicked;

    public interface OnItemClicked {
        void onItemClickListener(int position);
        void onItemAcceptClickListener(int position);
        void onItemDispatchClickListener(int position);
    }

    public PendingOrderAdapter(ArrayList<String> customerNames, ArrayList<String> orderItemsTotalPrice, ArrayList<String> foodImages, Context context, OnItemClicked itemClicked) {
        this.customerNames = customerNames;
        this.orderItemsTotalPrice = orderItemsTotalPrice;
        this.foodImages = foodImages;
        this.context = context;
        this.itemClicked = itemClicked;
    }

    @NonNull
    @Override
    public PendingOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PendingOrderItemBinding binding = PendingOrderItemBinding.inflate(inflater, parent, false);
        return new PendingOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrderViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return customerNames.size();
    }

    public class PendingOrderViewHolder extends RecyclerView.ViewHolder {

        private final PendingOrderItemBinding binding;
        private final boolean[] isAccepted = {false};

        public PendingOrderViewHolder(PendingOrderItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.customerName.setText(customerNames.get(position));
            binding.totalPrice.setText(FormatString.formatAmountFromString(orderItemsTotalPrice.get(position)));

            Uri uri = Uri.parse(foodImages.get(position));
            Glide.with(context).load(uri).into(binding.orderFoodImage);

            if (!isAccepted[0]) {
                binding.orderedAcceptButton.setText("Nhận đơn");
            } else {
                binding.orderedAcceptButton.setText("Giao hàng");
            }

            binding.orderedAcceptButton.setOnClickListener(v -> {
                if (!isAccepted[0]) {
                    binding.orderedAcceptButton.setText("Giao hàng");
                    isAccepted[0] = true;

                    itemClicked.onItemAcceptClickListener(position);
                } else {
                    customerNames.remove(position);
                    orderItemsTotalPrice.remove(position);
                    foodImages.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, customerNames.size());

                    itemClicked.onItemDispatchClickListener(position);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                itemClicked.onItemClickListener(position);
            });
        }

        private void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
