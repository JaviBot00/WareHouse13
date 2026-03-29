package com.hotguy.warehouse13.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hotguy.warehouse13.databinding.FragmentAddBinding;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAdd.setOnClickListener(v -> {
            EditText descEdit = binding.txtFldDesc.getEditText();
            EditText priceEdit = binding.txtFldPrice.getEditText();
            EditText stockEdit = binding.txtFldStock.getEditText();

            if (descEdit == null || priceEdit == null || stockEdit == null) return;

            String itemDesc = descEdit.getText().toString().trim();
            String itemPrice = priceEdit.getText().toString().trim();
            String itemStock = stockEdit.getText().toString().trim();

            if (!itemDesc.isEmpty() && !itemPrice.isEmpty() && !itemStock.isEmpty()) {
                Toast.makeText(requireContext(), "Item added successfully", Toast.LENGTH_SHORT).show();

                descEdit.setText("");
                priceEdit.setText("");
                stockEdit.setText("");
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
