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
