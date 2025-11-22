# 📊 Análisis de Datos para Predicciones ML - Sistema POS

## 🗂️ Descripción de las Columnas (Diccionario de Datos)

### **Datos Principales Extraídos de la Base de Datos:**

**fecha_orden**: Fecha en que se realizó la orden de venta (formato YYYY-MM-DD). Se obtiene de la tabla `ordenes_de_ventas.fecha_orden`.

**productos_id**: Identificador único del producto vendido (string). Corresponde al ID del producto en la tabla `productos.id`.

**cantidad_pz**: Cantidad de piezas/unidades vendidas en esa transacción (debe ser un entero positivo). Se obtiene de `detalles_ordenes_de_ventas.cantidad_pz`.

**precio_venta**: Precio unitario al que se vendió el producto (debe ser un número flotante positivo). Se obtiene de `historial_precios.precio`.

**costo_compra**: Costo estimado del producto (número flotante). Se calcula como el 70% del precio_venta ya que no existe tabla de costos reales.

### **Datos Adicionales de Contexto:**

**producto_nombre**: Nombre descriptivo del producto (string). Se obtiene de `productos.nombre`.

**categoria**: Categoría del producto (string). Se obtiene de `categorias_productos.categoria`.

### **Características Generadas por el Pipeline ML:**

**dia_de_la_semana**: Día de la semana extraído de fecha_orden (0=Lunes, 6=Domingo).

**es_fin_de_semana**: Indicador binario si la venta ocurrió en fin de semana (0/1).

**mes**: Mes extraído de fecha_orden (1-12).

**trimestre**: Trimestre del año (1-4).

**estacion_del_año**: Estación calculada del mes (primavera, verano, otoño, invierno).

**es_feriado**: Indicador binario si la fecha es día festivo en México (0/1).

**dias_hasta_feriado**: Número de días hasta el próximo feriado (0-14).

**clima_temperatura_max**: Temperatura máxima simulada para esa fecha (grados Celsius).

**clima_humedad**: Humedad relativa simulada (porcentaje).

**clima_precipitacion**: Precipitación simulada (milímetros).

**lag_cantidad_total_1d**: Cantidad vendida de ese producto 1 día antes.

**lag_cantidad_total_7d**: Cantidad vendida de ese producto 7 días antes.

**lag_cantidad_total_30d**: Cantidad vendida de ese producto 30 días antes.

**media_movil_cantidad_total_7d**: Promedio móvil de ventas de los últimos 7 días.

**media_movil_cantidad_total_30d**: Promedio móvil de ventas de los últimos 30 días.

## 🎯 Objetivo Principal del Modelo

### **Predicción Dual:**

1. **Cantidad Recomendada (Regressor)**: 
   - "Quiero predecir cuántas unidades de cada producto debería comprar para el próximo período de reabastecimiento"
   - Variable objetivo: cantidad_total (agregada por producto y fecha)

2. **Prioridad de Compra (Ranker)**:
   - "Quiero predecir qué tan urgente es reabastecer cada producto en una escala de 0-5"
   - Variable objetivo: prioridad_score (calculada basada en tendencias y patrones)

### **Casos de Uso Específicos:**

- **Optimización de inventario**: Evitar sobrestock y faltantes
- **Planificación de compras**: Determinar qué productos comprar primero
- **Gestión de capital**: Optimizar inversión en inventario
- **Predicción estacional**: Anticipar demanda por temporadas y eventos

## 📋 Datos de Muestra (5 Filas de Ejemplo)

```csv
fecha_orden,productos_id,cantidad_pz,precio_venta,costo_compra,producto_nombre,categoria
2025-10-01,PROD_001,15,45.50,31.85,Coca Cola 600ml,Bebidas
2025-10-01,PROD_002,8,25.00,17.50,Pan Bimbo Integral,Panadería
2025-10-01,PROD_003,3,120.00,84.00,Aceite Capullo 1L,Abarrotes
2025-10-02,PROD_001,12,45.50,31.85,Coca Cola 600ml,Bebidas
2025-10-02,PROD_004,5,89.90,62.93,Detergente Ariel 1kg,Limpieza
```

## 🔍 Ejemplo de Datos Procesados por Pipeline ML

```csv
fecha_orden,productos_id,cantidad_total,dia_de_la_semana,es_fin_de_semana,mes,clima_temperatura_max,lag_cantidad_total_7d,media_movil_cantidad_total_7d,producto_PROD_001,estacion_verano
2025-10-01,PROD_001,15,2,0,10,22.5,18,16.2,1,0
2025-10-01,PROD_002,8,2,0,10,22.5,12,11.8,0,0
2025-10-01,PROD_003,3,2,0,10,22.5,5,4.1,0,0
```

## 📊 Características del Dataset

### **Volumen de Datos:**
- **Registros típicos**: 500-2000 transacciones por consulta
- **Período temporal**: Últimos 90 días por defecto
- **Productos únicos**: Variable (depende del inventario del negocio)
- **Frecuencia**: Datos diarios agregados por producto

### **Calidad de Datos:**
- **Datos faltantes**: Se manejan con valores por defecto (0 para lag features)
- **Outliers**: Se preservan (pueden indicar eventos especiales)
- **Consistencia**: Validación automática de tipos de datos
- **Integridad referencial**: Garantizada por la base de datos relacional

### **Estacionalidad Esperada:**
- **Semanal**: Mayor venta en fines de semana
- **Mensual**: Picos al inicio y fin de mes (días de pago)
- **Anual**: Variaciones por temporadas (verano, navidad, etc.)
- **Eventos**: Incrementos durante feriados y festividades

## 🎮 Casos de Uso en Producción

### **Escenario 1: Reabastecimiento Semanal**
- **Input**: Ventas de últimas 8 semanas
- **Output**: Lista de productos con cantidades y prioridades
- **Decisión**: Qué comprar para la próxima semana

### **Escenario 2: Planificación Mensual**
- **Input**: Ventas de últimos 6 meses
- **Output**: Proyección de demanda por categoría
- **Decisión**: Presupuesto de compras del próximo mes

### **Escenario 3: Eventos Especiales**
- **Input**: Ventas históricas + proximidad a feriados
- **Output**: Incremento esperado por producto
- **Decisión**: Stock adicional para días festivos

## 🔧 Configuración Técnica

### **Parámetros del Modelo:**
- **Ventana temporal**: 90 días de historial
- **Features engineering**: 60+ características automatizadas
- **Modelos**: XGBoost Regressor + Ranker
- **Actualización**: Re-entrenamiento mensual recomendado

### **Métricas de Evaluación:**
- **MAE (Mean Absolute Error)**: Error promedio en unidades
- **RMSE**: Error cuadrático medio
- **R²**: Porcentaje de varianza explicada
- **Precisión de ranking**: Correctitud en prioridades