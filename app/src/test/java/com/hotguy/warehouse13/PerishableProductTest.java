package com.hotguy.warehouse13;

import static org.junit.Assert.*;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerishableProductTest {

    private PerishableProduct product;

    @Before
    public void setUp() {
        product = new PerishableProduct("PERI0001A", "Leche entera 1L", 1.20, 50, "20251231");
    }

    @After
    public void tearDown() {
        product = null;
    }

    // ==================== HERENCIA ====================

    @Test
    public void shouldBeInstanceOfProduct() {
        assertTrue(product instanceof Product);
    }

    @Test
    public void shouldBeInstanceOfPerishableProduct() {
        assertTrue(product instanceof PerishableProduct);
    }

    @Test
    public void shouldInheritProductCode() {
        assertEquals("PERI0001A", product.getProductCode());
    }

    @Test
    public void shouldInheritDescription() {
        assertEquals("Leche entera 1L", product.getDescription());
    }

    @Test
    public void shouldInheritPrice() {
        assertEquals(1.20, product.getPrice(), 0.01);
    }

    @Test
    public void shouldInheritStock() {
        assertEquals(50, product.getStock());
    }

    // ==================== CAMPO PROPIO ====================

    @Test
    public void shouldReturnCorrectExpirationDate() {
        assertEquals("20251231", product.getExpirationDate());
    }

    @Test
    public void shouldUpdateExpirationDate() {
        product.setExpirationDate("20261231");
        assertEquals("20261231", product.getExpirationDate());
    }

    // ==================== CONSTRUCTOR ====================

    @Test
    public void shouldCreateWithValidParameters() {
        PerishableProduct p = new PerishableProduct("PERI0002B", "Yogur natural", 0.80, 100, "20260101");
        assertNotNull(p);
        assertEquals("PERI0002B", p.getProductCode());
        assertEquals("20260101", p.getExpirationDate());
    }

    @Test
    public void shouldThrowExceptionWhenCodeTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> new PerishableProduct("PERI001", "Yogur", 0.80, 10, "20261231"));
    }

    @Test
    public void shouldThrowExceptionWhenCodeNull() {
        assertThrows(IllegalArgumentException.class, () -> new PerishableProduct(null, "Yogur", 0.80, 10, "20261231"));
    }

    // ==================== HERENCIA DE STOCK ====================

    @Test
    public void shouldIncreaseStockInherited() {
        product.changeStock(10);
        assertEquals(60, product.getStock());
    }

    @Test
    public void shouldDecreaseStockInherited() {
        product.changeStock(-10);
        assertEquals(40, product.getStock());
    }

    @Test
    public void shouldNotAllowNegativeStockInherited() {
        int original = product.getStock();
        product.changeStock(-999);
        assertEquals(original, product.getStock());
    }

    // ==================== toString ====================

    @Test
    public void shouldIncludeExpirationDateInToString() {
        String result = product.toString();
        assertTrue(result.contains("20251231"));
    }

    @Test
    public void shouldIncludeParentFieldsInToString() {
        String result = product.toString();
        assertTrue(result.contains("PERI0001A"));
        assertTrue(result.contains("Leche entera 1L"));
    }
}
