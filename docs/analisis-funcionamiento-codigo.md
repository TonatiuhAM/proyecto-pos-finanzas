# Análisis de Funcionamiento del Código - Sistema POS Finanzas

## PARTE 1: DESCRIPCIONES AMPLIAS DE FUNCIONAMIENTO

### RF-001: Autenticación de Usuarios

**Funcionamiento del Código:**

El sistema de autenticación se implementa a través de múltiples capas:

**Backend (AuthController.java):**
- Recibe credenciales via endpoint POST `/api/auth/login`
- Utiliza `UsuariosRepository` para buscar usuario por nombre
- Valida contraseña usando `PasswordEncoder` con soporte para BCrypt y texto plano
- Verifica que el usuario esté en estado "Activo" consultando la tabla `estados`
- Obtiene información del rol del usuario desde la tabla `roles`
- Genera token JWT usando `JwtService` con tiempo de expiración
- Retorna `LoginResponseDTO` con token, datos de usuario y rol

**Frontend (AuthContext.tsx + LoginScreen.tsx):**
- Contexto React global para gestionar estado de autenticación
- Almacena datos de usuario en `localStorage` para persistencia
- Hook `useAuth` proporciona funciones de login/logout y utilidades
- Interceptor de axios añade token JWT automáticamente a todas las peticiones
- Manejo de expiración de token con logout automático en respuestas 401

**Flujo de Datos:**
1. Usuario ingresa credenciales → LoginScreen
2. LoginScreen llama a `apiService.login()`
3. Backend valida y genera JWT
4. Frontend almacena respuesta en AuthContext y localStorage
5. Redirección automática según rol del usuario

### RF-002: Control de Acceso Basado en Roles

**Funcionamiento del Código:**

**RoleBasedNavigation.tsx:**
- Componente que renderiza botones condicionalmente según el rol
- Utiliza `useAuth().isAdmin()` y `useAuth().isEmployee()` para determinar permisos
- Administradores ven: PDV, Inventario, Administración (Empleados)
- Empleados ven únicamente: PDV

**ProtectedRoute.tsx:**
- Componente de orden superior que protege rutas administrativas
- Verifica rol antes de renderizar componentes sensibles
- Redirige empleados si intentan acceder a funciones de administrador

**AuthContext lógica de roles:**
- `isAdmin()`: Verifica si `rolNombre === "Administrador"`
- `isEmployee()`: Verifica si `rolNombre === "Empleado"`
- Roles se obtienen desde JWT decodificado almacenado en localStorage

**Backend Security:**
- Cada endpoint valida permisos según la lógica de negocio
- Los controladores verifican roles cuando es necesario
- Separación de responsabilidades entre autenticación y autorización

### RF-003: Administración de Productos

**Funcionamiento del Código:**

**ProductoService.java:**
- Gestiona CRUD completo de productos
- `crearProductoCompleto()`: Crea producto, historial de precios, costos e inventario inicial
- `registrarMovimientoCreacion()`: Registra automáticamente movimiento de inventario
- `registrarMovimientoEdicion()`: Detecta cambios en productos y registra auditoría
- Validaciones de negocio: producto único, proveedor válido, categoría existente

**ProductosController.java:**
- Endpoints REST para operaciones CRUD
- GET `/api/productos` - Lista productos con filtros
- POST `/api/productos` - Crear nuevo producto
- PUT/PATCH `/api/productos/{id}` - Actualizar producto
- DELETE `/api/productos/{id}` - Eliminar (soft delete)

**Frontend (Inventario.tsx):**
- Tabla responsive con datos de productos
- Filtros por categoría y proveedor en tiempo real
- Modales para crear/editar productos con validación
- Integración con servicios API para operaciones CRUD

**Modelos Involucrados:**
- `Productos`: Datos principales del producto
- `CategoriasProductos`: Clasificación por categoría
- `HistorialPrecios`: Historial de precios de venta
- `HistorialCostos`: Historial de costos de compra
- `Inventarios`: Stock actual por ubicación

### RF-004: Control de Stock

**Funcionamiento del Código:**

**InventarioRepository.java:**
- Consultas optimizadas para obtener stock disponible
- `findProductosConStock()`: Productos activos con cantidad > 0
- Soporte para cantidades en piezas y kilogramos
- Relación con ubicaciones de almacenamiento

**OrdenesWorkspaceService.java:**
- `decrementarInventario()`: Reduce stock al agregar productos al carrito
- `restaurarInventario()`: Restaura stock al limpiar carrito
- Validaciones de stock antes de permitir operaciones
- Transacciones atómicas para evitar condiciones de carrera

**Inventarios.java (Modelo):**
- Cantidad mínima y máxima por producto
- Stock actual en piezas y kilogramos
- Relación con ubicaciones específicas
- Campos para auditoría y control

**Frontend Validaciones:**
- Verificación de stock en tiempo real antes de agregar al carrito
- Mensajes de error cuando no hay suficiente inventario
- Actualización automática de interfaz después de operaciones

### RF-005: Registro de Movimientos de Inventario

**Funcionamiento del Código:**

**MovimientosInventarios.java (Modelo):**
- Tabla central para auditoría de todos los movimientos
- Campos: fecha, usuario, tipo de movimiento, cantidad, clave descriptiva
- Relaciones con productos, ubicaciones, usuarios y tipos de movimiento

**ProductoService.registrarMovimiento():**
- Método central para registrar movimientos
- Tipos automáticos: Creación, Edición, Venta, Compra
- Genera claves descriptivas únicas para trazabilidad
- Registra usuario responsable y timestamp exacto

**TipoMovimientos.java:**
- Catálogo de tipos de movimiento
- Creación automática de tipos si no existen
- Escalabilidad para nuevos tipos de movimiento

**OrdenesWorkspaceService y VentaService:**
- Integración automática con sistema de movimientos
- Cada cambio de stock genera movimiento correspondiente
- Trazabilidad completa desde carrito temporal hasta venta final

