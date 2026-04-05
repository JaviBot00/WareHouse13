package com.hotguy.warehouse13;

import static org.junit.Assert.*;

import com.hotguy.warehouse13.model.Product;

import org.junit.After;
import org.junit.Before;
//import org.junit.DisplayName;
import org.junit.Test;

/**
 * Test class for the Product model.
 * <p>
 * Contains comprehensive unit tests for the Product class,
 * including constructor validation, getters, setters,
 * and business logic methods.
 *
 * @author HotGuy
 * @version 1.0
 */
public class ProductTest {

    private Product product;

    /**
     * Default constructor.
     */
    public ProductTest() {
    }

    /**
     * Sets up test data before each test.
     */
    @Before
    public void setUp() {
        // Test data
        String validProductCode = "CC112244A";
        String validDescription = "Asus Laptop AST-45";
        double validPrice = 899.99;
        int validStock = 10;

        product = new Product(validProductCode, validDescription, validPrice, validStock);
    }

    /**
     * Cleans up after each test.
     */
    @After
    public void tearDown() {
        product = null;
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
//    @DisplayName("Constructor with valid parameters should create product successfully")
    public void shouldCreateProductWithValidParameters() {
        String productCode = "PROD00001";
        String description = "Test Product";
        double price = 49.99;
        int stock = 5;

        Product p = new Product(productCode, description, price, stock);

        assertNotNull(p);
        assertEquals(productCode, p.getProductCode());
        assertEquals(description, p.getDescription());
        assertEquals(price, p.getPrice());
        assertEquals(stock, p.getStock());
    }

    @Test
//    @DisplayName("Constructor with null productCode should throw IllegalArgumentException")
    public void shouldThrowExceptionWhenProductCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product(null, "Test Product", 49.99, 5);
        });
    }

    @Test
//    @DisplayName("Constructor with short productCode should throw IllegalArgumentException")
    public void shouldThrowExceptionWhenProductCodeIsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product("PROD001", "Test Product", 49.99, 5);
        });
    }

    @Test
//    @DisplayName("Constructor with long productCode should throw IllegalArgumentException")
    public void shouldThrowExceptionWhenProductCodeIsTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Product("PRODUCTO0000000001", "Test Product", 49.99, 5);
        });
    }

    @Test
//    @DisplayName("Constructor with minimum valid productCode length should succeed")
    public void shouldAcceptMinimumLengthProductCode() {
        Product p = new Product("PROD0001", "Test Product", 49.99, 5);
        assertEquals("PROD0001", p.getProductCode());
    }

    @Test
//    @DisplayName("Constructor with maximum valid productCode length should succeed")
    public void shouldAcceptMaximumLengthProductCode() {
        Product p = new Product("PROD00000000001A", "Test Product", 49.99, 5);
        assertEquals("PROD00000000001A", p.getProductCode());
    }

    // ==================== GETTERS & SETTERS ====================

    @Test
//    @DisplayName("getPrice should return correct value")
    public void shouldReturnCorrectPrice() {
        assertEquals(899.99, product.getPrice(), 0.01);
    }

    @Test
//    @DisplayName("setPrice with positive value should update price")
    public void shouldUpdatePriceWhenPositive() {
        product.setPrice(1299.99);
        assertEquals(1299.99, product.getPrice(), 0.01);
    }

    @Test
//    @DisplayName("setPrice with zero should update price to zero")
    public void shouldAllowZeroPrice() {
        product.setPrice(0.0);
        assertEquals(0.0, product.getPrice());
    }

    @Test
//    @DisplayName("setPrice with negative value should not update price")
    public void shouldRejectNegativePrice() {
        double originalPrice = product.getPrice();
        product.setPrice(-100.0);
        assertEquals(originalPrice, product.getPrice());
    }

    @Test
//    @DisplayName("getDescription should return correct value")
    public void shouldReturnCorrectDescription() {
        assertEquals("Asus Laptop AST-45", product.getDescription());
    }

    @Test
//    @DisplayName("setDescription should update description")
    public void shouldUpdateDescription() {
        product.setDescription("LG Monitor 32 inch 4K");
        assertEquals("LG Monitor 32 inch 4K", product.getDescription());
    }

    @Test
//    @DisplayName("getProductCode should return correct value")
    public void shouldReturnCorrectProductCode() {
        assertEquals("CC112244A", product.getProductCode());
    }

    @Test
