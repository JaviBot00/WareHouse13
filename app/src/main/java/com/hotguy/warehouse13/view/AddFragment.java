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

/**
 * AddFragment — Vista para añadir productos al almacén.
 * <p>
 * Responsabilidad MVC:
 * · Recoge datos del usuario desde los campos del formulario.
 * · Construye un JSON y delega al Controlador (nunca toca el Modelo).
 * · Muestra feedback al usuario según el resultado.
 * <p>
 * Funcionalidades:
 * · Toggle para indicar si el producto es perecedero.
 * · Campo de fecha de caducidad que aparece/desaparece según el toggle.
 * · Validaciones básicas de campos vacíos antes de llamar al Controlador.
 * · Limpieza del formulario tras un añadido exitoso.
 */
public class AddFragment extends Fragment {

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

        // ── Toggle perecedero: mostrar/ocultar campo caducidad ──
        binding.switchPerishable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.tilExpiration.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                // Limpiar el campo si se desactiva
                if (binding.editExpiration.getText() != null)
                    binding.editExpiration.getText().clear();
                binding.tilExpiration.setError(null);
            }
        });

        // ── Botón Añadir ──
        binding.btnAdd.setOnClickListener(v -> attemptAddProduct());
    }

    /**
     * Recoge y valida los campos del formulario.
     * Si todo es correcto, construye el JSON y llama al Controlador.
     * <p>
     * PREGUNTA GUIADA:
     * ¿Por qué usamos un LinkedHashMap + Gson en vez de construir
     * el String JSON a mano con concatenación? Piénsalo antes de seguir.
     */
    private void attemptAddProduct() {
        // Limpiar errores previos
        clearErrors();

        String code = getText(binding.editCode);
        String description = getText(binding.editDesc);
        String priceStr = getText(binding.editPrice);
        String stockStr = getText(binding.editStock);
        boolean isPerishable = binding.switchPerishable.isChecked();
        String expiration = isPerishable ? getText(binding.editExpiration) : "";

        // ── Validaciones de campo vacío ──
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

        if (!valid)
            return;

        // ── Conversión de tipos ──
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

        // ── Construcción del JSON para el Controlador ──
        // La Vista NO crea objetos del Modelo: usa un Map + Gson
        // y delega la creación del objeto al Controlador.
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productCode", code.toUpperCase());
        data.put("description", description);
        data.put("price", price);
        data.put("stock", stock);
        if (isPerishable) {
            data.put("expirationDate", expiration);
        }

        String jsonProduct = new Gson().toJson(data);

        // ── Llamada al Controlador ──
        boolean added = Controller.getSingleton().addProduct(isPerishable, jsonProduct);

        if (added) {
            Toast.makeText(requireContext(),
                    getString(R.string.toast_item_added), Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            // El Controlador devuelve false si el código ya existe
            binding.tilCode.setError(getString(R.string.error_duplicate_code));
        }
    }

    // ── Helpers ──

    /** Extrae texto de un EditText y elimina espacios sobrantes. */
    private String getText(android.widget.EditText edit) {
        return edit != null && edit.getText() != null
                ? edit.getText().toString().trim()
                : "";
    }

    /** Elimina todos los mensajes de error de los campos. */
    private void clearErrors() {
        binding.tilCode.setError(null);
        binding.tilDesc.setError(null);
        binding.tilPrice.setError(null);
        binding.tilStock.setError(null);
        binding.tilExpiration.setError(null);
    }

    /** Limpia el formulario tras un añadido exitoso. */
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
