package com.hotguy.warehouse13.controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente de la API REST del servidor Warehouse13.
 * <p>
 * Responsabilidad única: sabe QUÉ pedir (endpoints, parámetros,
 * interpretación de respuestas JSON). No sabe CÓMO hacer HTTP —
 * eso lo delega en {@link HttpClient}.
 * <p>
 * Todos los métodos son estáticos y bloqueantes; el llamador
 * ({@link Controller}) debe invocarlos desde un hilo en segundo plano.
 */
public class DatabaseAccess {

    private static final String TAG = "DatabaseAccess";

    /**
     * URL base del servidor Tomcat con los Servlets.
     * Sustituye la IP por la del equipo que ejecuta Docker.
     * Ejemplo: {@code "http://192.168.1.50:8080/WareHouse13-Servlets"}
     */
    private static final String BASE_URL = "http://192.168.56.79:8080/WareHouse13-Servlets";

    private static final Gson GSON = new Gson();

    // ── Endpoints ─────────────────────────────────────────────────────────────

    private static final String EP_LIST_ACTIVE = "/listar-activos";
    private static final String EP_LIST_RETIRED = "/listar-retirados";
    private static final String EP_INSERT = "/insertar";
    private static final String EP_UPDATE = "/actualizar";
    private static final String EP_RETIRE = "/retirar";
    private static final String EP_UNRETIRE = "/reactivar";
    private static final String EP_DELETE = "/eliminar";

    // ── Carga ─────────────────────────────────────────────────────────────────

    /**
     * Obtiene todos los productos activos del servidor.
     *
     * @return lista de productos activos; vacía si la petición falla
     */
    public static List<Product> loadProductList() {
        return fetchProductList(EP_LIST_ACTIVE);
    }

    /**
     * Obtiene todos los productos retirados del servidor.
     *
     * @return lista de productos retirados; vacía si la petición falla
     */
    public static List<Product> loadRetiredList() {
        return fetchProductList(EP_LIST_RETIRED);
    }

    // ── Insertar ──────────────────────────────────────────────────────────────

    /**
     * Inserta un producto nuevo en el servidor.
     * Admite {@link PerishableProduct}: si el producto tiene fecha de
     * caducidad, se añade el parámetro {@code expirationDate}.
     *
     * @param product producto a insertar
     * @return {@code true} si el servidor responde con {@code "status":"OK"}
     */
    public static boolean insertProduct(Product product) {
        try {
            StringBuilder params = new StringBuilder();
            params.append("code=").append(encode(product.getCode()));
            params.append("&description=").append(encode(product.getDescription()));
            params.append("&price=").append(product.getPrice());
            params.append("&stock=").append(product.getStock());

            if (product instanceof PerishableProduct) {
                params.append("&expirationDate=")
                    .append(encode(((PerishableProduct) product).getExpirationDate()));
            }

            String response = HttpClient.post(BASE_URL + EP_INSERT, params.toString());
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "insertProduct failed", e);
            return false;
        }
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos de un producto existente en el servidor.
     * Se llama tras modificar el stock en memoria para mantener la BD en sync.
     *
     * @param product producto con los nuevos valores ya aplicados
     * @return {@code true} si el servidor responde con {@code "status":"OK"}
     */
    public static boolean updateProduct(Product product) {
        try {
            String params = "code=" + encode(product.getCode())
                + "&description=" + encode(product.getDescription())
                + "&price=" + product.getPrice()
                + "&stock=" + product.getStock();

            String response = HttpClient.put(BASE_URL + EP_UPDATE, params);
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "updateProduct failed", e);
            return false;
        }
    }

    // ── Retirar ───────────────────────────────────────────────────────────────

    /**
     * Marca un producto como retirado en el servidor.
     * Se llama tras moverlo a la lista de retirados en memoria.
     *
     * @param productCode código del producto a retirar
     * @return {@code true} si el servidor responde con {@code "status":"OK"}
     */
    public static boolean retireProduct(String productCode) {
        try {
            String params = "code=" + encode(productCode);
            String response = HttpClient.put(BASE_URL + EP_RETIRE, params);
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "retireProduct failed", e);
            return false;
        }
    }

    // ── Privado: parsing de listas ────────────────────────────────────────────

    /**
     * Ejecuta un GET al endpoint dado y parsea el array JSON de vuelta
     * a una lista de {@link Product} o {@link PerishableProduct}.
     * Distingue el tipo por la presencia del campo {@code expirationDate}.
     *
     * @param endpoint ruta relativa a {@link #BASE_URL}
     * @return lista parseada; vacía si hay cualquier error
     */
    private static List<Product> fetchProductList(String endpoint) {
        List<Product> result = new ArrayList<>();
        try {
            String json = HttpClient.get(BASE_URL + endpoint);
            if (json == null || json.isEmpty()) return result;

            JsonArray array = GSON.fromJson(json, JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();

                String expDate = obj.has("expirationDate")
                    && !obj.get("expirationDate").isJsonNull()
                    ? obj.get("expirationDate").getAsString()
                    : null;

                Product p;
                if (expDate != null && !expDate.isBlank()) {
                    p = new PerishableProduct(
                        obj.get("code").getAsString(),
                        obj.get("description").getAsString(),
                        obj.get("price").getAsDouble(),
                        obj.get("stock").getAsInt(),
                        expDate);
                } else {
                    p = new Product(
                        obj.get("code").getAsString(),
                        obj.get("description").getAsString(),
                        obj.get("price").getAsDouble(),
                        obj.get("stock").getAsInt());
                }

                p.setRetired(obj.has("retired")
                    && obj.get("retired").getAsBoolean());
                result.add(p);
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchProductList failed for " + endpoint, e);
        }
        return result;
    }

    // ── Privado: helpers ──────────────────────────────────────────────────────

    /**
     * Comprueba si la respuesta JSON del servidor contiene
     * {@code "status":"OK"}.
     *
     * @param json respuesta raw del servidor
     * @return {@code true} si el status es OK
     */
    private static boolean isOk(String json) {
        if (json == null || json.isEmpty()) return false;
        try {
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            return obj.has("status") && "OK".equals(obj.get("status").getAsString());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Codifica un valor para usarlo como parámetro URL-encoded.
     *
     * @param value texto a codificar
     * @return texto percent-encoded
     */
    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
