package com.hotguy.warehouse13.controller;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * FilePickerManager — Gestiona el selector de ficheros del sistema (SAF).
 * <p>
 * ¿Por qué SAF (Storage Access Framework)?
 * · No necesita permisos de almacenamiento en el Manifest.
 * · El sistema operativo muestra su propio selector nativo.
 * · El usuario decide libremente dónde guardar o cargar.
 * · Compatible con Android 5.0+ y con Scoped Storage (Android 10+).
 * <p>
 * ¿Por qué el ActivityResultLauncher vive aquí y no en el Fragment?
 * · Técnicamente Android obliga a registrarlo en el Fragment/Activity.
 * · Esta clase recibe el Fragment en el constructor y registra
 * los launchers en su nombre, centralizando toda la lógica SAF.
 * <p>
 * Patrón de comunicación:
 * · Igual que OnEditStockListener en ProductsRVAdapter, usamos
 * interfaces callback para notificar el resultado al Fragment
 * sin que esta clase tenga que conocer la implementación concreta.
 * <p>
 * Responsabilidad MVC:
 * · Esta clase pertenece a 'controller': no es lógica de negocio
 * (eso es del Controller/DataAccess) ni es UI (eso es del Fragment).
 * Es infraestructura de acceso a datos — igual que DataAccess.
 */
public class FilePickerManager {

    // ── Interfaces callback ──────────────────────────────────────────────────

    private final ActivityResultLauncher<String> saveLauncher;
    private final ActivityResultLauncher<String[]> loadLauncher;

    // ── Estado ──────────────────────────────────────────────────────────────
    private OnSaveLocationPicked saveCallback;
    private OnLoadFilePicked loadCallback;

    /**
     * Registra los ActivityResultLaunchers en el Fragment dado.
     * IMPORTANTE: debe llamarse en onCreate() o onAttach() del Fragment,
     * antes de onStart(). Android lanza IllegalStateException si se
     * registra después.
     *
     * @param fragment Fragment que contiene los botones de guardar/cargar.
     */
    public FilePickerManager(Fragment fragment) {

        // Launcher para GUARDAR: abre el selector "Guardar como..."
        // ACTION_CREATE_DOCUMENT — el usuario elige nombre y ubicación
        saveLauncher = fragment.registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null && saveCallback != null) {
                    saveCallback.onSaveLocationPicked(uri);
                }
            });

        // Launcher para CARGAR: abre el selector "Abrir fichero..."
        // ACTION_OPEN_DOCUMENT — el usuario elige el fichero existente
        loadLauncher = fragment.registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null && loadCallback != null) {
                    loadCallback.onLoadFilePicked(uri);
                }
            });
    }

    /**
     * Abre el selector del sistema para elegir dónde guardar.
     * El resultado llega de forma asíncrona en el callback.
     *
     * @param suggestedName Nombre de fichero sugerido (ej: "products.json")
     * @param callback      Quién recibe el Uri cuando el usuario confirma
     */
    public void openSavePicker(String suggestedName, OnSaveLocationPicked callback) {
        this.saveCallback = callback;
        saveLauncher.launch(suggestedName);
    }

    // ── Constructor ─────────────────────────────────────────────────────────

    /**
     * Abre el selector del sistema para elegir qué fichero cargar.
     * El resultado llega de forma asíncrona en el callback.
     *
     * @param callback Quién recibe el Uri cuando el usuario confirma
     */
    public void openLoadPicker(OnLoadFilePicked callback) {
        this.loadCallback = callback;
        loadLauncher.launch(new String[]{"application/json", "text/plain"});
    }

    // ── API pública ──────────────────────────────────────────────────────────

    /**
     * Escribe un String JSON en el Uri elegido por el usuario.
     * Llamado desde el Fragment tras recibir el Uri del saveLauncher.
     *
     * @param context  Context para abrir el OutputStream
     * @param uri      Uri devuelto por el selector SAF
     * @param jsonData Contenido JSON a escribir
     * @return true si se guardó correctamente
     */
    public boolean writeToUri(Context context, Uri uri, String jsonData) {
        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (out == null) return false;
            out.write(jsonData.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lee el contenido de un Uri elegido por el usuario.
     * Llamado desde el Fragment tras recibir el Uri del loadLauncher.
     *
     * @param context Context para abrir el InputStream
     * @param uri     Uri devuelto por el selector SAF
     * @return contenido del fichero como String, o null si falla
     */
    public String readFromUri(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Helpers de I/O con Uri ───────────────────────────────────────────────

    /**
     * Notifica al Fragment que el usuario ha elegido dónde GUARDAR.
     * El Fragment recibirá el Uri y llamará al Controller para escribir.
     */
    public interface OnSaveLocationPicked {
        void onSaveLocationPicked(Uri uri);
    }

    /**
     * Notifica al Fragment que el usuario ha elegido qué fichero CARGAR.
     * El Fragment recibirá el Uri y llamará al Controller para leer.
     */
    public interface OnLoadFilePicked {
        void onLoadFilePicked(Uri uri);
    }
}
