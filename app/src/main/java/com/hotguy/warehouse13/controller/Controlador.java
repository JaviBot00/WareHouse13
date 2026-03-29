package com.hotguy.warehouse13.controller;


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

    public boolean addProducto(boolean perecedero, String csvProduct) {
        Producto p;
        if (perecedero) {
            p = parseProductPerecedero(csvProduct);
        } else {
            p = parseProduct(csvProduct);
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
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            sb.append(p).append("\n");
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public boolean retirarProducto(String codigoProducto) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(String.valueOf(codigoProducto))) {
                listaDeProductosRetirados.add(p);
                return listaDeProductosInicial.remove(p);
            }
        }
        return false;
    }

    public String listProductoSinStock() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            if (p.getStock() == 0) {
                sb.append(p).append("\n");
            }
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public String listProductosCaducados() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        LocalDate hoy = LocalDate.now();

        for (Producto pp : listaDeProductosInicial) {
            if (pp instanceof ProductoPerecedero perecedero) {
                if (perecedero.getFechaCaducidad().isBefore(hoy)) {
                    sb.append(perecedero).append("\n");
                }
            }
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public String listProductosBtwPrecios(double min, double max) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            if (p.getPrecio() >= min && p.getPrecio() <= max) {
                sb.append(p).append("\n");
            }
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public String listProductosRetirados() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosRetirados);

        for (Producto p : listaDeProductosRetirados) {
            sb.append(p).append("\n");
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    private Producto parseProduct(String csvProduct) {
        if (csvProduct == null)
            return null;
        String[] dataset = csvProduct.split(";");
        if (dataset.length < 4)
            return null;
        return new Producto(dataset[0], dataset[1], Double.parseDouble(dataset[2]), Integer.parseInt(dataset[3]));
    }

    private ProductoPerecedero parseProductPerecedero(String csvProduct) {
        if (csvProduct == null)
            return null;
        String[] dataset = csvProduct.split(";");
        if (dataset.length < 5)
            return null;
        return new ProductoPerecedero(dataset[0], dataset[1], Double.parseDouble(dataset[2]),
            Integer.parseInt(dataset[3]), dataset[4]);
    }
}
