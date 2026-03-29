package src.main.test.java;

import src.main.java.model.Producto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The test class ProductoTestkk.
 *
 * @author HotGuy
 * @version hoy
 */
public class ProductoTestkk {

    /**
     * Default constructor for test class ProductoTest
     */
    public ProductoTestkk() {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetPrecio() {

        // Triple AAA

        // String codigoProducto, String nombre, String descripcion, double precio, int
        // stock
        String codigo = "CC112244A";
        String descripcion = "Portatil Asus AST-45";
        double precio = -500;
        // double precio2 = 11.5;
        int stock = 4;
        for (int i = 0; i < 1000; i++) {

            // Add - Activar, añadir, crear
            Producto p = new Producto(codigo, descripcion, precio, stock);
            // Producto p2 = new Producto(codigo, descripcion, precio2, stock);
            // Producto p3 = new Producto(codigo, descripcion, precio2, stock);

            // Act - Actuar, llamar a metodo, funcion, asignar
            double precioResultado = p.getPrecio();

            // Assert - Aseguro que el resultado es el esperado
            assertTrue(Math.abs(precio) == Math.abs(precioResultado));
            assertTrue(precioResultado >= 0);
            precio += 1;
        }

    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @AfterEach
    public void tearDown() {
    }
}