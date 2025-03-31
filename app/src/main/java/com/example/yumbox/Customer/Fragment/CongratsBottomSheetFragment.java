package com.example.yumbox.Customer.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yumbox.Customer.CustomerMainActivity;
import com.example.yumbox.databinding.FragmentCongratsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CongratsBottomSheetFragment extends BottomSheetDialogFragment {
    private FragmentCongratsBottomSheetBinding binding;

    public CongratsBottomSheetFragment() {
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
        binding = FragmentCongratsBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back home
        binding.goHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), CustomerMainActivity.class);
                startActivity(intent);
            }
        });
    }
}