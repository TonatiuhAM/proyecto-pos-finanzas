# Requerimientos del Sistema POS - Gestión Integral de Finanzas

## Requerimientos Funcionales

### RF001 - Gestión de Productos
- **Descripción:** El sistema debe permitir crear, editar, visualizar y desactivar productos.
- **Funcionalidades específicas:**
  - Registrar productos con nombre, categoría, proveedor y estado
  - Asignar productos a categorías predefinidas
  - Vincular productos con proveedores específicos
  - Cambiar el estado de productos (activo/inactivo)
  - Gestionar precios de venta y costos de compra históricos

### RF002 - Gestión de Inventarios
- **Descripción:** El sistema debe controlar las existencias de productos en diferentes ubicaciones.
- **Funcionalidades específicas:**
  - Registrar cantidades en piezas (pz) y kilogramos (kg)
  - Establecer cantidades mínimas y máximas por producto
  - Gestionar ubicaciones de almacenamiento
  - Rastrear movimientos de inventario con tipos específicos
  - Generar alertas de stock bajo

### RF003 - Procesamiento de Ventas
- **Descripción:** El sistema debe procesar órdenes de venta completas usando workspaces temporales.
- **Funcionalidades específicas:**
  - Crear órdenes de venta en workspaces temporales
  - Agregar múltiples productos al carrito de compra
  - Calcular totales automáticamente con precios vigentes
  - Finalizar ventas y generar tickets
  - Actualizar inventario automáticamente tras la venta
  - Gestionar métodos de pago

### RF004 - Procesamiento de Compras
- **Descripción:** El sistema debe gestionar órdenes de compra a proveedores.
- **Funcionalidades específicas:**
  - Seleccionar proveedores para órdenes de compra
  - Crear órdenes de compra con múltiples productos
  - Registrar cantidades y costos por producto
  - Gestionar estados de órdenes de compra
  - Actualizar inventario al recibir mercancía

### RF005 - Gestión de Precios e Historial
- **Descripción:** El sistema debe mantener un historial completo de precios y costos.
- **Funcionalidades específicas:**
  - Registrar cambios de precios de venta con fecha y hora
  - Mantener historial de costos de compra por proveedor
  - Consultar precios históricos por producto y fecha
  - Utilizar precios vigentes automáticamente en transacciones
  - Alertar sobre variaciones significativas de precios

### RF006 - Gestión de Personas (Clientes/Proveedores)
- **Descripción:** El sistema debe administrar la información de clientes y proveedores.
- **Funcionalidades específicas:**
  - Registrar datos personales completos (nombre, apellidos, RFC, teléfono, email)
  - Clasificar personas por categorías (cliente, proveedor, ambos)
  - Gestionar información de ubicación por estados
  - Mantener estados activos/inactivos
  - Asociar proveedores específicos a productos

### RF007 - Control de Pagos y Deudas
- **Descripción:** El sistema debe gestionar pagos de clientes y proveedores con control de deudas.
- **Funcionalidades específicas:**
  - Registrar pagos de clientes vinculados a órdenes de venta
  - Registrar pagos a proveedores vinculados a órdenes de compra
  - Manejar diferentes métodos de pago (efectivo, tarjeta, transferencia)
  - Controlar fechas y montos de pagos
  - Generar reportes de deudas pendientes

### RF008 - Gestión de Usuarios y Roles
- **Descripción:** El sistema debe controlar el acceso mediante autenticación JWT y roles.
- **Funcionalidades específicas:**
  - Crear y gestionar cuentas de usuario con contraseñas seguras
  - Asignar roles específicos (Administrador, Empleado, etc.)
  - Controlar autenticación mediante JWT tokens
  - Autorización basada en roles (RBAC)
  - Rastrear acciones por usuario en el sistema

### RF009 - Sistema de Workspaces
- **Descripción:** El sistema debe permitir trabajar con espacios de trabajo temporales para ventas.
- **Funcionalidades específicas:**
  - Crear workspaces temporales y permanentes (mesas)
  - Gestionar órdenes en workspaces antes de confirmar
  - Transferir órdenes de workspaces a órdenes definitivas
  - Permitir múltiples workspaces simultáneos
  - Guardar y recuperar órdenes en progreso

