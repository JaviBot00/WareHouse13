package com.hotguy.warehouse13.controller;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// ── JAVA NATIVO (sustituido por OkHttp) ──────────────────────────────────────
// import java.io.OutputStream;
// import java.net.HttpURLConnection;
// import java.net.URL;
// import java.util.Scanner;

/**
 * Capa HTTP pura: sabe CÓMO hacer peticiones, no QUÉ pedir.
 * <p>
 * Responsabilidad única: abrir conexiones, escribir bodies y leer
 * respuestas. No conoce endpoints, parámetros de negocio ni JSON.
 * <p>
 * Todos los métodos son estáticos y bloqueantes.
 * El llamador ({@link DatabaseAccess}) debe invocarlos desde un hilo
 * en segundo plano.
 * <p>
 * Implementación activa: OkHttp 4.x<br>
 * Implementación alternativa: {@code HttpURLConnection} (comentada).
 */
public class HttpClient {

    private static final String TAG = "HttpClient";

    /** Tipo de contenido para formularios URL-encoded. */
    private static final MediaType FORM =
        MediaType.get("application/x-www-form-urlencoded");

    // ── JAVA NATIVO ──
    // No hay instancia compartida; cada llamada abría su propia conexión.

    /** Instancia compartida de OkHttp. Thread-safe; reutilizar siempre. */
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * GET — obtiene el cuerpo de la respuesta como String.
     *
     * @param url URL completa del recurso
     * @return cuerpo de la respuesta
     * @throws IOException si la conexión falla o el servidor devuelve error
     */
    public static String get(String url) throws IOException {

        // ── OkHttp ──
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        return execute(request);

        // ── JAVA NATIVO ──
        // HttpURLConnection conn = openNative(url, "GET");
        // return readNative(conn);
    }

    /**
     * POST — envía un formulario URL-encoded y devuelve la respuesta.
     *
     * @param url      URL completa del recurso
     * @param formBody parámetros URL-encoded (ej: {@code "code=ABC&price=9.99"})
     * @return cuerpo de la respuesta
     * @throws IOException si la conexión falla o el servidor devuelve error
     */
    public static String post(String url, String formBody) throws IOException {

        // ── OkHttp ──
        Request request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(formBody, FORM))
            .build();
        return execute(request);

        // ── JAVA NATIVO ──
        // HttpURLConnection conn = openNative(url, "POST");
        // writeNative(conn, formBody);
        // return readNative(conn);
    }

    /**
     * PUT — envía un formulario URL-encoded y devuelve la respuesta.
     *
     * @param url      URL completa del recurso
     * @param formBody parámetros URL-encoded
     * @return cuerpo de la respuesta
     * @throws IOException si la conexión falla o el servidor devuelve error
     */
    public static String put(String url, String formBody) throws IOException {

        // ── OkHttp ──
        Request request = new Request.Builder()
            .url(url)
            .put(RequestBody.create(formBody, FORM))
            .build();
        return execute(request);

        // ── JAVA NATIVO ──
        // HttpURLConnection conn = openNative(url, "PUT");
        // writeNative(conn, formBody);
        // return readNative(conn);
    }

    // ── Privado: ejecución OkHttp ─────────────────────────────────────────────

    /**
     * Ejecuta la {@link Request} y devuelve el cuerpo como String.
     * Lanza {@link IOException} si la respuesta no es exitosa (HTTP 2xx).
     */
    private static String execute(Request request) throws IOException {
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " — " + request.url());
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    // ── Privado: implementación Java nativo (comentada) ───────────────────────

    // /**
    //  * Abre una HttpURLConnection con timeout de 5 s.
    //  */
    // private static HttpURLConnection openNative(String url, String method)
    //         throws IOException {
    //     HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    //     conn.setRequestMethod(method);
    //     conn.setConnectTimeout(5000);
    //     conn.setReadTimeout(5000);
    //     boolean hasBody = method.equals("POST") || method.equals("PUT");
    //     if (hasBody) {
    //         conn.setDoOutput(true);
    //         conn.setRequestProperty("Content-Type",
    //                 "application/x-www-form-urlencoded");
    //     }
    //     return conn;
    // }

    // /**
    //  * Escribe el body URL-encoded en el OutputStream de la conexión.
    //  */
    // private static void writeNative(HttpURLConnection conn, String formBody)
    //         throws IOException {
    //     byte[] bytes = formBody.getBytes(StandardCharsets.UTF_8);
    //     conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
    //     try (OutputStream out = conn.getOutputStream()) {
    //         out.write(bytes);
    //     }
    // }

    // /**
    //  * Lee el InputStream completo y lo devuelve como String.
    //  */
    // private static String readNative(HttpURLConnection conn) throws IOException {
    //     try (Scanner scanner = new Scanner(
    //             conn.getInputStream(), StandardCharsets.UTF_8)) {
    //         scanner.useDelimiter("\\A");
    //         return scanner.hasNext() ? scanner.next() : "";
    //     } finally {
    //         conn.disconnect();
    //     }
    // }
}
