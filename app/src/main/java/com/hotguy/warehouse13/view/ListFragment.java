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

/**
 * ListFragment — Vista de la lista completa de productos activos.
 * <p>
 * Responsabilidad MVC:
 * · Pide la lista al Controlador vía getProductList().
 * · Delega la representación al Adapter/Holder (sin tocar el Modelo).
 * · Lanza diálogos para editar stock y retirar productos.
 * <p>
 * El Adapter recibe un listener del Fragment para cada acción de card,
 * manteniendo la separación: el Holder notifica → Fragment actúa.
 */
public class ListFragment extends Fragment {

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
     * Configura el RecyclerView con el Adapter.
     * Inyecta dos listeners para las acciones de cada card:
     * · onEditStock → diálogo para modificar stock
     * · onWithdraw → diálogo de confirmación de baja
     */
    private void setupRecyclerView() {
        adapter = new ProductsRVAdapter(
                Controller.getSingleton().getProductList(),
                this::showEditStockDialog,
                this::showWithdrawDialog);
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProducts.setAdapter(adapter);
    }

    // ── Refresca la lista cuando volvemos a este fragment ──
    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    // ── Diálogo: Editar stock ──

    /**
     * Muestra un diálogo con un campo numérico para el cambio de stock.
     * Permite valores negativos (retirar unidades) y positivos (añadir).
     *
     * @param productCode código del producto a modificar
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
                    String input = edit.getText() != null ? edit.getText().toString().trim() : "";
                    if (input.isEmpty()) {
                        til.setError(getString(R.string.error_required));
                        return;
                    }
                    try {
                        int change = Integer.parseInt(input);
                        boolean ok = Controller.getSingleton().editStockForProduct(productCode, change);
                        if (ok) {
                            refreshList();
                            Toast.makeText(requireContext(),
                                    getString(R.string.toast_stock_updated), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    getString(R.string.error_stock_negative), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(),
                                getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    // ── Diálogo: Retirar producto ──

    /**
     * Diálogo de confirmación antes de retirar un producto del almacén.
     * El producto pasa a la lista de retirados en el Controlador.
     *
     * @param productCode código del producto a retirar
     */
    private void showWithdrawDialog(String productCode) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_withdraw_title))
                .setMessage(getString(R.string.dialog_withdraw_msg, productCode))
                .setPositiveButton(getString(R.string.btn_withdraw), (dialog, which) -> {
                    boolean ok = Controller.getSingleton().withdrawProduct(productCode);
                    if (ok) {
                        refreshList();
                        Toast.makeText(requireContext(),
                                getString(R.string.toast_withdrawn), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                getString(R.string.error_not_found), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    // ── Helpers ──

    /** Notifica al adapter que el dataset cambió y actualiza el estado vacío. */
    private void refreshList() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    /** Muestra u oculta el estado vacío según si hay productos. */
    private void updateEmptyState() {
        boolean isEmpty = Controller.getSingleton().getProductList() == null
                || Controller.getSingleton().getProductList().isEmpty();
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
