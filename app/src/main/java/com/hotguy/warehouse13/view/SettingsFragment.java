package com.hotguy.warehouse13.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.hotguy.warehouse13.R;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.controller.FilePickerManager;
import com.hotguy.warehouse13.databinding.FragmentSettingsBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SettingsFragment — Vista de configuración y persistencia de datos.
 * <p>
 * Responsabilidad MVC:
 * · Gestiona la UI de guardar/cargar (botones, feedback).
 * · Delega la apertura del selector a FilePickerManager.
 * · Cuando recibe el Uri (callback), llama al Controller para
 * leer o escribir los datos. Nunca toca DataAccess directamente.
 * <p>
 * Flujo guardar:
 * btnSave → FilePickerManager.openSavePicker()
 * → [usuario elige ubicación en el selector del sistema]
 * → callback onSaveLocationPicked(uri)
 * → FilePickerManager.writeToUri(uri, json)   ← escribe el fichero
 * → showSnackbar()
 * <p>
 * Flujo cargar:
 * btnLoad → FilePickerManager.openLoadPicker()
 * → [usuario elige fichero en el selector del sistema]
 * → callback onLoadFilePicked(uri)
 * → FilePickerManager.readFromUri(uri)         ← lee el fichero
 * → Controller.loadProductListFromJson(json)   ← parsea y carga
 * → showSnackbar()
 * <p>
 * ¿Por qué FilePickerManager se crea en onCreate() y no en onCreateView()?
 * · Los ActivityResultLaunchers deben registrarse ANTES de onStart().
 * · onCreate() es el momento correcto y seguro.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FilePickerManager filePickerManager;

    public SettingsFragment() {
    }

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // IMPORTANTE: los launchers se registran aquí, antes de onCreateView
        filePickerManager = new FilePickerManager(this);
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
        binding.btnSaveFile.setOnClickListener(v -> openSavePicker());

        // ── Cargar desde fichero ──
        binding.btnLoadFile.setOnClickListener(v -> openLoadPicker());

        // ── Guardar en BD remota ──
        binding.btnSaveDB.setOnClickListener(v -> saveToDatabase());

        // ── Cargar desde BD remota ──
        binding.btnLoadDB.setOnClickListener(v -> loadToDatabase()) ;
    }

    // ── Cargar ───────────────────────────────────────────────────────────────

    /**
     * Abre el selector SAF para elegir qué fichero cargar.
     * Solo muestra ficheros JSON y de texto plano.
     */
    private void openLoadPicker() {
        filePickerManager.openLoadPicker(uri -> {
            // Este callback llega cuando el usuario elige el fichero
            String json = filePickerManager.readFromUri(requireContext(), uri);
            if (json == null || json.isEmpty()) {
                showSnackbar(getString(R.string.error_file_load), true);
                return;
            }
            boolean ok = Controller.getSingleton().loadProductListFromJson(json);
            showSnackbar(
                ok ? getString(R.string.toast_file_loaded)
                    : getString(R.string.error_file_load),
                !ok);
        });
    }

    // ── Guardar ─────────────────────────────────────────────────────────────

    /**
     * Abre el selector SAF para elegir dónde guardar.
     * El nombre sugerido es "products.json" pero el usuario puede cambiarlo.
     */
    private void openSavePicker() {
        filePickerManager.openSavePicker("products.json", uri -> {
            // Este callback llega cuando el usuario confirma la ubicación
            String json = new Gson().toJson(Controller.getSingleton().listProducts());
            boolean ok = filePickerManager.writeToUri(requireContext(), uri, json);
            showSnackbar(
                ok ? getString(R.string.toast_file_saved)
                    : getString(R.string.error_file_save),
                !ok);
        });
    }

    private void loadToDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean ok = Controller.getSingleton().loadProductListFromDb() && Controller.getSingleton().loadRetiredListFromDb();
            // Volver al hilo UI para el feedback
            requireActivity().runOnUiThread(() ->
                showSnackbar(
                    ok ? "Guardado en BD" : "Error al guardar en BD",
                    !ok));
        });
    }

    private void saveToDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean ok = Controller.getSingleton().saveProductListToDb() && Controller.getSingleton().saveRetiredListToDb();
            // Volver al hilo UI para el feedback
            requireActivity().runOnUiThread(() ->
                showSnackbar(
                    ok ? "Guardado en BD" : "Error al guardar en BD",
                    !ok));
        });
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
        if (getView() == null) return;
        Snackbar snack = Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT);
        if (isError) {
            snack.setBackgroundTint(requireContext().getColor(R.color.colorError));
        }
        snack.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