**MovimientosInventariosController.java:**
- Endpoint para consultar movimientos por producto
- Filtros por fecha, tipo y usuario
- Reportes de auditoría para administradores

### RF-006: Gestión de Workspaces (Mesas/Áreas)

**Funcionamiento del Código:**

**Workspaces.java (Modelo):**
- Definición de áreas de trabajo (mesas, zonas, etc.)
- Campo `permanente`: distingue mesas fijas de temporales
- Estados implícitos: libre, ocupado, cuenta solicitada

**WorkspacesController.java:**
- CRUD básico para workspaces
- Endpoint para obtener estado actual de cada workspace
- Información de órdenes activas por workspace

**Frontend (WorkspaceScreen.tsx):**
- Visualización de workspaces como botones grandes
- Estados visuales diferenciados por color:
  - Verde: Libre (sin órdenes activas)
  - Azul: Ocupado (con órdenes en carrito)
  - Amarillo: Cuenta solicitada (venta procesada)
- Navegación directa al POS desde cada workspace

**Estado del Workspace:**
- Calculado dinámicamente según órdenes en `ordenes_workspace`
- Sin órdenes = Libre
- Con órdenes activas = Ocupado
- Órdenes procesadas en venta = Cuenta solicitada

### RF-007: Carrito de Compras Temporal

**Funcionamiento del Código:**

**OrdenesWorkspace.java (Modelo):**
- Tabla temporal para almacenar productos seleccionados por workspace
- Campos: workspace_id, producto_id, cantidades, historial_precios_id
- Persistencia temporal hasta finalizar venta

**OrdenesWorkspaceService.java:**
- `agregarOActualizarProducto()`: Lógica central del carrito
- Suma cantidades si producto ya existe en carrito
- Validación de stock disponible antes de agregar
- Decremento automático de inventario al guardar

**Frontend (PuntoDeVenta.tsx):**
- Estado local del carrito sincronizado con backend
- Componentes para agregar/modificar/eliminar productos
- Cálculo automático de totales en tiempo real
- Persistencia automática en base de datos al "Guardar Orden"

**Flujo de Carrito:**
1. Usuario selecciona producto y cantidad
2. Frontend valida stock disponible
3. Llama a `agregarProductoOrden()` en backend
4. Backend actualiza `ordenes_workspace` y decrementa inventario
5. Frontend recarga datos y actualiza interfaz
6. Estado persiste hasta finalizar venta o limpiar carrito

### RF-008: Procesamiento de Ventas

**Funcionamiento del Código:**

**VentaService.java:**
- `procesarVentaCompleta()`: Método principal para finalizar ventas
- Convierte carrito temporal (`ordenes_workspace`) a venta final
- Crea registro en `ordenes_de_ventas` con totales
- Genera detalles en `detalles_ordenes_de_ventas`
- Registra movimientos de inventario tipo "Venta"
- Limpia workspace después de procesar

**WorkspacesController.finalizar-venta:**
- Endpoint POST que recibe método de pago y cliente
- Integración con VentaService para procesar venta completa
- Manejo de errores y rollback automático en caso de fallo

**Frontend (TicketVenta.tsx):**
- Interfaz para seleccionar método de pago
- Resumen de productos y total
- Integración con impresión de tickets
- Notificaciones de éxito/error

**Modelos Involucrados:**
- `OrdenesDeVentas`: Cabecera de la venta
- `DetallesOrdenesDeVentas`: Líneas de detalle por producto
- `MovimientosInventarios`: Registro de salida de inventario
- `MetodosPago`: Catálogo de formas de pago

### RF-009: Gestión de Órdenes de Compra

**Funcionamiento del Código:**

**ComprasService.java:**
- `crearOrdenCompra()`: Procesa compras completas a proveedores
- Validación de proveedor activo y productos disponibles
- Cálculo automático de totales de compra
- Actualización de inventario con productos comprados
- Registro de movimientos tipo "Compra"

**ComprasController.java:**
- Endpoints para flujo completo de compras
- GET `/api/compras/proveedores` - Lista proveedores con estado de deuda
- POST `/api/compras/crear` - Crear nueva orden de compra
- POST `/api/compras/pagar` - Registrar pagos a proveedores

**Frontend (PuntoDeCompras.tsx):**
- Interfaz similar al POS pero orientada a compras
- Selección de productos filtrados por proveedor
- Carrito de compras con cálculo de totales
- Integración con sistema de pagos inmediatos

**Modelos Involucrados:**
- `OrdenesDeCompras`: Cabecera de la orden de compra
- `DetallesOrdenesDeCompras`: Productos y cantidades compradas
- `HistorialCargosProveedores`: Pagos realizados a proveedores

### RF-010: Control de Deudas con Proveedores

**Funcionamiento del Código:**

**ComprasService.calcularDeudaProveedor():**
- Fórmula: SUMA(ordenes_de_compras.total) - SUMA(historial_cargos_proveedores.monto)
- Consultas optimizadas para cálculo en tiempo real
- Resultado: deuda pendiente por proveedor

**Frontend (SeleccionProveedores.tsx):**
- Visualización de proveedores con estado de deuda
- Colores: Verde (sin deuda), Amarillo (con deuda)
- Navegación condicional al sistema de compras

**HistorialCargosProveedores.java:**
- Registro de todos los pagos realizados
- Relación con órdenes de compra específicas
- Métodos de pago y fechas de transacción

**DeudasProveedoresService.java:**
- Servicio especializado para reportes de deuda
- Cálculos de antigüedad de saldos
- Análisis de flujo de pagos por periodo

### RF-011: Administración de Empleados

**Funcionamiento del Código:**

**EmpleadoService.java:**
- CRUD completo para gestión de empleados
- Encriptación automática de contraseñas con BCrypt
- Validación de estado antes de permitir login
- Gestión de roles y permisos

**EmpleadoController.java:**
- Endpoints REST para administración
- GET `/api/empleados` - Lista empleados activos/inactivos
- POST `/api/empleados` - Crear nuevo empleado
- PUT `/api/empleados/{id}/estado` - Cambiar estado activo/inactivo

