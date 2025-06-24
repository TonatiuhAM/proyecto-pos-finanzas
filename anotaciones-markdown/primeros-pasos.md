# Plan de Desarrollo del Proyecto POS

Este documento describe el plan de ataque recomendado para desarrollar la aplicación, siguiendo un enfoque **Backend-First** incremental.

## Fase 1: La Base - El Módulo de Inventario (Backend)

Comenzaremos construyendo el núcleo del sistema: la gestión de productos. El objetivo es implementar la funcionalidad completa para Crear, Leer, Actualizar y Eliminar (CRUD) productos.

### Paso 1.1: Crear el Modelo de Datos (`Entity`)

1.  En tu proyecto de backend, navega a `src/main/java/com/tu_paquete/`.
2.  Crea un nuevo paquete llamado `models`.
3.  Dentro de `models`, crea una nueva clase de Java: **`Producto.java`**.
4.  Define la clase con las anotaciones de Jakarta Persistence (JPA) para mapearla a una tabla en la base de datos.

<!-- end list -->

```java
// Archivo: backend/src/main/java/com/tu_paquete/models/Producto.java

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private int cantidad;

    private double precio;

    // --- Constructores, Getters y Setters ---
    // (Puedes usar Lombok para generarlos automáticamente o tu IDE)
    public Producto() {
    }

    public Producto(String nombre, int cantidad, double precio) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
```

### Paso 1.2: Crear el Acceso a Datos (`Repository`)

1.  Crea un nuevo paquete llamado `repositories`.
2.  Dentro, crea una nueva **interfaz** de Java: **`ProductoRepository.java`**.
3.  Esta interfaz extenderá de `JpaRepository` para obtener toda la funcionalidad CRUD de forma automática.

<!-- end list -->

```java
// Archivo: backend/src/main/java/com/tu_paquete/repositories/ProductoRepository.java

import com.tu_paquete.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // ¡Magia! No necesitas escribir nada más aquí.
    // Spring Data JPA implementará automáticamente los métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
}
```

### Paso 1.3: Crear el Controlador de la API (`Controller`)

1.  Crea un nuevo paquete llamado `controllers`.
2.  Dentro, crea una nueva clase: **`ProductoController.java`**.
3.  Define los endpoints públicos para interactuar con los productos.

<!-- end list -->

```java
// Archivo: backend/src/main/java/com/tu_paquete/controllers/ProductoController.java

import com.tu_paquete.models.Producto;
import com.tu_paquete.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos") // Todas las rutas aquí empezarán con /api/productos
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // Endpoint para GET /api/productos - Obtener todos los productos
    @GetMapping
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    // Endpoint para GET /api/productos/{id} - Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para POST /api/productos - Crear un nuevo producto
    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoRepository.save(producto);
    }

    // Endpoint para PUT /api/productos/{id} - Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto detallesProducto) {
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setNombre(detallesProducto.getNombre());
                    producto.setCantidad(detallesProducto.getCantidad());
                    producto.setPrecio(detallesProducto.getPrecio());
                    Producto actualizado = productoRepository.save(producto);
                    return ResponseEntity.ok(actualizado);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para DELETE /api/productos/{id} - Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(producto -> {
                    productoRepository.delete(producto);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
```

### Paso 1.4: Probar el Backend de Forma Aislada

Antes de continuar, es fundamental verificar que la API funciona correctamente.

1.  Levanta todo el entorno con Docker:

    ```bash
    docker-compose up --build -d
    ```

2.  Usa una herramienta como **Postman**, **Insomnia** o **Thunder Client** (extensión para VSCode).

3.  **Prueba \#1: Obtener todos los productos (GET)**

    - **Método:** `GET`
    - **URL:** `http://localhost:8080/api/productos`
    - **Resultado esperado:** Un array JSON vacío: `[]`.

4.  **Prueba \#2: Crear un producto (POST)**

    - **Método:** `POST`
    - **URL:** `http://localhost:8080/api/productos`
    - **Body (JSON):**
      ```json
      {
        "nombre": "Bolillo",
        "cantidad": 50,
        "precio": 2.5
      }
      ```
    - **Resultado esperado:** El objeto del producto recién creado con su `id`.

5.  **Prueba \#3: Verificar la creación (GET)**

    - Vuelve a ejecutar la petición del paso 3.
    - **Resultado esperado:** Un array JSON que contiene el "Bolillo" que acabas de crear.

## Fase 2: El Puente - Conectar el Frontend

Con la API funcionando, ahora conectamos la interfaz de usuario para que muestre los datos.

### Paso 2.1: Crear un Servicio de API en React

1.  En tu proyecto de `frontend`, dentro de `src/`, crea una carpeta `services`.
2.  Dentro de `services`, crea un archivo: **`productoService.ts`**.

<!-- end list -->

```typescript
// Archivo: frontend/src/services/productoService.ts
import axios from "axios";

// Definimos un tipo para el producto para usar TypeScript
export interface Producto {
  id?: number;
  nombre: string;
  cantidad: number;
  precio: number;
}

const apiClient = axios.create({
  baseURL: "/api", // Vite y Nginx redirigirán esto a http://localhost:8080/api
  headers: {
    "Content-Type": "application/json",
  },
});

export const getProductos = () => {
  return apiClient.get<Producto[]>("/productos");
};

export const createProducto = (producto: Producto) => {
  return apiClient.post<Producto>("/productos", producto);
};

// Aquí puedes añadir las funciones para updateProducto y deleteProducto
```

### Paso 2.2: Consumir el Servicio desde un Componente de React

1.  Abre el componente principal de React (ej. `src/App.tsx`).
2.  Usa los hooks `useState` y `useEffect` para obtener y mostrar la lista de productos.

<!-- end list -->

```tsx
// Archivo: frontend/src/App.tsx
import React, { useState, useEffect } from "react";
import { getProductos, Producto } from "./services/productoService";
import "./App.css"; // O tu archivo de estilos

function App() {
  const [productos, setProductos] = useState<Producto[]>([]);

  useEffect(() => {
    // Carga los productos cuando el componente se monta
    fetchProductos();
  }, []);

  const fetchProductos = () => {
    getProductos()
      .then((response) => {
        setProductos(response.data);
      })
      .catch((error) => console.error("Error al obtener productos:", error));
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Punto de Venta - Inventario</h1>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Cantidad</th>
              <th>Precio</th>
            </tr>
          </thead>
          <tbody>
            {productos.map((producto) => (
              <tr key={producto.id}>
                <td>{producto.id}</td>
                <td>{producto.nombre}</td>
                <td>{producto.cantidad}</td>
                <td>${producto.precio.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {/* Aquí puedes agregar un formulario para crear nuevos productos */}
      </header>
    </div>
  );
}

export default App;
```

---

## Fases Siguientes (Resumen)

- ### **Fase 3: Construir el Punto de Venta (POS)**

  - Crear entidades `Venta` y `DetalleVenta` en el backend.
  - Crear el endpoint `POST /api/ventas` con lógica **`@Transactional`** para asegurar que el descuento de inventario y el registro de la venta ocurran de forma atómica.
  - Desarrollar la interfaz del carrito de compras en el frontend.

- ### **Fase 4: Gestión de Usuarios y Deudas**

  - Integrar **Spring Security** en el backend para la autenticación y autorización de usuarios.
  - Crear las entidades `Usuario`, `Rol`, `Cliente`, `Proveedor`, `Deuda`, etc.
  - Proteger los endpoints de la API según los roles de usuario.
  - Construir las interfaces de login y los módulos para gestionar clientes y deudas.