### RF010 - Reportes y Consultas
- **Descripción:** El sistema debe generar reportes detallados de operaciones.
- **Funcionalidades específicas:**
  - Consultar inventario actual por ubicación y producto
  - Generar reportes de ventas por período y empleado
  - Consultar historial de movimientos de inventario
  - Mostrar deudas pendientes de clientes y proveedores
  - Exportar datos en formatos estándar

## Requerimientos No Funcionales

### RNF001 - Rendimiento
- **Tiempo de respuesta:** Las consultas de inventario deben responder en menos de 2 segundos
- **Capacidad:** El sistema debe soportar hasta 1000 productos simultáneamente
- **Concurrencia:** Debe manejar al menos 10 usuarios concurrentes sin degradación

### RNF002 - Seguridad
- **Autenticación:** Todas las operaciones requieren autenticación de usuario
- **Autorización:** Control de acceso basado en roles (RBAC)
- **Integridad de datos:** Validación de datos en frontend y backend
- **Encriptación:** Contraseñas encriptadas y comunicación HTTPS

### RNF003 - Disponibilidad
- **Uptime:** El sistema debe mantener 99% de disponibilidad
- **Recuperación:** Tiempo de recuperación ante fallos menor a 5 minutos
- **Respaldo:** Respaldos automáticos diarios de la base de datos

### RNF004 - Escalabilidad
- **Crecimiento de datos:** Soporte para crecimiento de hasta 100,000 transacciones anuales
- **Arquitectura:** Diseño modular para facilitar expansión de funcionalidades
- **Base de datos:** Optimización de consultas para grandes volúmenes de datos

### RNF005 - Usabilidad
- **Interfaz intuitiva:** Interfaz web responsive que funcione en dispositivos móviles y desktop
- **Tiempo de aprendizaje:** Usuarios nuevos deben poder usar funciones básicas en menos de 30 minutos
- **Accesibilidad:** Cumplimiento básico de estándares de accesibilidad web

### RNF006 - Compatibilidad
- **Navegadores:** Soporte para Chrome, Firefox, Safari y Edge (últimas 2 versiones)
- **Dispositivos:** Responsive design para tabletas y móviles
- **Base de datos:** Compatible con PostgreSQL 12+

### RNF007 - Mantenibilidad
- **Código:** Seguimiento de principios SOLID y Clean Code
- **Documentación:** Documentación técnica actualizada
- **Versionado:** Control de versiones con Git
- **Testing:** Cobertura de pruebas mínima del 70%

## Requerimientos de Interfaz

### RI001 - Pantalla de Login
- **Elementos:** Campo de usuario, campo de contraseña, botón de acceso
- **Validaciones:** Verificación de credenciales antes del acceso
- **Comportamiento:** Redirección basada en rol del usuario

### RI002 - Menu Principal
- **Elementos:** Navegación principal con opciones según rol de usuario
- **Opciones:** Inventario, Ventas, Compras, Reportes, Configuración
- **Comportamiento:** Navegación contextual y logout

### RI003 - Pantalla de Inventario
- **Elementos:** Tabla de productos, filtros, botones de acción
- **Funciones:** Crear, editar, buscar productos
- **Información mostrada:** Nombre, categoría, proveedor, cantidades, estado

### RI004 - Pantalla de Punto de Venta
- **Elementos:** Selector de productos, carrito de compra, calculadora de total
- **Funciones:** Agregar productos, calcular subtotales, procesar pago
- **Comportamiento:** Validación de inventario antes de venta

### RI005 - Pantalla de Punto de Compras
- **Elementos:** Selector de proveedores, lista de productos, registro de costos
- **Funciones:** Crear órdenes de compra, registrar precios
- **Comportamiento:** Actualización automática de inventario

### RI006 - Pantalla de Gestión de Personas
- **Elementos:** Formulario de registro, tabla de personas, filtros
- **Funciones:** Crear, editar clientes y proveedores
- **Validaciones:** RFC, email, teléfono

### RI007 - Pantalla de Reportes
- **Elementos:** Filtros de fecha, gráficos, tablas de resultados
- **Funciones:** Generar reportes de inventario, ventas y compras
- **Exportación:** Capacidad de exportar datos en formatos comunes

### RI008 - Modales de Confirmación
- **Elementos:** Ventanas emergentes para confirmaciones importantes
- **Uso:** Eliminación de registros, procesamiento de pagos
- **Comportamiento:** Validación antes de acciones irreversibles