**Frontend (GestionEmpleados.tsx):**
- Tabla con información de empleados
- Toggle switches para activar/desactivar usuarios
- Modal para crear nuevos empleados con validaciones
- Integración con sistema de roles

**AuthController validación:**
- Verificación de estado "Activo" en proceso de login
- Empleados inactivos no pueden acceder al sistema
- Mensajes de error específicos para cuentas deshabilitadas

### RF-012: Gestión de Personas (Clientes/Proveedores)

**Funcionamiento del Código:**

**Personas.java (Modelo):**
- Tabla central para todas las personas del sistema
- Categorización: Cliente, Proveedor, Empleado
- Campos: nombre, apellidos, RFC, teléfono, email
- Estados activo/inactivo por persona

**PersonaService.java:**
- Gestión unificada de personas por categoría
- Validaciones de RFC y datos fiscales
- Relaciones con transacciones específicas

**CategoriaPersonas.java:**
- Catálogo de tipos de persona
- Permite extensibilidad para nuevas categorías
- Filtros automáticos en consultas

**Frontend (ModalCrearPersona.tsx):**
- Formulario unificado para crear personas
- Selección de categoría y validaciones específicas
- Integración con módulos de ventas y compras

### RF-013: Historial de Transacciones

**Funcionamiento del Código:**

**Reportes integrados en Controllers:**
- Cada controlador expone endpoints para consultas históricas
- Filtros por fecha, usuario, producto, cliente, proveedor
- Paginación para manejar grandes volúmenes de datos

**MovimientosInventariosController:**
- Endpoint para historial completo de movimientos
- Filtros avanzados por tipo, producto, fechas
- Exportación de reportes para auditoría

**Frontend servicios de consulta:**
- Servicios especializados para cada tipo de reporte
- Interfaces de filtrado y búsqueda
- Visualización de datos en tablas responsivas

**Modelos de auditoría:**
- Todas las tablas principales mantienen historial
- Campos timestamp automáticos
- Trazabilidad completa de cambios

---

## PARTE 2: DESCRIPCIÓN DE DIAGRAMAS DE FLUJO

### RF-001: Autenticación de Usuarios

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Usuario accede al sistema
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario ingresa credenciales (nombre/contraseña)
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Frontend envía POST /api/auth/login
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Backend busca usuario en base de datos
- **CONEXIÓN:** Hacia Paso 5

**Paso 5:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario existe?
- **CONEXIÓN:** NO hacia Paso 13, SÍ hacia Paso 6

**Paso 6:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Validar contraseña (BCrypt o texto plano)
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Contraseña correcta?
- **CONEXIÓN:** NO hacia Paso 13, SÍ hacia Paso 8

**Paso 8:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Verificar estado del usuario
- **CONEXIÓN:** Hacia Paso 9

**Paso 9:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario activo?
- **CONEXIÓN:** NO hacia Paso 14, SÍ hacia Paso 10

**Paso 10:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Obtener información del rol
- **CONEXIÓN:** Hacia Paso 11

**Paso 11:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Generar token JWT con expiración
- **CONEXIÓN:** Hacia Paso 12

**Paso 12:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar LoginResponseDTO con token y datos de usuario
- **CONEXIÓN:** Hacia Paso 15

**Paso 13:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error: "Credenciales incorrectas"
- **CONEXIÓN:** Hacia Paso 16

**Paso 14:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error: "Usuario inactivo"
- **CONEXIÓN:** Hacia Paso 16

**Paso 15:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Frontend almacena datos en AuthContext y localStorage
- **CONEXIÓN:** Hacia Paso 17

**Paso 16:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Login fallido
- **CONEXIÓN:** Ninguna

**Paso 17:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Es Administrador?
- **CONEXIÓN:** SÍ hacia Paso 18, NO hacia Paso 19

**Paso 18:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Redirigir a menú completo (PDV, Inventario, Empleados)
- **CONEXIÓN:** Hacia Paso 20

**Paso 19:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Redirigir solo a Punto de Venta
- **CONEXIÓN:** Hacia Paso 20

**Paso 20:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Login exitoso
- **CONEXIÓN:** Ninguna

### RF-002: Control de Acceso Basado en Roles

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Usuario autenticado accede a funcionalidad
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** RoleBasedNavigation evalúa rol del usuario
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Es Administrador?
- **CONEXIÓN:** SÍ hacia Paso 4, NO hacia Paso 5

**Paso 4:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Mostrar todos los botones: PDV, Inventario, Administración
- **CONEXIÓN:** Hacia Paso 7

**Paso 5:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Es Empleado?
- **CONEXIÓN:** SÍ hacia Paso 6, NO hacia Paso 9

**Paso 6:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Mostrar únicamente botón Punto de Venta
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario selecciona funcionalidad disponible
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** ProtectedRoute valida permisos antes de renderizar
- **CONEXIÓN:** Hacia Paso 10

**Paso 9:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error: "Rol no reconocido"
- **CONEXIÓN:** Hacia Paso 12

**Paso 10:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Tiene permisos para la funcionalidad?
- **CONEXIÓN:** SÍ hacia Paso 11, NO hacia Paso 13

**Paso 11:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Renderizar componente solicitado
- **CONEXIÓN:** Hacia Paso 14

**Paso 12:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Acceso denegado por rol inválido
- **CONEXIÓN:** Ninguna

**Paso 13:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar mensaje "Acceso denegado" y redirigir
- **CONEXIÓN:** Hacia Paso 15

**Paso 14:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Acceso concedido
- **CONEXIÓN:** Ninguna

**Paso 15:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Acceso denegado por permisos insuficientes
- **CONEXIÓN:** Ninguna

### RF-003: Administración de Productos

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Administrador accede a gestión de productos
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar lista de productos desde GET /api/productos
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar tabla de productos con filtros
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere crear producto?
- **CONEXIÓN:** SÍ hacia Paso 5, NO hacia Paso 12

