package com.example.yumbox.Customer.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yumbox.Customer.Adapter.NotificationAdapter;
import com.example.yumbox.R;
import com.example.yumbox.databinding.FragmentNotificationBottomBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class NotificationBottomFragment extends BottomSheetDialogFragment {
    private FragmentNotificationBottomBinding binding;

    public NotificationBottomFragment() {
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
        binding = FragmentNotificationBottomBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the RecyclerView and adapter
        ArrayList<String> notifications = new ArrayList<>(Arrays.asList("Đơn hàng của bạn đã bị huỷ",
                "Shipper đã nhận đơn hàng của bạn",
                "Đơn hàng của bạn đã được giao thành công"));
        ArrayList<Integer> notificationImages = new ArrayList<>(Arrays.asList(R.drawable.sademoji, R.drawable.truck, R.drawable.congrats));

        NotificationAdapter adapter = new NotificationAdapter(notifications, notificationImages);
        binding.notificationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notificationRecyclerView.setAdapter(adapter);

        // Go back
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}