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
 * SettingsFragment — View for data persistence settings.
 * <p>
 * MVC responsibility:
 * Manages the save/load UI (buttons, feedback). Delegates file picking
 * to {@link FilePickerManager} and data operations to the Controller.
 * Never accesses DataAccess or DatabaseAccess directly.
 * <p>
 * Save flow (file):
 * btnSaveFile → FilePickerManager.openSavePicker()
 * → [user picks location] → callback → FilePickerManager.writeToUri()
 * <p>
 * Load flow (file):
 * btnLoadFile → FilePickerManager.openLoadPicker()
 * → [user picks file] → callback → Controller.loadProductListFromJson()
 * <p>
 * Load flow (DB):
 * btnLoadDB → ExecutorService → Controller.loadProductListFromDb()
 * → runOnUiThread → Snackbar feedback
 * <p>
 * Note on "Save to DB":
 * There is no bulk save operation anymore. Every mutating action
 * (add, edit stock, withdraw) is persisted to the DB immediately
 * by the Controller at the time it occurs. The save button has
 * been removed from the layout accordingly.
 */
public class SettingsFragment extends Fragment {

    /**
     * Single background thread for DB network operations.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FragmentSettingsBinding binding;
    private FilePickerManager filePickerManager;

    public SettingsFragment() {
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Launchers must be registered before onStart()
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

        binding.btnSaveFile.setOnClickListener(v -> openSavePicker());
        binding.btnLoadFile.setOnClickListener(v -> openLoadPicker());
        binding.btnLoadDB.setOnClickListener(v -> loadFromDatabase());

        // btnSaveDB has been removed from the layout:
        // saves are now performed per-operation inside the Controller.
    }

    // ── File: load ────────────────────────────────────────────────────────────

    /**
     * Opens the SAF picker to choose a JSON file to load.
     * Only JSON and plain-text files are shown.
     */
    private void openLoadPicker() {
        filePickerManager.openLoadPicker(uri -> {
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

    // ── File: save ────────────────────────────────────────────────────────────

    /**
     * Opens the SAF picker to choose where to save the current product list.
     * The suggested filename is "products.json".
     */
    private void openSavePicker() {
        filePickerManager.openSavePicker("products.json", uri -> {
            String json = new Gson().toJson(Controller.getSingleton().listProducts());
            boolean ok = filePickerManager.writeToUri(requireContext(), uri, json);
            showSnackbar(
                ok ? getString(R.string.toast_file_saved)
                    : getString(R.string.error_file_save),
                !ok);
        });
    }

    // ── DB: load ──────────────────────────────────────────────────────────────

    /**
     * Loads active and retired product lists from the remote DB.
     * <p>
     * The operation runs on a background thread because
     * {@link Controller#loadProductListFromDb()} performs a blocking HTTP request.
     * UI feedback is posted back on the main thread.
     */
    private void loadFromDatabase() {
        executor.execute(() -> {
            boolean ok = Controller.getSingleton().loadProductListFromDb()
                && Controller.getSingleton().loadRetiredListFromDb();

            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() ->
                showSnackbar(
                    ok ? getString(R.string.toast_file_loaded)
                        : getString(R.string.error_file_load),
                    !ok));
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Displays a short Snackbar with optional error styling.
     *
     * @param message text to show
     * @param isError if {@code true}, applies the error background color
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
