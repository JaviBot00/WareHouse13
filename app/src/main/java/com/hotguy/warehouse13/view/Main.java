package com.hotguy.warehouse13.view;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hotguy.warehouse13.controller.Controlador;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


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
 * Producto,TECL5678X,Teclado mecánico RGB con switches rojos,89.99,45
 * Producto,RATN9012K,Ratón inalámbrico ergonómico con 5 botones,34.50,67
 * Producto,AURC3456L,Auriculares inalámbricos con cancelación de
 * ruido,129.99,23
 * Producto,WEBC7890P,Webcam Full HD 1080p con micrófono integrado,59.90,32
 * Producto,HUBB2345M,Hub USB 3.0 de 4 puertos con alimentación,24.75,56
 * Producto,INK55665F,Toner b/w genérico HP 8750,79.99,18,20260713
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
        System.out.println("Bienvenido al sistema de gestión del Almacén 13.");

        boolean onGoing = true;

        do {
            mostrarMenu();
            int option = getOptionMenu();
            if (option != 0) {
                realizarOpcion(option);
            } else {
                onGoing = false;
            }
        } while (onGoing);
    }

    public static void mostrarMenu() {
        System.out.println("--------------------------------------------------------");
        System.out.println("-                                                      -");
        System.out.println("- Menú del proyecto: Almacén de productos              -");
        System.out.println("-                                                      -");
        System.out.println("- 0. Salir                                             -");
        System.out.println("- 1. Añadir nuevo producto                             -");
        System.out.println("- 2. Añadir/quitar stock a un producto                 -");
        System.out.println("- 3. Listar todo (productos ordenados por descripción) -");
        System.out.println("- 4. Retirar un producto y su stock                    -");
        System.out.println("- 5. Mostrar productos con stock 0                     -");
        System.out.println("- 6. Mostrar productos caducados                       -");
        System.out.println("- 7. Mostrar productos entre dos precios               -");
        System.out.println("- 8. Mostrar productos retirados                       -");
        System.out.println("-                                                      -");
        System.out.println("-                                                      -");
        System.out.println("--------------------------------------------------------");
    }

    public static void realizarOpcion(int opcion) {
        switch (opcion) {
            case 1:
                addProducto();
                break;
            case 2:
                editStockForProducto();
                break;
            case 3:
                listProductos();
                break;
            case 4:
                retirarProducto();
                break;
            case 5:
                listProductosSinStock();
                break;
            case 6:
                listProductosCaducados();
                break;
            case 7:
                listProductosBtwPrecios();
                break;
            case 8:
                listarProductosRetirados();
                break;
            default:
                break;
        }
    }

    public static void addProducto() {
        Map<String, Object> data = new LinkedHashMap<>();
        System.out.println("Introduce el producto a añadir: ");
        boolean isPerecedero = requestData("¿Es un producto perecedero? (s/n): ").equalsIgnoreCase("s");
        data.put("codigoProducto", requestData("Código del producto: "));
        data.put("descripcion", requestData("Descripción del producto: "));
        data.put("precio", readDouble("Precio del producto: "));
        data.put("stock", readInt("Stock del producto: "));
        if (isPerecedero) {
            data.put("fechaCaducidad", requestData("Fecha de caducidad (AAAAMMDD): "));
        }
        if (Controlador.getSingleton().addProducto(isPerecedero, new Gson().toJson(data))) {
            System.out.println("Producto añadido correctamente.");
        } else {
            System.out.println("No se ha podido añadir el producto. Revisa el formato de los datos introducidos.");
        }
    }

    public static void editStockForProducto() {
        String codigoProducto = requestData("Código del producto a modificar: ");
        int stock = readInt("Nuevo stock (en negativo para quitar): ");
        if (Controlador.getSingleton().editStockForProducto(codigoProducto, stock)) {
            System.out.println("Stock modificado correctamente.");
        } else {
            System.out.println("No se ha encontrado el producto con el código indicado.");
        }
    }

    public static void listProductos() {
        System.out.println("Listado of Productos:\n");
        imprimirDatos(Controlador.getSingleton().listProductos());
    }

    public static void retirarProducto() {
        if (Controlador.getSingleton().retirarProduct(requestData("Código del producto a retirar: "))) {
            System.out.println("Producto retirado correctamente.");
        } else {
            System.out.println("No se ha encontrado el producto con el código indicado.");
        }
    }

    public static void listProductosSinStock() {
        System.out.println("Listado of Productos sin stock:\n");
        imprimirDatos(Controlador.getSingleton().listProductsSinStock());
    }

    public static void listProductosCaducados() {
        System.out.println("Listado of Productos caducados:\n");
        imprimirDatos(Controlador.getSingleton().listProductsCaducados());
    }

    public static void listProductosBtwPrecios() {
        double minPrice = readDouble("Precio mínimo: ");
        double maxPrice = readDouble("Precio máximo: ");
        System.out.println("Listado of Productos entre " + minPrice + " y " + maxPrice + ":\n");
        imprimirDatos(Controlador.getSingleton().listProductosBtwPrecios(minPrice, maxPrice));
    }

    public static void listarProductosRetirados() {
        System.out.println("Listado of Productos retirados:\n");
        imprimirDatos(Controlador.getSingleton().listProductosRetirados());
    }

    private static void imprimirDatos(String dataset) {
        if (dataset == null || dataset.isEmpty()) return;

        JsonArray jsonArray = JsonParser.parseString(dataset).getAsJsonArray();
        if (jsonArray.isEmpty()) return;

        // Cabecera con las claves del primer elemento
        JsonObject primero = jsonArray.get(0).getAsJsonObject();
        for (String clave : primero.keySet()) {
            System.out.printf("| %-20.20s ", clave);
        }
//        System.out.println("|");
//        for (int i = 0; i < primero.keySet().size(); i++) {
//            System.out.print("-----------------------");
//        }
//        System.out.println();

        // Datos — todos los elementos
        for (int i = 0; i < jsonArray.size(); i++) {
            imprimirColumnas(jsonArray.get(i).getAsJsonObject());
        }

        System.out.println("\n-------------- END --------------\n");
    }

    private static void imprimirColumnas(JsonObject line) {
        for (String clave : line.keySet()) {
            System.out.printf("| %-20.20s ", line.get(clave));
        }
        System.out.println();
//        System.out.println("|");
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
