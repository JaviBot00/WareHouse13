package com.hotguy.warehouse13.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.model.Product;

import java.util.List;

/**
 * ProductsRVAdapter — Adapter del RecyclerView de productos.
 * <p>
 * Responsabilidad MVC:
 * · Vive en el paquete controller porque actúa de puente entre
 * Vista y datos, igual que en el patrón estándar de Android.
 * · No accede al Controlador directamente: recibe la List<Product>
 * ya obtenida por el Fragment, y notifica acciones de vuelta
 * al Fragment mediante listeners.
 * <p>
 * Listeners opcionales (pueden ser null para listas de solo lectura):
 * · OnEditStockListener → Fragment lanza el diálogo de editar stock
 * · OnWithdrawListener → Fragment lanza el diálogo de retirar
 */
public class ProductsRVAdapter extends RecyclerView.Adapter<ProductsRVHolder> {

    // ── Interfaces para los listeners de acción ──

    public interface OnEditStockListener {
        void onEditStock(String productCode);
    }

    public interface OnWithdrawListener {
        void onWithdraw(String productCode);
    }

    // ── Estado ──

    private List<Product> productList;
    private final OnEditStockListener editStockListener;
    private final OnWithdrawListener withdrawListener;

    // ── Constructor ──

    public ProductsRVAdapter(List<Product> productList,
            OnEditStockListener editStockListener,
            OnWithdrawListener withdrawListener) {
        this.productList = productList;
        this.editStockListener = editStockListener;
        this.withdrawListener = withdrawListener;
    }

    // ── Ciclo RecyclerView ──

    @NonNull
    @Override
    public ProductsRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductsRVHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_product_card, parent, false),
                editStockListener,
                withdrawListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsRVHolder holder, int position) {
        holder.loadData(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    // ── Actualización de datos ──

    /**
     * Reemplaza la lista del adapter.
     * El Fragment llama a notifyDataSetChanged() después de esto.
     */
    public void updateData(List<Product> newList) {
        this.productList = newList;
    }
}
