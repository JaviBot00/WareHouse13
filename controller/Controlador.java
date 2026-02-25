package controller;

import model.Producto;
import model.ProductoPerecedero;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controlador {
    private static List<Producto> listaDeProductosInicial;
    private static List<Producto> listaDeProductosRetirados;
    // instance variables
    private static Controlador instance;

    //Singleton
    //poner aquí

    /**
     * Constructor for objects of class Controlador
     */
    private Controlador() {
        //Poner código aquí para que la lista inicial de productos esté
        //siempre disponible cuando se arranca el programa.
        listaDeProductosInicial = DataAccess.loadData();
        listaDeProductosRetirados = new ArrayList<>();
    }

    public static Controlador getSingleton() {
        // put your code here
        if (instance == null) {
            instance = new Controlador();
        }
        return instance;
    }


    public static boolean addProducto(boolean perecedero, String csvProduct) {
        Producto p = null;
        if (perecedero) {
            p = parseProductPerecedero(csvProduct);
        } else {
            p = parseProduct(csvProduct);
        }
        if (p == null) return false;

        for (Producto aux : listaDeProductosInicial) {
            if (aux.getCodigoProducto().equalsIgnoreCase(p.getCodigoProducto())) {
                return false;
            }
        }
        return listaDeProductosInicial.add(p);
    }

    public static boolean editStockForProducto(String codigoProducto, int stock) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(String.valueOf(codigoProducto))) {
                p.changeStock(stock);
                return true;
            }
        }
        return false;
    }

    public static String listProductos() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            sb.append(p).append("\n");
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public static boolean retirarProducto(String codigoProducto) {
        for (Producto p : listaDeProductosInicial) {
            if (p.getCodigoProducto().equalsIgnoreCase(String.valueOf(codigoProducto))) {
                listaDeProductosRetirados.add(p);
                return listaDeProductosInicial.remove(p);
            }
        }
        return false;
    }

    public static String listProductoSinStock() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            if (p.getStock() == 0) {
                sb.append(p).append("\n");
            }
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public static String listProductosCaducados() {
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

    public static String listProductosBtwPrecios(double min, double max) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosInicial);

        for (Producto p : listaDeProductosInicial) {
            if (p.getPrecio() >= min && p.getPrecio() <= max) {
                sb.append(p).append("\n");
            }
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    public static String listProductosRetirados() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(listaDeProductosRetirados);

        for (Producto p : listaDeProductosRetirados) {
            sb.append(p).append("\n");
        }
        return ProductoPerecedero.getCsvFormat() + "\n" + sb;
    }

    private static Producto parseProduct(String csvProduct) {
        if (csvProduct == null) return null;
        String[] dataset = csvProduct.split(";");
        if (dataset.length < 4) return null;
        return new Producto(dataset[0], dataset[1], Double.parseDouble(dataset[2]), Integer.parseInt(dataset[3]));
    }

    private static ProductoPerecedero parseProductPerecedero(String csvProduct) {
        if (csvProduct == null) return null;
        String[] dataset = csvProduct.split(";");
        if (dataset.length < 5) return null;
        return new ProductoPerecedero(dataset[0], dataset[1], Double.parseDouble(dataset[2]), Integer.parseInt(dataset[3]), dataset[4]);
    }
}
