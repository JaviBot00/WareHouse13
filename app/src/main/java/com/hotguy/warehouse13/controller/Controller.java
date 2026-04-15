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

public class Controller {
    private static List<Product> productList;
    private static List<Product> retiredProductList;
    private static Controller instance;

    private Controller() {
        productList = new ArrayList<>();
        retiredProductList = new ArrayList<>();
    }

    public static Controller getSingleton() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public boolean loadProductList(Context context) {
        productList = DataAccess.loadDataFromFile(context, "products.json");
        return productList.isEmpty();
    }

    public boolean saveProductList(Context context) {
        return DataAccess.saveDataToFile(context, "products.json", productList);
    }

    public boolean addProduct(boolean perishable, String jsonProduct) {
        Product p = perishable ? parsePerishableProduct(jsonProduct) : parseProduct(jsonProduct);

        if (p == null)
            return false;

        for (Product aux : productList) {
            if (aux.getProductCode().equalsIgnoreCase(p.getProductCode())) {
                return false;
            }
        }
        return productList.add(p);
    }

    public Product getProductByCode(String productCode) {
        for (Product p : productList) {
            if (p.getProductCode().equalsIgnoreCase(productCode)) {
                return p;
            }
        }
        return null;
    }

    public boolean editStockForProduct(String productCode, int stock) {
        for (Product p : productList) {
            if (p.getProductCode().equalsIgnoreCase(productCode)) {
                p.changeStock(stock);
                return true;
            }
        }
        return false;
    }

    public List<Product> listProducts() {
        Collections.sort(productList);
        return productList;
    }

    public boolean withdrawProduct(String productCode) {
        for (Product p : productList) {
            if (p.getProductCode().equalsIgnoreCase(productCode)) {
                retiredProductList.add(p);
                return productList.remove(p);
            }
        }
        return false;
    }

    public List<Product> listProductsNoStock() {
        Collections.sort(productList);
        return productList.stream().filter(p -> p.getStock() == 0).collect(Collectors.toList());
    }

    public List<Product> listExpiredProducts() {
        Collections.sort(productList);
        LocalDate hoy = LocalDate.now();
        return productList.stream()
            .filter(p -> p instanceof PerishableProduct)
            .filter(p -> LocalDate.parse(((PerishableProduct) p).getExpirationDate(), PerishableProduct.FORMAT)
                .isBefore(hoy))
            .collect(Collectors.toList());
    }

    public List<Product> listProductsBetweenPrices(double min, double max) {
        Collections.sort(productList);
        return productList.stream().filter(p -> p.getPrice() >= min && p.getPrice() <= max)
            .collect(Collectors.toList());
    }

    public List<Product> listWithdrawnProducts() {
        Collections.sort(retiredProductList);
        return retiredProductList;
    }

    /**
     * Carga la lista de productos desde un String JSON ya leído.
     * Usado cuando el fichero fue leído por FilePickerManager (SAF).
     * <p>
     * ¿Por qué existe este método además de loadProductList(Context)?
     * · loadProductList(Context) lee desde el almacenamiento interno
     * privado de la app (getFilesDir) — fichero interno.
     * · loadProductListFromJson(String) recibe el contenido ya leído
     * desde cualquier Uri SAF — fichero elegido por el usuario.
     * · DataAccess sigue siendo el único que parsea JSON: la Vista
     * nunca toca el Modelo directamente.
     *
     * @param json Contenido JSON leído desde el Uri SAF
     * @return true si se cargaron productos, false si la lista quedó vacía
     */
    public boolean loadProductListFromJson(String json) {
        productList = DataAccess.parseProductListFromJson(json);
        return !productList.isEmpty();
    }

    // ── BD: guardar y cargar lista activa ────────────────────────────────────

    public boolean saveProductListToDb() {
        return DatabaseAccess.saveProductList(productList);
    }

    public boolean loadProductListFromDb() {
        productList = DatabaseAccess.loadProductList();
        return !productList.isEmpty();
    }

// ── BD: guardar y cargar lista retirados ─────────────────────────────────

    public boolean saveRetiredListToDb() {
        return DatabaseAccess.saveRetiredList(retiredProductList);
    }

    public boolean loadRetiredListFromDb() {
        retiredProductList = DatabaseAccess.loadRetiredList();
        return !retiredProductList.isEmpty();
    }

    private Product parseProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty())
            return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty())
            return null;

        // Clean y explícit
        if (!jsonObject.has("productCode") || !jsonObject.has("description") ||
            !jsonObject.has("price") || !jsonObject.has("stock"))
            return null;

        return new Product(
            jsonObject.get("productCode").getAsString(),
            jsonObject.get("description").getAsString(),
            jsonObject.get("price").getAsDouble(),
            jsonObject.get("stock").getAsInt());
    }

    private Product parsePerishableProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty())
            return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty())
            return null;

        // Clean y explícit
        if (!jsonObject.has("productCode") || !jsonObject.has("description") ||
            !jsonObject.has("price") || !jsonObject.has("stock") || !jsonObject.has("expirationDate"))
            return null;

        return new PerishableProduct(
            jsonObject.get("productCode").getAsString(),
            jsonObject.get("description").getAsString(),
            jsonObject.get("price").getAsDouble(),
            jsonObject.get("stock").getAsInt(),
            jsonObject.get("expirationDate").getAsString());
    }
}
