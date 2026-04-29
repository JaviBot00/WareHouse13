package com.hotguy.warehouse13.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.controller.ProductsRVAdapter;
import com.hotguy.warehouse13.databinding.FragmentListBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ListFragment — View for the full list of active products.
 * <p>
 * MVC responsibility:
 * Requests data from the Controller and delegates rendering to the
 * Adapter/Holder. Launches dialogs for stock editing and product retirement.
 * <p>
 * Threading:
 * {@link Controller#editStockForProduct} and {@link Controller#withdrawProduct}
 * are blocking (perform HTTP requests via DatabaseAccess).
 * Both are dispatched on a single-thread {@link ExecutorService};
 * UI updates are posted back on the main thread.
 */
public class ListFragment extends Fragment {

    /**
     * Single background thread for DB network operations.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FragmentListBinding binding;
    private ProductsRVAdapter adapter;

    public ListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        updateEmptyState();
    }

    /**
     * Configures the RecyclerView with the Adapter.
     * Injects two action listeners for each card:
     * · onEditStock → dialog to modify stock
     * · onWithdraw  → confirmation dialog for retirement
     */
    private void setupRecyclerView() {
        adapter = new ProductsRVAdapter(
            Controller.getSingleton().listProducts(),
            this::showEditStockDialog,
            this::showWithdrawDialog);
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Notifies the adapter of dataset changes and updates the empty state.
     */
    private void refreshList() {
        if (adapter != null) adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    /**
     * Shows or hides the empty-state layout depending on the product list size.
     */
    private void updateEmptyState() {
        boolean isEmpty = Controller.getSingleton().listProducts() == null
            || Controller.getSingleton().listProducts().isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ── Dialog: Edit stock ────────────────────────────────────────────────────

    /**
     * Shows a dialog with a signed numeric field for the stock change.
     * Positive values add units; negative values remove them.
     * <p>
     * On confirmation, {@link Controller#editStockForProduct} is called
     * on a background thread since it performs an HTTP request.
     *
     * @param productCode code of the product to update
     */
    private void showEditStockDialog(String productCode) {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_stock, null);

        TextInputLayout til = dialogView.findViewById(R.id.tilStockChange);
        TextInputEditText edit = dialogView.findViewById(R.id.editStockChange);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_edit_stock_title))
            .setMessage(getString(R.string.dialog_edit_stock_msg, productCode))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.btn_apply), (dialog, which) -> {
                String input = edit.getText() != null
                    ? edit.getText().toString().trim() : "";
                if (input.isEmpty()) {
                    til.setError(getString(R.string.error_required));
                    return;
                }

                int change;
                try {
                    change = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(),
                        getString(R.string.error_number_format),
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                // ── Background thread: editStockForProduct is blocking ──
                executor.execute(() -> {
                    boolean ok = Controller.getSingleton()
                        .editStockForProduct(productCode, change);

                    if (getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (ok) {
                            refreshList();
                            Toast.makeText(requireContext(),
                                getString(R.string.toast_stock_updated),
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                getString(R.string.error_stock_negative),
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    // ── Dialog: Withdraw product ──────────────────────────────────────────────

    /**
     * Confirmation dialog before retiring a product from the warehouse.
     * <p>
     * On confirmation, {@link Controller#withdrawProduct} is called
     * on a background thread since it performs an HTTP request.
     *
     * @param productCode code of the product to retire
     */
    private void showWithdrawDialog(String productCode) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_withdraw_title))
            .setMessage(getString(R.string.dialog_withdraw_msg, productCode))
            .setPositiveButton(getString(R.string.btn_withdraw), (dialog, which) -> {

                // ── Background thread: withdrawProduct is blocking ──
                executor.execute(() -> {
                    boolean ok = Controller.getSingleton()
                        .withdrawProduct(productCode);

                    if (getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        if (ok) {
                            refreshList();
                            Toast.makeText(requireContext(),
                                getString(R.string.toast_withdrawn),
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                getString(R.string.error_not_found),
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            })
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) refreshList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
