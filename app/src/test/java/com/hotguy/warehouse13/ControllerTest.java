package com.hotguy.warehouse13;

import static org.junit.Assert.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.controller.Controller;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ControllerTest {

    private Controller controller;

    // JSON helpers — igual que hace la Vista real
    private String buildProductJson(String code, String desc, double price, int stock) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productCode", code);
        data.put("description", desc);
        data.put("price", price);
        data.put("stock", stock);
        return new Gson().toJson(data);
    }

    private String buildPerishableJson(String code, String desc, double price, int stock, String expDate) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productCode", code);
        data.put("description", desc);
        data.put("price", price);
        data.put("stock", stock);
        data.put("expirationDate", expDate);
        return new Gson().toJson(data);
    }

    @Before
    public void setUp() {
        // Resetea el Singleton antes de cada test
        Controller.resetForTesting();
        controller = Controller.getSingleton();
    }

    // ==================== ADD PRODUCT ====================

    @Test
    public void shouldAddValidProduct() {
        String json = buildProductJson("PROD0001A", "Teclado mecánico", 89.99, 10);
        assertTrue(controller.addProduct(false, json));
    }

    @Test
    public void shouldAddValidPerishableProduct() {
        String json = buildPerishableJson("PERI0001A", "Leche 1L", 1.20, 50, "20261231");
        assertTrue(controller.addProduct(true, json));
    }

    @Test
    public void shouldRejectDuplicateProductCode() {
        String json = buildProductJson("PROD0001A", "Teclado", 89.99, 10);
        controller.addProduct(false, json);
        assertFalse(controller.addProduct(false, json));
    }

    @Test
    public void shouldRejectDuplicateCaseInsensitive() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        assertFalse(controller.addProduct(false, buildProductJson("prod0001a", "Otro", 50.0, 5)));
    }

    @Test
    public void shouldRejectNullJson() {
        assertFalse(controller.addProduct(false, null));
    }

    @Test
    public void shouldRejectEmptyJson() {
        assertFalse(controller.addProduct(false, ""));
    }

    @Test
    public void shouldRejectJsonMissingFields() {
        // Falta stock
        assertFalse(
                controller.addProduct(false, "{\"productCode\":\"PROD0001A\",\"description\":\"X\",\"price\":10.0}"));
    }

    @Test
    public void shouldRejectPerishableWithoutExpirationDate() {
        // JSON de producto normal pasado como perecedero — falta expirationDate
        String json = buildProductJson("PERI0001A", "Leche", 1.20, 50);
        assertFalse(controller.addProduct(true, json));
    }

    @Test
    public void shouldRejectProductCodeTooShort() {
        // El código tiene 7 chars — Product constructor lanza excepción
        assertThrows(IllegalArgumentException.class,
                () -> controller.addProduct(false, buildProductJson("PROD001", "X", 10.0, 5)));
    }

    // ==================== GET BY CODE ====================

    @Test
    public void shouldFindProductByCode() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        Product p = controller.getProductByCode("PROD0001A");
        assertNotNull(p);
        assertEquals("PROD0001A", p.getProductCode());
    }

    @Test
    public void shouldFindProductByCodeCaseInsensitive() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        assertNotNull(controller.getProductByCode("prod0001a"));
    }

    @Test
    public void shouldReturnNullWhenCodeNotFound() {
        assertNull(controller.getProductByCode("NOTEXIST1"));
    }

    // ==================== EDIT STOCK ====================

    @Test
    public void shouldIncreaseStock() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        assertTrue(controller.editStockForProduct("PROD0001A", 5));
        assertEquals(15, controller.getProductByCode("PROD0001A").getStock());
    }

    @Test
    public void shouldDecreaseStock() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        assertTrue(controller.editStockForProduct("PROD0001A", -3));
        assertEquals(7, controller.getProductByCode("PROD0001A").getStock());
    }

    @Test
    public void shouldNotAllowNegativeStock() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        controller.editStockForProduct("PROD0001A", -999);
        assertEquals(10, controller.getProductByCode("PROD0001A").getStock());
    }

    @Test
    public void shouldReturnFalseWhenEditingNonExistentProduct() {
        assertFalse(controller.editStockForProduct("NOTEXIST1", 5));
    }

    // ==================== WITHDRAW ====================

    @Test
    public void shouldWithdrawExistingProduct() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        assertTrue(controller.withdrawProduct("PROD0001A"));
    }

    @Test
    public void shouldRemoveFromActiveListAfterWithdraw() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        controller.withdrawProduct("PROD0001A");
        assertNull(controller.getProductByCode("PROD0001A"));
    }

    @Test
    public void shouldAppearInWithdrawnListAfterWithdraw() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        controller.withdrawProduct("PROD0001A");
        JsonArray withdrawn = JsonParser.parseString(controller.listWithdrawnProducts()).getAsJsonArray();
        assertEquals(1, withdrawn.size());
        assertEquals("PROD0001A", withdrawn.get(0).getAsJsonObject().get("productCode").getAsString());
    }

    @Test
    public void shouldReturnFalseWhenWithdrawingNonExistentProduct() {
        assertFalse(controller.withdrawProduct("NOTEXIST1"));
    }

    // ==================== LIST PRODUCTS ====================

    @Test
    public void shouldReturnEmptyArrayWhenNoProducts() {
        JsonArray result = JsonParser.parseString(controller.listProducts()).getAsJsonArray();
        assertEquals(0, result.size());
    }

    @Test
    public void shouldReturnAllProducts() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        controller.addProduct(false, buildProductJson("PROD0002B", "Ratón", 34.50, 20));
        JsonArray result = JsonParser.parseString(controller.listProducts()).getAsJsonArray();
        assertEquals(2, result.size());
    }

    @Test
    public void shouldReturnProductsSortedAlphabetically() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Zebra", 89.99, 10));
        controller.addProduct(false, buildProductJson("PROD0002B", "Apple", 34.50, 20));
        JsonArray result = JsonParser.parseString(controller.listProducts()).getAsJsonArray();
        assertEquals("Apple", result.get(0).getAsJsonObject().get("description").getAsString());
        assertEquals("Zebra", result.get(1).getAsJsonObject().get("description").getAsString());
    }

    // ==================== LIST NO STOCK ====================

    @Test
    public void shouldReturnOnlyProductsWithZeroStock() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 0));
        controller.addProduct(false, buildProductJson("PROD0002B", "Ratón", 34.50, 5));
        JsonArray result = JsonParser.parseString(controller.listProductsNoStock()).getAsJsonArray();
        assertEquals(1, result.size());
        assertEquals("PROD0001A", result.get(0).getAsJsonObject().get("productCode").getAsString());
    }

    @Test
    public void shouldReturnEmptyWhenAllProductsHaveStock() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        JsonArray result = JsonParser.parseString(controller.listProductsNoStock()).getAsJsonArray();
        assertEquals(0, result.size());
    }

    // ==================== LIST EXPIRED ====================

    @Test
    public void shouldReturnExpiredProducts() {
        // Fecha en el pasado
        controller.addProduct(true, buildPerishableJson("PERI0001A", "Leche", 1.20, 10, "20200101"));
        JsonArray result = JsonParser.parseString(controller.listExpiredProducts()).getAsJsonArray();
        assertEquals(1, result.size());
    }

    @Test
    public void shouldNotReturnNonExpiredProducts() {
        // Fecha en el futuro
        controller.addProduct(true, buildPerishableJson("PERI0001A", "Leche", 1.20, 10, "20991231"));
        JsonArray result = JsonParser.parseString(controller.listExpiredProducts()).getAsJsonArray();
        assertEquals(0, result.size());
    }

    @Test
    public void shouldNotReturnNormalProductsInExpiredList() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        JsonArray result = JsonParser.parseString(controller.listExpiredProducts()).getAsJsonArray();
        assertEquals(0, result.size());
    }

    // ==================== LIST BETWEEN PRICES ====================

    @Test
    public void shouldReturnProductsWithinPriceRange() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Barato", 10.0, 5));
        controller.addProduct(false, buildProductJson("PROD0002B", "Medio", 50.0, 5));
        controller.addProduct(false, buildProductJson("PROD0003C", "Caro", 200.0, 5));
        JsonArray result = JsonParser.parseString(controller.listProductsBetweenPrices(20.0, 100.0)).getAsJsonArray();
        assertEquals(1, result.size());
        assertEquals("PROD0002B", result.get(0).getAsJsonObject().get("productCode").getAsString());
    }

    @Test
    public void shouldIncludeBoundaryPrices() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Exacto min", 20.0, 5));
        controller.addProduct(false, buildProductJson("PROD0002B", "Exacto max", 100.0, 5));
        JsonArray result = JsonParser.parseString(controller.listProductsBetweenPrices(20.0, 100.0)).getAsJsonArray();
        assertEquals(2, result.size());
    }

    @Test
    public void shouldReturnEmptyWhenNoPriceMatch() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Caro", 500.0, 5));
        JsonArray result = JsonParser.parseString(controller.listProductsBetweenPrices(10.0, 50.0)).getAsJsonArray();
        assertEquals(0, result.size());
    }

    // ==================== LIST WITHDRAWN ====================

    @Test
    public void shouldReturnEmptyWithdrawnListInitially() {
        JsonArray result = JsonParser.parseString(controller.listWithdrawnProducts()).getAsJsonArray();
        assertEquals(0, result.size());
    }

    @Test
    public void shouldAccumulateMultipleWithdrawnProducts() {
        controller.addProduct(false, buildProductJson("PROD0001A", "Teclado", 89.99, 10));
        controller.addProduct(false, buildProductJson("PROD0002B", "Ratón", 34.50, 5));
        controller.withdrawProduct("PROD0001A");
        controller.withdrawProduct("PROD0002B");
        JsonArray result = JsonParser.parseString(controller.listWithdrawnProducts()).getAsJsonArray();
        assertEquals(2, result.size());
    }
}