### RI009 - Notificaciones del Sistema
- **Elementos:** Alertas de éxito, error, advertencia e información
- **Posicionamiento:** Parte superior de la pantalla
- **Comportamiento:** Auto-desaparición después de tiempo definido

### RI010 - Responsive Design
- **Adaptabilidad:** Interfaz que se adapte a diferentes tamaños de pantalla
- **Elementos móviles:** Menú hamburguesa, botones táctiles apropiados
- **Comportamiento:** Reorganización de elementos según dispositivo

---

# Análisis de Funcionamiento del Código

## Descripción Amplia de Funcionamiento

### RF001 - Gestión de Productos

**Funcionamiento en el código:**
El sistema implementa un patrón arquitectónico MVC completo para la gestión de productos. En el backend, la entidad `Productos` se mapea a la tabla `productos` usando JPA/Hibernate con relaciones `@ManyToOne` hacia `CategoriasProductos`, `Personas` (proveedor) y `Estados`. El `ProductosController` expone endpoints REST que utilizan DTOs (`ProductosDTO`, `ProductoCreacionDTO`) para la transferencia de datos, evitando exponer directamente las entidades JPA.

El `ProductoService` contiene la lógica de negocio, manejando la creación, actualización y gestión de estados de productos. La validación se realiza tanto en el frontend usando TypeScript interfaces como en el backend con anotaciones Jakarta Validation.

En el frontend, el servicio `inventarioService.ts` centraliza las llamadas a la API usando Axios con interceptores para manejo de autenticación JWT. Los componentes React como `Inventario.tsx` y los modales `ModalCrearProducto.tsx` y `ModalEditarProducto.tsx` proporcionan la interfaz de usuario, implementando validación de formularios y manejo de estados locales con hooks de React.

### RF002 - Gestión de Inventarios

**Funcionamiento en el código:**
El inventario se gestiona mediante la entidad `Inventarios` que relaciona productos con ubicaciones específicas. El `InventoryController` provee endpoints para consultar y actualizar existencias, mientras que el `MovimientosInventarios` registra cada transacción de entrada o salida con timestamps, usuarios responsables y tipos de movimiento.

La arquitectura implementa un sistema de doble contabilidad donde cada movimiento actualiza automáticamente los niveles de inventario. El `InventarioRepository` utiliza consultas JPA derivadas del nombre del método para operaciones CRUD eficientes. El frontend presenta esta información en tiempo real mediante el componente `Inventario.tsx` que actualiza automáticamente las existencias tras cada transacción.

### RF003 - Procesamiento de Ventas

**Funcionamiento en el código:**
El proceso de ventas utiliza un sistema de workspaces temporales implementado mediante las entidades `Workspaces` y `OrdenesWorkspace`. El flujo inicia en `PuntoDeVenta.tsx` donde los usuarios agregan productos a un carrito local, que se sincroniza con el workspace en el backend mediante el `OrdenesWorkspaceService`.

El componente `PuntoDeVenta` implementa un estado complejo usando múltiples hooks de React para manejar productos, carrito, totales y sincronización automática. Cuando se finaliza una venta, el `VentaService` crea una `OrdenesDeVentas` con sus correspondientes `DetallesOrdenesDeVentas`, actualiza el inventario automáticamente y genera un ticket mediante `TicketVenta.tsx`.

Los precios se obtienen del `HistorialPrecios` usando el precio vigente más reciente para cada producto. La transacción completa está envuelta en un contexto transaccional de Spring para garantizar la consistencia de datos.

### RF004 - Procesamiento de Compras

**Funcionamiento en el código:**
Las compras se gestionan mediante `OrdenesDeCompras` y `DetallesOrdenesDeCompras`. El `ComprasController` y `ComprasService` manejan la lógica de negocio para crear órdenes de compra, seleccionar proveedores y gestionar estados de orden.

El componente `PuntoDeCompras.tsx` permite seleccionar proveedores mediante `SeleccionProveedores.tsx` y agregar productos con sus costos. El sistema actualiza automáticamente el `HistorialCostos` y puede opcionalmente actualizar el inventario cuando se recibe la mercancía. La gestión de deudas con proveedores se maneja mediante `DeudasProveedoresController` y `HistorialCargosProveedores`.

### RF005 - Gestión de Precios e Historial

