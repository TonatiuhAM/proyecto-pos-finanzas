# Prompts utilizados

### Para pasar de la BD a Java las tablas

Actúa como un desarrollador experto en Java con Spring Boot, Spring Data JPA y Hibernate. Tu objetivo es generar el código completo para una API REST a partir del esquema SQL que te proporcionaré.

Debes seguir estrictamente las siguientes reglas y mejores prácticas de arquitectura para TODO el código que generes:

**Reglas Generales de Arquitectura:**

1.  **Estructura de Paquetes:** Todo el código Java debe estar dentro del paquete base `com.posfin.pos_finanzas_backend`. Utiliza los siguientes sub-paquetes:

    - `.models` para las clases de Entidad.
    - `.repositories` para las interfaces de Repositorio.
    - `.controllers` para las clases de Controlador.
    - `.dtos` para las clases de Data Transfer Objects.

2.  **Modelo de Datos (Entidades):**

    - Usa anotaciones de **Jakarta Persistence (JPA)**.
    - Cada clase de modelo debe tener la anotación `@Entity` y `@Table(name = "nombre_de_tabla")`.
    - La clave primaria (`id`) debe ser de tipo `String` y debe autogenerarse con un **UUID** usando el método `@PrePersist`.
    - **Manejo de Claves Foráneas (¡MUY IMPORTANTE!):** Las columnas que son claves foráneas **NUNCA** deben ser mapeadas como un simple `String`. Deben ser mapeadas como una **relación de objeto** usando la anotación `@ManyToOne` y `@JoinColumn`.
      - **Ejemplo:** Si la tabla `inventarios` tiene una columna `producto_id`, en la clase `Inventarios.java` la propiedad debe ser `private Productos producto;`.
      - Usa `fetch = FetchType.LAZY` en todas las relaciones `@ManyToOne` para optimizar el rendimiento y evitar problemas de carga.
      - Usa `@JoinColumn(name = "nombre_de_columna_fk", nullable = false)` para especificar la columna de la base de datos.

3.  **API REST (Controladores):**

    - Sigue las convenciones RESTful. Usa `@RestController` y `@RequestMapping("/api/recurso")`.
    - Implementa los cinco endpoints CRUD básicos: `GET` (todos), `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`.

4.  **Manejo de Relaciones y Prevención de Errores de Serialización (Uso de DTOs):**

    - **Problema a evitar:** Retornar directamente entidades con relaciones `LAZY` causa errores de serialización (`LazyInitializationException` o errores con `HibernateProxy`).
    - **Regla:** Para cualquier endpoint `GET` que devuelva una entidad que tenga relaciones (`@ManyToOne`), **debes crear y retornar un DTO (Data Transfer Object)**.
    - **Implementación del DTO:**
      - El DTO debe ser una clase Java simple (POJO) en el paquete `.dtos`.
      - Debe contener solo los campos primitivos que el frontend necesita.
      - Debe "aplanar" las relaciones. Por ejemplo, `InventarioDTO` no tendrá un objeto `Productos`, sino campos como `private String productoId;` y `private String productoNombre;`.
    - **Lógica en el Controlador:** El método `GET` del controlador debe:
      1.  Obtener la(s) Entidad(es) del repositorio.
      2.  Crear una nueva instancia del DTO.
      3.  Mapear los datos de la Entidad al DTO manualmente. **Este paso es crucial porque al hacer `entidad.getProducto().getNombre()` se fuerza la carga de los datos de la relación de forma controlada.**
      4.  Retornar el DTO.

5.  **Lógica de Creación/Actualización (POST/PUT):**
    - Para los endpoints que crean o actualizan entidades con relaciones, el `@RequestBody` no debe ser la entidad directamente si eso complica la lógica.
    - El método del controlador debe recibir los IDs de las entidades relacionadas en el JSON, buscarlas en sus respectivos repositorios para obtener las instancias completas y luego asignarlas a la entidad principal antes de guardarla.

---

**Instrucción Principal:**

A continuación, te proporciono mi esquema de base de datos SQL. Para **CADA TABLA**, genera los archivos `.java` necesarios siguiendo todas las reglas mencionadas anteriormente. Asegúrate de incluir:

