# Funcionalidad de Deudas a Proveedores

## Resumen
Se ha implementado una funcionalidad completa para el cálculo y visualización de deudas a proveedores en el sistema POS. Esta funcionalidad permite tener un control preciso de las obligaciones financieras pendientes con los proveedores.

## Componentes Implementados

### Backend (Java/Spring Boot)

#### 1. **DeudaProveedorDTO** 
- **Archivo**: `backend/src/main/java/com/posfin/pos_finanzas_backend/dtos/DeudaProveedorDTO.java`
- **Descripción**: DTO que encapsula la información de deuda de un proveedor
- **Campos principales**:
  - `proveedorId`, `proveedorNombre`: Información del proveedor
  - `telefonoProveedor`, `emailProveedor`: Datos de contacto
  - `totalCompras`, `totalPagos`, `deudaPendiente`: Montos financieros
  - `estadoDeuda`: Estado visual ('verde' sin deuda, 'amarillo' con deuda)
  - `fechaOrdenMasAntigua`, `cantidadOrdenesPendientes`: Información adicional

#### 2. **Consultas de Repositorio Mejoradas**
- **Archivo**: `backend/src/main/java/com/posfin/pos_finanzas_backend/repositories/OrdenesDeComprasRepository.java`
- **Nuevos métodos**:
  - `obtenerDeudasProveedores()`: Consulta nativa SQL optimizada que calcula deudas por proveedor
  - `findOrdersMasAntiguasByProveedorId()`: Obtiene la orden más antigua de un proveedor
  - `contarOrdenesPendientesByProveedorId()`: Cuenta órdenes pendientes

#### 3. **DeudasProveedoresService**
- **Archivo**: `backend/src/main/java/com/posfin/pos_finanzas_backend/services/DeudasProveedoresService.java`
- **Funcionalidades**:
  - Cálculo de deudas por proveedor usando la diferencia entre compras y pagos
  - Obtención de estadísticas generales (total proveedores con deuda, total adeudado, promedio)
  - Filtrado de proveedores que solo tienen deudas pendientes
  - Manejo de valores nulos y casos extremos

#### 4. **DeudasProveedoresController**
- **Archivo**: `backend/src/main/java/com/posfin/pos_finanzas_backend/controllers/DeudasProveedoresController.java`
- **Endpoints REST**:
  - `GET /api/deudas-proveedores`: Lista completa de proveedores con deudas
  - `GET /api/deudas-proveedores/{proveedorId}`: Deuda específica de un proveedor
  - `GET /api/deudas-proveedores/estadisticas`: Estadísticas generales
  - `GET /api/deudas-proveedores/total`: Total de deudas pendientes

#### 5. **Pruebas Unitarias**
- **Archivo**: `backend/src/test/java/com/posfin/pos_finanzas_backend/services/DeudasProveedoresServiceTest.java`
- **Cobertura**: Casos de éxito, casos sin deudas, manejo de nulos, cálculos estadísticos

### Frontend (React/TypeScript)

#### 1. **Tipos TypeScript**
- **Archivo**: `frontend/src/types/index.ts`
- **Nuevos tipos**:
  - `DeudaProveedor`: Interface principal para deudas de proveedores
  - `EstadisticasDeudas`: Interface para estadísticas agregadas

#### 2. **deudasService**
- **Archivo**: `frontend/src/services/deudasService.ts`
- **Funcionalidades**:
  - Comunicación con API REST del backend
  - Métodos de filtrado y ordenamiento client-side
  - Manejo de autenticación JWT
  - Gestión de errores y casos extremos

#### 3. **Componente DeudasProveedores**
- **Archivo**: `frontend/src/components/DeudasProveedores.tsx`
- **Funcionalidades**:
  - Visualización de deudas en tabla responsiva
  - Filtros por estado (con/sin deuda)
  - Búsqueda por nombre de proveedor
  - Ordenamiento por deuda, nombre o fecha
  - Tarjetas de estadísticas en tiempo real
  - Estados de carga y manejo de errores

#### 4. **Estilos CSS**
- **Archivo**: `frontend/src/components/DeudasProveedores.css`
- **Características**:
  - Diseño responsivo para móviles y escritorio
  - Indicadores visuales de estado (verde/amarillo)
  - Animaciones suaves y feedback visual
  - Paleta de colores consistente con el sistema

## Funcionalidades Principales

### 1. **Cálculo Automático de Deudas**
- Resta automática: `Deuda = Total Compras - Total Pagos`
- Filtrado automático de proveedores sin deudas pendientes
- Actualización en tiempo real al realizar nuevos pagos

### 2. **Visualización Intuitiva**
- **Tarjetas de estadísticas**: Resumen ejecutivo en la parte superior
- **Tabla detallada**: Información completa por proveedor
- **Indicadores de estado**: Verde (sin deuda) / Amarillo (con deuda)
- **Información de contacto**: Teléfono y email accesibles

### 3. **Filtros y Búsqueda Avanzada**
- **Filtro por estado**: Todos, Con deuda, Sin deuda
- **Búsqueda por nombre**: Búsqueda en tiempo real
- **Ordenamiento múltiple**: Por deuda (mayor a menor), nombre (A-Z), fecha más antigua

### 4. **Información Contextual**
- **Fecha de orden más antigua**: Para priorizar pagos urgentes
- **Cantidad de órdenes pendientes**: Volumen de transacciones
- **Contacto directo**: Información para comunicación inmediata

## Integración con el Sistema

### Base de Datos
La funcionalidad utiliza las siguientes tablas existentes:
- `ordenes_de_compras`: Para obtener los totales de compras por proveedor
- `historial_cargos_proveedores`: Para sumar los pagos realizados
- `personas`: Para información del proveedor (con filtro de categoría 'Proveedor')

### Seguridad
- Autenticación JWT requerida para todos los endpoints
- Validación de tokens en frontend y backend
- Manejo seguro de errores sin exposición de información sensible

### Performance
- Consulta SQL optimizada con JOINs eficientes
- Paginación potencial para grandes volúmenes de datos
- Carga asíncrona en frontend con estados de loading

## Casos de Uso

1. **Administrador financiero**: Revisar deudas pendientes semanalmente
2. **Gerente de compras**: Priorizar pagos por antigüedad de orden
3. **Contabilidad**: Generar reportes de obligaciones pendientes
4. **Atención a proveedores**: Consultar estado de cuenta específico

## Mantenimiento y Extensibilidad

### Posibles Mejoras Futuras
- **Exportación a Excel/PDF**: Para reportes formales
- **Alertas automáticas**: Notificaciones de deudas vencidas
- **Historial de pagos**: Vista detallada por proveedor
- **Proyección de flujo de caja**: Predicción basada en deudas pendientes

### Consideraciones Técnicas
- El componente es completamente reutilizable
- La lógica de negocio está separada en el servicio
- Los estilos son modulares y customizables
- Las pruebas unitarias garantizan la funcionalidad core

## Configuración y Despliegue

No se requieren configuraciones adicionales. La funcionalidad utiliza:
- La misma base de datos existente
- El mismo sistema de autenticación
- Las mismas dependencias del proyecto
- Los mismos patrones de arquitectura establecidos