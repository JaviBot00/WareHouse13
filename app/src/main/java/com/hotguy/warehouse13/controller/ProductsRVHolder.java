package com.hotguy.warehouse13.controller;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.databinding.ItemProductCardBinding;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ProductsRVHolder — ViewHolder para las cards de producto.
 * <p>
 * BUG CORREGIDO:
 * El código anterior usaba append() para rellenar los TextViews.
 * Eso causa acumulación de texto al reutilizarse el ViewHolder
 * (el RecyclerView reutiliza vistas para ahorro de memoria).
 * SIEMPRE usar setText() para sobrescribir el valor anterior.
 * <p>
 * Muestra condicionalmente la fila de caducidad para PerishableProduct.
 * El chip de tipo indica visualmente si es perecedero o no.
 */
public class ProductsRVHolder extends RecyclerView.ViewHolder {

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ItemProductCardBinding binding;
    private final ProductsRVAdapter.OnEditStockListener editStockListener;
    private final ProductsRVAdapter.OnWithdrawListener withdrawListener;

    public ProductsRVHolder(@NonNull View itemView,
            ProductsRVAdapter.OnEditStockListener editStockListener,
            ProductsRVAdapter.OnWithdrawListener withdrawListener) {
        super(itemView);
        binding = ItemProductCardBinding.bind(itemView);
        this.editStockListener = editStockListener;
        this.withdrawListener = withdrawListener;
    }

    /**
     * Rellena la card con los datos del producto.
     * <p>
     * IMPORTANTE: usamos setText() y NO append().
     * append() acumula texto en cada reutilización del ViewHolder.
     *
     * @param p producto a mostrar (puede ser PerishableProduct)
     */
    public void loadData(Product p) {
        // ── Campos comunes ──
        binding.txtDescription.setText(p.getDescription());
        binding.txtCode.setText(p.getProductCode());
        binding.txtPrice.setText(String.format("%.2f €", p.getPrice()));
        binding.txtStock.setText(String.valueOf(p.getStock()));

        // ── Chip de tipo ──
        boolean isPerishable = p instanceof PerishableProduct;
        binding.chipType.setText(isPerishable
                ? itemView.getContext().getString(R.string.type_perishable)
                : itemView.getContext().getString(R.string.type_standard));

        // ── Fila caducidad (solo perecedero) ──
        if (isPerishable) {
            binding.rowExpiration.setVisibility(View.VISIBLE);
            String rawDate = ((PerishableProduct) p).getExpirationDate();
            try {
                LocalDate date = LocalDate.parse(rawDate, INPUT_FORMAT);
                binding.txtExpiration.setText(date.format(DISPLAY_FORMAT));

                // Colorear en rojo si ya está caducado
                boolean expired = date.isBefore(LocalDate.now());
                int color = itemView.getContext().getColor(
                        expired
                                ? R.color.colorError
                                : R.color.colorWarning);
                binding.txtExpiration.setTextColor(color);
            } catch (Exception e) {
                binding.txtExpiration.setText(rawDate);
            }
        } else {
            binding.rowExpiration.setVisibility(View.GONE);
        }

        // ── Botones de acción ──
        if (editStockListener != null) {
            binding.btnEditStock.setOnClickListener(v -> editStockListener.onEditStock(p.getProductCode()));
        } else {
            binding.btnEditStock.setVisibility(View.GONE);
        }

        if (withdrawListener != null) {
            binding.btnWithdraw.setOnClickListener(v -> withdrawListener.onWithdraw(p.getProductCode()));
        } else {
            binding.btnWithdraw.setVisibility(View.GONE);
        }
    }
}
