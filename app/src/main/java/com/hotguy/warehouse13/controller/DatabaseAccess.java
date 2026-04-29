package com.hotguy.warehouse13.controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * HTTP client that communicates with the WareHouse13 Servlet API.
 * <p>
 * Replaces the former JDBC implementation — all data operations
 * are now delegated to the remote Tomcat server via HTTP requests.
 * The public API (method names and signatures) is identical to the
 * previous version so {@link Controller} requires no changes.
 * <p>
 * Every method in this class is <strong>blocking</strong>: callers
 * must invoke them from a background thread (e.g. {@code ExecutorService}).
 */
public class DatabaseAccess {

    private static final String TAG = "DatabaseAccess";

    /**
     * Base URL of the Servlet deployment.
     * Replace with the actual IP of the machine running Docker.
     * Example: "http://192.168.1.50:8080/WareHouse13-Servlets"
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

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Fetches all active (non-retired) products from the server.
     *
     * @return list of active products, empty if the request fails
     */
    public static List<Product> loadProductList() {
        return fetchProductList(EP_LIST_ACTIVE);
    }

    /**
     * Fetches all retired products from the server.
     *
     * @return list of retired products, empty if the request fails
     */
    public static List<Product> loadRetiredList() {
        return fetchProductList(EP_LIST_RETIRED);
    }

    // ── Insert ────────────────────────────────────────────────────────────────

    /**
     * Sends a single product to the server for insertion.
     * <p>
     * Used by {@link Controller#addProduct} to persist a newly
     * created product immediately after adding it to the in-memory list.
     *
     * @param product the product to insert (may be a {@link PerishableProduct})
     * @return {@code true} if the server responded with status "OK"
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

            String response = doPost(EP_INSERT, params.toString());
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "insertProduct failed", e);
            return false;
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Sends updated stock for an existing product to the server.
     * <p>
     * Called after {@link Controller#editStockForProduct} modifies
     * the in-memory list, to keep the remote database in sync.
     *
     * @param product product with the new stock value already applied
     * @return {@code true} if the server responded with status "OK"
     */
    public static boolean updateProduct(Product product) {
        try {
            String params = "code=" + encode(product.getCode())
                + "&description=" + encode(product.getDescription())
                + "&price=" + product.getPrice()
                + "&stock=" + product.getStock();

            String response = doPut(EP_UPDATE, params);
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "updateProduct failed", e);
            return false;
        }
    }

    // ── Retire ────────────────────────────────────────────────────────────────

    /**
     * Marks a product as retired on the server.
     * <p>
     * Called after {@link Controller#withdrawProduct} moves the product
     * to the retired list in memory.
     *
     * @param productCode code of the product to retire
     * @return {@code true} if the server responded with status "OK"
     */
    public static boolean retireProduct(String productCode) {
        try {
            String params = "code=" + encode(productCode);
            String response = doPut(EP_RETIRE, params);
            return isOk(response);

        } catch (IOException e) {
            Log.e(TAG, "retireProduct failed", e);
            return false;
        }
    }

    // ── Private: HTTP methods ─────────────────────────────────────────────────

    /**
     * Performs a GET request and parses the JSON array response
     * into a list of {@link Product} or {@link PerishableProduct} objects.
     *
     * @param endpoint path relative to {@link #BASE_URL}
     * @return parsed product list, empty on any error
     */
    private static List<Product> fetchProductList(String endpoint) {
        List<Product> result = new ArrayList<>();
        try {
            String json = doGet(endpoint);
            if (json == null || json.isEmpty()) return result;

            JsonArray array = GSON.fromJson(json, JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                String expDate = obj.has("expirationDate") && !obj.get("expirationDate").isJsonNull()
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
                p.setRetired(obj.has("retired") && obj.get("retired").getAsBoolean());
                result.add(p);
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchProductList failed for " + endpoint, e);
        }
        return result;
    }

    /**
     * Opens a GET connection to {@code endpoint} and returns the raw response body.
     *
     * @param endpoint path relative to {@link #BASE_URL}
     * @return response body as a String
     * @throws IOException on connection or read failure
     */
    private static String doGet(String endpoint) throws IOException {
        HttpURLConnection conn = openConnection(endpoint, "GET");
        return readResponse(conn);
    }

    /**
     * Opens a POST connection, writes {@code formBody}, and returns the response.
     *
     * @param endpoint path relative to {@link #BASE_URL}
     * @param formBody URL-encoded form parameters (e.g. "code=ABC&price=9.99")
     * @return response body as a String
     * @throws IOException on connection, write, or read failure
     */
    private static String doPost(String endpoint, String formBody) throws IOException {
        HttpURLConnection conn = openConnection(endpoint, "POST");
        writeBody(conn, formBody);
        return readResponse(conn);
    }

    /**
     * Opens a PUT connection, writes {@code formBody}, and returns the response.
     *
     * @param endpoint path relative to {@link #BASE_URL}
     * @param formBody URL-encoded form parameters
     * @return response body as a String
     * @throws IOException on connection, write, or read failure
     */
    private static String doPut(String endpoint, String formBody) throws IOException {
        HttpURLConnection conn = openConnection(endpoint, "PUT");
        writeBody(conn, formBody);
        return readResponse(conn);
    }

    // ── Private: connection helpers ───────────────────────────────────────────

    /**
     * Creates and configures an {@link HttpURLConnection} for the given endpoint and method.
     *
     * @param endpoint   path relative to {@link #BASE_URL}
     * @param httpMethod "GET", "POST", or "PUT"
     * @return configured (but not yet executed) connection
     * @throws IOException if the URL is malformed or the connection cannot be opened
     */
    private static HttpURLConnection openConnection(String endpoint, String httpMethod)
        throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        boolean hasBody = httpMethod.equals("POST") || httpMethod.equals("PUT");
        if (hasBody) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        }
        return conn;
    }

    /**
     * Writes a URL-encoded form body to the connection's output stream.
     *
     * @param conn     open connection with {@code doOutput = true}
     * @param formBody URL-encoded parameters string
     * @throws IOException on write failure
     */
    private static void writeBody(HttpURLConnection conn, String formBody) throws IOException {
        byte[] bytes = formBody.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
        }
    }

    /**
     * Reads the full response body from the connection.
     *
     * @param conn connection whose request has already been sent
     * @return response body as a String
     * @throws IOException on read failure
     */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (Scanner scanner = new Scanner(
            conn.getInputStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Checks whether the server response JSON contains {@code "status": "OK"}.
     *
     * @param json raw JSON response from the server
     * @return {@code true} if status is OK
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
     * URL-encodes a string value for use in form parameters.
     *
     * @param value raw string
     * @return percent-encoded string
     */
    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
