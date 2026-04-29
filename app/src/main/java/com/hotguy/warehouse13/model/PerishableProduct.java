package com.hotguy.warehouse13.model;

import java.time.format.DateTimeFormatter;

/**
 * Extends {@link Product} with an expiration date.
 * <p>
 * The expiration date is stored as a plain {@code YYYYMMDD} string
 * to avoid serialization issues with {@link java.time.LocalDate}.
 */
public class PerishableProduct extends Product {

    /**
     * Formatter used to parse and validate expiration dates (YYYYMMDD).
     */
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String expirationDate; // YYYYMMDD

    /**
     * @param code           unique product code (8–16 alphanumeric characters)
     * @param description    human-readable product name
     * @param price          unit price (must be &gt;= 0)
     * @param stock          initial stock quantity
     * @param expirationDate expiration date as a YYYYMMDD string
     * @throws IllegalArgumentException if {@code code} does not meet length requirements
     */
    public PerishableProduct(String code, String description, double price,
                             int stock, String expirationDate) {
        super(code, description, price, stock);
        this.expirationDate = expirationDate;
    }

    /**
     * @return expiration date as a YYYYMMDD string
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate expiration date as a YYYYMMDD string
     */
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
