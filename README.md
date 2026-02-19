# Examen de Programación

## Febrero 2026

### Enunciado

Se pide desarrollar una aplicación de gestión de productos para un almacen. La aplicación debe permitir al usuario realizar las siguientes operaciones:

1. Añadir un nuevo producto.
2. Listar todos los productos.
3. Editar el stock de un producto existente.
4. Eliminar un producto.

Cada producto debe tener un código único, una descripción, un precio y un stock. Además, algunos productos pueden ser perecederos, en cuyo caso también deben tener una fecha de caducidad. La aplicación debe validar los datos introducidos por el usuario y manejar adecuadamente los casos de error, como intentar añadir un producto con un código ya existente o editar el stock de un producto que no existe. La interfaz de usuario puede ser de consola, y la aplicación debe seguir una arquitectura MVC (Modelo-Vista-Controlador). Se recomienda utilizar colecciones para almacenar los productos y aplicar principios de programación orientada a objetos para diseñar las clases necesarias.

### Requisitos adicionales

- El código del producto debe ser una cadena alfanumérica única.
- El precio debe ser un número decimal positivo.
- El stock debe ser un número entero no negativo.
- La fecha de caducidad, en caso de ser un producto perecedero, debe seguir el formato AAAAMMDD y ser una fecha válida.
- La aplicación debe manejar excepciones de manera adecuada, mostrando mensajes de error claros al usuario.
- Se debe implementar una función para listar los productos ordenados por su código de forma ascendente.
- La aplicación debe permitir eliminar un producto por su código, mostrando un mensaje de confirmación antes de la eliminación.
- Se debe incluir una función para editar el stock de un producto existente, solicitando al usuario el código del producto y el nuevo stock, y validando que el producto exista antes de realizar la actualización.
- La aplicación debe seguir una arquitectura MVC, separando claramente las responsabilidades entre el modelo (clases que representan los productos), la vista (interfaz de usuario) y el controlador (lógica de la aplicación).

### Criterios de evaluación

- Correcta implementación de las operaciones solicitadas (añadir, listar, editar stock, eliminar).
- Validación adecuada de los datos introducidos por el usuario.
- Manejo adecuado de excepciones y errores.
- Uso correcto de colecciones para almacenar los productos.
- Aplicación de principios de programación orientada a objetos en el diseño de las clases.
- Implementación de una interfaz de usuario clara y funcional.
- Organización del código siguiendo la arquitectura MVC.
- Calidad del código, incluyendo legibilidad, uso de nombres descriptivos y comentarios adecuados.
