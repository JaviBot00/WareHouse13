package com.hotguy.warehouse13.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.model.Producto;
import com.hotguy.warehouse13.model.ProductoPerecedero;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controlador {
    private static List<Producto> listaDeProductosInicial;
    private static List<Producto> listaDeProductosRetirados;
    private static Controlador instance;

    private Controlador() {
        listaDeProductosInicial = DataAccess.loadData();
        listaDeProductosRetirados = new ArrayList<>();
    }

    public static Controlador getSingleton() {
        if (instance == null) {
            instance = new Controlador();
        }
        return instance;
    }

//    public void saveData() {
//        DataAccess.saveData(listaDeProductosInicial);
//    }

    public boolean addProducto(boolean perecedero, String jsonProduct) {
        Producto p;
        if (perecedero) {
            p = parseProductPerecedero(jsonProduct);
        } else {
            p = parseProduct(jsonProduct);
        }
        if (p == null)
            return false;

        for (Producto aux : listaDeProductosInicial) {
            if (aux.getCodigoProducto().equalsIgnoreCase(p.getCodigoProducto())) {
                return false;
            }
        }
        return listaDeProductosInicial.add(p);
    }

    public Producto getProductoByCodigo(String codigoProducto) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(codigoProducto)) {
                return p;
            }
        }
        return null;
    }

    public boolean editStockForProducto(String codigoProducto, int stock) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(String.valueOf(codigoProducto))) {
                p.changeStock(stock);
                return true;
            }
        }
        return false;
    }

    public List<Producto> getList() {
        return listaDeProductosInicial;
    }

    public String listProductos() {
        Collections.sort(listaDeProductosInicial);
        return new Gson().toJson(listaDeProductosInicial);
    }

    public boolean retirarProduct(String codigoProducto) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(String.valueOf(codigoProducto))) {
                listaDeProductosRetirados.add(p);
                return listaDeProductosInicial.remove(p);
            }
        }
        return false;
    }

    public String listProductsSinStock() {
        Collections.sort(listaDeProductosInicial);
        return new Gson().toJson(listaDeProductosInicial.stream().filter(p -> p.getStock() == 0));
    }

    public String listProductsCaducados() {
        Collections.sort(listaDeProductosInicial);
        LocalDate hoy = LocalDate.now();
        return new Gson().toJson(listaDeProductosInicial.stream()
            .filter(p -> p instanceof ProductoPerecedero)
            .filter(p -> ((ProductoPerecedero) p).getFechaCaducidad().isBefore(hoy)));
    }

    public String listProductosBtwPrecios(double min, double max) {
        Collections.sort(listaDeProductosInicial);
        return new Gson().toJson(listaDeProductosInicial.stream().filter(p -> p.getPrecio() >= min && p.getPrecio() <= max));
    }

    public String listProductosRetirados() {
        Collections.sort(listaDeProductosRetirados);
        return new Gson().toJson(listaDeProductosRetirados);
    }

    private Producto parseProduct(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty()) return null;

        // Limpio y explícito
        if (!jsonObject.has("codigoProducto") || !jsonObject.has("descripcion") ||
            !jsonObject.has("precio") || !jsonObject.has("stock")) return null;

        return new Producto(
            jsonObject.get("codigoProducto").getAsString(),
            jsonObject.get("descripcion").getAsString(),
            jsonObject.get("precio").getAsDouble(),
            jsonObject.get("stock").getAsInt()
        );
    }

    private ProductoPerecedero parseProductPerecedero(String jsonProduct) {
        if (jsonProduct == null || jsonProduct.isEmpty()) return null;

        JsonObject jsonObject = JsonParser.parseString(jsonProduct).getAsJsonObject();
        if (jsonObject.isEmpty()) return null;

        // Limpio y explícito
        if (!jsonObject.has("codigoProducto") || !jsonObject.has("descripcion") ||
            !jsonObject.has("precio") || !jsonObject.has("stock") || !jsonObject.has("fechaCaducidad")) return null;

        return new ProductoPerecedero(
            jsonObject.get("codigoProducto").getAsString(),
            jsonObject.get("descripcion").getAsString(),
            jsonObject.get("precio").getAsDouble(),
            jsonObject.get("stock").getAsInt(),
            jsonObject.get("fechaCaducidad").getAsString()
        );
    }
}
