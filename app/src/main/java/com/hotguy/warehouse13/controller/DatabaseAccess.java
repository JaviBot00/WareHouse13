package com.hotguy.warehouse13.controller;

import android.util.Log;

import com.hotguy.warehouse13.model.PerishableProduct;
import com.hotguy.warehouse13.model.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccess {

    private static final String TAG = "DatabaseAccess";

    // ── Credenciales hardcodeadas (se sustituirán por llamada API) ──
    private static final String HOST = "192.168.1.X";   // IP de tu PC en la red local
    private static final String PORT = "3306";
    private static final String DATABASE = "warehouse13";
    private static final String USER = "tu_usuario";
    private static final String PASSWORD = "tu_password";

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true";

    // ── Conexión ─────────────────────────────────────────────────────────────

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ── GUARDAR lista activa ─────────────────────────────────────────────────

    /**
     * Reemplaza todos los productos activos en BD con la lista actual.
     * Estrategia: DELETE ALL + INSERT — simple y consistente con el
     * enfoque de fichero (sobrescribe el fichero completo cada vez).
     */
    public static boolean saveProductList(List<Product> products) {
        String deleteSql = "DELETE FROM products";
        String insertSql =
            "INSERT INTO products (product_code, description, price, stock, type, expiration_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql);
                 PreparedStatement ins = conn.prepareStatement(insertSql)) {

                del.executeUpdate();

                for (Product p : products) {
                    ins.setString(1, p.getProductCode());
                    ins.setString(2, p.getDescription());
                    ins.setDouble(3, p.getPrice());
                    ins.setInt(4, p.getStock());

                    if (p instanceof PerishableProduct) {
                        ins.setString(5, "perishableProduct");
                        ins.setString(6, ((PerishableProduct) p).getExpirationDate());
                    } else {
                        ins.setString(5, "product");
                        ins.setNull(6, java.sql.Types.CHAR);
                    }
                    ins.addBatch();
                }
                ins.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                Log.e(TAG, "Error en saveProductList, rollback", e);
                return false;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error de conexión en saveProductList", e);
            return false;
        }
    }

    // ── CARGAR lista activa ──────────────────────────────────────────────────

    public static List<Product> loadProductList() {
        return loadFromTable("products");
    }

    // ── GUARDAR lista retirados ──────────────────────────────────────────────

    public static boolean saveRetiredList(List<Product> retired) {
        String deleteSql = "DELETE FROM retired_products";
        String insertSql =
            "INSERT INTO retired_products (product_code, description, price, stock, type, expiration_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql);
                 PreparedStatement ins = conn.prepareStatement(insertSql)) {

                del.executeUpdate();

                for (Product p : retired) {
                    ins.setString(1, p.getProductCode());
                    ins.setString(2, p.getDescription());
                    ins.setDouble(3, p.getPrice());
                    ins.setInt(4, p.getStock());

                    if (p instanceof PerishableProduct) {
                        ins.setString(5, "perishableProduct");
                        ins.setString(6, ((PerishableProduct) p).getExpirationDate());
                    } else {
                        ins.setString(5, "product");
                        ins.setNull(6, java.sql.Types.CHAR);
                    }
                    ins.addBatch();
                }
                ins.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                Log.e(TAG, "Error en saveRetiredList, rollback", e);
                return false;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error de conexión en saveRetiredList", e);
            return false;
        }
    }

    // ── CARGAR lista retirados ───────────────────────────────────────────────

    public static List<Product> loadRetiredList() {
        return loadFromTable("retired_products");
    }

    // ── Helper privado: leer una tabla y construir objetos ───────────────────

    private static List<Product> loadFromTable(String table) {
        List<Product> result = new ArrayList<>();
        String sql = "SELECT product_code, description, price, stock, type, expiration_date FROM " + table;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String code = rs.getString("product_code");
                String desc = rs.getString("description");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String type = rs.getString("type");
                String expiry = rs.getString("expiration_date");

                if ("perishableProduct".equals(type) && expiry != null) {
                    result.add(new PerishableProduct(code, desc, price, stock, expiry));
                } else {
                    result.add(new Product(code, desc, price, stock));
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error cargando tabla " + table, e);
        }
        return result;
    }
}