1.  **El Modelo (`.models/NombreTabla.java`):** Con las anotaciones JPA y las relaciones `@ManyToOne` correctas.
2.  **El Repositorio (`.repositories/NombreTablaRepository.java`):** Interfaz que extiende `JpaRepository`.
3.  **El Controlador (`.controllers/NombreTablaController.java`):** Con los 5 endpoints CRUD. Si la entidad tiene relaciones, los métodos `GET` deben usar y retornar un DTO.
4.  **El DTO (`.dtos/NombreTablaDTO.java`):** **Crea este archivo únicamente si la entidad correspondiente tiene relaciones `@ManyToOne`.**

NOTAS EXTRA:
Obviamente evita crear nuevamente las tablas que ya estan creadas, que en este caso son todos los catalogs, y corrige tambien el funcionamiento de Productos, ya que esta no implementa de manera correcta el DTO ni las funciones anteriormnte emncionadas cuando hay FKs.
--- MI ESQUEMA SQL ---

```sql
-- --------------------------------------------------
-- Catálogos
-- --------------------------------------------------

CREATE TABLE categorias_productos (
  id          VARCHAR2(36)                    NOT NULL,
  categoria   VARCHAR2(30)                    NOT NULL,
  CONSTRAINT pk_categorias_productos PRIMARY KEY (id)
);

CREATE TABLE estados (
  id          VARCHAR2(36)                    NOT NULL,
  estado      VARCHAR2(20)                    NOT NULL,
  CONSTRAINT pk_estados PRIMARY KEY (id)
);

CREATE TABLE categoria_personas (
  id          VARCHAR2(36)                    NOT NULL,
  categoria   VARCHAR2(30)                    NOT NULL,
  CONSTRAINT pk_categoria_personas PRIMARY KEY (id)
);

CREATE TABLE roles (
  id          VARCHAR2(36)                    NOT NULL,
  roles       VARCHAR2(20)                    NOT NULL,
  CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE metodos_pago (
  id            VARCHAR2(36)                  NOT NULL,
  metodo_pago   VARCHAR2(30)                  NOT NULL,
  CONSTRAINT pk_metodos_pago PRIMARY KEY (id)
);

CREATE TABLE tipo_movimientos (
  id          VARCHAR2(36)                    NOT NULL,
  movimiento  VARCHAR2(12)                    NOT NULL,
  CONSTRAINT pk_tipo_movimientos PRIMARY KEY (id)
);

CREATE TABLE ubicaciones (
  id          VARCHAR2(36)                    NOT NULL,
  nombre      VARCHAR2(30)                    NOT NULL,
  ubicacion   VARCHAR2(30)                    NOT NULL,
  CONSTRAINT pk_ubicaciones PRIMARY KEY (id)
);

CREATE TABLE workspaces (
  id          VARCHAR2(36)                    NOT NULL,
  nombre      VARCHAR2(30)                    NOT NULL,
  CONSTRAINT pk_workspaces PRIMARY KEY (id)
);

-- --------------------------------------------------
-- Tablas principales
-- --------------------------------------------------

CREATE TABLE personas (
  id                      VARCHAR2(36)                    NOT NULL,
  nombre                  VARCHAR2(30)                    NOT NULL,
  apellido_paterno        VARCHAR2(30),
  apellido_materno        VARCHAR2(30),
  rfc                     VARCHAR2(10),
  telefono                VARCHAR2(20)                    NOT NULL,
  email                   VARCHAR2(32),
  estados_id              VARCHAR2(36)                    NOT NULL,
  categoria_personas_id   VARCHAR2(36)                    NOT NULL,
  CONSTRAINT pk_personas PRIMARY KEY (id),
  CONSTRAINT fk_pers_estados FOREIGN KEY (estados_id)
    REFERENCES estados(id),
  CONSTRAINT fk_pers_catpers FOREIGN KEY (categoria_personas_id)
    REFERENCES categoria_personas(id)
);

CREATE TABLE productos (
  id                          VARCHAR2(36)            NOT NULL,
  nombre                      VARCHAR2(30)            NOT NULL,
  categorias_productos_id     VARCHAR2(36)            NOT NULL,
  proveedor_id                VARCHAR2(36)            NOT NULL,
  estados_id                  VARCHAR2(36)            NOT NULL,
  CONSTRAINT pk_productos PRIMARY KEY (id),
  CONSTRAINT fk_prod_catprod FOREIGN KEY (categorias_productos_id)
    REFERENCES categorias_productos(id),
  CONSTRAINT fk_prod_proveedor FOREIGN KEY (proveedor_id)
    REFERENCES personas(id),
  CONSTRAINT fk_prod_estados FOREIGN KEY (estados_id)
    REFERENCES estados(id)
);

CREATE TABLE historial_precios (
  id                   VARCHAR2(36)                NOT NULL,
  productos_id         VARCHAR2(36)                NOT NULL,
  precio               NUMBER                      NOT NULL,
  fecha_de_registro    TIMESTAMP WITH TIME ZONE    NOT NULL,
  CONSTRAINT pk_historial_precios PRIMARY KEY (id),
  CONSTRAINT fk_hp_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id)
);

CREATE TABLE historial_costos (
  id                   VARCHAR2(36)                NOT NULL,
  productos_id         VARCHAR2(36)                NOT NULL,
  costo                NUMBER                      NOT NULL,
  fecha_de_registro    TIMESTAMP WITH TIME ZONE    NOT NULL,
  CONSTRAINT pk_historial_costos PRIMARY KEY (id),
  CONSTRAINT fk_hc_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id)
);

CREATE TABLE usuarios (
  id          VARCHAR2(36)            NOT NULL,
  nombre      VARCHAR2(50)            NOT NULL,
  contrasena  VARCHAR2(100)           NOT NULL,
  telefono    VARCHAR2(50)            NOT NULL,
  roles_id    VARCHAR2(36)            NOT NULL,
  estados_id  VARCHAR2(36)            NOT NULL,
  CONSTRAINT pk_usuarios PRIMARY KEY (id),
  CONSTRAINT fk_us_roles FOREIGN KEY (roles_id)
    REFERENCES roles(id),
  CONSTRAINT fk_us_estados FOREIGN KEY (estados_id)
    REFERENCES estados(id)
);

CREATE TABLE ordenes_de_ventas (
  id                VARCHAR2(36)                NOT NULL,
  personas_id       VARCHAR2(36)                NOT NULL,
  fecha_orden       TIMESTAMP WITH TIME ZONE    NOT NULL,
  usuarios_id       VARCHAR2(36)                NOT NULL,
  total_venta       NUMBER                      NOT NULL,
  metodos_pago_id   VARCHAR2(36)                NOT NULL,
  CONSTRAINT pk_ord_ventas PRIMARY KEY (id),
  CONSTRAINT fk_ov_personas FOREIGN KEY (personas_id)
    REFERENCES personas(id),
  CONSTRAINT fk_ov_usuarios FOREIGN KEY (usuarios_id)
    REFERENCES usuarios(id),
  CONSTRAINT fk_ov_metodo FOREIGN KEY (metodos_pago_id)
    REFERENCES metodos_pago(id)
);

CREATE TABLE detalles_ordenes_de_ventas (
  id                         VARCHAR2(36)   NOT NULL,
  ordenes_de_ventas_id       VARCHAR2(36)   NOT NULL,
  productos_id               VARCHAR2(36)   NOT NULL,
  historial_precios_id       VARCHAR2(36)   NOT NULL,
  cantidad_pz                NUMBER         NOT NULL,
  cantidad_kg                NUMBER         NOT NULL,
  CONSTRAINT pk_det_ordv PRIMARY KEY (id),
  CONSTRAINT fk_dov_ordv FOREIGN KEY (ordenes_de_ventas_id)
    REFERENCES ordenes_de_ventas(id),
  CONSTRAINT fk_dov_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id),
  CONSTRAINT fk_dov_hp FOREIGN KEY (historial_precios_id)
    REFERENCES historial_precios(id)
);

CREATE TABLE historial_pagos_clientes (
  id                   VARCHAR2(36)                NOT NULL,
  personas_id          VARCHAR2(36)                NOT NULL,
  orden_de_venta_id    VARCHAR2(36)                NOT NULL,
  monto_pagado         NUMBER                      NOT NULL,
  fecha                TIMESTAMP WITH TIME ZONE    NOT NULL,
  CONSTRAINT pk_hist_pagos PRIMARY KEY (id),
  CONSTRAINT fk_hpc_personas FOREIGN KEY (personas_id)
    REFERENCES personas(id),
  CONSTRAINT fk_hpc_ordv FOREIGN KEY (orden_de_venta_id)
    REFERENCES ordenes_de_ventas(id)
);

CREATE TABLE ordenes_de_compras (
  id                VARCHAR2(36)                NOT NULL,
  persona_id        VARCHAR2(36)                NOT NULL,
  fecha_orden       TIMESTAMP WITH TIME ZONE    NOT NULL,
  estados_id        VARCHAR2(36)                NOT NULL,
  total_compra      NUMBER                      NOT NULL,
  CONSTRAINT pk_ord_compras PRIMARY KEY (id),
  CONSTRAINT fk_oc_personas FOREIGN KEY (persona_id)
    REFERENCES personas(id),
  CONSTRAINT fk_oc_estados FOREIGN KEY (estados_id)
    REFERENCES estados(id)
);

CREATE TABLE detalles_ordenes_de_compras (
  id                          VARCHAR2(36)   NOT NULL,
  ordenes_de_compras_id       VARCHAR2(36)   NOT NULL,
  producto_id                 VARCHAR2(36)   NOT NULL,
  cantidad_pz                 NUMBER         NOT NULL,
  cantidad_kg                 NUMBER         NOT NULL,
  historial_costos_id         VARCHAR2(36)   NOT NULL,
  CONSTRAINT pk_det_ordc PRIMARY KEY (id),
  CONSTRAINT fk_doc_ordc FOREIGN KEY (ordenes_de_compras_id)
    REFERENCES ordenes_de_compras(id),
  CONSTRAINT fk_doc_prod FOREIGN KEY (producto_id)
    REFERENCES productos(id),
  CONSTRAINT fk_doc_hc FOREIGN KEY (historial_costos_id)
    REFERENCES historial_costos(id)
);

CREATE TABLE historial_cargos_proveedores (
  id                        VARCHAR2(36)                NOT NULL,
  personas_id               VARCHAR2(36)                NOT NULL,
  ordenes_de_compras_id     VARCHAR2(36)                NOT NULL,
  monto_pagado              NUMBER                      NOT NULL,
  fecha                     TIMESTAMP WITH TIME ZONE    NOT NULL,
  CONSTRAINT pk_hist_cargos PRIMARY KEY (id),
  CONSTRAINT fk_hcp_personas FOREIGN KEY (personas_id)
    REFERENCES personas(id),
  CONSTRAINT fk_hcp_ordc FOREIGN KEY (ordenes_de_compras_id)
    REFERENCES ordenes_de_compras(id)
);

CREATE TABLE inventarios (
  id              VARCHAR2(36)   NOT NULL,
  productos_id    VARCHAR2(36)   NOT NULL,
  ubicaciones_id  VARCHAR2(36)   NOT NULL,
  cantidad_pz     NUMBER         NOT NULL,
  cantidad_kg     NUMBER         NOT NULL,
  cantidad_minima NUMBER         NOT NULL,
  cantidad_maxima NUMBER         NOT NULL,
  CONSTRAINT pk_inventarios PRIMARY KEY (id),
  CONSTRAINT fk_inv_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id),
  CONSTRAINT fk_inv_ubica FOREIGN KEY (ubicaciones_id)
    REFERENCES ubicaciones(id)
);

CREATE TABLE movimientos_inventarios (
  id                   VARCHAR2(36)                NOT NULL,
  productos_id         VARCHAR2(36)                NOT NULL,
  ubicaciones_id       VARCHAR2(36)                NOT NULL,
  tipo_movimientos_id  VARCHAR2(36)                NOT NULL,
  cantidad             NUMBER                      NOT NULL,
  fecha_movimiento     TIMESTAMP WITH TIME ZONE    NOT NULL,
  usuarios_id          VARCHAR2(36)                NOT NULL,
  clave_movimiento     VARCHAR2(30)                NOT NULL,
  CONSTRAINT pk_mov_inv PRIMARY KEY (id),
  CONSTRAINT fk_mi_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id),
  CONSTRAINT fk_mi_ubica FOREIGN KEY (ubicaciones_id)
    REFERENCES ubicaciones(id),
  CONSTRAINT fk_mi_tipomv FOREIGN KEY (tipo_movimientos_id)
    REFERENCES tipo_movimientos(id),
  CONSTRAINT fk_mi_user FOREIGN KEY (usuarios_id)
    REFERENCES usuarios(id)
);

CREATE TABLE ordenes_workspace (
  id                    VARCHAR2(36)                NOT NULL,
  workspace_id          VARCHAR2(36)                NOT NULL,
  productos_id          VARCHAR2(36)                NOT NULL,
  cantidad_pz           NUMBER                      NOT NULL,
  cantidad_kg           NUMBER                      NOT NULL,
  historial_precios_id  VARCHAR2(36)                NOT NULL,
  CONSTRAINT pk_ord_ws PRIMARY KEY (id),
  CONSTRAINT fk_ow_ws FOREIGN KEY (workspace_id)
    REFERENCES workspaces(id),
  CONSTRAINT fk_ow_prod FOREIGN KEY (productos_id)
    REFERENCES productos(id),
  CONSTRAINT fk_ow_hp FOREIGN KEY (historial_precios_id)
    REFERENCES historial_precios(id)
);
```