**Funcionamiento en el código:**
El sistema implementa un historial completo de precios mediante las entidades `HistorialPrecios` y `HistorialCostos`. Cada cambio de precio genera un nuevo registro con timestamp usando `ZonedDateTime` para precisión temporal. El `HistorialPreciosRepository` implementa consultas para obtener precios vigentes por fecha y producto.

En las transacciones, el sistema automáticamente selecciona el precio más reciente disponible. Los controladores `HistorialPreciosController` y `HistorialCostosController` proporcionan endpoints para consultar historiales y generar reportes de variación de precios. El frontend puede mostrar gráficos de evolución de precios usando estos datos.

### RF006 - Gestión de Personas (Clientes/Proveedores)

**Funcionamiento en el código:**
La entidad `Personas` centraliza tanto clientes como proveedores, diferenciándolos mediante `CategoriaPersonas`. El `PersonaController` y `PersonaService` implementan CRUD completo con validación de RFC, email y teléfono tanto en backend como frontend.

El componente `ModalCrearPersona.tsx` proporciona formularios dinámicos que se adaptan según la categoría seleccionada. La relación con `Estados` permite gestión geografica, mientras que las relaciones con productos (como proveedor) y órdenes (como cliente) mantienen la trazabilidad completa del negocio.

### RF007 - Control de Pagos y Deudas

**Funcionamiento en el código:**
Los pagos se gestionan mediante `HistorialPagosClientes` y `HistorialCargosProveedores`, vinculados respectivamente a órdenes de venta y compra. El `DeudasProveedoresService` implementa lógica compleja para calcular saldos pendientes, fechas de vencimiento y generar reportes.

El componente `DeudasProveedores.tsx` presenta una interfaz tabular con filtros y opciones de pago. Los `MetodosPago` proporcionan flexibilidad en las formas de pago, mientras que la arquitectura permite pagos parciales y seguimiento de saldos pendientes en tiempo real.

### RF008 - Gestión de Usuarios y Roles

**Funcionamiento en el código:**
La autenticación se implementa mediante JWT usando `JwtService` y Spring Security. Los `Usuarios` se relacionan con `Roles` que determinan permisos de acceso. El `AuthController` maneja login/logout, mientras que interceptors en el frontend gestionan automáticamente el token en las peticiones.

El sistema implementa Role-Based Access Control (RBAC) mediante el componente `RoleBasedNavigation.tsx` que adapta la interfaz según los permisos del usuario. Las rutas están protegidas mediante `ProtectedRoute.tsx` que valida tanto el token como los roles requeridos.

### RF009 - Sistema de Workspaces

**Funcionamiento en el código:**
Los workspaces se implementan mediante `Workspaces` y `OrdenesWorkspace` que actúan como un carrito de compras persistente. El `OrdenesWorkspaceService` maneja la sincronización entre el carrito local del frontend y el workspace en el backend.

El `WorkspaceScreen.tsx` permite gestionar múltiples workspaces simultáneamente, mientras que `PuntoDeVenta.tsx` trabaja dentro de un workspace específico. El sistema permite crear workspaces temporales (para órdenes específicas) y permanentes (como mesas en un restaurante). La transición de workspace a orden definitiva se maneja atómicamente para prevenir inconsistencias.

### RF010 - Reportes y Consultas

**Funcionamiento en el código:**
Los reportes se generan mediante consultas especializadas en los repositorios JPA, utilizando `@Query` con JPQL para consultas complejas que involucran múltiples tablas. Los controladores exponen endpoints específicos para cada tipo de reporte, retornando DTOs optimizados.

El frontend puede consumir estos endpoints para generar gráficos y tablas dinámicas. La arquitectura permite exportación de datos mediante servicios especializados que pueden generar diferentes formatos (JSON, CSV, PDF) según las necesidades del usuario.

---

## Descripción de Diagramas de Flujo

### RF001 - Gestión de Productos - Crear Nuevo Producto

**PASO 1:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Inicio - Usuario accede a Inventario
- **CONEXIÓN:** Flecha hacia Paso 2

**PASO 2:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema carga lista de productos activos
- **CONEXIÓN:** Flecha hacia Paso 3

**PASO 3:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Usuario hace clic en "Crear Nuevo Producto"
- **CONEXIÓN:** Flecha hacia Paso 4

**PASO 4:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema abre ModalCrearProducto
- **CONEXIÓN:** Flecha hacia Paso 5