**Paso 5:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar modal de creación con formulario
- **CONEXIÓN:** Hacia Paso 6

**Paso 6:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario llena datos: nombre, categoría, proveedor, precios
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Validar formulario en frontend
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Datos válidos?
- **CONEXIÓN:** NO hacia Paso 11, SÍ hacia Paso 9

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar POST /api/productos con datos completos
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** ProductoService.crearProductoCompleto() ejecuta:
- Crear producto
- Crear historial de precios
- Crear historial de costos  
- Crear inventario inicial
- Registrar movimiento "Creación"
- **CONEXIÓN:** Hacia Paso 18

**Paso 11:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar errores de validación
- **CONEXIÓN:** Hacia Paso 6

**Paso 12:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere editar producto?
- **CONEXIÓN:** SÍ hacia Paso 13, NO hacia Paso 19

**Paso 13:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar modal de edición con datos pre-cargados
- **CONEXIÓN:** Hacia Paso 14

**Paso 14:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario modifica campos necesarios
- **CONEXIÓN:** Hacia Paso 15

**Paso 15:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar PUT/PATCH /api/productos/{id}
- **CONEXIÓN:** Hacia Paso 16

**Paso 16:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** ProductoService detecta cambios específicos
- **CONEXIÓN:** Hacia Paso 17

**Paso 17:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Registrar movimiento "Edición" con campos modificados
- **CONEXIÓN:** Hacia Paso 18

**Paso 18:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar notificación de éxito
- **CONEXIÓN:** Hacia Paso 21

**Paso 19:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere eliminar producto?
- **CONEXIÓN:** SÍ hacia Paso 20, NO hacia Paso 22

**Paso 20:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cambiar estado del producto a "Inactivo" (soft delete)
- **CONEXIÓN:** Hacia Paso 18

**Paso 21:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Recargar lista de productos actualizada
- **CONEXIÓN:** Hacia Paso 3

**Paso 22:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Gestión de productos completada
- **CONEXIÓN:** Ninguna

### RF-006: Gestión de Workspaces (Mesas/Áreas)

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Usuario accede a selección de workspace
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar lista de workspaces desde GET /api/workspaces
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Para cada workspace, consultar órdenes activas
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Workspace tiene órdenes activas?
- **CONEXIÓN:** SÍ hacia Paso 5, NO hacia Paso 6

**Paso 5:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Determinar estado: Ocupado (órdenes en carrito) o Cuenta Solicitada (venta procesada)
- **CONEXIÓN:** Hacia Paso 7

**Paso 6:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Marcar workspace como Libre
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar workspace con color según estado:
- Verde: Libre
- Azul: Ocupado  
- Amarillo: Cuenta Solicitada
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario selecciona workspace?
- **CONEXIÓN:** SÍ hacia Paso 9, NO hacia Paso 11

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Navegar a PuntoDeVenta con workspaceId seleccionado
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Workspace seleccionado, continuar con POS
- **CONEXIÓN:** Ninguna

**Paso 11:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Usuario permanece en selección de workspace
- **CONEXIÓN:** Ninguna

### RF-007: Carrito de Compras Temporal

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Usuario en POS selecciona producto
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar productos disponibles con stock > 0
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario selecciona producto y especifica cantidad
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Validar stock disponible en tiempo real
- **CONEXIÓN:** Hacia Paso 5

**Paso 5:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Stock suficiente?
- **CONEXIÓN:** NO hacia Paso 6, SÍ hacia Paso 7

**Paso 6:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error "Stock insuficiente"
- **CONEXIÓN:** Hacia Paso 3

**Paso 7:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar POST /api/ordenes-workspace/agregar-producto
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** OrdenesWorkspaceService.agregarOActualizarProducto():
- Verificar si producto ya está en carrito
- Si existe, sumar cantidades
- Si no existe, crear nueva línea
- **CONEXIÓN:** Hacia Paso 9

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Decrementar inventario automáticamente
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Guardar orden en tabla ordenes_workspace
- **CONEXIÓN:** Hacia Paso 11

**Paso 11:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Actualizar interfaz del carrito con nuevo producto
- **CONEXIÓN:** Hacia Paso 12

**Paso 12:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Calcular totales automáticamente
- **CONEXIÓN:** Hacia Paso 13

**Paso 13:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere agregar más productos?
- **CONEXIÓN:** SÍ hacia Paso 3, NO hacia Paso 14

**Paso 14:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere modificar cantidad?
- **CONEXIÓN:** SÍ hacia Paso 15, NO hacia Paso 18

**Paso 15:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario cambia cantidad en interfaz
- **CONEXIÓN:** Hacia Paso 16

**Paso 16:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Validar nueva cantidad contra stock
- **CONEXIÓN:** Hacia Paso 17

**Paso 17:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Actualizar orden en backend y ajustar inventario
- **CONEXIÓN:** Hacia Paso 11

**Paso 18:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere eliminar producto?
- **CONEXIÓN:** SÍ hacia Paso 19, NO hacia Paso 21

**Paso 19:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Eliminar línea de ordenes_workspace
- **CONEXIÓN:** Hacia Paso 20

**Paso 20:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Restaurar inventario del producto eliminado
- **CONEXIÓN:** Hacia Paso 11

**Paso 21:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Carrito listo para procesar venta
- **CONEXIÓN:** Ninguna

### RF-008: Procesamiento de Ventas

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Usuario presiona "Solicitar Cuenta"
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Carrito tiene productos?
- **CONEXIÓN:** NO hacia Paso 3, SÍ hacia Paso 4

**Paso 3:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error "Carrito vacío"
- **CONEXIÓN:** Hacia Paso 19

**Paso 4:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Obtener órdenes del workspace actual
- **CONEXIÓN:** Hacia Paso 5

**Paso 5:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar ticket con resumen de productos y total
- **CONEXIÓN:** Hacia Paso 6

