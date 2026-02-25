package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProductoPerecedero extends Producto {

    private static final String CSV_FORMAT = "fechaCaducidad";
    private static final DateTimeFormatter FORMATO =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private LocalDate fechaCaducidad; // AAAAMMDD

    public ProductoPerecedero(String codigoProducto, String descripcion, double precio, int stock, String fechaCaducidadString) {
        super(codigoProducto, descripcion, precio, stock);
        this.fechaCaducidad = LocalDate.parse(fechaCaducidadString, FORMATO);
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidadString) {
        this.fechaCaducidad = LocalDate.parse(fechaCaducidadString, FORMATO);
    }

    public static String getCsvFormat() {
        return Producto.getCsvFormat() + ";" + ProductoPerecedero.CSV_FORMAT;
    }

    @Override
    public String toString() {
        return super.toString() + fechaCaducidad + ";";
    }
}