### Para primer diseño de inventario

Actúa como un desarrollador experto en frontend utilizando React, TypeScript, Vite y la librería `axios`. Tu objetivo es crear un componente completo y funcional para una página de "Gestión de Inventarios" que se conecte con una API de Spring Boot ya existente.

**Contexto y Arquitectura del Proyecto:**

1.  **Conexión con la API:**

    - Toda la comunicación con el backend se debe centralizar en un archivo de servicio: `src/services/inventarioService.ts`.
    - Este servicio utilizará una instancia de `axios` configurada con una `baseURL` de `/api`. No es necesario incluir `http://localhost:8080` en las URLs de las peticiones, ya que un proxy de Nginx en Docker se encarga de la redirección.
    - La API ya expone endpoints RESTful para el CRUD de inventarios en `/api/inventarios`.

2.  **Manejo de Datos y Tipado:**

    - El backend devuelve DTOs para las peticiones `GET`. Debes crear las interfaces de TypeScript correspondientes para asegurar un tipado fuerte en todo el componente (ej. `InventarioDTO`).
    - Las peticiones `POST` y `PUT` para crear o actualizar un registro de inventario esperan un JSON con objetos anidados para las relaciones, como se muestra: `{ "producto": { "id": "uuid" }, "ubicacion": { "id": "uuid" }, ... }`.