**Paso 6:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario selecciona método de pago
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Método de pago seleccionado?
- **CONEXIÓN:** NO hacia Paso 8, SÍ hacia Paso 9

**Paso 8:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error "Seleccione método de pago"
- **CONEXIÓN:** Hacia Paso 6

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar POST /api/workspaces/{id}/finalizar-venta
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** VentaService.procesarVentaCompleta():
- Obtener órdenes del workspace
- Calcular totales
- Crear registro en ordenes_de_ventas
- **CONEXIÓN:** Hacia Paso 11

**Paso 11:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Para cada producto en el carrito:
- Crear detalle en detalles_ordenes_de_ventas
- Registrar movimiento de inventario tipo "Venta"
- **CONEXIÓN:** Hacia Paso 12

**Paso 12:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Limpiar ordenes_workspace del workspace procesado
- **CONEXIÓN:** Hacia Paso 13

**Paso 13:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Venta procesada exitosamente?
- **CONEXIÓN:** NO hacia Paso 17, SÍ hacia Paso 14

**Paso 14:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar notificación "Venta completada exitosamente"
- **CONEXIÓN:** Hacia Paso 15

**Paso 15:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Actualizar estado del workspace a "Cuenta Solicitada"
- **CONEXIÓN:** Hacia Paso 16

**Paso 16:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Redirigir a lista de workspaces
- **CONEXIÓN:** Hacia Paso 18

**Paso 17:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar error específico y realizar rollback
- **CONEXIÓN:** Hacia Paso 19

**Paso 18:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Venta completada exitosamente
- **CONEXIÓN:** Ninguna

**Paso 19:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Venta cancelada o fallida
- **CONEXIÓN:** Ninguna

### RF-009: Gestión de Órdenes de Compra

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Administrador accede a sistema de compras
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar lista de proveedores con estado de deuda
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar proveedores con colores según deuda:
- Verde: Sin deuda
- Amarillo: Con deuda pendiente
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario selecciona proveedor
- **CONEXIÓN:** Hacia Paso 5

**Paso 5:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar productos disponibles del proveedor seleccionado
- **CONEXIÓN:** Hacia Paso 6

**Paso 6:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar interfaz de compras con productos filtrados
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario selecciona productos y cantidades para comprar
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Agregar productos al carrito de compras
- **CONEXIÓN:** Hacia Paso 9

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Calcular total de compra + deuda existente
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar resumen con:
- Productos seleccionados
- Total de compra
- Deuda anterior
- Total a deber
- **CONEXIÓN:** Hacia Paso 11

**Paso 11:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario confirma compra?
- **CONEXIÓN:** NO hacia Paso 20, SÍ hacia Paso 12

**Paso 12:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar POST /api/compras/crear
- **CONEXIÓN:** Hacia Paso 13

**Paso 13:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** ComprasService.crearOrdenCompra():
- Crear registro en ordenes_de_compras
- Crear detalles en detalles_ordenes_de_compras
- **CONEXIÓN:** Hacia Paso 14

**Paso 14:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Para cada producto comprado:
- Incrementar inventario
- Registrar movimiento tipo "Compra"
- **CONEXIÓN:** Hacia Paso 15

**Paso 15:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere realizar pago inmediato?
- **CONEXIÓN:** NO hacia Paso 18, SÍ hacia Paso 16

**Paso 16:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario especifica monto y método de pago
- **CONEXIÓN:** Hacia Paso 17

**Paso 17:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Registrar pago en historial_cargos_proveedores
- **CONEXIÓN:** Hacia Paso 18

**Paso 18:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar notificación "Compra registrada exitosamente"
- **CONEXIÓN:** Hacia Paso 19

**Paso 19:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Compra completada
- **CONEXIÓN:** Ninguna

**Paso 20:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Compra cancelada
- **CONEXIÓN:** Ninguna

### RF-011: Administración de Empleados

**Paso 1:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** INICIO - Administrador accede a gestión de empleados
- **CONEXIÓN:** Hacia Paso 2

**Paso 2:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cargar lista de empleados desde GET /api/empleados
- **CONEXIÓN:** Hacia Paso 3

**Paso 3:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar tabla con empleados y estados (activo/inactivo)
- **CONEXIÓN:** Hacia Paso 4

**Paso 4:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere crear empleado?
- **CONEXIÓN:** SÍ hacia Paso 5, NO hacia Paso 13

**Paso 5:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar modal con formulario de creación
- **CONEXIÓN:** Hacia Paso 6

**Paso 6:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario llena: nombre, contraseña, teléfono, rol
- **CONEXIÓN:** Hacia Paso 7

**Paso 7:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Validar datos del formulario
- **CONEXIÓN:** Hacia Paso 8

**Paso 8:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Datos válidos?
- **CONEXIÓN:** NO hacia Paso 12, SÍ hacia Paso 9

**Paso 9:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar POST /api/empleados
- **CONEXIÓN:** Hacia Paso 10

**Paso 10:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** EmpleadoService:
- Encriptar contraseña con BCrypt
- Crear usuario con estado "Activo"
- Asignar rol seleccionado
- **CONEXIÓN:** Hacia Paso 11

**Paso 11:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar notificación "Empleado creado exitosamente"
- **CONEXIÓN:** Hacia Paso 2

**Paso 12:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Mostrar errores de validación
- **CONEXIÓN:** Hacia Paso 6

**Paso 13:**
- **FORMA:** Rombo
- **TEXTO INTERIOR:** ¿Usuario quiere cambiar estado de empleado?
- **CONEXIÓN:** SÍ hacia Paso 14, NO hacia Paso 18

**Paso 14:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Usuario hace clic en toggle de estado
- **CONEXIÓN:** Hacia Paso 15

**Paso 15:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Enviar PUT /api/empleados/{id}/estado
- **CONEXIÓN:** Hacia Paso 16

