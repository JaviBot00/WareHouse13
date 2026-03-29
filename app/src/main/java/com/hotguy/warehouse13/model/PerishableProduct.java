package com.hotguy.warehouse13.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PerishableProduct extends Product {

    private static final String CSV_FORMAT = "expiryDate";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private LocalDate expiryDate; // YYYYMMDD

    public PerishableProduct(String productCode, String description, double price, int stock,
                             String expiryDateString) {
        super(productCode, description, price, stock);
        this.expiryDate = LocalDate.parse(expiryDateString, FORMAT);
    }

    public static String getCsvFormat() {
        return Product.getCsvFormat() + ";" + PerishableProduct.CSV_FORMAT;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDateString) {
        this.expiryDate = LocalDate.parse(expiryDateString, FORMAT);
    }

    @Override
    public String toString() {
        return super.toString() + expiryDate + ";";
    }
}
