package com.hotguy.warehouse13.controller;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {

    private static final Gson GSON = new Gson();

    public static List<Product> loadDataFromFile(String path, String file) {
        List<String> lines = getData(path, file);
        if (lines.isEmpty())
            return new ArrayList<>();
        return parseFromJsonArray(String.join("", lines));
    }

    public static List<Product> loadDataFromFile(Context context, String file) {
        List<String> lines = getData(context, file);
        if (lines.isEmpty())
            return new ArrayList<>();
        return parseFromJsonArray(String.join("", lines));
    }

    private static List<Product> parseFromJsonArray(String jsonContent) {
        List<Product> products = new ArrayList<>();
        JsonArray jsonArray = GSON.fromJson(jsonContent, JsonArray.class);

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj = jsonArray.get(i).getAsJsonObject();
            String type = obj.get("class").getAsString();
            if ("perishableProduct".equals(type)) {
                products.add(GSON.fromJson(obj, PerishableProduct.class));
            } else {
                products.add(GSON.fromJson(obj, Product.class));
            }
        }
        return products;
    }

    public static boolean saveDataToFile(String path, String file, List<Product> products) {
        return saveData(path, file, GSON.toJson(parseToJsonArray(products)));
    }

    public static boolean saveDataToFile(Context context, String file, List<Product> products) {
        return saveData(context, file, GSON.toJson(parseToJsonArray(products)));
    }

    private static JsonArray parseToJsonArray(List<Product> products) {
        JsonArray data = new JsonArray();
        for (Product p : products) {
            JsonObject aux = GSON.toJsonTree(p).getAsJsonObject();
            if (p instanceof PerishableProduct) {
                aux.addProperty("class", "perishableProduct");
                // aux.addProperty("expirationDate", ((PerishableProduct)
                // p).getExpirationDate());
            } else if (p != null) {
                aux.addProperty("class", "product");
            }
            data.add(aux);
        }
        return data;
    }

    private static List<String> getData(String path, String file) {
        try {
            return Files.readAllLines(Paths.get(path, file));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static boolean saveData(String path, String file, String lines) {
        try {
            Files.write(Paths.get(path, file), lines.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<String> getData(Context context, String file) {
        try {
            return Files.readAllLines(new File(context.getFilesDir(), file).toPath());
        } catch (IOException e) {
            Log.e("DataAccess", "Error loading file", e);
            return new ArrayList<>();
        }
    }

    private static boolean saveData(Context context, String file, String lines) {
        try {
            Files.write(new File(context.getFilesDir(), file).toPath(), lines.getBytes());
            return true;
        } catch (IOException e) {
            Log.e("DataAccess", "Error saving file", e);
            return false;
        }
    }

}