**PASO 5:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema carga categorías y proveedores disponibles
- **CONEXIÓN:** Flecha hacia Paso 6

**PASO 6:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario ingresa: nombre, categoría, proveedor
- **CONEXIÓN:** Flecha hacia Paso 7

**PASO 7:**
- **FORMA:** Rombo  
- **TEXTO INTERIOR:** ¿Datos válidos?
- **CONEXIÓN:** Flecha "Sí" hacia Paso 8, Flecha "No" hacia Paso 12

**PASO 8:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema envía datos a ProductosController
- **CONEXIÓN:** Flecha hacia Paso 9

**PASO 9:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** ProductoService valida y crea entidad Productos
- **CONEXIÓN:** Flecha hacia Paso 10

**PASO 10:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema guarda en base de datos PostgreSQL
- **CONEXIÓN:** Flecha hacia Paso 11

**PASO 11:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema actualiza lista de productos en frontend
- **CONEXIÓN:** Flecha hacia Paso 13

**PASO 12:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema muestra errores de validación
- **CONEXIÓN:** Flecha hacia Paso 6

**PASO 13:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema muestra notificación de éxito
- **CONEXIÓN:** Flecha hacia Paso 14

**PASO 14:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Fin - Producto creado exitosamente
- **CONEXIÓN:** Sin conexión

### RF003 - Procesamiento de Ventas - Venta Completa

**PASO 1:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Inicio - Usuario selecciona Punto de Venta
- **CONEXIÓN:** Flecha hacia Paso 2

**PASO 2:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema carga PuntoDeVenta.tsx con workspace
- **CONEXIÓN:** Flecha hacia Paso 3

**PASO 3:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema carga productos y categorías disponibles
- **CONEXIÓN:** Flecha hacia Paso 4

**PASO 4:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema recupera carrito guardado del workspace
- **CONEXIÓN:** Flecha hacia Paso 5

**PASO 5:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario selecciona categoría de producto
- **CONEXIÓN:** Flecha hacia Paso 6

**PASO 6:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema filtra productos por categoría
- **CONEXIÓN:** Flecha hacia Paso 7

**PASO 7:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario selecciona producto y cantidad
- **CONEXIÓN:** Flecha hacia Paso 8

**PASO 8:**
- **FORMA:** Rombo  
- **TEXTO INTERIOR:** ¿Hay suficiente inventario?
- **CONEXIÓN:** Flecha "Sí" hacia Paso 9, Flecha "No" hacia Paso 22

**PASO 9:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema agrega producto al carrito local
- **CONEXIÓN:** Flecha hacia Paso 10

**PASO 10:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema sincroniza carrito con workspace en backend
- **CONEXIÓN:** Flecha hacia Paso 11

**PASO 11:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema actualiza total de venta
- **CONEXIÓN:** Flecha hacia Paso 12

**PASO 12:**
- **FORMA:** Rombo  
- **TEXTO INTERIOR:** ¿Usuario desea agregar más productos?
- **CONEXIÓN:** Flecha "Sí" hacia Paso 5, Flecha "No" hacia Paso 13

**PASO 13:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario hace clic en "Finalizar Venta"
- **CONEXIÓN:** Flecha hacia Paso 14

**PASO 14:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario selecciona cliente y método de pago
- **CONEXIÓN:** Flecha hacia Paso 15

**PASO 15:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema envía datos a VentaService
- **CONEXIÓN:** Flecha hacia Paso 16

**PASO 16:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** VentaService inicia transacción de base de datos
- **CONEXIÓN:** Flecha hacia Paso 17

**PASO 17:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema crea OrdenesDeVentas y DetallesOrdenesDeVentas
- **CONEXIÓN:** Flecha hacia Paso 18

**PASO 18:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema actualiza inventario restando cantidades vendidas
- **CONEXIÓN:** Flecha hacia Paso 19

**PASO 19:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema limpia workspace temporal
- **CONEXIÓN:** Flecha hacia Paso 20

**PASO 20:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema genera ticket de venta
- **CONEXIÓN:** Flecha hacia Paso 21

**PASO 21:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Fin - Venta procesada exitosamente
- **CONEXIÓN:** Sin conexión

**PASO 22:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema muestra error de inventario insuficiente
- **CONEXIÓN:** Flecha hacia Paso 7

