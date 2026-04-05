package com.hotguy.warehouse13.model;

import java.time.format.DateTimeFormatter;

public class PerishableProduct extends Product {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private String expirationDate; // YYYYMMDD

    public PerishableProduct(String productCode, String description, double price, int stock,
                             String expirationDate) {
        super(productCode, description, price, stock);
        this.expirationDate = expirationDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return super.toString() + expirationDate + ";";
    }
}
