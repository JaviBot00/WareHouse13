# WareHouse13 — Cliente Android

Cliente Android para el sistema de gestión de inventario WareHouse13. Se comunica con el [backend Servlet](https://github.com/tu-usuario/WareHouse13-Servlets) a través de su API REST y mantiene una copia en memoria del inventario para ofrecer una interfaz fluida y responsiva.

---

## Repositorios relacionados

| Componente | Repositorio |
|---|---|
| Backend (Servlets + MySQL + Docker) | [WareHouse13-Servlets](https://github.com/tu-usuario/WareHouse13-Servlets) |

---

## Descripción

La app permite gestionar el inventario del almacén desde un dispositivo Android: añadir productos (incluyendo perecederos con fecha de caducidad), consultar el stock activo, aplicar filtros, editar stock, retirar productos y cargar/exportar datos en JSON. Toda operación mutante se persiste inmediatamente en la base de datos remota a través del backend.

---

## Arquitectura

La app sigue el patrón **MVC** adaptado a Android:

| Capa | Clases | Responsabilidad |
|---|---|---|
| Vista | `AddFragment`, `ListFragment`, `FiltersFragment`, `SettingsFragment` | UI, validación de formularios, diálogos |
| Controlador | `Controller`, `ProductsRVAdapter`, `ProductsRVHolder`, `FilePickerManager` | Mediación entre Vista y datos; adaptadores del RecyclerView |
| Acceso a datos | `DatabaseAccess`, `DataAccess`, `HttpClient` | HTTP contra la API REST; lectura/escritura de ficheros JSON |
| Modelo | `Product`, `PerishableProduct` | Entidades con validación en setters |

### Flujo de una operación típica

```cmd
Fragment
  └─► Controller          (hilo secundario vía ExecutorService)
        ├─► DatabaseAccess   (construye parámetros, interpreta JSON)
        │     └─► HttpClient    (OkHttp — abre conexión, lee respuesta)
        └─► runOnUiThread    (feedback al usuario)
```

Los Fragments nunca acceden a `DatabaseAccess` ni a `HttpClient` directamente.

---

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java |
| UI | Fragments + `BottomNavigationView` + Material 3 |
| Listas | `RecyclerView` con ViewBinding |
| HTTP | OkHttp 4.x (migrado desde `HttpURLConnection`) |
| Serialización | Gson |
| Ficheros | Storage Access Framework (SAF) — sin permisos de almacenamiento |
| Threading | `ExecutorService` + `runOnUiThread` |
| Tests | JUnit 4 |

---

## Pantallas

| Fragmento | Funcionalidad |
|---|---|
| **Añadir** | Formulario para registrar productos normales o perecederos |
| **Inventario** | Lista de productos activos con edición de stock y retirada |
| **Filtros** | Sin stock · Caducados · Rango de precio · Retirados |
| **Ajustes** | Exportar/importar JSON (SAF) · Cargar datos desde la BD remota |

---

## Conexión con el backend

La URL base del servidor se configura en `DatabaseAccess.java`:

```java
private static final String BASE_URL = "http://<IP_DEL_SERVIDOR>:8080/WareHouse13-Servlets";
```

Sustituye `<IP_DEL_SERVIDOR>` por la IP del equipo que ejecuta el backend. Si usas Docker en local, es la IP de la interfaz de red del host (no `localhost`, ya que el emulador o dispositivo físico no resuelven esa dirección).

Los endpoints que consume la app son los mismos que expone el backend:

| Método | Endpoint | Uso en la app |
|---|---|---|
| `GET` | `/listar-activos` | Cargar inventario activo |
| `GET` | `/listar-retirados` | Cargar productos retirados |
| `POST` | `/insertar` | Añadir producto |
| `PUT` | `/actualizar` | Editar stock |
| `PUT` | `/retirar` | Retirar producto |

---

## Estructura del proyecto

```cmd
app/src/main/java/com/hotguy/warehouse13/
├── MainActivity.java
├── model/
│   ├── Product.java
│   └── PerishableProduct.java
├── controller/
│   ├── Controller.java
│   ├── DatabaseAccess.java
│   ├── DataAccess.java
│   ├── HttpClient.java
│   ├── FilePickerManager.java
│   ├── ProductsRVAdapter.java
│   └── ProductsRVHolder.java
└── view/
    ├── AddFragment.java
    ├── ListFragment.java
    ├── FiltersFragment.java
    └── SettingsFragment.java
```

---

## Decisiones de diseño destacadas

**Threading:** todas las operaciones de red son bloqueantes y se ejecutan en un `ExecutorService` de un solo hilo. El resultado vuelve al hilo principal con `runOnUiThread()` para actualizar la UI.

**SAF para ficheros:** la exportación e importación de JSON usa el Storage Access Framework, lo que elimina la necesidad de declarar permisos de almacenamiento en el Manifest. El sistema operativo intermediia la selección de ubicación.

**Persistencia por operación:** no existe un "guardar todo" hacia la BD. Cada acción mutante (añadir, editar stock, retirar) se persiste en el momento en que ocurre. El Controller aplica rollback en memoria si el servidor rechaza la operación.

**OkHttp sobre `HttpURLConnection`:** la capa HTTP está encapsulada en `HttpClient`, lo que permitió migrar de `HttpURLConnection` a OkHttp sin tocar `DatabaseAccess`. El código anterior queda comentado como referencia.

**`onHiddenChanged` en lugar de `onResume`:** la navegación entre Fragments usa `hide()`/`show()` para preservar el estado. `onResume()` no se dispara en este caso; `onHiddenChanged(boolean hidden)` es el hook correcto para refrescar la lista al volver al fragmento.

---

## Tests

Tests unitarios JUnit 4 sobre el modelo, sin dependencias de Android:

| Clase | Cobertura |
|---|---|
| `ProductTest` | Constructor, getters/setters, `changeStock`, `compareTo`, `toString` |
| `PerishableProductTest` | Herencia, campo `expirationDate`, validaciones heredadas |

```bash
./gradlew test
```