### RF008 - Gestión de Usuarios - Proceso de Login

**PASO 1:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Inicio - Usuario accede al sistema
- **CONEXIÓN:** Flecha hacia Paso 2

**PASO 2:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema muestra LoginScreen.tsx
- **CONEXIÓN:** Flecha hacia Paso 3

**PASO 3:**
- **FORMA:** Paralelogramo  
- **TEXTO INTERIOR:** Usuario ingresa nombre de usuario y contraseña
- **CONEXIÓN:** Flecha hacia Paso 4

**PASO 4:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema envía credenciales a AuthController
- **CONEXIÓN:** Flecha hacia Paso 5

**PASO 5:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** AuthController valida credenciales con base de datos
- **CONEXIÓN:** Flecha hacia Paso 6

**PASO 6:**
- **FORMA:** Rombo  
- **TEXTO INTERIOR:** ¿Credenciales válidas?
- **CONEXIÓN:** Flecha "Sí" hacia Paso 7, Flecha "No" hacia Paso 12

**PASO 7:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** JwtService genera token JWT con roles del usuario
- **CONEXIÓN:** Flecha hacia Paso 8

**PASO 8:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema envía LoginResponseDTO con token
- **CONEXIÓN:** Flecha hacia Paso 9

**PASO 9:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Frontend guarda token en localStorage
- **CONEXIÓN:** Flecha hacia Paso 10

**PASO 10:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** RoleBasedNavigation determina menú según rol
- **CONEXIÓN:** Flecha hacia Paso 11

**PASO 11:**
- **FORMA:** Óvalo  
- **TEXTO INTERIOR:** Fin - Usuario autenticado y redirigido
- **CONEXIÓN:** Sin conexión

**PASO 12:**
- **FORMA:** Rectángulo  
- **TEXTO INTERIOR:** Sistema muestra mensaje de error de credenciales
- **CONEXIÓN:** Flecha hacia Paso 3

---

## Descripción de Casos de Uso UML

### Actores del Sistema

**ACTOR PRINCIPAL: Administrador**
- **Descripción:** Usuario con permisos completos sobre el sistema
- **Responsabilidades:** Gestión de usuarios, configuración del sistema, acceso a todos los módulos

**ACTOR PRINCIPAL: Empleado**  
- **Descripción:** Usuario operativo con permisos limitados
- **Responsabilidades:** Procesamiento de ventas, consulta de inventario, gestión básica de productos

**ACTOR PRINCIPAL: Cliente**
- **Descripción:** Persona que realiza compras en el sistema
- **Responsabilidades:** Ser seleccionado en ventas, tener historial de compras

**ACTOR PRINCIPAL: Proveedor**
- **Descripción:** Persona o empresa que suministra productos
- **Responsabilidades:** Ser seleccionado en compras, recibir órdenes de compra

### Casos de Uso por Actor

**ADMINISTRADOR puede realizar:**

**CU001 - Gestionar Productos**
- **Descripción:** Crear, editar, activar/desactivar productos del inventario
- **Precondiciones:** Usuario autenticado con rol Administrador
- **Flujo Principal:** Accede a inventario → Crea/edita producto → Define categoría y proveedor → Guarda cambios
- **Postcondiciones:** Producto actualizado en base de datos e inventario

**CU002 - Gestionar Usuarios**
- **Descripción:** Crear, editar y asignar roles a usuarios del sistema
- **Precondiciones:** Usuario autenticado con rol Administrador
- **Flujo Principal:** Accede a gestión de empleados → Crea usuario → Asigna rol → Guarda configuración
- **Postcondiciones:** Usuario creado con permisos asignados

**CU003 - Gestionar Proveedores**
- **Descripción:** Registrar y mantener información de proveedores
- **Precondiciones:** Usuario autenticado con rol Administrador
- **Flujo Principal:** Accede a gestión de personas → Crea proveedor → Ingresa datos → Asigna categoría proveedor
- **Postcondiciones:** Proveedor disponible para órdenes de compra

**CU004 - Procesar Compras**
- **Descripción:** Crear órdenes de compra y gestionar recepción de mercancía
- **Precondiciones:** Usuario autenticado, proveedores y productos existentes
- **Flujo Principal:** Selecciona proveedor → Agrega productos → Define cantidades y costos → Confirma orden
- **Postcondiciones:** Orden de compra generada, inventario actualizado

