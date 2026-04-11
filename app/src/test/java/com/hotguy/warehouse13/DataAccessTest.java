package com.hotguy.warehouse13;

import static org.junit.Assert.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.controller.DataAccess;
import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataAccessTest {

    private static final String TEST_PATH = "./";
    private static final String TEST_FILE = "test_products.json";

    @Before
    public void setUp() throws IOException {
        // Elimina el fichero de test si existe de una ejecución anterior
        Files.deleteIfExists(Paths.get(TEST_PATH, TEST_FILE));
    }

    @After
    public void tearDown() throws IOException {
        // Limpieza tras cada test
        Files.deleteIfExists(Paths.get(TEST_PATH, TEST_FILE));
    }

    // ==================== SAVE + LOAD ROUNDTRIP ====================

    @Test
    public void shouldSaveAndLoadNormalProduct() {
        List<Product> original = new ArrayList<>();
        original.add(new Product("PROD0001A", "Teclado mecánico", 89.99, 10));

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, original);
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);

        assertEquals(1, loaded.size());
        assertEquals("PROD0001A", loaded.get(0).getProductCode());
        assertEquals("Teclado mecánico", loaded.get(0).getDescription());
        assertEquals(89.99, loaded.get(0).getPrice(), 0.01);
        assertEquals(10, loaded.get(0).getStock());
    }

    @Test
    public void shouldSaveAndLoadPerishableProduct() {
        List<Product> original = new ArrayList<>();
        original.add(new PerishableProduct("PERI0001A", "Leche 1L", 1.20, 50, "20261231"));

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, original);
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);

        assertEquals(1, loaded.size());
        assertTrue(loaded.get(0) instanceof PerishableProduct);
        assertEquals("20261231", ((PerishableProduct) loaded.get(0)).getExpirationDate());
    }

    @Test
    public void shouldPreserveTypeDiscriminatorOnSave() throws IOException {
        List<Product> original = new ArrayList<>();
        original.add(new PerishableProduct("PERI0001A", "Leche 1L", 1.20, 50, "20261231"));
        original.add(new Product("PROD0001A", "Teclado", 89.99, 10));

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, original);

        String content = new String(Files.readAllBytes(Paths.get(TEST_PATH, TEST_FILE)));
        JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();

        // Comprueba que el discriminador "class" está presente
        assertEquals("perishableProduct", jsonArray.get(0).getAsJsonObject().get("class").getAsString());
        assertEquals("product", jsonArray.get(1).getAsJsonObject().get("class").getAsString());
    }

    @Test
    public void shouldSaveAndLoadMixedList() {
        List<Product> original = new ArrayList<>();
        original.add(new Product("PROD0001A", "Teclado", 89.99, 10));
        original.add(new PerishableProduct("PERI0001A", "Leche 1L", 1.20, 50, "20261231"));
        original.add(new Product("PROD0002B", "Ratón", 34.50, 20));

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, original);
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);

        assertEquals(3, loaded.size());

        // El orden se conserva
        assertFalse(loaded.get(0) instanceof PerishableProduct);
        assertTrue(loaded.get(1) instanceof PerishableProduct);
        assertFalse(loaded.get(2) instanceof PerishableProduct);
    }

    @Test
    public void shouldSaveAndLoadEmptyList() {
        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, new ArrayList<>());
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    // ==================== FICHERO INEXISTENTE ====================

    @Test
    public void shouldReturnEmptyListWhenFileNotFound() {
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, "fichero_inexistente.json");
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    // ==================== INTEGRIDAD DE DATOS ====================

    @Test
    public void shouldPreserveAllFieldValuesAfterRoundtrip() {
        PerishableProduct original = new PerishableProduct("PERI0001A", "Yogur natural", 0.85, 100, "20270615");
        List<Product> list = new ArrayList<>();
        list.add(original);

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, list);
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);

        PerishableProduct result = (PerishableProduct) loaded.get(0);
        assertEquals(original.getProductCode(), result.getProductCode());
        assertEquals(original.getDescription(), result.getDescription());
        assertEquals(original.getPrice(), result.getPrice(), 0.01);
        assertEquals(original.getStock(), result.getStock());
        assertEquals(original.getExpirationDate(), result.getExpirationDate());
    }

    @Test
    public void shouldHandleSpecialCharactersInDescription() {
        List<Product> original = new ArrayList<>();
        original.add(new Product("PROD0001A", "Teclado ñoño & especial <100€>", 89.99, 10));

        DataAccess.saveDataToFile(TEST_PATH, TEST_FILE, original);
        List<Product> loaded = DataAccess.loadDataFromFile(TEST_PATH, TEST_FILE);

        assertEquals("Teclado ñoño & especial <100€>", loaded.get(0).getDescription());
    }
}