**Paso 16:**
- **FORMA:** Rectángulo
- **TEXTO INTERIOR:** Cambiar estado en base de datos (Activo ↔ Inactivo)
- **CONEXIÓN:** Hacia Paso 17

**Paso 17:**
- **FORMA:** Paralelogramo
- **TEXTO INTERIOR:** Actualizar interfaz con nuevo estado
- **CONEXIÓN:** Hacia Paso 2

**Paso 18:**
- **FORMA:** Óvalo
- **TEXTO INTERIOR:** FIN - Gestión de empleados completada
- **CONEXIÓN:** Ninguna

---

## PARTE 3: DESCRIPCIÓN DE DIAGRAMAS DE CASOS DE USO UML

### Actores del Sistema

#### Actor Principal: Administrador
- **Descripción:** Usuario con permisos completos para gestionar todas las funcionalidades del sistema
- **Representación UML:** Figura humana con etiqueta "Administrador"
- **Características:** Acceso a todos los casos de uso del sistema

#### Actor Principal: Empleado
- **Descripción:** Usuario con permisos limitados, únicamente para operaciones de punto de venta
- **Representación UML:** Figura humana con etiqueta "Empleado"
- **Características:** Acceso restringido solo a casos de uso de ventas

#### Actor Secundario: Sistema de Base de Datos
- **Descripción:** Sistema externo que almacena y gestiona la persistencia de datos
- **Representación UML:** Rectángulo con etiqueta "<<system>> Base de Datos PostgreSQL"
- **Características:** Interactúa con todos los casos de uso para persistencia

#### Actor Secundario: Sistema de Autenticación JWT
- **Descripción:** Servicio interno para manejo de tokens y sesiones
- **Representación UML:** Rectángulo con etiqueta "<<system>> Servicio JWT"
- **Características:** Gestiona autenticación y autorización

### Casos de Uso por Módulo

#### Módulo 1: Gestión de Autenticación y Autorización

**Caso de Uso: Iniciar Sesión**
- **ID:** UC-001
- **Actor Principal:** Administrador, Empleado
- **Descripción:** El usuario se autentica en el sistema mediante credenciales
- **Representación UML:** Óvalo con texto "Iniciar Sesión"
- **Relaciones:**
  - **Include:** <<include>> "Validar Credenciales"
  - **Include:** <<include>> "Generar Token JWT"
  - **Include:** <<include>> "Verificar Estado Usuario"
- **Precondiciones:** Usuario tiene credenciales válidas
- **Postcondiciones:** Usuario autenticado con sesión activa

**Caso de Uso: Validar Credenciales**
- **ID:** UC-002
- **Actor Principal:** Sistema de Base de Datos
- **Descripción:** Verificar usuario y contraseña contra la base de datos
- **Representación UML:** Óvalo con texto "Validar Credenciales"
- **Relaciones:**
  - **Included by:** UC-001 "Iniciar Sesión"

**Caso de Uso: Controlar Acceso por Rol**
- **ID:** UC-003
- **Actor Principal:** Sistema
- **Descripción:** Restringir funcionalidades según el rol del usuario
- **Representación UML:** Óvalo con texto "Controlar Acceso por Rol"
- **Relaciones:**
  - **Extend:** <<extend>> "Denegar Acceso" (condición: rol insuficiente)

**Caso de Uso: Cerrar Sesión**
- **ID:** UC-004
- **Actor Principal:** Administrador, Empleado
- **Descripción:** Terminar sesión activa y limpiar tokens
- **Representación UML:** Óvalo con texto "Cerrar Sesión"

#### Módulo 2: Gestión de Inventarios

**Caso de Uso: Gestionar Productos**
- **ID:** UC-005
- **Actor Principal:** Administrador
- **Descripción:** Crear, editar, consultar y eliminar productos del catálogo
- **Representación UML:** Óvalo con texto "Gestionar Productos"
- **Relaciones:**
  - **Include:** <<include>> "Validar Datos Producto"
  - **Include:** <<include>> "Registrar Movimiento Inventario"
  - **Extend:** <<extend>> "Crear Historial Precios" (condición: precio modificado)
  - **Extend:** <<extend>> "Crear Historial Costos" (condición: costo modificado)

**Caso de Uso: Consultar Stock**
- **ID:** UC-006
- **Actor Principal:** Administrador, Empleado
- **Descripción:** Verificar disponibilidad de productos en inventario
- **Representación UML:** Óvalo con texto "Consultar Stock"
- **Relaciones:**
  - **Include:** <<include>> "Filtrar por Ubicación"

**Caso de Uso: Registrar Movimiento Inventario**
- **ID:** UC-007
- **Actor Principal:** Sistema
- **Descripción:** Documentar todos los cambios en el inventario para auditoría
- **Representación UML:** Óvalo con texto "Registrar Movimiento Inventario"
- **Relaciones:**
  - **Included by:** UC-005, UC-010, UC-013

**Caso de Uso: Actualizar Stock**
- **ID:** UC-008
- **Actor Principal:** Sistema
- **Descripción:** Modificar cantidades en inventario por ventas/compras
- **Representación UML:** Óvalo con texto "Actualizar Stock"
- **Relaciones:**
  - **Include:** <<include>> "Validar Stock Suficiente"

#### Módulo 3: Sistema de Punto de Venta (POS)

**Caso de Uso: Seleccionar Workspace**
- **ID:** UC-009
- **Actor Principal:** Administrador, Empleado
- **Descripción:** Elegir mesa o área de trabajo para iniciar atención
- **Representación UML:** Óvalo con texto "Seleccionar Workspace"
- **Relaciones:**
  - **Include:** <<include>> "Consultar Estado Workspace"

**Caso de Uso: Gestionar Carrito Temporal**
- **ID:** UC-010
- **Actor Principal:** Administrador, Empleado
- **Descripción:** Agregar, modificar y eliminar productos en orden temporal
- **Representación UML:** Óvalo con texto "Gestionar Carrito Temporal"
- **Relaciones:**
  - **Include:** <<include>> "Validar Stock Disponible"
  - **Include:** <<include>> "Calcular Totales"
  - **Include:** <<include>> "Actualizar Stock"

