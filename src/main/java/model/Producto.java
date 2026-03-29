package src.main.java.model;

import java.util.Comparator;

//Clase producto

public class Producto implements Comparable<Producto> {

    private static final String CSV_FORMAT = "codigoProducto;descripcion;precio;stock";

    private String codigoProducto;
    private String descripcion;
    private double precio;
    private int stock;

    // Constructor
    public Producto(String codigoProducto, String descripcion, double precio, int stock) {
        setCodigoProducto(codigoProducto); // Usamos el setter para validar
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
    }

    public static String getCsvFormat() {
        return CSV_FORMAT;
    }

    public static Producto cargarDesdeCSVPlus(String data) {
        String codigoProducto = "";
        String descripcion = "";
        double precio = 0;
        int stock = 0;

        // Poner aquí tu código

        return new Producto(codigoProducto, descripcion, precio, stock);

    }

    // Getters y Setters
    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        if (codigoProducto == null || codigoProducto.length() < 8 || codigoProducto.length() > 16) {
            throw new IllegalArgumentException(
                    "El código de producto debe ser alfanumérico y tener entre 8 y 16 caracteres.");
        }
        this.codigoProducto = codigoProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio >= 0.0)
            this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void changeStock(int newStock) {
        if (this.stock + newStock < 0)
            return; // nunca tenemos stock negativo
        this.stock += newStock; // si newStock es negativo, se quita al almacén unidades del producto
    }

    // Método para mostrar la información del producto. CSV Plus
    @Override
    public String toString() {
        return codigoProducto + ";"
                + descripcion + ";"
                + precio + ";"
                + stock + ";";
    }

    @Override
    public int compareTo(Producto p) {
        return this.descripcion.compareToIgnoreCase(p.descripcion);
    }
}
