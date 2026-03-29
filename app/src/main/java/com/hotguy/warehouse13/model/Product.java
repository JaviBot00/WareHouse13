package com.hotguy.warehouse13.model;

public class Product implements Comparable<Product> {

    private static final String CSV_FORMAT = "productCode;description;price;stock";

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

    public static String getCsvFormat() {
        return CSV_FORMAT;
    }

    public static Product loadFromCSVPlus(String data) {
        String productCode = "";
        String description = "";
        double price = 0;
        int stock = 0;

        String[] pair = data.split(";");
        for (String pr : pair) {
            String[] value = pr.split("=");
            if (value[0].equals("productCode"))
                productCode = value[1];
            if (value[0].equals("description"))
                description = value[1];
            if (value[0].equals("price"))
                price = Double.parseDouble(value[1]);
            if (value[0].equals("stock"))
                stock = Integer.parseInt(value[1]);
        }
        return new Product(productCode, description, price, stock);
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

    // Method to display product information. CSV Plus
    @Override
    public String toString() {
        return productCode + ";"
            + description + ";"
            + price + ";"
            + stock + ";";
    }

    @Override
    public int compareTo(Product p) {
        return this.description.compareToIgnoreCase(p.description);
    }
}