**Caso de Uso: Procesar Venta**
- **ID:** UC-011
- **Actor Principal:** Administrador, Empleado
- **Descripción:** Finalizar venta, generar ticket y actualizar inventario
- **Representación UML:** Óvalo con texto "Procesar Venta"
- **Relaciones:**
  - **Include:** <<include>> "Seleccionar Método Pago"
  - **Include:** <<include>> "Generar Ticket Venta"
  - **Include:** <<include>> "Registrar Movimiento Inventario"
  - **Include:** <<include>> "Limpiar Workspace"

**Caso de Uso: Generar Ticket Venta**
- **ID:** UC-012
- **Actor Principal:** Sistema
- **Descripción:** Crear comprobante de venta con detalles de productos
- **Representación UML:** Óvalo con texto "Generar Ticket Venta"
- **Relaciones:**
  - **Included by:** UC-011 "Procesar Venta"

#### Módulo 4: Sistema de Compras a Proveedores

**Caso de Uso: Gestionar Compras a Proveedores**
- **ID:** UC-013
- **Actor Principal:** Administrador
- **Descripción:** Realizar órdenes de compra y gestionar inventario entrante
- **Representación UML:** Óvalo con texto "Gestionar Compras a Proveedores"
- **Relaciones:**
  - **Include:** <<include>> "Seleccionar Proveedor"
  - **Include:** <<include>> "Calcular Deuda Proveedor"
  - **Include:** <<include>> "Registrar Movimiento Inventario"
  - **Extend:** <<extend>> "Realizar Pago Inmediato" (condición: usuario elige pagar)

**Caso de Uso: Calcular Deuda Proveedor**
- **ID:** UC-014
- **Actor Principal:** Sistema
- **Descripción:** Determinar saldo pendiente por proveedor (compras - pagos)
- **Representación UML:** Óvalo con texto "Calcular Deuda Proveedor"
- **Relaciones:**
  - **Included by:** UC-013 "Gestionar Compras a Proveedores"

**Caso de Uso: Registrar Pago Proveedor**
- **ID:** UC-015
- **Actor Principal:** Administrador
- **Descripción:** Documentar pagos realizados a proveedores
- **Representación UML:** Óvalo con texto "Registrar Pago Proveedor"
- **Relaciones:**
  - **Include:** <<include>> "Validar Monto Pago"

#### Módulo 5: Gestión de Personal

**Caso de Uso: Administrar Empleados**
- **ID:** UC-016
- **Actor Principal:** Administrador
- **Descripción:** Crear, editar y gestionar estados de empleados
- **Representación UML:** Óvalo con texto "Administrar Empleados"
- **Relaciones:**
  - **Include:** <<include>> "Encriptar Contraseña"
  - **Include:** <<include>> "Asignar Rol"
  - **Extend:** <<extend>> "Desactivar Empleado" (condición: empleado inactivo)

**Caso de Uso: Gestionar Personas**
- **ID:** UC-017
- **Actor Principal:** Administrador
- **Descripción:** Administrar datos de clientes, proveedores y empleados
- **Representación UML:** Óvalo con texto "Gestionar Personas"
- **Relaciones:**
  - **Include:** <<include>> "Categorizar Persona"
  - **Include:** <<include>> "Validar Datos Fiscales"

#### Módulo 6: Reportes y Consultas

**Caso de Uso: Generar Reportes**
- **ID:** UC-018
- **Actor Principal:** Administrador
- **Descripción:** Crear reportes de ventas, compras y movimientos
- **Representación UML:** Óvalo con texto "Generar Reportes"
- **Relaciones:**
  - **Include:** <<include>> "Filtrar por Fecha"
  - **Include:** <<include>> "Exportar Datos"

**Caso de Uso: Consultar Historial Transacciones**
- **ID:** UC-019
- **Actor Principal:** Administrador
- **Descripción:** Revisar historial de operaciones para auditoría
- **Representación UML:** Óvalo con texto "Consultar Historial Transacciones"
- **Relaciones:**
  - **Include:** <<include>> "Aplicar Filtros Búsqueda"

### Diagramas de Casos de Uso Específicos

#### Diagrama 1: Módulo de Autenticación
```
Elementos del diagrama:
- Actor: Administrador (izquierda superior)
- Actor: Empleado (izquierda inferior)
- Actor: <<system>> Servicio JWT (derecha)

Casos de Uso:
- UC-001: Iniciar Sesión (centro)
- UC-002: Validar Credenciales (centro-derecha)
- UC-003: Controlar Acceso por Rol (centro-superior)
- UC-004: Cerrar Sesión (centro-inferior)

Relaciones:
- Administrador ---> UC-001 (association)
- Empleado ---> UC-001 (association)
- UC-001 <<include>> UC-002 (include)
- UC-001 ---> Servicio JWT (association)
- UC-003 <<extend>> UC-001 (extend, condición: "usuario autenticado")
- Administrador ---> UC-004 (association)
- Empleado ---> UC-004 (association)
```

#### Diagrama 2: Módulo de Punto de Venta
```
Elementos del diagrama:
- Actor: Administrador (izquierda superior)
- Actor: Empleado (izquierda inferior)
- Actor: <<system>> Base de Datos (derecha)

Casos de Uso:
- UC-009: Seleccionar Workspace (izquierda-centro)
- UC-010: Gestionar Carrito Temporal (centro)
- UC-011: Procesar Venta (centro-derecha)
- UC-012: Generar Ticket Venta (derecha-superior)
- UC-006: Consultar Stock (centro-inferior)

Relaciones:
- Administrador ---> UC-009, UC-010, UC-011 (association)
- Empleado ---> UC-009, UC-010, UC-011 (association)
- UC-010 <<include>> UC-006 (include)
- UC-011 <<include>> UC-012 (include)
- UC-010 ---> Base de Datos (association)
- UC-011 ---> Base de Datos (association)
```

