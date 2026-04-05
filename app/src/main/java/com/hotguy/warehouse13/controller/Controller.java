package com.hotguy.warehouse13.controller;

import android.content.Context;

import com.google.gson.Gson;
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
//        productList = DataAccess.loadData();
        retiredProductList = new ArrayList<>();
    }

    public static Controller getSingleton() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public void loadDataFromFile() {
        productList = DataAccess.loadDataFromFile("./", "products.csv");
    }

    public void loadDataFromFile(Context context) {
        productList = DataAccess.loadDataFromFile(context, "products.csv");
    }

    public void saveDataToFile() {
        DataAccess.saveDataToFile("./", "products.csv", productList);
    }

    public void saveDataToFile(Context context) {
        DataAccess.saveDataToFile(context, "products.csv", productList);
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
        return new Gson().toJson(productList.stream().filter(p -> p.getStock() == 0).collect(Collectors.toList()));
    }

    public String listExpiredProducts() {
        Collections.sort(productList);
        LocalDate hoy = LocalDate.now();
        return new Gson().toJson(productList.stream()
            .filter(p -> p instanceof PerishableProduct)
            .filter(p -> LocalDate.parse(((PerishableProduct) p).getExpirationDate(), PerishableProduct.FORMAT).isBefore(hoy))
            .collect(Collectors.toList()));
    }

    public String listProductsBetweenPrices(double min, double max) {
        Collections.sort(productList);
        return new Gson().toJson(productList.stream().filter(p -> p.getPrice() >= min && p.getPrice() <= max).collect(Collectors.toList()));
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

    private Product parsePerishableProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty()) return null;

        // Clean y explícit
        if (!jsonObject.has("productCode") || !jsonObject.has("description") ||
            !jsonObject.has("price") || !jsonObject.has("stock") || !jsonObject.has("expirationDate")) return null;

        return new PerishableProduct(
            jsonObject.get("productCode").getAsString(),
            jsonObject.get("description").getAsString(),
            jsonObject.get("price").getAsDouble(),
            jsonObject.get("stock").getAsInt(),
            jsonObject.get("expirationDate").getAsString()
        );
    }
}
