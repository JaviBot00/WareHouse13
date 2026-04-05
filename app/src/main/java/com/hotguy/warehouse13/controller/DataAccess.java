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

    private static final String data = """
        [
        { "class": "product", "productCode": "TECL5678X", "description": "Teclado mecánico RGB con switches rojos", "price": 89.99, "stock": 45 },
        { "class": "product", "productCode": "RATN9012K", "description": "Ratón inalámbrico ergonómico con 5 botones", "price": 34.50, "stock": 67 },
        { "class": "product", "productCode": "AURC3456L", "description": "Auriculaes inalámbricos con cancelación de ruido", "price": 129.99, "stock": 23 },
        { "class": "product", "productCode": "WEBC7890P", "description": "Webcam Full HD 1080p con micrófono integrado", "price": 59.90, "stock": 32 },
        { "class": "product", "productCode": "HUBB2345M", "description": "Hub USB 3.0 de 4 puertos con alimentación", "price": 24.75, "stock": 56 },
        { "class": "product", "productCode": "DISK1234R", "description": "Disco duro externo 1TB USB-C resistente al agua", "price": 79.99, "stock": 18 },
        { "class": "product", "productCode": "MONS4567T", "description": "Monitor portátil 15.6 pulgadas Full HD", "price": 189.50, "stock": 12 },
        { "class": "product", "productCode": "PADT8901Y", "description": "Alfombrilla de ratón XXL con base de goma", "price": 19.99, "stock": 89 },
        { "class": "product", "productCode": "MICR2345U", "description": "Micrófono USB de condensador para streaming", "price": 65.30, "stock": 27 },
        { "class": "product", "productCode": "COOL6789I", "description": "Base refrigeradora para portátil con 3 ventiladores", "price": 29.95, "stock": 41 },
        { "class": "product", "productCode": "CARG5678O", "description": "Cargador rápido USB-C 65W con 2 puertos", "price": 45.80, "stock": 34 },
        { "class": "product", "productCode": "LAPD9012P", "description": "Soporte ajustable para portátil de aluminio", "price": 39.99, "stock": 50 },
        { "class": "perishableProduct", "productCode": "INK55665F", "description": "Toner b/w genérico HP 8750", "price": 79.99, "stock": 18, "expirationDate": "20260713" }
        ]""";


    public static List<Product> loadData() {
        return parseFromJsonArray(data);
    }

    public static List<Product> loadDataFromFile(String path, String file) {
        List<String> lines = getData(path, file);
        if (lines.isEmpty()) return new ArrayList<>();
        return parseFromJsonArray(String.join("", lines));
    }

    public static List<Product> loadDataFromFile(Context context, String file) {
        List<String> lines = getData(context, file);
        if (lines.isEmpty()) return new ArrayList<>();
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

    public static void saveDataToFile(String path, String file, List<Product> products) {
        saveData(path, file, GSON.toJson(parseToJsonArray(products)));
    }

    public static void saveDataToFile(Context context, String file, List<Product> products) {
        saveData(context, file, GSON.toJson(parseToJsonArray(products)));
    }

    private static JsonArray parseToJsonArray(List<Product> products) {
        JsonArray data = new JsonArray();
        for (Product p : products) {
            JsonObject aux = GSON.toJsonTree(p).getAsJsonObject();
            if (p instanceof PerishableProduct) {
                aux.addProperty("class", "perishableProduct");
//                aux.addProperty("expirationDate", ((PerishableProduct) p).getExpirationDate());
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

    private static List<String> getData(Context context, String file) {
        try {
            return Files.readAllLines(new File(context.getFilesDir(), file).toPath());
        } catch (IOException e) {
            Log.e("DataAccess", "Error loading file", e);
            return new ArrayList<>();
        }
    }

    private static void saveData(String path, String file, String lines) {
        try {
            Files.write(Paths.get(path, file), lines.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveData(Context context, String file, String lines) {
        try {
            Files.write(new File(context.getFilesDir(), file).toPath(), lines.getBytes());
        } catch (IOException e) {
            Log.e("DataAccess", "Error saving file", e);
        }
    }

}
