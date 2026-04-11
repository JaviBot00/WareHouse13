package com.hotguy.warehouse13.model;

public class Product implements Comparable<Product> {

    private String productCode;
    private String description;
    private double price;
    private int stock;

    // Constructor
    public Product(String productCode, String description, double price, int stock) {
        setProductCode(productCode); // Use setter for validation
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        if (productCode == null || productCode.length() < 8 || productCode.length() > 16) {
            throw new IllegalArgumentException(
                    "Product code must be alphanumeric and between 8 and 16 characters.");
        }
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price >= 0.0)
            this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void changeStock(int newStock) {
        if (this.stock + newStock < 0)
            return; // we never have negative stock
        this.stock += newStock; // if newStock is negative, units are removed from the warehouse
    }

    @Override
    public int compareTo(Product p) {
        return this.description.compareToIgnoreCase(p.description);
    }
}