3.  **Estilo y Diseño:**
    - Utiliza clases de CSS simples y descriptivas (ej. `inventario-container`, `inventario-table`, `btn-primary`).
    - El diseño general debe seguir la estructura y estilo del código guía que se proporcionará más abajo.

**Requerimientos del Componente `Inventario.tsx`:**

1.  **Estado:** Usa el hook `useState` para manejar la lista de registros del inventario, el estado de los formularios y la visibilidad de los modales.
2.  **Carga de Datos:** Usa el hook `useEffect` para llamar al servicio y obtener todos los registros del inventario cuando el componente se monte por primera vez.
3.  **Visualización:**
    - Renderiza los datos en una tabla (`<table>`).
    - La tabla debe tener columnas para: ID, Producto, Ubicación, Cantidad (Pz), Cantidad Mínima, Cantidad Máxima y una columna de **Acciones**.
4.  **Formulario de Creación/Edición:**
    - Incluye un botón "Añadir Nuevo Inventario".
    - Al hacer clic en "Añadir" o "Editar", debe aparecer un formulario (preferiblemente en un **modal/popup** para una mejor experiencia de usuario).
    - El formulario debe contener campos para todas las propiedades de un registro de inventario.
    - **Importante:** Los campos para "Producto" y "Ubicación" deben ser menús desplegables (`<select>`). Estos desplegables se deben poblar haciendo peticiones `GET` a sus respectivos endpoints (`/api/productos` y `/api/ubicaciones`) cuando el formulario se carga.
