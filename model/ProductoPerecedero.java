package model;


/**
 * Write a description of class ProductoPerecedero here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ProductoPerecedero extends Producto {
    
    private static final String CSV_FORMAT = "fechaCaducidad";
    private String fechaCaducidad; // AAAA MM DD

    public ProductoPerecedero(String codigoProducto, String descripcion, double precio, int stock, String fechaCaducidad) {
        super(codigoProducto, descripcion, precio, stock);
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public static String getCsvFormat() {
        return Producto.getCsvFormat() + ";" + ProductoPerecedero.CSV_FORMAT;
    }

    @Override
    public String toString() {
        return super.toString() + fechaCaducidad + ";";
    }
}