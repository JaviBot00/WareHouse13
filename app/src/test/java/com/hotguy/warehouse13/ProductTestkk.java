package com.hotguy.warehouse13;

import com.hotguy.warehouse13.model.Product;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductTestkk {

    public ProductTestkk() {
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetPrice() {

        // Triple AAA

        // String productCode, String name, String description, double price, int
        // stock
        String code = "CC112244A";
        String description = "Portatil Asus AST-45";
        double price = -500;
        // double price2 = 11.5;
        int stock = 4;
        for (int i = 0; i < 1000; i++) {

            // Add - Activar, añadir, crear
            Product p = new Product(code, description, price, stock);
            // Product p2 = new Product(code, description, price2, stock);
            // Product p3 = new Product(code, description, price2, stock);

            // Act - Actuar, llamar a metodo, funcion, asignar
            double resultPrice = p.getPrice();

            // Assert - Aseguro que el resultado es el esperado
            assertTrue(Math.abs(price) == Math.abs(resultPrice));
            assertTrue(resultPrice >= 0);
            price += 1;
        }

    }

    @AfterEach
    public void tearDown() {
    }
}