5.  **Funcionalidad CRUD:**
    - **Crear:** El formulario de "Añadir" debe hacer una petición `POST` al backend.
    - **Editar:** El botón "Editar" de cada fila debe abrir el formulario modal, precargado con los datos de ese registro. Al guardar, debe hacer una petición `PUT`.
    - **Eliminar:** El botón "Eliminar" debe pedir confirmación y luego hacer una petición `DELETE`.
    - Después de cada operación exitosa (Crear, Editar, Eliminar), la tabla de inventarios se debe **actualizar automáticamente** para reflejar los cambios.

--- MI CÓDIGO GUÍA DE DISEÑO ---

```tsx
import React, from 'react';
import './InventarioScreen.css';

// Definimos el tipo de dato para un producto del inventario
type Producto = {
  id: number;
  nombre: string;
  cantidad: number;
  categoria: string;
  proveedor: string;
  ubicacion: string;
  precioVenta: number;
  precioCompra: number;
  estado: 'activo' | 'inactivo';
};

// Datos de ejemplo para la tabla
const productosDeEjemplo: Producto[] = [
  { id: 1, nombre: 'Laptop Pro 15"', cantidad: 25, categoria: 'Electrónica', proveedor: 'TechCorp', ubicacion: 'Almacén A', precioVenta: 1500.00, precioCompra: 1200.00, estado: 'activo' },
  { id: 2, nombre: 'Teclado Mecánico RGB', cantidad: 50, categoria: 'Accesorios', proveedor: 'GamerGear', ubicacion: 'Estante B-2', precioVenta: 120.50, precioCompra: 85.00, estado: 'activo' },
  { id: 3, nombre: 'Monitor UltraWide 34"', cantidad: 15, categoria: 'Monitores', proveedor: 'DisplayInc', ubicacion: 'Almacén A', precioVenta: 799.99, precioCompra: 650.00, estado: 'activo' },
  { id: 4, nombre: 'Silla Ergonómica', cantidad: 30, categoria: 'Mobiliario', proveedor: 'OfficeComfort', ubicacion: 'Bodega C', precioVenta: 350.00, precioCompra: 280.00, estado: 'inactivo' },
];

const InventarioScreen: React.FC = () => {

  // Lógica para manejar la apertura del modal (a implementar)
  const handleCrearNuevoProducto = () => {
    // NOTA: Aquí se implementaría la lógica para abrir el modal/dialogo.
    alert('Abriendo diálogo para crear un nuevo producto...');
  };

  // Lógica para editar un producto
  const handleEditar = (id: number) => {
    alert(`Editando producto con ID: ${id}`);
  };

  // Lógica para eliminar (cambiar estado) un producto
  const handleEliminar = (id: number) => {
    // NOTA: Como indica el diseño, esto no borra el registro,
    // solo cambia su estado de 'activo' a 'inactivo'.
    alert(`Cambiando estado del producto con ID: ${id} a inactivo.`);
  };


  return (
    <div className="inventory-page">
      <button className="back-to-home-btn">Regresar a Inicio</button>

      <div className="inventory-container">
        <header className="inventory-header">
          <h1 className="inventory-title">MANEJO DE INVENTARIO</h1>
          <button
            className="create-product-btn"
            onClick={handleCrearNuevoProducto}
          >
            CREAR NUEVO PRODUCTO
          </button>
        </header>

        <main className="inventory-table-container">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Cantidad</th>
                <th>Categoría</th>
                <th>Proveedor</th>
                <th>Ubicación</th>
                <th>Precio venta</th>
                <th>Precio Compra</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {productosDeEjemplo.map((producto) => (
                <tr key={producto.id} className={producto.estado === 'inactivo' ? 'inactive-row' : ''}>
                  <td>{producto.nombre}</td>
                  <td>{producto.cantidad}</td>
                  <td>{producto.categoria}</td>
                  <td>{producto.proveedor}</td>
                  <td>{producto.ubicacion}</td>
                  <td>{`$${producto.precioVenta.toFixed(2)}`}</td>
                  <td>{`$${producto.precioCompra.toFixed(2)}`}</td>
                  <td>
                    <div className="action-buttons">
                      <button className="action-btn edit-btn" onClick={() => handleEditar(producto.id)}>
                        Editar
                      </button>
                      <button className="action-btn delete-btn" onClick={() => handleEliminar(producto.id)}>
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </main>
      </div>
    </div>
  );
};

export default InventarioScreen;
```

