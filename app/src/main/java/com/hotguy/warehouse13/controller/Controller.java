package com.hotguy.warehouse13.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.model.Product;
import com.hotguy.warehouse13.model.PerishableProduct;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controller {
    private static List<Product> productList;
    private static List<Product> retiredProductList;
    private static Controller instance;

    private Controller() {
        productList = DataAccess.loadData();
        retiredProductList = new ArrayList<>();
    }

    public static Controller getSingleton() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

//    public void saveData() {
//        DataAccess.saveData(productList);
//    }

    public boolean addProduct(boolean perishable, String jsonProduct) {
        Product p;
        if (perishable) {
            p = parsePerishableProduct(jsonProduct);
        } else {
            p = parseProduct(jsonProduct);
        }
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

    public List<Product> getList() {
        return productList;
    }

    public String listProducts() {
        Collections.sort(productList);
        return new Gson().toJson(productList);
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

    public String listProductsNoStock() {
        Collections.sort(productList);
        return new Gson().toJson(productList.stream().filter(p -> p.getStock() == 0));
    }

    public String listExpiredProducts() {
        Collections.sort(productList);
        LocalDate hoy = LocalDate.now();
        return new Gson().toJson(productList.stream()
            .filter(p -> p instanceof PerishableProduct)
            .filter(p -> ((PerishableProduct) p).getExpiryDate().isBefore(hoy)));
    }

    public String listProductsBetweenPrices(double min, double max) {
        Collections.sort(productList);
        return new Gson().toJson(productList.stream().filter(p -> p.getPrice() >= min && p.getPrice() <= max));
    }

    public String listWithdrawnProducts() {
        Collections.sort(retiredProductList);
        return new Gson().toJson(retiredProductList);
    }

    private Product parseProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty()) return null;

        // Clean y explícit
        if (!jsonObject.has("productCode") || !jsonObject.has("description") ||
            !jsonObject.has("price") || !jsonObject.has("stock")) return null;

        return new Product(
            jsonObject.get("productCode").getAsString(),
            jsonObject.get("description").getAsString(),
            jsonObject.get("price").getAsDouble(),
            jsonObject.get("stock").getAsInt()
        );
    }

    private PerishableProduct parsePerishableProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty()) return null;

        // Clean y explícit
        if (!jsonObject.has("productCode") || !jsonObject.has("description") ||
            !jsonObject.has("price") || !jsonObject.has("stock") || !jsonObject.has("expiryDate")) return null;

        return new PerishableProduct(
            jsonObject.get("productCode").getAsString(),
            jsonObject.get("description").getAsString(),
            jsonObject.get("price").getAsDouble(),
            jsonObject.get("stock").getAsInt(),
            jsonObject.get("expiryDate").getAsString()
        );
    }
}
