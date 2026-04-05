package com.hotguy.warehouse13.view;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.controller.Controller;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


/**
 * Descripción del examen:
 * <p>
 * Hay que terminar un prototipo para gestión del stock de productos de un
 * almacén
 * El proyecto tiene una serie de clases incompletas añadidas, y tendrás que
 * crear también
 * una clase ProductoPerecedero que será un producto que tendrá fecha de
 * caducidad (AAAAMMDD)
 * Tu objetivo: implementar el prototipo hasta hacer funcionales todas las
 * opciones del menú,
 * teniendo en cuenta que:
 * - El controlador tendrá una colección List de productos, donde estarán todos
 * los productos
 * activos
 * - El controlador tendrá otra colección List de productos con los productos
 * retirados (por
 * obsoletos, caducados, o lo que sea)
 * - Cuando añadimos un producto puede ser normal o perecedero
 * - Cuando modificamos el stock de un producto, se solicita el código del
 * producto y el stock
 * - Cuando queremos retirar un producto, pedimos su código
 * - Cuando queremos mostrar productos entre dos precios pedimos el mínimo y el
 * máximo precio
 * que usaremos para obtener de la lista de productos activos, los que cumplan
 * con el filtro
 * - En las demás opciones realizamos lo solicitado sin necesidad de obtener
 * ningún dato por
 * teclado
 * - El controlador es de tipo Singleton
 * - La vista NO usa objetos ni clases del modelo. El proyecto es MVC con capas
 * puras
 * - Hay que implementar los listados como tablas, con una cabecera y los datos
 * en forma de filas
 * pero sin tener la información de los atributos. Ejemplo:
 * Clase,Código Producto,Descripción,Precio,Stock,Caducidad
 *
 * <p>
 * - En el ejemplo anterior, la última línea es una producto perecedero
 * <p>
 * - Podéis reutilizar código del Taller mecánico, el que queráis
 * - El examen dura unas 2 horas y media (se cerrará la entrega a las 10:55)
 * <p>
 * - Antes de esa hora, hay que entregar el proyecto en ZIP en la classroom
 * - Después, se puede seguir trabajando en el proyecto, que completo, se
 * defenderá
 * a la vuelta de semana blanca.
 * <p>
 * Evaluación:
 * 1ª Oportunidad: ZIP entregado en la classroom
 * 2ª Oportunidad: Repositorio depués de semana blanca
 * <p>
 * Cada opción de menú: 1.25 puntos. Total: 10p. Aprobar: 5p.
 *
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to the Warehouse 13 management system.");

        boolean onGoing = true;

        do {
            showMenu();
            int option = getOptionMenu();
            if (option != 0) {
                handleOption(option);
            } else {
                onGoing = false;
            }
        } while (onGoing);
    }

    public static void showMenu() {
        System.out.println("--------------------------------------------------------");
        System.out.println("-                                                      -");
        System.out.println("- Project Menu: Product Warehouse                      -");
        System.out.println("-                                                      -");
        System.out.println("- 0. Exit                                              -");
        System.out.println("- 1. Add new product                                   -");
        System.out.println("- 2. Add/remove stock from a product                   -");
        System.out.println("- 3. List all (products sorted by description)         -");
        System.out.println("- 4. Withdraw a product and its stock                  -");
        System.out.println("- 5. Show products with 0 stock                        -");
        System.out.println("- 6. Show expired products                             -");
        System.out.println("- 7. Show products between two prices                  -");
        System.out.println("- 8. Show withdrawn products                           -");
        System.out.println("- 9. Load data from file                               -");
        System.out.println("- 10. Save data from file                              -");
        System.out.println("-                                                      -");
        System.out.println("-                                                      -");
        System.out.println("--------------------------------------------------------");
    }

    public static void handleOption(int opt) {
        switch (opt) {
            case 1:
                addProduct();
                break;
            case 2:
                editStockForProduct();
                break;
            case 3:
                listProducts();
                break;
            case 4:
                withdrawProduct();
                break;
            case 5:
                listProductsNoStock();
                break;
            case 6:
                listExpiredProducts();
                break;
            case 7:
                listProductsBetweenPrices();
                break;
            case 8:
                listWithdrawnProducts();
                break;
                case 9:
                loadDataFromFile();
                break;
            case 10:
                saveDataToFile();
                break;
            default:
                break;
        }
    }

    public static void addProduct() {
        Map<String, Object> data = new LinkedHashMap<>();
        System.out.println("Enter the product details: ");
        boolean isPerishable = requestData("Is it a perishable product? (y/n): ").equalsIgnoreCase("y");
        data.put("productCode", requestData("Product code: "));
        data.put("description", requestData("Product description: "));
        data.put("price", readDouble("Product price: "));
        data.put("stock", readInt("Product stock: "));
        if (isPerishable) {
            data.put("expirationDate", requestData("Expiration date (YYYYMMDD): "));
        }
        if (Controller.getSingleton().addProduct(isPerishable, new Gson().toJson(data))) {
            System.out.println("Product added successfully.");
        } else {
            System.out.println("Could not add the product. Please check the data format.");
        }
    }

    public static void editStockForProduct() {
        String code = requestData("Product code to update: ");
        int stockChange = readInt("Stock change (negative to remove): ");
        if (Controller.getSingleton().editStockForProduct(code, stockChange)) {
            System.out.println("Stock modified successfully.");
        } else {
            System.out.println("Product with the specified code not found.");
        }
    }

    public static void listProducts() {
        System.out.println("Product list:\n");
        printData(Controller.getSingleton().listProducts());
    }

    public static void withdrawProduct() {
        if (Controller.getSingleton().withdrawProduct(requestData("Product code to withdraw: "))) {
            System.out.println("Product withdrawn successfully.");
        } else {
            System.out.println("Product with the specified code not found.");
        }
    }

    public static void listProductsNoStock() {
        System.out.println("Products out of stock:\n");
        printData(Controller.getSingleton().listProductsNoStock());
    }

    public static void listExpiredProducts() {
        System.out.println("Expired products:\n");
        printData(Controller.getSingleton().listExpiredProducts());
    }

    public static void listProductsBetweenPrices() {
        double minPrice = readDouble("Minimum price: ");
        double maxPrice = readDouble("Maximum price: ");
        System.out.println("Products between " + minPrice + " and " + maxPrice + ":\n");
        printData(Controller.getSingleton().listProductsBetweenPrices(minPrice, maxPrice));
    }

    public static void listWithdrawnProducts() {
        System.out.println("List of Withdrawn products:\n");
        printData(Controller.getSingleton().listWithdrawnProducts());
    }

    public static void loadDataFromFile() {
        Controller.getSingleton().loadDataFromFile();
        System.out.println("Data loaded successfully.");
    }

    public static void saveDataToFile() {
        Controller.getSingleton().saveDataToFile();
        System.out.println("Data saved successfully.");
    }

    private static void printData(String dataset) {
        if (dataset == null || dataset.isEmpty()) return;

        JsonArray jsonArray = JsonParser.parseString(dataset).getAsJsonArray();
        if (jsonArray.isEmpty()) return;

        // Header with the keys of all elements
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            keys.addAll(jsonArray.get(i).getAsJsonObject().keySet());
        }

        printDelimiter(keys);

        for (String k : keys) {
            System.out.printf("| %-20.20s ", k.toUpperCase());
        }
        System.out.println("|");

        printDelimiter(keys);

        // Data — all elements
        for (int i = 0; i < jsonArray.size(); i++) {
            printColumns(jsonArray.get(i).getAsJsonObject(), keys);
        }

        System.out.println("\n-------------- END --------------\n");
    }

    private static void printColumns(JsonObject line, Set<String> keys) {
        for (String clave : keys) {
            String valor = line.has(clave) ? line.get(clave).getAsString() : "-";
            System.out.printf("| %-20.20s ", valor);
        }
        System.out.println("|");
    }

    private static void printDelimiter(Set<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            System.out.print("+----------------------");
        }
        System.out.println("+");
    }

    private static int getOptionMenu() {
        return readInt("Option: ");
    }

    private static int readInt(String info) {
        System.out.print(info);
        return (new Scanner(System.in)).nextInt();
    }

    private static Double readDouble(String info) {
        System.out.print(info);
        return (new Scanner(System.in)).nextDouble();
    }

    private static String requestData(String data) {
        System.out.print(data);
        return (new Scanner(System.in)).nextLine();
    }
}