#### Diagrama 3: Módulo de Gestión de Inventarios
```
Elementos del diagrama:
- Actor: Administrador (izquierda)
- Actor: <<system>> Base de Datos (derecha)

Casos de Uso:
- UC-005: Gestionar Productos (centro-superior)
- UC-006: Consultar Stock (centro)
- UC-007: Registrar Movimiento Inventario (centro-derecha)
- UC-008: Actualizar Stock (centro-inferior)

Relaciones:
- Administrador ---> UC-005, UC-006 (association)
- UC-005 <<include>> UC-007 (include)
- UC-008 <<include>> UC-007 (include)
- Todos los casos de uso ---> Base de Datos (association)
```

#### Diagrama 4: Módulo de Compras a Proveedores
```
Elementos del diagrama:
- Actor: Administrador (izquierda)
- Actor: <<system>> Base de Datos (derecha)

Casos de Uso:
- UC-013: Gestionar Compras a Proveedores (centro)
- UC-014: Calcular Deuda Proveedor (centro-superior)
- UC-015: Registrar Pago Proveedor (centro-inferior)
- UC-007: Registrar Movimiento Inventario (derecha)

Relaciones:
- Administrador ---> UC-013, UC-015 (association)
- UC-013 <<include>> UC-014 (include)
- UC-013 <<include>> UC-007 (include)
- UC-013 <<extend>> UC-015 (extend, condición: "pago inmediato")
- Todos los casos de uso ---> Base de Datos (association)
```

#### Diagrama 5: Módulo de Gestión de Personal
```
Elementos del diagrama:
- Actor: Administrador (izquierda)
- Actor: <<system>> Base de Datos (derecha)

Casos de Uso:
- UC-016: Administrar Empleados (centro-superior)
- UC-017: Gestionar Personas (centro-inferior)

Relaciones:
- Administrador ---> UC-016, UC-017 (association)
- UC-016 ---> Base de Datos (association)
- UC-017 ---> Base de Datos (association)
```

### Especificaciones de Relaciones UML

#### Relaciones Include (<<include>>)
- **Uso:** Cuando un caso de uso siempre requiere la funcionalidad de otro
- **Representación:** Línea punteada con flecha y estereotipo <<include>>
- **Ejemplos en el sistema:**
  - "Iniciar Sesión" <<include>> "Validar Credenciales"
  - "Procesar Venta" <<include>> "Generar Ticket Venta"
  - "Gestionar Carrito" <<include>> "Consultar Stock"

#### Relaciones Extend (<<extend>>)
- **Uso:** Cuando un caso de uso opcionalmente extiende a otro bajo ciertas condiciones
- **Representación:** Línea punteada con flecha y estereotipo <<extend>>
- **Ejemplos en el sistema:**
  - "Realizar Pago Inmediato" <<extend>> "Gestionar Compras" (condición: usuario elige pagar)
  - "Desactivar Empleado" <<extend>> "Administrar Empleados" (condición: empleado inactivo)

#### Relaciones de Asociación
- **Uso:** Conexión directa entre actor y caso de uso
- **Representación:** Línea sólida simple
- **Ejemplos en el sistema:**
  - Administrador ---> Gestionar Productos
  - Empleado ---> Procesar Venta

#### Relaciones de Generalización
- **Uso:** Cuando un actor es una especialización de otro
- **Representación:** Línea sólida con triángulo vacío
- **Nota:** En este sistema no se implementaron generalizaciones de actores

### Paquetes del Sistema

#### Paquete: Autenticación y Seguridad
- **Casos de Uso:** UC-001, UC-002, UC-003, UC-004
- **Responsabilidad:** Gestionar acceso y permisos del sistema

#### Paquete: Gestión de Inventarios
- **Casos de Uso:** UC-005, UC-006, UC-007, UC-008
- **Responsabilidad:** Administrar productos y stock

#### Paquete: Punto de Venta
- **Casos de Uso:** UC-009, UC-010, UC-011, UC-012
- **Responsabilidad:** Procesar ventas y atención al cliente

#### Paquete: Compras y Proveedores
- **Casos de Uso:** UC-013, UC-014, UC-015
- **Responsabilidad:** Gestionar adquisiciones y relaciones con proveedores

#### Paquete: Gestión de Personal
- **Casos de Uso:** UC-016, UC-017
- **Responsabilidad:** Administrar empleados y personas del sistema

#### Paquete: Reportes y Consultas
- **Casos de Uso:** UC-018, UC-019
- **Responsabilidad:** Generar información para toma de decisiones

### Notas para Implementación del Diagrama

#### Herramientas Recomendadas
- **Enterprise Architect:** Para diagramas profesionales completos
- **Lucidchart:** Para colaboración en línea
- **Draw.io (diagrams.net):** Herramienta gratuita y efectiva
- **Visual Paradigm:** Para modelado UML completo
- **PlantUML:** Para diagramas como código

#### Convenciones de Nomenclatura
- **Casos de Uso:** Verbos en infinitivo (ej. "Gestionar Productos")
- **Actores:** Sustantivos que representan roles (ej. "Administrador")
- **Sistemas:** Estereotipo <<system>> (ej. "<<system>> Base de Datos")

#### Distribución Visual Recomendada
- **Actores principales:** Lado izquierdo del diagrama
- **Casos de uso:** Centro del diagrama organizados por funcionalidad
- **Actores secundarios:** Lado derecho del diagrama
- **Relaciones:** Minimizar cruces de líneas para claridad

#### Colores Sugeridos
- **Administrador:** Azul (mayor autoridad)
- **Empleado:** Verde (operativo)
- **Sistemas externos:** Gris (diferenciación)
- **Casos de uso críticos:** Amarillo (destacar importancia)
- **Casos de uso de soporte:** Blanco (función estándar)

---

**Documento generado mediante análisis exhaustivo del codebase del Sistema POS Finanzas**  
**Fecha:** Diciembre 2024  
**Versión:** 1.0