```css
/* Estilos generales para la página */
.inventory-page {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, sans-serif;
  background-color: #f4f5f7;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start; /* Alinea el botón de regreso a la izquierda */
  min-height: 100vh;
}

/* Botón para regresar a Inicio */
.back-to-home-btn {
  background-color: #ffffff;
  border: 1px solid #cccccc;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  cursor: pointer;
  margin-bottom: 1.5rem;
  transition: background-color 0.2s;
}

.back-to-home-btn:hover {
  background-color: #f0f0f0;
}

/* Contenedor principal del inventario */
.inventory-container {
  background-color: #ffffff;
  border: 1px solid #dfe1e6;
  border-radius: 12px;
  padding: 2rem;
  width: 100%;
  max-width: 1400px; /* Ancho máximo para el contenedor */
  margin: 0 auto; /* Centra el contenedor si la página es más ancha */
  box-sizing: border-box;
}

/* Cabecera del contenedor */
.inventory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.inventory-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #172b4d;
  margin: 0;
}

/* Botón para crear nuevo producto */
.create-product-btn {
  background-color: #0052cc;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.75rem 1.5rem;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.create-product-btn:hover {
  background-color: #0065ff;
}

/* Contenedor y estilos de la tabla */
.inventory-table-container {
  overflow-x: auto; /* Permite scroll horizontal en pantallas pequeñas */
}

.inventory-table {
  width: 100%;
  border-collapse: collapse; /* Une los bordes de las celdas */
}

.inventory-table th,
.inventory-table td {
  border: 1px solid #dfe1e6;
  padding: 0.8rem 1rem;
  text-align: left;
  vertical-align: middle;
  font-size: 0.9rem;
  color: #42526e;
}

.inventory-table thead th {
  background-color: #f9fafb; /* Un fondo muy sutil para la cabecera */
  font-weight: 600;
  color: #172b4d;
}

/* Estilo para filas marcadas como inactivas */
.inventory-table tr.inactive-row {
  background-color: #fafafa;
  color: #999999;
}

.inventory-table tr.inactive-row td {
  color: #999999;
}

/* Contenedor para los botones de acción para alinearlos correctamente */
.action-buttons {
  display: flex;
  gap: 0.5rem; /* Espacio entre botones */
}

/* Estilo base para los botones de acción en la tabla */
.action-btn {
  padding: 0.4rem 0.8rem;
  border-radius: 5px;
  border: 1px solid #cccccc;
  background-color: #f4f4f4;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.2s, border-color 0.2s;
}

.action-btn:hover {
  background-color: #e9e9e9;
  border-color: #bbbbbb;
}

/* Botón específico de eliminar (podría tener un color distintivo) */
.delete-btn {
  /* En el diseño no se ve rojo, pero es una convención común */
  /* background-color: #fce8e6; */
  /* color: #c52929; */
  /* border-color: #f8c8c4; */
}

.delete-btn:hover {
  /* background-color: #f7d5d2; */
  /* border-color: #f0b2ad; */
}
```

