package com.hotguy.warehouse13.controller;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotguy.warehouse13.databinding.ItemListBinding;
import com.hotguy.warehouse13.model.Product;


public class ProductsRVHolder extends RecyclerView.ViewHolder {

    private final TextView txtCode;
    private final TextView txtDesc;
    private final TextView txtPrice;
    private final TextView txtStock;

    public ProductsRVHolder(@NonNull View itemView) {
        super(itemView);
        ItemListBinding binding = ItemListBinding.bind(itemView);
        this.txtCode = binding.txtCode;
        this.txtDesc = binding.txtDesc;
        this.txtPrice = binding.textPrice;
        this.txtStock = binding.txtStock;
    }

    public void loadData(Product p) {
        txtCode.append(": " + p.getProductCode());
        txtDesc.append(": " + p.getDescription());
        txtPrice.append(": " + p.getPrice());
        txtStock.append(": " + p.getStock());

    }

}
