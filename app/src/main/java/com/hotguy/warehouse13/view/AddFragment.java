package com.hotguy.warehouse13.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.databinding.FragmentAddBinding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AddFragment — View for adding products to the warehouse.
 * <p>
 * MVC responsibility:
 * Collects form data, builds a JSON string, and delegates to the
 * Controller. Never touches model classes directly.
 * <p>
 * Threading:
 * {@link Controller#addProduct} is blocking (performs an HTTP request
 * via {@link com.hotguy.warehouse13.controller.DatabaseAccess}).
 * The call is therefore dispatched on a single-thread
 * {@link ExecutorService}; UI feedback is posted back on the main thread.
 */
public class AddFragment extends Fragment {

    /**
     * Single background thread for DB network operations.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FragmentAddBinding binding;

    public AddFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show / hide expiration field based on perishable toggle
        binding.switchPerishable.setOnCheckedChangeListener((btn, isChecked) -> {
            binding.tilExpiration.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                if (binding.editExpiration.getText() != null)
                    binding.editExpiration.getText().clear();
                binding.tilExpiration.setError(null);
            }
        });

        binding.btnAdd.setOnClickListener(v -> attemptAddProduct());
    }

    /**
     * Validates form fields, builds the product JSON, and calls the Controller
     * on a background thread. UI is updated on the main thread once the
     * network operation completes.
     */
    private void attemptAddProduct() {
        clearErrors();

        String code = getText(binding.editCode);
        String description = getText(binding.editDesc);
        String priceStr = getText(binding.editPrice);
        String stockStr = getText(binding.editStock);
        boolean isPerishable = binding.switchPerishable.isChecked();
        String expiration = isPerishable ? getText(binding.editExpiration) : "";

        // ── Field validation ──
        boolean valid = true;
        if (code.isEmpty()) {
            binding.tilCode.setError(getString(R.string.error_required));
            valid = false;
        }
        if (description.isEmpty()) {
            binding.tilDesc.setError(getString(R.string.error_required));
            valid = false;
        }
        if (priceStr.isEmpty()) {
            binding.tilPrice.setError(getString(R.string.error_required));
            valid = false;
        }
        if (stockStr.isEmpty()) {
            binding.tilStock.setError(getString(R.string.error_required));
            valid = false;
        }
        if (isPerishable && expiration.isEmpty()) {
            binding.tilExpiration.setError(getString(R.string.error_required));
            valid = false;
        }
        if (!valid) return;

        // ── Type conversion ──
        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Build JSON for the Controller ──
        // The View never instantiates model objects: it passes a JSON map
        // and lets the Controller parse and create the Product.
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productCode", code.toUpperCase());
        data.put("description", description);
        data.put("price", price);
        data.put("stock", stock);
        if (isPerishable) data.put("expirationDate", expiration);

        String jsonProduct = new Gson().toJson(data);

        // Disable the button while the request is in flight
        binding.btnAdd.setEnabled(false);

        // ── Background thread: addProduct is blocking (HTTP request) ──
        executor.execute(() -> {
            boolean added;
            String errorMessage = null;
            try {
                added = Controller.getSingleton().addProduct(isPerishable, jsonProduct);
            } catch (Exception e) {
                added = false;
                errorMessage = e.getMessage();
            }

            final boolean result = added;
            final String finalError = errorMessage;

            // ── Return to main thread for UI feedback ──
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                binding.btnAdd.setEnabled(true);
                if (result) {
                    Toast.makeText(requireContext(),
                        getString(R.string.toast_item_added), Toast.LENGTH_SHORT).show();
                    clearForm();
                } else if (finalError != null) {
                    binding.tilCode.setError(finalError);
                } else {
                    binding.tilCode.setError(getString(R.string.error_duplicate_code));
                }
            });
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extracts trimmed text from an EditText, returning an empty string if null.
     */
    private String getText(android.widget.EditText edit) {
        return edit != null && edit.getText() != null
            ? edit.getText().toString().trim()
            : "";
    }

    /**
     * Clears all field error messages.
     */
    private void clearErrors() {
        binding.tilCode.setError(null);
        binding.tilDesc.setError(null);
        binding.tilPrice.setError(null);
        binding.tilStock.setError(null);
        binding.tilExpiration.setError(null);
    }

    /**
     * Resets all form fields after a successful add.
     */
    private void clearForm() {
        binding.editCode.getText().clear();
        binding.editDesc.getText().clear();
        binding.editPrice.getText().clear();
        binding.editStock.getText().clear();
        binding.editExpiration.getText().clear();
        binding.switchPerishable.setChecked(false);
        clearErrors();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