**CU005 - Generar Reportes**
- **Descripción:** Consultar reportes de ventas, inventario y deudas
- **Precondiciones:** Usuario autenticado con rol Administrador, datos existentes
- **Flujo Principal:** Accede a reportes → Selecciona tipo y filtros → Genera reporte → Exporta datos
- **Postcondiciones:** Reporte generado y disponible para análisis

**CU006 - Gestionar Deudas**
- **Descripción:** Controlar pagos de clientes y deudas con proveedores
- **Precondiciones:** Usuario autenticado, órdenes existentes
- **Flujo Principal:** Accede a deudas → Selecciona cliente/proveedor → Registra pago → Actualiza saldo
- **Postcondiciones:** Pago registrado, saldo actualizado

**EMPLEADO puede realizar:**

**CU007 - Procesar Ventas**
- **Descripción:** Realizar ventas completas usando el punto de venta
- **Precondiciones:** Usuario autenticado, productos e inventario disponible
- **Flujo Principal:** Accede a punto de venta → Agrega productos al carrito → Selecciona cliente → Procesa pago → Genera ticket
- **Postcondiciones:** Venta registrada, inventario actualizado, ticket generado

**CU008 - Consultar Inventario**
- **Descripción:** Verificar existencias y estado de productos
- **Precondiciones:** Usuario autenticado
- **Flujo Principal:** Accede a inventario → Filtra productos → Consulta existencias → Verifica estado
- **Postcondiciones:** Información de inventario consultada

**CU009 - Gestionar Workspaces**
- **Descripción:** Usar espacios de trabajo temporales para ventas
- **Precondiciones:** Usuario autenticado con acceso a ventas
- **Flujo Principal:** Selecciona workspace → Agrega productos → Guarda progreso → Finaliza o continúa más tarde
- **Postcondiciones:** Orden guardada en workspace para continuación posterior

**CU010 - Consultar Precios**
- **Descripción:** Verificar precios vigentes e historial de productos
- **Precondiciones:** Usuario autenticado, productos existentes
- **Flujo Principal:** Busca producto → Consulta precio actual → Revisa historial si tiene permisos
- **Postcondiciones:** Información de precios consultada

**CLIENTE (indirectamente a través del sistema):**

**CU011 - Ser Seleccionado en Venta**
- **Descripción:** Cliente es seleccionado durante el proceso de venta
- **Precondiciones:** Cliente registrado en el sistema
- **Flujo Principal:** Empleado busca cliente → Selecciona de lista → Asocia venta al cliente
- **Postcondiciones:** Venta vinculada al cliente para historial

**CU012 - Generar Historial de Compras**
- **Descripción:** Sistema mantiene historial de compras del cliente
- **Precondiciones:** Cliente con ventas previas
- **Flujo Principal:** Sistema registra automáticamente cada venta del cliente
- **Postcondiciones:** Historial actualizado para reportes y análisis

**PROVEEDOR (indirectamente a través del sistema):**

**CU013 - Recibir Órdenes de Compra**
- **Descripción:** Proveedor es seleccionado para órdenes de compra
- **Precondiciones:** Proveedor registrado y asociado a productos
- **Flujo Principal:** Sistema permite seleccionar proveedor → Genera orden de compra → Registra productos y costos
- **Postcondiciones:** Orden de compra asociada al proveedor

**CU014 - Gestionar Pagos Pendientes**
- **Descripción:** Sistema controla deudas y pagos hacia el proveedor
- **Precondiciones:** Órdenes de compra existentes
- **Flujo Principal:** Sistema registra deuda → Administrador procesa pago → Sistema actualiza saldo
- **Postcondiciones:** Control de deudas actualizado

### Relaciones entre Casos de Uso

**Extensiones (extends):**
- CU007 (Procesar Ventas) extends CU009 (Gestionar Workspaces)
- CU005 (Generar Reportes) extends CU008 (Consultar Inventario)

**Especializaciones:**
- CU001 (Gestionar Productos) es especialización de gestión de inventario
- CU004 (Procesar Compras) es especialización de gestión de proveedores

**Dependencias:**
- CU007 (Procesar Ventas) requiere CU008 (Consultar Inventario)
- CU004 (Procesar Compras) requiere CU003 (Gestionar Proveedores)
- CU005 (Generar Reportes) requiere datos de múltiples casos de uso