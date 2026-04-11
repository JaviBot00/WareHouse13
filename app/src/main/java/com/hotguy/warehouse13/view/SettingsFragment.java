package com.hotguy.warehouse13.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.databinding.FragmentSettingsBinding;

/**
 * SettingsFragment — Vista de configuración y persistencia de datos.
 * <p>
 * Responsabilidad MVC:
 * · Delega guardar/cargar al Controlador, que a su vez usa DataAccess.
 * · Muestra feedback de éxito o error con Snackbar.
 * · La sección de BD es placeholder visual (botón deshabilitado).
 * <p>
 * NOTA sobre permisos:
 * La app usa context.getFilesDir() (almacenamiento interno privado),
 * que NO necesita permisos en tiempo de ejecución en ninguna versión
 * de Android. Los permisos READ/WRITE_EXTERNAL_STORAGE en el Manifest
 * están preparados solo para si en el futuro se quiere acceder al
 * almacenamiento externo (Downloads, SD card, etc.).
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Guardar en fichero ──
        binding.btnSaveFile.setOnClickListener(v -> saveToFile());

        // ── Cargar desde fichero ──
        binding.btnLoadFile.setOnClickListener(v -> loadFromFile());

        // ── BD remota: solo placeholder, botón deshabilitado ──
        // Se habilitará cuando se implemente la conexión MySQL
        binding.btnConnectDb.setEnabled(false);
    }

    // ── Guardar ──

    private void saveToFile() {
        showProgress(true);
        try {
            Controller.getSingleton().saveProductList(requireContext());
            showProgress(false);
            showSnackbar(getString(R.string.toast_file_saved), false);
        } catch (Exception e) {
            showProgress(false);
            showSnackbar(getString(R.string.error_file_save), true);
        }
    }

    // ── Cargar ──

    private void loadFromFile() {
        showProgress(true);
        try {
            Controller.getSingleton().loadProductList(requireContext());
            showProgress(false);
            showSnackbar(getString(R.string.toast_file_loaded), false);
        } catch (Exception e) {
            showProgress(false);
            showSnackbar(getString(R.string.error_file_load), true);
        }
    }

    // ── Helpers ──

    private void showProgress(boolean show) {
        binding.progressFile.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Muestra un Snackbar de feedback.
     *
     * @param message texto a mostrar
     * @param isError si true, usa color de error
     */
    private void showSnackbar(String message, boolean isError) {
        if (getView() == null)
            return;
        Snackbar snack = Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT);
        if (isError) {
            snack.setBackgroundTint(
                    requireContext().getColor(R.color.colorError));
        }
        snack.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
