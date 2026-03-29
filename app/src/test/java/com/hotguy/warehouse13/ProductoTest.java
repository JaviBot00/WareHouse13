package com.hotguy.warehouse13;

import com.hotguy.warehouse13.model.Producto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Test class for Producto model.
 *
 * This class contains comprehensive unit tests for the Producto class,
 * testing constructor, getters, setters, and business logic methods.
 *
 * @author HotGuy
 * @version 1.0
 */
public class ProductoTest {

    // Test data
    private String validCodigo;
    private String validDescripcion;
    private double validPrecio;
    private int validStock;
    private Producto producto;

    /**
     * Default constructor for test class ProductoTest.
     */
    public ProductoTest() {
    }

    /**
     * Sets up the test fixture.
     *
     * This method is called before every test case method.
     * It initializes common test data used across multiple tests.
     */
    @BeforeEach
    public void setUp() {
        // Initialize valid test data
        validCodigo = "CC112244A";
        validDescripcion = "Portatil Asus AST-45";
        validPrecio = 899.99;
        validStock = 10;

        // Create a valid product instance
        producto = new Producto(validCodigo, validDescripcion, validPrecio, validStock);
    }

    /**
     * Tears down the test fixture.
     *
     * This method is called after every test case method.
     * Cleanup operations can be performed here if needed.
     */
    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
        producto = null;
    }

    // ==================== CONSTRUCTOR TESTS ====================

    /**
     * Test valid constructor with valid parameters.
     *
     * This test verifies that a Producto object can be successfully created
     * with valid parameters: codigo (8-16 chars), descripcion, precio, and stock.
     */
    @Test
    @DisplayName("Constructor with valid parameters should create product successfully")
    public void testConstructorWithValidParameters() {
        // AAA Pattern: Arrange, Act, Assert

        // Arrange - Setup test data
        String codigo = "PROD00001";
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act - Create product instance
        Producto p = new Producto(codigo, descripcion, precio, stock);

        // Assert - Verify object was created correctly
        assertNotNull(p);
        assertEquals(codigo, p.getCodigoProducto());
        assertEquals(descripcion, p.getDescripcion());
        assertEquals(precio, p.getPrecio());
        assertEquals(stock, p.getStock());
    }

    /**
     * Test constructor with null codigo should throw IllegalArgumentException.
     *
     * Verifies that passing null as codigo raises an exception,
     * as per the validation rule in setCodigoProducto().
     */
    @Test
    @DisplayName("Constructor with null codigo should throw IllegalArgumentException")
    public void testConstructorWithNullCodigo() {
        // Arrange
        String codigoNulo = null;
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act & Assert - Verify exception is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(codigoNulo, descripcion, precio, stock);
        });
    }

    /**
     * Test constructor with codigo too short (less than 8 characters).
     *
     * Verifies that the setCodigoProducto validation enforces
     * minimum length requirement of 8 characters.
     */
    @Test
    @DisplayName("Constructor with short codigo should throw IllegalArgumentException")
    public void testConstructorWithShortCodigo() {
        // Arrange
        String codigoCorto = "PROD001"; // Only 7 characters
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(codigoCorto, descripcion, precio, stock);
        });
    }

    /**
     * Test constructor with codigo too long (more than 16 characters).
     *
     * Verifies that the setCodigoProducto validation enforces
     * maximum length requirement of 16 characters.
     */
    @Test
    @DisplayName("Constructor with long codigo should throw IllegalArgumentException")
    public void testConstructorWithLongCodigo() {
        // Arrange
        String codigoLargo = "PRODUCTO0000000001"; // 18 characters
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(codigoLargo, descripcion, precio, stock);
        });
    }

    /**
     * Test constructor with exactly 8 character codigo (minimum valid length).
     *
     * Boundary test to verify that exactly 8 characters is accepted.
     */
    @Test
    @DisplayName("Constructor with exactly 8 character codigo should succeed")
    public void testConstructorWithMinimumValidCodigo() {
        // Arrange
        String codigoMinimo = "PROD0001"; // Exactly 8 characters
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act
        Producto p = new Producto(codigoMinimo, descripcion, precio, stock);

        // Assert
        assertEquals(codigoMinimo, p.getCodigoProducto());
    }

    /**
     * Test constructor with exactly 16 character codigo (maximum valid length).
     *
     * Boundary test to verify that exactly 16 characters is accepted.
     */
    @Test
    @DisplayName("Constructor with exactly 16 character codigo should succeed")
    public void testConstructorWithMaximumValidCodigo() {
        // Arrange
        String codigoMaximo = "PROD00000000001A"; // Exactly 16 characters
        String descripcion = "Test Product";
        double precio = 49.99;
        int stock = 5;

        // Act
        Producto p = new Producto(codigoMaximo, descripcion, precio, stock);

        // Assert
        assertEquals(codigoMaximo, p.getCodigoProducto());
    }

    // ==================== GETTER AND SETTER TESTS ====================

    /**
     * Test getPrecio getter method.
     *
     * Verifies that getPrecio returns the correct price value.
     */
    @Test
    @DisplayName("getPrecio should return the correct price")
    public void testGetPrecio() {
        // Arrange
        double expectedPrecio = 899.99;

        // Act
        double actualPrecio = producto.getPrecio();

        // Assert
        assertEquals(expectedPrecio, actualPrecio, 0.01); // Delta for floating point comparison
    }

    /**
     * Test setPrecio with valid positive price.
     *
     * Verifies that setPrecio correctly updates the price value
     * when given a valid positive number.
     */
    @Test
    @DisplayName("setPrecio with positive value should update price")
    public void testSetPrecioWithPositiveValue() {
        // Arrange
        double newPrecio = 1299.99;

        // Act
        producto.setPrecio(newPrecio);

        // Assert
        assertEquals(newPrecio, producto.getPrecio(), 0.01);
    }

    /**
     * Test setPrecio with zero price.
     *
     * Verifies that setPrecio accepts zero as a valid price value
     * (free products).
     */
    @Test
    @DisplayName("setPrecio with zero should update price to zero")
    public void testSetPrecioWithZero() {
        // Arrange
        double newPrecio = 0.0;

        // Act
        producto.setPrecio(newPrecio);

        // Assert
        assertEquals(0.0, producto.getPrecio());
    }

    /**
     * Test setPrecio with negative price.
     *
     * Verifies that setPrecio rejects negative prices by not updating
     * the price value (business rule: price must be >= 0).
     */
    @Test
    @DisplayName("setPrecio with negative value should NOT update price")
    public void testSetPrecioWithNegativeValue() {
        // Arrange
        double originalPrecio = producto.getPrecio();
        double negativePrecio = -100.0;

        // Act
        producto.setPrecio(negativePrecio);

        // Assert - Price should remain unchanged
        assertEquals(originalPrecio, producto.getPrecio());
    }

    /**
     * Test getDescripcion getter method.
     *
     * Verifies that getDescripcion returns the correct description.
     */
    @Test
    @DisplayName("getDescripcion should return the correct description")
    public void testGetDescripcion() {
        // Arrange
        String expectedDescripcion = "Portatil Asus AST-45";

        // Act
        String actualDescripcion = producto.getDescripcion();

        // Assert
        assertEquals(expectedDescripcion, actualDescripcion);
    }

    /**
     * Test setDescripcion with valid string.
     *
     * Verifies that setDescripcion correctly updates the description.
     */
    @Test
    @DisplayName("setDescripcion should update description")
    public void testSetDescripcion() {
        // Arrange
        String newDescripcion = "Monitor LG 32 pulgadas 4K";

        // Act
        producto.setDescripcion(newDescripcion);

        // Assert
        assertEquals(newDescripcion, producto.getDescripcion());
    }

    /**
     * Test getCodigoProducto getter method.
     *
     * Verifies that getCodigoProducto returns the correct codigo.
     */
    @Test
    @DisplayName("getCodigoProducto should return the correct codigo")
    public void testGetCodigoProducto() {
        // Arrange
        String expectedCodigo = "CC112244A";

        // Act
        String actualCodigo = producto.getCodigoProducto();

        // Assert
        assertEquals(expectedCodigo, actualCodigo);
    }

    /**
     * Test getStock getter method.
     *
     * Verifies that getStock returns the correct stock value.
     */
    @Test
    @DisplayName("getStock should return the correct stock")
    public void testGetStock() {
        // Arrange
        int expectedStock = 10;

        // Act
        int actualStock = producto.getStock();

        // Assert
        assertEquals(expectedStock, actualStock);
    }

    // ==================== STOCK MANAGEMENT TESTS ====================

    /**
     * Test changeStock method with positive value (adding stock).
     *
     * Verifies that changeStock correctly increases the stock
     * when given a positive number.
     */
    @Test
    @DisplayName("changeStock with positive value should increase stock")
    public void testChangeStockWithPositiveValue() {
        // Arrange
        int initialStock = producto.getStock(); // 10
        int addStock = 5;
        int expectedStock = initialStock + addStock; // 15

        // Act
        producto.changeStock(addStock);

        // Assert
        assertEquals(expectedStock, producto.getStock());
    }

    /**
     * Test changeStock method with negative value (removing stock).
     *
     * Verifies that changeStock correctly decreases the stock
     * when given a negative number (sale or adjustment).
     */
    @Test
    @DisplayName("changeStock with negative value should decrease stock")
    public void testChangeStockWithNegativeValue() {
        // Arrange
        int initialStock = producto.getStock(); // 10
        int removeStock = -3;
        int expectedStock = initialStock + removeStock; // 7

        // Act
        producto.changeStock(removeStock);

        // Assert
        assertEquals(expectedStock, producto.getStock());
    }

    /**
     * Test changeStock prevents negative stock.
     *
     * Verifies that changeStock does not allow the stock to go below zero
     * (business rule: stock cannot be negative).
     */
    @Test
    @DisplayName("changeStock should prevent negative stock")
    public void testChangeStockPreventNegativeStock() {
        // Arrange
        int initialStock = producto.getStock(); // 10
        int removeStock = -15; // Try to remove more than available

        // Act
        producto.changeStock(removeStock);

        // Assert - Stock should remain unchanged (method returns early)
        assertEquals(initialStock, producto.getStock());
    }

    /**
     * Test changeStock with value that would result in exactly zero stock.
     *
     * Verifies that changeStock allows stock to reach exactly zero.
     */
    @Test
    @DisplayName("changeStock should allow stock to become zero")
    public void testChangeStockToZero() {
        // Arrange
        int initialStock = 5;
        Producto p = new Producto("PROD0001", "Test", 10.0, initialStock);
        int removeStock = -5;

        // Act
        p.changeStock(removeStock);

        // Assert
        assertEquals(0, p.getStock());
    }

    /**
     * Test changeStock with zero value.
     *
     * Verifies that changeStock correctly handles zero change
     * (stock should remain unchanged).
     */
    @Test
    @DisplayName("changeStock with zero value should not modify stock")
    public void testChangeStockWithZero() {
        // Arrange
        int initialStock = producto.getStock();

        // Act
        producto.changeStock(0);

        // Assert
        assertEquals(initialStock, producto.getStock());
    }

    // ==================== TOSTRING AND COMPARETO TESTS ====================

    /**
     * Test toString method returns proper CSV format.
     *
     * Verifies that toString produces the correct CSV format string:
     * codigo;descripcion;precio;stock;
     */
    @Test
    @DisplayName("toString should return CSV format string")
    public void testToString() {
        // Arrange
        String expectedFormat = "CC112244A;Portatil Asus AST-45;899.99;10;";

        // Act
        String result = producto.toString();

        // Assert
        assertEquals(expectedFormat, result);
    }

    /**
     * Test compareTo method with different descriptions (alphabetical order).
     *
     * Verifies that compareTo correctly compares products based on
     * their descriptions (case-insensitive).
     */
    @Test
    @DisplayName("compareTo should compare descriptions in alphabetical order")
    public void testCompareToAlphabetical() {
        // Arrange
        Producto p1 = new Producto("PROD0001", "Apple", 100.0, 5);
        Producto p2 = new Producto("PROD0002", "Banana", 200.0, 3);

        // Act
        int result = p1.compareTo(p2);

        // Assert - p1 comes before p2 alphabetically, so result should be negative
        assertTrue(result < 0);
    }

    /**
     * Test compareTo method with identical descriptions.
     *
     * Verifies that compareTo returns 0 when descriptions are identical.
     */
    @Test
    @DisplayName("compareTo should return 0 for equal descriptions")
    public void testCompareToEqual() {
        // Arrange
        Producto p1 = new Producto("PROD0001", "Same Product", 100.0, 5);
        Producto p2 = new Producto("PROD0002", "Same Product", 200.0, 3);

        // Act
        int result = p1.compareTo(p2);

        // Assert
        assertEquals(0, result);
    }

    /**
     * Test compareTo method is case-insensitive.
     *
     * Verifies that compareTo ignores case differences in descriptions.
     */
    @Test
    @DisplayName("compareTo should be case-insensitive")
    public void testCompareToIgnoreCase() {
        // Arrange
        Producto p1 = new Producto("PROD0001", "apple", 100.0, 5);
        Producto p2 = new Producto("PROD0002", "APPLE", 200.0, 3);

        // Act
        int result = p1.compareTo(p2);

        // Assert - Should be equal (case-insensitive)
        assertEquals(0, result);
    }

    /**
     * Test compareTo method with reverse alphabetical order.
     *
     * Verifies that compareTo correctly identifies when first product
     * comes after second in alphabetical order.
     */
    @Test
    @DisplayName("compareTo should return positive when first product comes after")
    public void testCompareToReverseOrder() {
        // Arrange
        Producto p1 = new Producto("PROD0001", "Zebra", 100.0, 5);
        Producto p2 = new Producto("PROD0002", "Apple", 200.0, 3);

        // Act
        int result = p1.compareTo(p2);

        // Assert - p1 comes after p2, so result should be positive
        assertTrue(result > 0);
    }

    // ==================== STATIC METHOD TESTS ====================

    /**
     * Test getCsvFormat static method.
     *
     * Verifies that getCsvFormat returns the correct CSV format header.
     */
    @Test
    @DisplayName("getCsvFormat should return correct format string")
    public void testGetCsvFormat() {
        // Arrange
        String expectedFormat = "codigoProducto;descripcion;precio;stock";

        // Act
        String result = Producto.getCsvFormat();

        // Assert
        assertEquals(expectedFormat, result);
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Integration test: Create product, modify properties, and verify state.
     *
     * This test verifies that multiple operations on a product work
     * together correctly.
     */
    @Test
    @DisplayName("Integration test: Create, modify, and verify product state")
    public void testProductoIntegration() {
        // Arrange
        String codigo = "PROD0010";
        String descripcion = "Integration Test Product";
        double precio = 249.99;
        int stock = 20;

        // Act
        Producto p = new Producto(codigo, descripcion, precio, stock);
        p.setPrecio(299.99);
        p.changeStock(-5);
        p.setDescripcion("Modified Description");

        // Assert
        assertEquals(codigo, p.getCodigoProducto());
        assertEquals("Modified Description", p.getDescripcion());
        assertEquals(299.99, p.getPrecio(), 0.01);
        assertEquals(15, p.getStock());
    }

    /**
     * Test multiple price updates in sequence.
     *
     * Verifies that multiple valid price updates work correctly
     * and each update overrides the previous one.
     */
    @Test
    @DisplayName("Multiple price updates should work correctly")
    public void testMultiplePriceUpdates() {
        // Arrange & Act
        producto.setPrecio(100.0);
        assertEquals(100.0, producto.getPrecio(), 0.01);

        producto.setPrecio(200.0);
        assertEquals(200.0, producto.getPrecio(), 0.01);

        producto.setPrecio(0.0);
        assertEquals(0.0, producto.getPrecio());

        // Assert - All assertions passed above
    }

    /**
     * Test multiple stock changes in sequence.
     *
     * Verifies that multiple stock changes accumulate correctly.
     */
    @Test
    @DisplayName("Multiple stock changes should accumulate correctly")
    public void testMultipleStockChanges() {
        // Arrange
        int initialStock = producto.getStock(); // 10

        // Act
        producto.changeStock(5); // Now 15
        assertEquals(15, producto.getStock());

        producto.changeStock(-3); // Now 12
        assertEquals(12, producto.getStock());

        producto.changeStock(8); // Now 20
        assertEquals(20, producto.getStock());

        // Assert - All assertions passed above
    }
}