### Para corregir la primera versión de la interfaz

Vamos a corregir algunas partes que no me gustan.

- [x] En la página de Login, no muestres ya las credenciales de prueba que pusiste hasta abajo ("admin"), solo deja la página de login normal.
- [ ] Los Workspaces en su interfaz no muestran los nombres de los workspaces que estan en la base de datos, solo muestran la palabra temporal.
- [ ] Quita los iconos de lso workspace, solo quiero el nombre del workspace y si esta ocupado o disponible.
- [ ] El botón de eliminación de Workspace efimeros no funciona, marca error: "Error al limpiar cuentas temporales" y al volver a presionar en "Reintentar" no hace nada, en este caso tengo un workspace temporal que se deberia de borrar tanto de la UI como de la base de datos.
- [x] Me gustaria que los botones que representan a los workspace (En la pestaña de Workspace) se pusieran de color verde cuando disponibles, rojo cuando ocupados, azul cuando se solicita la cuenta.
- [x] En esa misma página de los workspaces, en la parte superiro, en vez de mostrar el botón de cerra sesión (Elimina ese boton de esa pagina) debe mostrar un botón el cual dice "Limpiar cuentas", que lo qeu hace hasta el momento (placeholder) es eliminar los workspaces que esten marcados como efimeros. Dejando los permannetes, ya que esos no se pueden borrar tan fácil.
