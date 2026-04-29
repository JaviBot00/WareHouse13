package com.hotguy.warehouse13.controller;

import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application controller — mediates between the View (Fragments)
 * and both the in-memory model and the remote database.
 * <p>
 * Every method that mutates state performs two actions:
 * <ol>
 *   <li>Updates the in-memory list immediately (so the UI stays responsive).</li>
 *   <li>Calls {@link DatabaseAccess} to persist the change remotely.</li>
 * </ol>
 * <p>
 * <strong>Threading:</strong> {@link DatabaseAccess} methods are blocking.
 * All callers must invoke mutating Controller methods from a background
 * thread (e.g. {@code ExecutorService}) and return to the UI thread
 * via {@code requireActivity().runOnUiThread()} for feedback.
 */
public class Controller {

    private static List<Product> productList;
    private static List<Product> retiredProductList;
    private static Controller instance;

    private Controller() {
        productList = new ArrayList<>();
        retiredProductList = new ArrayList<>();
    }

    /**
     * @return the application-wide singleton instance
     */
    public static Controller getSingleton() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    // ── File persistence (kept for JSON file load/save via SAF) ──────────────

    /**
     * Loads the product list from a local JSON file in internal storage.
     *
     * @param context Android context used to resolve {@code getFilesDir()}
     * @return {@code true} if the list is empty after loading
     */
    public boolean loadProductList(Context context) {
        productList = DataAccess.loadDataFromFile(context, "products.json");
        return productList.isEmpty();
    }

    /**
     * Saves the current product list to a local JSON file in internal storage.
     *
     * @param context Android context used to resolve {@code getFilesDir()}
     * @return {@code true} if the file was written successfully
     */
    public boolean saveProductList(Context context) {
        return DataAccess.saveDataToFile(context, "products.json", productList);
    }

    /**
     * Replaces the in-memory product list with one parsed from a JSON string.
     * Used when loading from a file chosen via the Storage Access Framework (SAF).
     *
     * @param json full JSON array string
     * @return {@code true} if at least one product was loaded
     */
    public boolean loadProductListFromJson(String json) {
        productList = DataAccess.parseProductListFromJson(json);
        return !productList.isEmpty();
    }

    // ── Remote DB — load only (save is per-operation, see below) ─────────────

    /**
     * Replaces the in-memory product list with the active products from the DB.
     * <p>
     * <strong>Must be called from a background thread.</strong>
     *
     * @return {@code true} if at least one product was loaded
     */
    public boolean loadProductListFromDb() {
        productList = DatabaseAccess.loadProductList();
        return !productList.isEmpty();
    }

    /**
     * Replaces the in-memory retired list with the retired products from the DB.
     * <p>
     * <strong>Must be called from a background thread.</strong>
     *
     * @return {@code true} if at least one product was loaded
     */
    public boolean loadRetiredListFromDb() {
        retiredProductList = DatabaseAccess.loadRetiredList();
        return !retiredProductList.isEmpty();
    }

    // ── Mutating operations — update memory AND remote DB ─────────────────────

    /**
     * Adds a product to the in-memory list and inserts it in the remote DB.
     * <p>
     * The product is built from a JSON string so the View never touches
     * the model classes directly.
     * <p>
     * <strong>Must be called from a background thread.</strong>
     *
     * @param perishable  {@code true} if the product has an expiration date
     * @param jsonProduct JSON object with productCode, description, price, stock
     *                    and optionally expirationDate
     * @return {@code true} if the product was added to memory and persisted
     */
    public boolean addProduct(boolean perishable, String jsonProduct) {
        Product p = perishable ? parsePerishableProduct(jsonProduct) : parseProduct(jsonProduct);

        if (p == null) return false;

        for (Product aux : productList) {
            if (aux.getCode().equalsIgnoreCase(p.getCode())) return false;
        }

        boolean addedToMemory = productList.add(p);
        if (!addedToMemory) return false;

        // Persist to remote DB — blocking, must run on background thread
        boolean savedToDb = DatabaseAccess.insertProduct(p);
        if (!savedToDb) {
            // Roll back in-memory addition if the server rejected it
            productList.remove(p);
            return false;
        }
        return true;
    }

    /**
     * Returns a product by its code, or {@code null} if not found.
     *
     * @param productCode exact product code (case-insensitive)
     * @return matching product or {@code null}
     */
    public Product getProductByCode(String productCode) {
        for (Product p : productList) {
            if (p.getCode().equalsIgnoreCase(productCode)) return p;
        }
        return null;
    }