//    @DisplayName("getStock should return correct value")
    public void shouldReturnCorrectStock() {
        assertEquals(10, product.getStock());
    }

    // ==================== STOCK MANAGEMENT ====================

    @Test
//    @DisplayName("changeStock with positive value should increase stock")
    public void shouldIncreaseStock() {
        product.changeStock(5);
        assertEquals(15, product.getStock());
    }

    @Test
//    @DisplayName("changeStock with negative value should decrease stock")
    public void shouldDecreaseStock() {
        product.changeStock(-3);
        assertEquals(7, product.getStock());
    }

    @Test
//    @DisplayName("changeStock should prevent negative stock")
    public void shouldNotAllowNegativeStock() {
        int initialStock = product.getStock();
        product.changeStock(-15);
        assertEquals(initialStock, product.getStock());
    }

    @Test
//    @DisplayName("changeStock should allow stock to reach zero")
    public void shouldAllowStockToReachZero() {
        Product p = new Product("PROD0001", "Test", 10.0, 5);
        p.changeStock(-5);
        assertEquals(0, p.getStock());
    }

    @Test
//    @DisplayName("changeStock with zero should not modify stock")
    public void shouldNotChangeStockWhenZero() {
        int initialStock = product.getStock();
        product.changeStock(0);
        assertEquals(initialStock, product.getStock());
    }

    // ==================== toString & compareTo ====================

    @Test
//    @DisplayName("toString should return CSV format")
    public void shouldReturnCsvFormat() {
        String expected = "CC112244A;Asus Laptop AST-45;899.99;10;";
        assertEquals(expected, product.toString());
    }

    @Test
//    @DisplayName("compareTo should sort alphabetically")
    public void shouldCompareAlphabetically() {
        Product p1 = new Product("PROD0001", "Apple", 100.0, 5);
        Product p2 = new Product("PROD0002", "Banana", 200.0, 3);

        assertTrue(p1.compareTo(p2) < 0);
    }

    @Test
//    @DisplayName("compareTo should return 0 for equal descriptions")
    public void shouldReturnZeroWhenEqual() {
        Product p1 = new Product("PROD0001", "Same Product", 100.0, 5);
        Product p2 = new Product("PROD0002", "Same Product", 200.0, 3);

        assertEquals(0, p1.compareTo(p2));
    }

    @Test
//    @DisplayName("compareTo should ignore case")
    public void shouldIgnoreCase() {
        Product p1 = new Product("PROD0001", "apple", 100.0, 5);
        Product p2 = new Product("PROD0002", "APPLE", 200.0, 3);

        assertEquals(0, p1.compareTo(p2));
    }

    @Test
//    @DisplayName("compareTo should detect reverse order")
    public void shouldDetectReverseOrder() {
        Product p1 = new Product("PROD0001", "Zebra", 100.0, 5);
        Product p2 = new Product("PROD0002", "Apple", 200.0, 3);

        assertTrue(p1.compareTo(p2) > 0);
    }

    // ==================== INTEGRATION ====================

    @Test
//    @DisplayName("Full integration test")
    public void shouldHandleFullWorkflow() {
        Product p = new Product("PROD0010", "Integration Test Product", 249.99, 20);

        p.setPrice(299.99);
        p.changeStock(-5);
        p.setDescription("Modified Description");

        assertEquals("PROD0010", p.getProductCode());
        assertEquals("Modified Description", p.getDescription());
        assertEquals(299.99, p.getPrice(), 0.01);
        assertEquals(15, p.getStock());
    }

    @Test
//    @DisplayName("Multiple price updates should work correctly")
    public void shouldHandleMultiplePriceUpdates() {
        product.setPrice(100.0);
        assertEquals(100.0, product.getPrice(), 0.01);

        product.setPrice(200.0);
        assertEquals(200.0, product.getPrice(), 0.01);

        product.setPrice(0.0);
        assertEquals(0.0, product.getPrice());
    }

    @Test
//    @DisplayName("Multiple stock changes should accumulate correctly")
    public void shouldHandleMultipleStockChanges() {
        product.changeStock(5);
        assertEquals(15, product.getStock());

        product.changeStock(-3);
        assertEquals(12, product.getStock());

        product.changeStock(8);
        assertEquals(20, product.getStock());
    }
}
