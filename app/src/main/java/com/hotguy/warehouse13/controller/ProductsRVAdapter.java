package com.hotguy.warehouse13.controller;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.model.Product;

import java.util.List;

public class ProductsRVAdapter extends RecyclerView.Adapter<ProductsRVHolder> {

    private final List<Product> myProductsList;

    public ProductsRVAdapter(List<Product> myProductsList) {
        this.myProductsList = myProductsList;
    }

    @NonNull
    @Override
    public ProductsRVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductsRVHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsRVHolder holder, int position) {
        holder.loadData(myProductsList.get(position));
    }

    @Override
    public int getItemCount() {
        return myProductsList.size();
    }
}