    /**
     * Adjusts the stock of a product in memory and updates the remote DB.
     * <p>
     * <strong>Must be called from a background thread.</strong>
     *
     * @param productCode code of the product to update
     * @param stock       delta to apply (positive adds, negative removes units)
     * @return {@code true} if the stock was updated in memory and persisted
     */
    public boolean editStockForProduct(String productCode, int stock) {
        for (Product p : productList) {
            if (p.getCode().equalsIgnoreCase(productCode)) {
                int previousStock = p.getStock();
                p.changeStock(stock);

                // Persist to remote DB — blocking, must run on background thread
                boolean savedToDb = DatabaseAccess.updateProduct(p);
                if (!savedToDb) {
                    // Roll back stock change if the server rejected it
                    p.changeStock(previousStock - p.getStock());
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Moves a product from the active list to the retired list in memory,
     * and marks it as retired in the remote DB.
     * <p>
     * <strong>Must be called from a background thread.</strong>
     *
     * @param productCode code of the product to retire
     * @return {@code true} if the product was retired in memory and persisted
     */
    public boolean withdrawProduct(String productCode) {
        for (Product p : productList) {
            if (p.getCode().equalsIgnoreCase(productCode)) {

                // Persist to remote DB first — roll back if it fails
                boolean savedToDb = DatabaseAccess.retireProduct(productCode);
                if (!savedToDb) return false;

                retiredProductList.add(p);
                productList.remove(p);
                return true;
            }
        }
        return false;
    }

    // ── Read-only queries (in-memory, no network) ─────────────────────────────

    /**
     * @return sorted list of all active products
     */
    public List<Product> listProducts() {
        Collections.sort(productList);
        return productList;
    }

    /**
     * @return sorted list of active products with zero stock
     */
    public List<Product> listProductsNoStock() {
        Collections.sort(productList);
        return productList.stream()
            .filter(p -> p.getStock() == 0)
            .collect(Collectors.toList());
    }

    /**
     * @return sorted list of perishable products whose expiration date is in the past
     */
    public List<Product> listExpiredProducts() {
        Collections.sort(productList);
        LocalDate today = LocalDate.now();
        return productList.stream()
            .filter(p -> p instanceof PerishableProduct)
            .filter(p -> LocalDate.parse(
                ((PerishableProduct) p).getExpirationDate(),
                PerishableProduct.FORMAT).isBefore(today))
            .collect(Collectors.toList());
    }

    /**
     * @param min minimum price (inclusive)
     * @param max maximum price (inclusive)
     * @return sorted list of active products within the given price range
     */
    public List<Product> listProductsBetweenPrices(double min, double max) {
        Collections.sort(productList);
        return productList.stream()
            .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
            .collect(Collectors.toList());
    }

    /**
     * @return sorted list of all retired products
     */
    public List<Product> listWithdrawnProducts() {
        Collections.sort(retiredProductList);
        return retiredProductList;
    }

    // ── Private: JSON parsing ─────────────────────────────────────────────────

    /**
     * Parses a standard {@link Product} from a JSON string.
     *
     * @param jsonProduct JSON object string
     * @return parsed product or {@code null} if the JSON is invalid or incomplete
     */
    private Product parseProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject obj = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (obj.isEmpty()) return null;

        if (!obj.has("productCode") || !obj.has("description")
            || !obj.has("price") || !obj.has("stock")) return null;

        return new Product(
            obj.get("productCode").getAsString(),
            obj.get("description").getAsString(),
            obj.get("price").getAsDouble(),
            obj.get("stock").getAsInt());
    }

    /**
     * Parses a {@link PerishableProduct} from a JSON string.
     *
     * @param jsonProduct JSON object string (must include expirationDate)
     * @return parsed product or {@code null} if the JSON is invalid or incomplete
     */
    private Product parsePerishableProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject obj = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (obj.isEmpty()) return null;

        if (!obj.has("productCode") || !obj.has("description")
            || !obj.has("price") || !obj.has("stock")
            || !obj.has("expirationDate")) return null;

        return new PerishableProduct(
            obj.get("productCode").getAsString(),
            obj.get("description").getAsString(),
            obj.get("price").getAsDouble(),
            obj.get("stock").getAsInt(),
            obj.get("expirationDate").getAsString());
    }
}
