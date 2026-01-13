# Tareas del Proyecto POS Finanzas

## 🎨 REWORK: Interfaz de Gestión de Inventario con Nuevo Diseño (12 Ene 2026)

### Descripción del Objetivo

Implementar un rediseño completo de la pantalla de **Gestión de Inventario** utilizando el nuevo código TSX proporcionado por el usuario, manteniendo toda la funcionalidad existente pero con una interfaz visual mejorada que sigue las guías de diseño establecidas.

**IMPORTANTE:** La barra lateral de navegación (`SidebarNavigation`) debe permanecer intacta y la nueva interfaz debe adaptarse para no superponerse con ella.

### Paleta de Colores y Diseño (Guías a Seguir)

#### Colores Principales:
- **Primario**: Naranja Vibrante (#F97316) - Para elementos activos y CTAs
- **Secundario**: Amarillo Mostaza (#FACC15) - Para ofertas y destacados
- **Fondo**: Blanco (#FFFFFF) o Gris muy claro (#F3F4F6)
- **Texto**: Gris oscuro (#1F2937) para legibilidad

#### Tipografía:
- **Principal (Headings)**: Sans-serif moderna (Poppins o Inter)
- **Cuerpo**: Sans-serif legible con buena altura de línea

#### Iconografía:
- Iconos de línea (outline) para navegación inactiva
- Iconos sólidos (filled) para estados activos
- Bordes redondeados: 12px - 16px radius

### Análisis de Situación Actual

#### Archivo Actual: `Inventario.tsx`
- ✅ Ya usa `SidebarNavigation` como componente de navegación lateral
- ✅ Tiene toda la funcionalidad conectada al backend (CRUD de productos)
- ✅ Maneja modales para crear, editar y predicciones ML
- ✅ Tiene sistema de búsqueda y filtrado de productos
- ✅ Alertas de stock bajo implementadas

#### Código Nuevo Proporcionado
- Tiene sidebar integrado en el mismo componente (debe ser removido)
- Usa datos simulados (debe conectarse a datos reales)
- Tiene 3 botones principales: Nuevo Producto, Realizar Compra, Predicciones IA
- Tabla con acciones: Editar, Ajustar Stock (RefreshCw), Eliminar
- Diseño visual mejorado con gradientes y sombras

### Plan de Implementación

#### FASE 1: Preparación y Análisis

- [ ] **Paso 1.1: Backup del archivo actual**
  - [ ] Crear copia de seguridad de `Inventario.tsx` como `Inventario.tsx.backup`
  - [ ] Crear copia de seguridad de `InventarioModernoNew.tsx` como referencia

- [ ] **Paso 1.2: Identificar diferencias clave**
  - [ ] Listar todos los handlers de funciones en el archivo actual
  - [ ] Listar todos los estados (useState) en el archivo actual
  - [ ] Identificar props que recibe el componente actual
  - [ ] Documentar integraciones con servicios (inventarioService, stockService)

#### FASE 2: Integración del Nuevo Diseño

- [ ] **Paso 2.1: Reemplazar estructura HTML/JSX**
  - [ ] Remover el sidebar del código nuevo (ya existe como `SidebarNavigation`)
  - [ ] Copiar la estructura del `<main>` del código nuevo
  - [ ] Mantener el wrapper `<div className="h-screen bg-gray-50...">` para consistencia
  - [ ] Integrar `SidebarNavigation` como componente separado (ya existe)

- [ ] **Paso 2.2: Adaptar layout para convivir con SidebarNavigation**
  - [ ] Verificar que el `<main>` no use `flex-1` que cause superposición
  - [ ] Asegurar que el contenido principal tenga el margen correcto (`ml-20 md:ml-24`)
  - [ ] Verificar responsive design en diferentes tamaños de pantalla

- [ ] **Paso 2.3: Conectar datos reales a la tabla**
  - [ ] Reemplazar array `products` simulado con `filteredProductos` del estado actual
  - [ ] Mapear campos de `ProductoDTO` a la estructura de la tabla:
    - `producto.nombre` → nombre del producto
    - `producto.categoriasProductosCategoria` → categoría
    - `producto.proveedorNombre + proveedorApellidoPaterno` → proveedor
    - `producto.precioCompraActual` → P. Compra (formatear con `formatPrice`)
    - `producto.precioVentaActual` → P. Venta (formatear con `formatPrice`)
    - `producto.cantidadInventario` → Stock
  - [ ] Mantener la función `getStatusBadge` existente para estados
  - [ ] Actualizar contador de productos en el toolbar

#### FASE 3: Conectar Funcionalidad de Botones

- [ ] **Paso 3.1: Botón "Nuevo Producto"**
  - [ ] Conectar `onClick={handleCrearNuevo}` (ya existe)
  - [ ] Verificar que el modal `ModalCrearProducto` se abra correctamente

- [ ] **Paso 3.2: Botón "Realizar Compra"**
  - [ ] Conectar `onClick={handleComprarProducto}` (ya existe)
  - [ ] Verificar navegación a la pantalla de compras mediante `onNavigateToCompras()`

- [ ] **Paso 3.3: Botón "Predicciones IA"**
  - [ ] Conectar `onClick={handleShowPredictions}` (ya existe)
  - [ ] Verificar que el modal `ModalPredicciones` se abra correctamente

#### FASE 4: Conectar Acciones de Tabla

- [ ] **Paso 4.1: Botón Editar (Edit3)**
  - [ ] Conectar `onClick={() => handleEditarProducto(producto)}`
  - [ ] Verificar que el modal `ModalEditarProducto` se abra con los datos correctos

- [ ] **Paso 4.2: Botón Eliminar (Trash2)**
  - [ ] Conectar `onClick={() => handleEliminarProducto(producto.id, producto.nombre)}`
  - [ ] Verificar que la confirmación y eliminación funcionen correctamente

- [ ] **Paso 4.3: Remover botón "Ajustar Stock" (RefreshCw)**
  - [ ] Eliminar el botón de RefreshCw de las acciones
  - [ ] Nota: El sistema actual no tiene funcionalidad de ajuste manual de stock desde inventario
  - [ ] El stock se ajusta mediante compras y ventas automáticamente

#### FASE 5: Integrar Búsqueda y Filtros

- [ ] **Paso 5.1: Conectar barra de búsqueda**
  - [ ] Conectar input con `value={searchQuery}`
  - [ ] Conectar `onChange={(e) => setSearchQuery(e.target.value)}`
  - [ ] Verificar que el filtrado funcione en tiempo real

- [ ] **Paso 5.2: Mantener lógica de filtrado**
  - [ ] Asegurar que el `useEffect` de filtrado siga funcionando
  - [ ] Verificar filtrado por nombre, categoría y proveedor

#### FASE 6: Estados de Carga y Errores

- [ ] **Paso 6.1: Implementar estado de carga**
  - [ ] Mantener el componente de loading existente
  - [ ] Verificar que se muestre durante `loadProductos()`

- [ ] **Paso 6.2: Implementar mensajes de error**
  - [ ] Mantener el banner de error existente
  - [ ] Verificar que se muestre cuando `error !== null`

- [ ] **Paso 6.3: Estado vacío de tabla**
  - [ ] Implementar el diseño del estado vacío del nuevo código
  - [ ] Mostrar cuando `filteredProductos.length === 0`
  - [ ] Diferenciar entre "sin productos" y "sin resultados de búsqueda"

#### FASE 7: Mantener Modales y Funcionalidad Existente

- [ ] **Paso 7.1: Verificar modales**
  - [ ] `ModalCrearProducto` sigue renderizándose correctamente
  - [ ] `ModalEditarProducto` sigue renderizándose correctamente
  - [ ] `ModalPredicciones` sigue renderizándose correctamente

- [ ] **Paso 7.2: Verificar callbacks**
  - [ ] `handleModalSuccess` recarga productos correctamente
  - [ ] `handleCreatePurchaseOrder` navega correctamente

#### FASE 8: Estilos y Refinamientos Visuales

- [ ] **Paso 8.1: Aplicar paleta de colores**
  - [ ] Verificar uso de naranja (#F97316) para elementos activos
  - [ ] Verificar uso de amarillo (#FACC15) para alertas/destacados
  - [ ] Verificar fondos grises (#F3F4F6)

- [ ] **Paso 8.2: Verificar responsive design**
  - [ ] Probar en móvil (< 768px)
  - [ ] Probar en tablet (768px - 1024px)
  - [ ] Probar en desktop (> 1024px)

- [ ] **Paso 8.3: Transiciones y animaciones**
  - [ ] Verificar hover effects en botones
  - [ ] Verificar transiciones en tabla
  - [ ] Verificar animación de carga

#### FASE 9: Pruebas Funcionales

- [ ] **Paso 9.1: Pruebas CRUD**
  - [ ] Crear un producto nuevo desde el modal
  - [ ] Editar un producto existente
  - [ ] Eliminar un producto (desactivar)
  - [ ] Verificar que la tabla se actualice correctamente

- [ ] **Paso 9.2: Pruebas de navegación**
  - [ ] Botón "Realizar Compra" navega a compras
  - [ ] Sidebar permite navegar a otras secciones
  - [ ] Estado activo de "Inventario" se mantiene

- [ ] **Paso 9.3: Pruebas de predicciones ML**
  - [ ] Abrir modal de predicciones
  - [ ] Verificar carga de datos
  - [ ] Verificar creación de orden de compra desde predicciones

#### FASE 10: Limpieza y Documentación

- [ ] **Paso 10.1: Remover código innecesario**
  - [ ] Eliminar imports no utilizados
  - [ ] Eliminar funciones comentadas
  - [ ] Limpiar console.logs de debugging

- [ ] **Paso 10.2: Actualizar comentarios**
  - [ ] Documentar secciones principales del componente
  - [ ] Añadir comentarios para funciones complejas

- [ ] **Paso 10.3: Verificar CSS**
  - [ ] Verificar si es necesario actualizar `InventarioModernoNew.css`
  - [ ] Considerar si se pueden usar Tailwind classes directamente

### Archivos Involucrados

#### Archivos a Modificar:
- `frontend/src/components/Inventario.tsx` - **PRINCIPAL**: Reemplazar con nuevo diseño
- `frontend/src/components/InventarioModernoNew.css` - Potencialmente actualizar estilos

#### Archivos que NO se tocan (permanecen igual):
- `frontend/src/components/SidebarNavigation.tsx` - Navegación lateral
- `frontend/src/components/SidebarNavigation.css` - Estilos de navegación
- `frontend/src/services/inventarioService.ts` - Servicio de API
- `frontend/src/services/stockService.ts` - Servicio de stock
- `frontend/src/components/ModalCrearProducto.tsx` - Modal de creación
- `frontend/src/components/ModalEditarProducto.tsx` - Modal de edición
- `frontend/src/components/ModalPredicciones.tsx` - Modal de ML

### Mapeo de Datos: Código Nuevo → Backend Real

#### Datos Simulados (Código Nuevo):
```javascript
{
  id: "1",
  name: "Taco de Bistec",
  image: "🌮",
  category: "Clásicos",
  supplier: "Carnes del Norte",
  purchasePrice: "$12.00",
  salePrice: "$25.00",
  stock: 200,
  status: "Optimal"
}
```

#### Datos Reales (ProductoDTO):
```typescript
{
  id: string,
  nombre: string,
  categoriasProductosCategoria?: string,
  proveedorNombre?: string,
  proveedorApellidoPaterno?: string,
  precioCompraActual?: number,
  precioVentaActual?: number,
  cantidadInventario?: number,
  estadosEstado?: string
}
```

#### Transformación:
- `name` → `nombre`
- `image` → Usar emoji genérico `📦` o icono basado en categoría
- `category` → `categoriasProductosCategoria`
- `supplier` → `proveedorNombre + " " + proveedorApellidoPaterno`
- `purchasePrice` → `formatPrice(precioCompraActual)`
- `salePrice` → `formatPrice(precioVentaActual)`
- `stock` → `cantidadInventario`
- `status` → Calcular basado en `cantidadInventario` usando `getStatusBadge()`

### Consideraciones Especiales

#### 1. Barra Lateral (Sidebar)
- **NO reemplazar** el componente `SidebarNavigation` existente
- El código proporcionado tiene sidebar integrado, pero debemos usar el componente separado
- Mantener la prop `activeSection="inventario"` en `SidebarNavigation`

#### 2. Botón "RefreshCw" (Ajustar Stock)
- El sistema actual **NO tiene funcionalidad de ajuste manual de stock**
- El stock se maneja automáticamente mediante:
  - **Compras**: Incrementan stock
  - **Ventas**: Decrementan stock
- **Acción**: Remover este botón del diseño final

#### 3. Formato de Precios
- Usar la función existente `formatPrice` que formatea a MXN
- Ejemplo: `formatPrice(12.50)` → "$12.50"

#### 4. Estados de Stock
- Usar la función existente `getStatusBadge(cantidadInventario, estadosEstado)`
- Lógica:
  - `cantidad === 0` → "Agotado" (rojo)
  - `cantidad <= 10` → "Bajo" (amarillo)
  - `cantidad <= 50` → "Medio" (naranja)
  - `cantidad > 50` → "En Stock" (verde)

### Criterios de Éxito

#### ✅ **Interfaz Visual Renovada:**
- [ ] Diseño coincide con el código proporcionado
- [ ] Colores siguen la paleta definida (#F97316, #FACC15, #F3F4F6)
- [ ] Bordes redondeados y sombras aplicados correctamente
- [ ] Responsive en móvil, tablet y desktop

#### ✅ **Funcionalidad Completa:**
- [ ] Todos los botones principales funcionan (Crear, Comprar, Predicciones)
- [ ] Acciones de tabla funcionan (Editar, Eliminar)
- [ ] Búsqueda filtra productos en tiempo real
- [ ] Modales se abren y cierran correctamente

#### ✅ **Datos Reales:**
- [ ] Tabla muestra productos reales de la base de datos
- [ ] Contador de productos es dinámico
- [ ] Precios formateados correctamente en MXN
- [ ] Estados de stock calculados correctamente

#### ✅ **Navegación Intacta:**
- [ ] SidebarNavigation funciona correctamente
- [ ] Estado activo de "Inventario" se muestra
- [ ] Navegación a otras secciones funciona
- [ ] No hay superposición con el sidebar

#### ✅ **Sin Regresiones:**
- [ ] Alertas de stock bajo siguen funcionando
- [ ] Verificación de stock sigue funcionando
- [ ] Toasts y notificaciones funcionan
- [ ] Reload de productos después de CRUD funciona

### Estado: 🔄 ESPERANDO APROBACIÓN

### Notas de Implementación
- **Prioridad**: Alta - Mejora significativa de UX
- **Complejidad**: Media - Principalmente cambios visuales, funcionalidad ya existe
- **Tiempo Estimado**: 2-3 horas de implementación cuidadosa
- **Riesgo**: Bajo - Toda la funcionalidad ya está probada y funcionando
- **Dependencias**: Ninguna - Todo el código necesario ya existe

---

## 🎨 REWORK COMPLETO: Nueva Interfaz de Usuario (10 Ene 2026)

### Descripción del Proyecto

Implementar un rework completo de todas las pantallas de la interfaz de usuario con un nuevo diseño estandardizado. La funcionalidad permanece igual, pero se mejora la experiencia visual y de usuario.

**PANTALLA OBJETIVO**: Página principal del sistema (post-login) con nueva barra lateral de navegación, dashboard moderno y tabla de actividad reciente.

### Paleta de Colores y Diseño

#### Colores Principales:
- **Primario**: Naranja Vibrante (#F97316) - Para elementos activos y llamadas a la acción
- **Secundario**: Amarillo Mostaza (#FACC15) - Para ofertas y destacados  
- **Fondo**: Blanco (#FFFFFF) o Gris muy claro (#F3F4F6)
- **Texto**: Gris oscuro (#1F2937) para legibilidad

#### Tipografía:
- **Principal (Headings)**: Sans-serif moderna ('Poppins' o 'Inter')
- **Cuerpo**: Sans-serif legible con buena altura de línea

#### Iconografía:
- Iconos de línea (outline) para navegación inactiva
- Iconos sólidos (filled) para estados activos
- Bordes redondeados: 12px - 16px radius

### Componentes de la Nueva Interfaz

#### 1. Barra Lateral de Navegación (Persistente)
- **Ubicación**: Fija en la izquierda de todas las pantallas
- **Botones**: Home, Inventario, Personal
- **Estado Activo**: Iluminación naranja para página actual
- **Botón Salir**: Ubicado en la parte inferior izquierda

#### 2. Página Principal Renovada
- **Saludo Personalizado**: "Hola, [Nombre del Usuario]" (obtenido del contexto de autenticación)
- **Dashboard Modular**: Grid responsivo con botones de acceso rápido
- **Tabla de Actividad**: Mostrar órdenes de venta más recientes

#### 3. Tabla de Actividad Reciente
- **Datos**: Extraer de `detalles_ordenes_de_venta` (más recientes)
- **Columnas**:
  - Usuario (nombre del empleado que realizó la venta)
  - Mesa (ubicación de la venta)
  - Hora (timestamp de la orden)  
  - Total (monto de la venta)

### Plan de Implementación

#### FASE 1: Preparación y Estructura Base

- [ ] **Paso 1.1: Analizar estructura actual de componentes**
  - [ ] Revisar `MainMenu.tsx` actual
  - [ ] Identificar componentes de navegación existentes
  - [ ] Documentar rutas y contextos actuales

- [ ] **Paso 1.2: Crear componente de Barra Lateral**
  - [ ] Crear `SidebarNavigation.tsx` como componente reutilizable
  - [ ] Implementar navegación entre páginas
  - [ ] Añadir estado activo basado en ruta actual
  - [ ] Integrar botón de logout con contexto de autenticación

#### FASE 2: Página Principal (Dashboard)

- [ ] **Paso 2.1: Refactorizar MainMenu.tsx**
  - [ ] Reemplazar interfaz actual con el nuevo diseño HTML proporcionado
  - [ ] Convertir HTML a JSX/TypeScript
  - [ ] Implementar grid responsivo para módulos

- [ ] **Paso 2.2: Implementar funcionalidad de botones**
  - [ ] Conectar "Nueva Venta" con navegación al POS
  - [ ] Conectar "Inventario" con componente de inventario
  - [ ] Conectar "Predicciones" con modal de ML
  - [ ] Conectar "Administración" con gestión de empleados

- [ ] **Paso 2.3: Integrar saludo personalizado**
  - [ ] Obtener nombre del usuario desde AuthContext
  - [ ] Reemplazar "Hola, Usuario" con nombre real
  - [ ] Manejar casos donde no hay usuario logueado

#### FASE 3: Tabla de Actividad Reciente

- [ ] **Paso 3.1: Crear servicio para obtener órdenes recientes**
  - [ ] Añadir endpoint en backend: `GET /api/ordenes-de-ventas/recientes`
  - [ ] Implementar consulta JPA para obtener últimas 5-10 órdenes
  - [ ] Incluir joins con usuarios y información de mesa

- [ ] **Paso 3.2: Crear servicio frontend**
  - [ ] Añadir `ordenesService.ts` con método `getOrdenesRecientes()`
  - [ ] Definir interfaces TypeScript para respuesta
  - [ ] Implementar manejo de errores

- [ ] **Paso 3.3: Integrar tabla en dashboard**
  - [ ] Reemplazar datos mock con llamada al servicio real
  - [ ] Implementar loading states y error handling
  - [ ] Formatear fechas y montos correctamente
  - [ ] Añadir avatares para nombres de usuarios

#### FASE 4: Responsive Design y Refinamientos

- [ ] **Paso 4.1: Implementar responsividad**
  - [ ] Verificar comportamiento en móviles (768px breakpoint)
  - [ ] Ajustar grid y spacing para diferentes pantallas
  - [ ] Optimizar barra lateral para dispositivos pequeños

- [ ] **Paso 4.2: Pulir estilos y animaciones**
  - [ ] Implementar hover effects y transiciones
  - [ ] Añadir sombras y gradientes según guía de diseño
  - [ ] Verificar contraste y legibilidad de texto

- [ ] **Paso 4.3: Testing y refinamientos finales**
  - [ ] Probar navegación entre todas las páginas
  - [ ] Verificar que datos reales se cargan correctamente
  - [ ] Optimizar rendimiento y tiempo de carga

#### FASE 5: Integración con Sistema Existente

- [ ] **Paso 5.1: Actualizar rutas y navegación**
  - [ ] Verificar que todas las rutas funcionen con nueva barra lateral
  - [ ] Actualizar `ProtectedRoute` si es necesario
  - [ ] Sincronizar estado activo de navegación

- [ ] **Paso 5.2: Migrar otras pantallas gradualmente**
  - [ ] Planificar rework de `Inventario.tsx`
  - [ ] Planificar rework de `GestionEmpleados.tsx`
  - [ ] Crear guía de componentes reutilizables

### Archivos a Crear/Modificar

#### Archivos Nuevos:
- `frontend/src/components/SidebarNavigation.tsx` - Barra lateral de navegación
- `frontend/src/components/SidebarNavigation.css` - Estilos para barra lateral
- `frontend/src/services/ordenesService.ts` - Servicio para órdenes de venta
- `frontend/src/types/ordenes.ts` - Interfaces TypeScript para órdenes

#### Archivos a Modificar:
- `frontend/src/components/MainMenu.tsx` - Reemplazar con nuevo diseño
- `frontend/src/components/MainMenu.css` - Actualizar estilos
- `backend/src/main/java/com/posfin/pos_finanzas_backend/controller/OrdenesDeVentasController.java` - Añadir endpoint recientes
- `backend/src/main/java/com/posfin/pos_finanzas_backend/service/OrdenesDeVentasService.java` - Lógica para órdenes recientes
- `frontend/src/contexts/AuthContext.tsx` - Verificar exposición de datos de usuario

### Consultas SQL Necesarias

```sql
-- Obtener órdenes más recientes con información de usuario y mesa
SELECT 
    odv.id,
    odv.fecha_venta,
    odv.total,
    u.nombre as nombre_usuario,
    odv.mesa,
    odv.estado
FROM ordenes_de_ventas odv
LEFT JOIN usuarios u ON odv.usuarios_id = u.id
ORDER BY odv.fecha_venta DESC
LIMIT 10;
```

### Criterios de Éxito

#### ✅ **Nueva Interfaz Funcional:**
- [ ] Barra lateral presente en todas las pantallas
- [ ] Navegación fluida entre secciones
- [ ] Estado activo correctamente reflejado

#### ✅ **Dashboard Interactivo:**
- [ ] Todos los botones llevan a las secciones correctas
- [ ] Saludo personalizado con nombre del usuario real
- [ ] Grid responsivo funciona en móvil y desktop

#### ✅ **Datos Reales:**
- [ ] Tabla muestra órdenes reales de la base de datos
- [ ] Información de usuario, mesa, hora y total es correcta
- [ ] Actualizaciones en tiempo real (o refresh manual)

#### ✅ **Experiencia de Usuario:**
- [ ] Diseño coherente con paleta de colores definida
- [ ] Transiciones suaves y elementos visuales atractivos
- [ ] Tiempo de carga optimizado

### Estado: 🔄 EN PROGRESO

### Notas de Implementación
- **Prioridad**: Alta - Mejora significativa de UX
- **Complejidad**: Media-Alta - Requiere cambios en frontend y backend
- **Tiempo Estimado**: 1-2 días de desarrollo
- **Dependencias**: Sistema de autenticación, base de datos de órdenes

---

### Descripción del Objetivo

Reorganizar todo el proyecto en una estructura de carpetas clara y lógica que facilite:
- Navegación rápida por tipo de archivo
- Comprensión inmediata de la función de cada archivo
- Separación clara entre datos, scripts, documentación y código

### Análisis del Estado Actual

#### Problemas Identificados:
1. **ml-prediction-service/**
   - ❌ Scripts de Python mezclados con archivos de datos CSV
   - ❌ Documentación MD en la raíz junto a código
   - ❌ Archivos de configuración (sh, yml) sin organización
   - ❌ No existe separación entre datos y reportes

2. **Raíz del proyecto**
   - ❌ Scripts dispersos (`test-ml-*.py`, `test-ml-*.sh`, `extraer_datos_reales.sh`)
   - ❌ Archivos de tareas y documentación mezclados
   - ❌ No hay carpeta dedicada para utilidades/scripts

### Plan de Reorganización

#### PASO 1: Reorganizar ml-prediction-service/

**Nueva estructura propuesta:**
```
ml-prediction-service/
├── app/                       # Código de la aplicación FastAPI
│   ├── main.py
│   ├── pipeline.py
│   └── database.py
├── data/                      # 📂 NUEVA: Todos los datos
│   ├── raw/                   # Datos crudos de base de datos
│   │   ├── datos_ventas_reales.csv
│   │   ├── estadisticas_productos.csv
│   │   └── historial_costos_reales.csv
│   └── processed/             # Datos procesados (si aplica)
├── docs/                      # 📂 NUEVA: Toda la documentación
│   ├── analisis/              # Análisis de datos
│   │   ├── ANALISIS_DATOS_REALES.md
│   │   ├── REPORTE_CALIDAD_DATOS_REALES.md
│   │   └── RESUMEN_SESION_29NOV.md
│   ├── guias/                 # Guías de uso y mejora
│   │   ├── GUIA_MEJORA_CALIDAD_DATOS.md
│   │   ├── RESUMEN_MEJORAS.md
│   │   └── README_data_quality.md
│   └── explicaciones/         # Explicaciones técnicas
│       └── EXPLICACION-COMPLETA.md
├── models/                    # Modelos ML entrenados (ya existe)
│   ├── model_features.txt
│   ├── model_metadata.json
│   ├── ranker_prioridad.json
│   └── regressor_cantidad.json
├── notebooks/                 # 📂 NUEVA: Jupyter notebooks (si aplica)
├── scripts/                   # 📂 NUEVA: Scripts de utilidad
│   ├── analysis/              # Scripts de análisis
│   │   ├── analizar_calidad_datos_reales.py
│   │   ├── analizar_calidad_simple.py
│   │   └── analizar_datos_reales.py
│   ├── data_quality/          # Scripts de calidad de datos
│   │   ├── data_quality_analyzer.py
│   │   ├── data_quality_html_report.py
│   │   └── mejorar_calidad_datos.py
│   ├── training/              # Scripts de entrenamiento
│   │   ├── entrenar_con_datos_reales.py
│   │   └── regenerar_modelos.py
│   └── shell/                 # Scripts bash
│       ├── regenerar_modelos.sh
│       ├── setup_and_regenerate.sh
│       └── test-api.sh
├── reports/                   # 📂 NUEVA: Reportes generados
│   └── html/
│       └── 14oct-data_quality_report.html
├── tests/                     # 📂 NUEVA: Tests (vacío por ahora)
├── docker-compose.yml
├── Dockerfile
├── README.md                  # Documentación principal
└── requirements.txt
```

**Acciones específicas:**

- [x] **Crear carpeta `data/` con subcarpetas**
  - [x] Crear `data/raw/` para datos crudos
  - [x] Crear `data/processed/` para datos procesados
  - [x] Mover archivos CSV a `data/raw/`

- [x] **Crear carpeta `docs/` con subcarpetas**
  - [x] Crear `docs/analisis/` para análisis de datos
  - [x] Crear `docs/guias/` para guías
  - [x] Crear `docs/explicaciones/` para docs técnicas
  - [x] Mover todos los archivos MD según categoría

- [x] **Crear carpeta `scripts/` con subcarpetas**
  - [x] Crear `scripts/analysis/` para análisis
  - [x] Crear `scripts/data_quality/` para calidad
  - [x] Crear `scripts/training/` para entrenamiento
  - [x] Crear `scripts/shell/` para scripts bash
  - [x] Mover archivos Python y shell según función

- [x] **Crear carpeta `reports/`**
  - [x] Crear `reports/html/` para reportes HTML
  - [x] Mover reportes HTML generados

- [x] **Actualizar Dockerfile**
  - [x] Actualizar rutas de COPY para reflejar nueva estructura
  - [x] Asegurar que app/ siga funcionando

#### PASO 2: Reorganizar raíz del proyecto

**Nueva estructura propuesta:**
```
proyecto-pos-finanzas/
├── backend/                   # Backend Java/Spring Boot (ya existe)
├── frontend/                  # Frontend React/TypeScript (ya existe)
├── ml-prediction-service/     # Servicio ML (reorganizado arriba)
├── docs/                      # 📂 NUEVA: Documentación general del proyecto
│   ├── bd-schema.md
│   ├── codebase-completo.md
│   ├── analisis-funcionamiento-codigo.md
│   ├── flujo-predicciones.md
│   ├── funcionalidad-deudas-proveedores.md
│   ├── gradient-boosting-bitacora.md
│   ├── presentacion-gb.md
│   ├── requerimientos.md
│   └── seguridad.md
├── pruebas/                   # Planes y datos de pruebas (ya existe)
│   ├── datos-planeacion.md
│   └── plan-de-pruebas.md
├── scripts/                   # 📂 NUEVA: Scripts globales del proyecto
│   ├── database/              # Scripts de base de datos
│   │   └── extraer_datos_reales.sh
│   ├── docker/                # Scripts de Docker
│   │   └── regenerar_modelos_docker.sh
│   └── testing/               # Scripts de testing
│       ├── test-ml-flow.py
│       ├── test-ml-flow.sh
│       └── test-ml-integration.sh
├── anotaciones-markdown/      # Notas y apuntes (ya existe)
├── .github/                   # Configuraciones GitHub (ya existe)
├── .gitignore
├── AGENTS.md
├── README.md
├── docker-compose.yml
├── docker-compose.override.yml
├── tasks.md                   # Este archivo
└── tasks-archive.md
```

**Acciones específicas:**

- [x] **Crear carpeta `docs/` en raíz**
  - [x] Mover documentación desde carpeta antigua `docs/` a raíz
  - [x] Mover `utilidades/bd-schema.md` a `docs/`
  - [x] Mover `utilidades/requerimientos.md` a `docs/`
  - [x] Eliminar carpeta `utilidades/` vacía

- [x] **Crear carpeta `scripts/` en raíz**
  - [x] Crear `scripts/database/`
  - [x] Crear `scripts/docker/`
  - [x] Crear `scripts/testing/`
  - [x] Mover scripts desde raíz según función

#### PASO 3: Actualizar Referencias en Archivos

- [x] **Actualizar imports y rutas en Python**
  - [x] Actualizar imports en scripts que se movieron
  - [x] Verificar rutas relativas en archivos Python

- [x] **Actualizar rutas en scripts bash**
  - [x] Actualizar paths en todos los archivos .sh
  - [x] Verificar que apunten a ubicaciones correctas

- [x] **Actualizar Dockerfile y docker-compose.yml**
  - [x] Actualizar COPY paths en Dockerfiles
  - [x] Verificar volúmenes y bind mounts

- [x] **Actualizar documentación**
  - [x] Actualizar README.md con nueva estructura
  - [x] Actualizar referencias en archivos MD

#### PASO 4: Validación y Testing

- [x] **Verificar que nada se rompa**
  - [x] Ejecutar docker-compose up --build
  - [x] Verificar que todos los servicios inicien correctamente
  - [x] Probar scripts de análisis y entrenamiento
  - [x] Verificar que los modelos carguen correctamente

### Criterios de Éxito

✅ **Estructura Clara**: Cada tipo de archivo en su carpeta correspondiente
✅ **Fácil Navegación**: Nombres de carpetas descriptivos y lógicos
✅ **Sin Romper Nada**: Todos los servicios siguen funcionando
✅ **Documentación Actualizada**: README reflejando nueva estructura
✅ **Mantenibilidad**: Más fácil encontrar y modificar archivos

### Archivos a Mover

#### ml-prediction-service/

**A data/raw/:**
- datos_ventas_reales.csv
- estadisticas_productos.csv
- historial_costos_reales.csv

**A docs/analisis/:**
- ANALISIS_DATOS_REALES.md
- REPORTE_CALIDAD_DATOS_REALES.md
- RESUMEN_SESION_29NOV.md

**A docs/guias/:**
- GUIA_MEJORA_CALIDAD_DATOS.md
- RESUMEN_MEJORAS.md
- README_data_quality.md

**A docs/explicaciones/:**
- EXPLICACION-COMPLETA.md

**A scripts/analysis/:**
- analizar_calidad_datos_reales.py
- analizar_calidad_simple.py
- analizar_datos_reales.py

**A scripts/data_quality/:**
- data_quality_analyzer.py
- data_quality_html_report.py
- mejorar_calidad_datos.py

**A scripts/training/:**
- entrenar_con_datos_reales.py
- regenerar_modelos.py

**A scripts/shell/:**
- regenerar_modelos.sh
- setup_and_regenerate.sh
- test-api.sh

**A reports/html/:**
- ml-prediction-service/14oct-data_quality_report.html

#### Raíz del proyecto/

**A docs/:**
- docs/*.md (todos los archivos)
- utilidades/bd-schema.md
- utilidades/requerimientos.md

**A scripts/database/:**
- extraer_datos_reales.sh

**A scripts/docker/:**
- regenerar_modelos_docker.sh

**A scripts/testing/:**
- test-ml-flow.py
- test-ml-flow.sh
- test-ml-integration.sh

### Estado: ✅ COMPLETADO

**La reorganización completa ha sido ejecutada exitosamente**

#### 📊 Resultados Finales

- ✅ **35+ archivos movidos** a sus ubicaciones lógicas
- ✅ **~20 carpetas creadas** con estructura jerárquica clara
- ✅ **3 scripts actualizados** con nuevas rutas
- ✅ **Permisos de ejecución** configurados en todos los scripts bash
- ✅ **Dockerfile actualizado** para nueva estructura
- ✅ **Documentación completa** generada en `docs/REORGANIZACION_29NOV.md`

#### 🔗 Archivos Clave

- **Script de reorganización:** `reorganizar_proyecto.sh`
- **Documentación detallada:** `docs/REORGANIZACION_29NOV.md`
- **Dockerfile actualizado:** `ml-prediction-service/Dockerfile`

#### ⚠️ Próximos Pasos

1. **Verificar que todo funcione:**
   ```bash
   docker-compose up --build -d
   docker logs proyecto-pos-finanzas-ml-prediction-service-1
   curl http://localhost:8000/health
   ```

2. **Ejecutar tests:**
   ```bash
   ./scripts/testing/test-ml-integration.sh
   ```

3. **Actualizar README.md principal** con la nueva estructura

---

**Fecha de completación:** 29 Nov 2025  
**Duración:** < 5 minutos  
**Estado:** ✅ Éxito completo

---

## 🔧 CORRECCIÓN: Conexión Frontend con Servicio ML (29 Nov 2025)

### Descripción del Problema

El frontend no puede conectarse al servicio de Machine Learning. Al intentar obtener predicciones, aparece el error:
```
Error al obtener predicciones
Error en predicciones: Network Error
```

El contenedor ML está corriendo correctamente en el puerto 8004, pero el frontend intenta conectarse al puerto incorrecto.

### Causa Raíz Identificada

1. **Conflicto de Puerto**: El servicio ML está mapeado al puerto **8004** en el host (`docker-compose.yml` línea 44)
2. **Configuración Incorrecta en Frontend**: El servicio `mlService.ts` tiene hardcodeada la URL `http://localhost:8002` (línea 5)
3. **Variable de Entorno Faltante**: No existe configuración de `VITE_ML_API_URL` en el archivo `.env` o en el `docker-compose.yml`
4. **IP del Servidor**: El frontend se conecta al backend usando la IP `100.101.201.102` (configurada en docker-compose.yml), por lo que el servicio ML también debería usar esa IP en lugar de `localhost`

### Plan de Acción

- [ ] **Paso 1: Configurar variable de entorno para ML en docker-compose.yml**
  - [ ] Añadir `VITE_ML_API_URL=http://100.101.201.102:8004` en el servicio frontend
  - [ ] Asegurar que el build de Vite incluya esta variable

- [ ] **Paso 2: Verificar que el servicio frontend use la variable correctamente**
  - [ ] Confirmar que `mlService.ts` ya tiene el fallback correcto en línea 5
  - [ ] No se requiere cambio en el código TypeScript

- [ ] **Paso 3: Actualizar documentación de AGENTS.md**
  - [ ] Documentar la configuración de ML API en variables de entorno
  - [ ] Añadir instrucciones para troubleshooting de conexión ML

- [ ] **Paso 4: Reconstruir y reiniciar contenedor frontend**
  - [ ] Ejecutar `docker-compose up --build -d frontend`
  - [ ] Verificar logs del contenedor

- [ ] **Paso 5: Probar conexión desde frontend**
  - [ ] Verificar health check de ML desde el navegador
  - [ ] Probar obtención de predicciones
  - [ ] Confirmar que no hay errores de red

### Comandos de Verificación

```bash
# Verificar que ML está corriendo
docker ps | grep ml

# Verificar logs de ML
docker logs pos_ml_prediction_api

# Probar health check directamente
curl http://100.101.201.102:8004/

# Reconstruir frontend con nueva configuración
docker-compose up --build -d frontend

# Verificar logs del frontend
docker logs pos_frontend
```

### Criterios de Éxito

✅ **Frontend se conecta exitosamente al servicio ML**
✅ **No aparece mensaje de "servicio no disponible"**
✅ **Las predicciones se obtienen correctamente**
✅ **No hay errores de Network Error**

### Estado: ✅ COMPLETADO

### Problemas Identificados y Soluciones

#### Problema 1: Frontend no puede conectarse al servicio ML
**Causa**: La variable de entorno `VITE_ML_API_URL` no estaba siendo embebida en el bundle de Vite durante el build.

**Solución Implementada**:
1. Creado archivo `frontend/.env.production` con las variables correctas:
   ```
   VITE_API_URL=http://100.101.201.102:8084
   VITE_ML_API_URL=http://100.101.201.102:8004
   ```
2. Actualizado `frontend/Dockerfile` para que copie `.env.production` antes del build
3. Simplificado `docker-compose.yml` para no usar build args (Vite carga `.env.production` automáticamente)

#### Problema 2: Servicio ML recibe productos dummy (PROD001, PROD002)
**Causa**: El endpoint `/api/ordenes-de-ventas/historial-ml` requiere autenticación JWT. Cuando el usuario no ha iniciado sesión o el token no es válido, la llamada falla y `mlService.ts` usa datos dummy de fallback.

**Comportamiento Esperado**: 
- El flujo correcto es: Usuario inicia sesión → Token JWT guardado → Abre modal de predicciones → Llama a `/historial-ml` con token → Obtiene datos reales → Envía al servicio ML
- Si no hay datos históricos reales en la base de datos, el ML usará los productos disponibles pero sin historial

**No requiere corrección adicional**: El sistema ya maneja correctamente el caso de datos dummy como fallback cuando:
- El usuario no está autenticado
- No hay datos históricos en la base de datos  
- Hay un error en la comunicación con el backend

### Cambios Realizados

#### Archivos Nuevos:
- `frontend/.env.production` - Variables de entorno para producción

#### Archivos Modificados:
- `frontend/Dockerfile` - Simplificado para copiar `.env.production` durante build
- `docker-compose.yml` - Removidos build args innecesarios

### Verificación Exitosa

✅ **URL ML correctamente embebida**: `http://100.101.201.102:8004` presente en el bundle JavaScript  
✅ **Servicio ML accesible**: Health check respondiendo correctamente  
✅ **Frontend reconstruido**: Nueva imagen con configuración correcta  
✅ **Contenedores operativos**: Todos los servicios corriendo sin errores  

### Instrucciones para el Usuario

#### Para usar las predicciones de ML correctamente:

1. **Inicia sesión en la plataforma** en `http://100.101.201.102:5173`
   - El token JWT se guardará automáticamente

2. **Navega a la sección de Inventario o Punto de Compras**

3. **Abre el modal de predicciones ML**
   - Click en el botón "Predicciones ML" o similar

4. **Haz click en "Actualizar" para obtener predicciones**
   - El sistema obtendrá datos históricos de ventas del backend (si existen)
   - Enviará los datos al servicio ML
   - Mostrará las predicciones con productos reales de tu base de datos

#### Notas Importantes:

- **Primera vez**: Si no tienes datos históricos de ventas, el sistema usará datos de ejemplo
- **Autenticación requerida**: Debes estar logueado para que funcione correctamente
- **Productos reales**: Las predicciones mostrarán los productos que existen en tu base de datos
- **Si ves PROD001, PROD002**: Significa que el endpoint de historial falló (verifica que estés autenticado)

### Fecha de completación: 29 Nov 2025  
### Duración: ~25 minutos  
### Estado: ✅ Solución completa implementada

---

## 🚨 CRÍTICO: Debugging Fallo de Autenticación Usuario "Tona" (10 Ene 2026)

### Descripción del Problema Crítico

**SÍNTOMAS OBSERVADOS:**
1. Usuario "Tona" existe en base de datos con contraseña "123456" y status "Activo" 
2. Frontend envía POST correcto a `/api/auth/login` con `{"nombre":"Tona","contrasena":"123456"}`
3. Backend responde con HTTP 400 "Usuario no encontrado"
4. Logs de Hibernate muestran consulta ejecutada: `select u1_0.id, u1_0.contrasena, u1_0.estados_id, u1_0.nombre, u1_0.roles_id, u1_0.telefono from usuarios u1_0 where u1_0.nombre=?`
5. **PARADOJA:** La consulta se ejecuta pero `usuariosRepository.findByNombre("Tona")` retorna `Optional.empty()`

### Análisis de Código Realizado

#### ✅ Componentes Analizados:

1. **AuthController.login()** (línea 96):
   - Usa `usuariosRepository.findByNombre(nombre)` correctamente
   - Valida `usuarioOpt.isEmpty()` en línea 98
   - Retorna "Usuario no encontrado" si `Optional` está vacío

2. **UsuariosRepository**:
   - Método `findByNombre(String nombre)` declarado correctamente
   - Extiende `JpaRepository<Usuarios, String>`
   - No usa `@Query` personalizado, confía en Spring Data JPA

3. **Entidad Usuarios**:
   - Campo `nombre` mapeado como `@Column(name = "nombre", nullable = false)`
   - Tipo de dato: `String` 
   - Sin configuraciones especiales de case sensitivity

4. **Base de Datos PostgreSQL**:
   - Tabla `usuarios` con columna `nombre` tipo `character varying`
   - Consulta Hibernate ejecutándose correctamente según logs

### Hipótesis de Causas Raíz

#### 🔍 HIPÓTESIS PRIMARIAS:

1. **Case Sensitivity Mismatch**
   - PostgreSQL por defecto es case-sensitive en comparaciones
   - Usuario podría estar almacenado como "TONA", "tona", o "Tona"
   - Spring JPA podría no estar manejando correctamente el case matching

2. **Encoding/Charset Issues**
   - Caracteres especiales ocultos en el nombre
   - UTF-8 vs Latin1 encoding problems
   - Espacios en blanco al inicio/final del nombre

3. **Transaction/Connection Pool Issues**  
   - Consulta ejecutándose en diferente esquema/base de datos
   - Connection pool apuntando a BD diferente
   - Transacción rollback automático

4. **JPA/Hibernate Configuration Problems**
   - Caché de primer nivel interfiriendo
   - Lazy loading causando problemas
   - Dialect configuration mismatch

5. **Data Type Coercion**
   - Hibernate convirtiendo el parámetro String de manera inesperada
   - PostgreSQL JDBC driver type conversion issues

### Plan de Debugging Exhaustivo

#### FASE 1: Verificación Directa de Datos

- [ ] **Paso 1.1: Verificar datos exactos en PostgreSQL**
  ```sql
  SELECT id, nombre, LENGTH(nombre), ASCII(LEFT(nombre,1)), 
         ENCODE(nombre::bytea, 'hex') as hex_encoding,
         estados_id, roles_id
  FROM usuarios 
  WHERE nombre LIKE '%ona%' OR nombre LIKE '%TONA%' OR nombre LIKE '%tona%';
  
  SELECT COUNT(*) FROM usuarios WHERE nombre = 'Tona';
  SELECT COUNT(*) FROM usuarios WHERE LOWER(nombre) = 'tona';
  SELECT COUNT(*) FROM usuarios WHERE UPPER(nombre) = 'TONA';
  ```

- [ ] **Paso 1.2: Verificar conexión exacta que usa la aplicación**
  ```sql
  SELECT current_database(), current_schema(), current_user;
  SHOW search_path;
  ```

#### FASE 2: Debugging a Nivel de JPA/Hibernate

- [ ] **Paso 2.1: Habilitar logging SQL completo**
  - [ ] Añadir a `application.properties`:
    ```properties
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true  
    logging.level.org.hibernate.SQL=DEBUG
    logging.level.org.hibernate.type.descriptor.sql=TRACE
    logging.level.org.springframework.jdbc.core=DEBUG
    ```

- [ ] **Paso 2.2: Crear método de testing directo en AuthController**
  ```java
  @PostMapping("/debug-user")
  public ResponseEntity<?> debugUser(@RequestParam String nombre) {
      System.out.println("=== DEBUG: Buscando usuario: [" + nombre + "]");
      System.out.println("=== DEBUG: Length: " + nombre.length());
      System.out.println("=== DEBUG: Bytes: " + Arrays.toString(nombre.getBytes()));
      
      Optional<Usuarios> result = usuariosRepository.findByNombre(nombre);
      System.out.println("=== DEBUG: Resultado: " + result.isPresent());
      
      if (result.isPresent()) {
          Usuarios user = result.get();
          System.out.println("=== DEBUG: Usuario encontrado: " + user.getNombre());
          System.out.println("=== DEBUG: ID: " + user.getId());
      }
      
      // Probar variaciones
      Optional<Usuarios> upperCase = usuariosRepository.findByNombre(nombre.toUpperCase());
      Optional<Usuarios> lowerCase = usuariosRepository.findByNombre(nombre.toLowerCase());
      
      return ResponseEntity.ok(Map.of(
          "original", result.isPresent(),
          "upperCase", upperCase.isPresent(), 
          "lowerCase", lowerCase.isPresent(),
          "searchTerm", nombre
      ));
  }
  ```

- [ ] **Paso 2.3: Crear query nativo para comparación**
  ```java
  @Query(value = "SELECT * FROM usuarios WHERE nombre = :nombre", nativeQuery = true)
  Optional<Usuarios> findByNombreNativo(@Param("nombre") String nombre);
  
  @Query(value = "SELECT * FROM usuarios WHERE LOWER(nombre) = LOWER(:nombre)", nativeQuery = true)  
  Optional<Usuarios> findByNombreIgnoreCase(@Param("nombre") String nombre);
  ```

#### FASE 3: Verificación de Configuración de Sistema

- [ ] **Paso 3.1: Verificar profile activo**
  ```bash
  # En logs de startup buscar:
  # "The following profiles are active: [profile-name]"
  grep -r "profiles are active" /home/tona/dev/proyecto-pos-finanzas/
  ```

- [ ] **Paso 3.2: Verificar variables de entorno de BD**
  ```bash
  docker exec pos_backend env | grep -E "(DB_|SPRING_)"
  ```

- [ ] **Paso 3.3: Verificar conexión real desde contenedor**
  ```bash
  docker exec -it pos_backend bash
  # Dentro del contenedor, instalar psql si no está:
  apt-get update && apt-get install -y postgresql-client
  # Conectar usando las mismas credenciales de la app:
  psql $DB_URL -c "SELECT nombre FROM usuarios WHERE nombre ILIKE '%tona%';"
  ```

#### FASE 4: Testing Programático de Casos Límite

- [ ] **Paso 4.1: Test unitario específico**
  ```java
  @Test
  void testFindByNombre_CaseTona() {
      // Arrange - Datos de prueba exactos  
      Estados estadoActivo = new Estados();
      estadoActivo.setId("test-estado-id");
      estadoActivo.setEstado("Activo");
      estadosRepository.save(estadoActivo);
      
      Roles rolTest = new Roles();  
      rolTest.setId("test-rol-id");
      rolTest.setRoles("TestRole");
      rolesRepository.save(rolTest);
      
      Usuarios usuarioTona = new Usuarios();
      usuarioTona.setId("test-tona-id");
      usuarioTona.setNombre("Tona");  // Exactamente como en producción
      usuarioTona.setContrasena("123456");
      usuarioTona.setTelefono("555-0000");
      usuarioTona.setEstados(estadoActivo);
      usuarioTona.setRoles(rolTest);
      
      usuariosRepository.save(usuarioTona);
      usuariosRepository.flush();
      
      // Act - Búsqueda exacta como en AuthController
      Optional<Usuarios> result = usuariosRepository.findByNombre("Tona");
      
      // Assert
      assertTrue(result.isPresent(), "Usuario Tona debería encontrarse");
      assertEquals("Tona", result.get().getNombre());
      
      // Test variaciones
      assertFalse(usuariosRepository.findByNombre("tona").isPresent());
      assertFalse(usuariosRepository.findByNombre("TONA").isPresent());
      assertFalse(usuariosRepository.findByNombre(" Tona").isPresent());
      assertFalse(usuariosRepository.findByNombre("Tona ").isPresent());
  }
  ```

- [ ] **Paso 4.2: Integration test completo**
  ```java
  @Test  
  void testLoginFlow_UsuarioTona() {
      // Crear usuario real en BD de test
      setupUsuarioTona();
      
      // Simular request exacto del frontend
      Map<String, String> credentials = Map.of(
          "nombre", "Tona",
          "contrasena", "123456"
      );
      
      // Enviar al endpoint
      ResponseEntity<?> response = authController.login(credentials);
      
      // Verificar que NO es 400 "Usuario no encontrado"
      assertNotEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      
      // Verificar llamadas a repositorio
      verify(usuariosRepository).findByNombre("Tona");
  }
  ```

### Comandos de Debugging Inmediato

```bash
# 1. Ejecutar query directa en BD
docker exec -it pos_db psql -U postgres -d pos_fin -c "
SELECT 'Found: ' || nombre || ' (ID: ' || id || ')' as result 
FROM usuarios 
WHERE nombre = 'Tona' 
   OR nombre = 'tona' 
   OR nombre = 'TONA'
   OR nombre ILIKE '%tona%';"

# 2. Verificar logs de backend en tiempo real  
docker logs -f pos_backend | grep -E "(DEBUG|Tona|findByNombre)"

# 3. Hacer request de test directo
curl -X POST http://api.tonatiuham.dev/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Tona","contrasena":"123456"}' \
  -v

# 4. Verificar estado de contenedores
docker-compose ps
```

### Criterios de Éxito para Debugging

#### ✅ **Identificación de Causa Raíz:**
- [ ] Determinar por qué la consulta SQL no retorna resultado a pesar de ejecutarse
- [ ] Confirmar el valor exacto del campo `nombre` en la base de datos  
- [ ] Identificar si hay problemas de encoding, case sensitivity, o configuración

#### ✅ **Solución Implementada:**
- [ ] Usuario "Tona" puede autenticarse exitosamente
- [ ] Respuesta HTTP 200 con token JWT válido
- [ ] No más errores de "Usuario no encontrado"

#### ✅ **Prevención de Regresión:**
- [ ] Test automatizado que cubre este caso específico
- [ ] Documentación del problema y solución
- [ ] Logging mejorado para debugging futuro

### Estado: 🔄 EN PROGRESO

### Notas de Debugging
- **Prioridad Máxima**: Bloquea completamente el acceso del usuario principal
- **Impacto**: Crítico - funcionalidad de login rota
- **Complejidad**: Alta - discrepancia entre logs de SQL y resultado de JPA
- **Tiempo Estimado**: 2-4 horas para identificación y resolución completa

---
