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

import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.controller.ProductsRVAdapter;
import com.hotguy.warehouse13.databinding.FragmentFiltersBinding;
import com.hotguy.warehouse13.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * FiltersFragment — Vista de filtros sobre el inventario.
 * <p>
 * Responsabilidad MVC:
 * · Cada chip corresponde a una operación del Controlador.
 * · Recibe el JSON de vuelta y lo muestra en el RecyclerView.
 * · No interpreta el JSON: lo pasa directamente al Adapter.
 * <p>
 * PREGUNTA GUIADA:
 * Observa que el Controlador devuelve un String JSON en todos los
 * métodos de listado. ¿Por qué crees que tiene sentido eso en vez
 * de devolver List<Product>? (Pista: piensa en qué capas pueden
 * ver el Modelo.)
 */
public class FiltersFragment extends Fragment {

    private FragmentFiltersBinding binding;

    // Adapter compartido para todos los filtros
    // Lo reinicializamos con una lista nueva en cada filtro
    private ProductsRVAdapter adapter;

    public FiltersFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFiltersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupChips();
        // Aplicar filtro "Todos" por defecto al entrar
        applyAllFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar cuando volvemos (por si se añadieron productos)
        applyCurrentChip();
    }

    private void setupRecyclerView() {
        adapter = new ProductsRVAdapter(new ArrayList<>(), null, null);
        binding.rvFiltered.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFiltered.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Ocultar panel de precios por defecto
            binding.cardPriceRange.setVisibility(View.GONE);

            if (checkedIds.isEmpty())
                return;
            int id = checkedIds.get(0);

            if (id == binding.chipAll.getId()) {
                applyAllFilter();
            } else if (id == binding.chipNoStock.getId()) {
                applyNoStockFilter();
            } else if (id == binding.chipExpired.getId()) {
                applyExpiredFilter();
            } else if (id == binding.chipPrices.getId()) {
                binding.cardPriceRange.setVisibility(View.VISIBLE);
                // No aplicamos hasta que el usuario pulse "Aplicar"
            } else if (id == binding.chipWithdrawn.getId()) {
                applyWithdrawnFilter();
            }
        });

        // Botón aplicar filtro de precios
        binding.btnApplyPrices.setOnClickListener(v -> applyPriceRangeFilter());
    }

    // ── Métodos de filtrado ──

    private void applyAllFilter() {
        showResults(Controller.getSingleton().listProducts());
    }

    private void applyNoStockFilter() {
        showResults(Controller.getSingleton().listProductsNoStock());
    }

    private void applyExpiredFilter() {
        showResults(Controller.getSingleton().listExpiredProducts());
    }

    private void applyWithdrawnFilter() {
        showResults(Controller.getSingleton().listWithdrawnProducts());
    }

    private void applyPriceRangeFilter() {
        String minStr = getText(binding.editMinPrice);
        String maxStr = getText(binding.editMaxPrice);

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            Toast.makeText(requireContext(),
                getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double min = Double.parseDouble(minStr);
            double max = Double.parseDouble(maxStr);

            if (min > max) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_price_range), Toast.LENGTH_SHORT).show();
                return;
            }

            showResults(Controller.getSingleton().listProductsBetweenPrices(min, max));

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                getString(R.string.error_number_format), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Vuelve a aplicar el chip activo (para onResume).
     */
    private void applyCurrentChip() {
        int checkedId = binding.chipGroup.getCheckedChipId();
        if (checkedId == binding.chipAll.getId())
            applyAllFilter();
        else if (checkedId == binding.chipNoStock.getId())
            applyNoStockFilter();
        else if (checkedId == binding.chipExpired.getId())
            applyExpiredFilter();
        else if (checkedId == binding.chipWithdrawn.getId())
            applyWithdrawnFilter();
        // Precio no se reaplica automáticamente (requiere input del usuario)
    }

    /**
     * Recibe el JSON del Controlador, cuenta los resultados
     * y actualiza el adapter con la nueva lista.
     * <p>
     * La Vista no construye objetos del Modelo: el Adapter sabe
     * interpretar el JSON internamente.
     */
    private void showResults(List<Product> list) {
        if (list == null || list.isEmpty()) {
            updateCount(0);
            adapter.updateData(new ArrayList<>());
            return;
        }
        // El adapter se recarga con el JSON → la lista es del Controlador
        updateCount(list.size());
        adapter.updateData(list);
        adapter.notifyDataSetChanged();
    }

    private void updateCount(int count) {
        binding.txtResultCount.setText(
            getResources().getQuantityString(R.plurals.results_count, count, count));
    }

    private String getText(android.widget.EditText edit) {
        return edit != null && edit.getText() != null
            ? edit.getText().toString().trim()
            : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
