# Prompt para Gemini: Análisis del Sistema de Predicción de Inventario con XGBoost y Factores de Escala

## 📋 Contexto del Sistema

Soy dueño de un restaurante de tacos en México y he implementado un sistema de predicción de inventario basado en Machine Learning para optimizar mis compras diarias. Sin embargo, existe una **discrepancia significativa** entre las ventas registradas en mi sistema POS y las compras reales que hago, debido a que no todas las ventas se registran (alta afluencia de clientes, operación rápida).

---

## 🤖 ¿Qué es XGBoost y cómo lo usamos?

### Definición
**XGBoost (eXtreme Gradient Boosting)** es un algoritmo de Machine Learning basado en árboles de decisión que utiliza una técnica llamada "gradient boosting". 

### Características principales:
- **Ensemble Learning:** Combina múltiples árboles de decisión débiles para crear un modelo fuerte
- **Boosting secuencial:** Cada árbol nuevo aprende de los errores de los árboles anteriores
- **Regularización:** Previene el sobreajuste (overfitting) con parámetros como `max_depth`, `min_child_weight`
- **Manejo de valores faltantes:** Puede trabajar con datos incompletos

### Nuestro uso específico:
Entrenamos **DOS modelos XGBoost separados:**

1. **Modelo de Cantidad (`modelo_cantidad`):**
   - **Objetivo:** Predecir cuántas unidades se venderán de cada producto
   - **Variable objetivo:** `cantidad_pz` (cantidad en piezas)
   - **Salida:** Número continuo (ej: 5.3 tacos, que redondeamos a 10 por múltiplo de 10)

2. **Modelo de Prioridad (`modelo_prioridad`):**
   - **Objetivo:** Predecir la urgencia/importancia de compra de cada producto
   - **Variable objetivo:** `prioridad` (0=baja, 1=media, 2=alta)
   - **Salida:** Clasificación de prioridad para la gestión de compras

### Parámetros de configuración usados:
```python
params_cantidad = {
    'objective': 'reg:squarederror',  # Regresión (números continuos)
    'max_depth': 6,                   # Profundidad máxima de cada árbol
    'learning_rate': 0.1,             # Tasa de aprendizaje (qué tan rápido aprende)
    'n_estimators': 100,              # Número de árboles
    'min_child_weight': 1,            # Peso mínimo en cada hoja
    'subsample': 0.8,                 # 80% de datos por árbol (evita overfitting)
    'colsample_bytree': 0.8,          # 80% de features por árbol
    'random_state': 42                # Semilla para reproducibilidad
}
```

---

## 📊 Datos de Entrada al Modelo

### 1. Datos Históricos de la Base de Datos

Extraemos datos de ventas históricas del período: **29 de septiembre al 3 de octubre de 2025** (5 días: Lunes a Viernes, periodo de alta actividad).

**Consulta SQL ejecutada:**
```sql
SELECT 
    o.id AS orden_id,
    o.fecha_orden,
    EXTRACT(DOW FROM o.fecha_orden) AS dia_semana,
    EXTRACT(HOUR FROM o.fecha_orden) AS hora,
    dv.productos_id,
    p.nombre AS producto_nombre,
    dv.cantidad AS cantidad_pz,
    dv.precio_unitario,
    dv.subtotal,
    o.total,
    o.descuento,
    o.metodo_pago,
    o.estado
FROM ordenes_de_ventas o
JOIN detalle_ventas dv ON o.id = dv.orden_venta_id
JOIN productos p ON dv.productos_id = p.id
WHERE o.fecha_orden >= '2025-09-29' 
  AND o.fecha_orden <= '2025-10-03'
  AND o.estado != 'cancelada'
ORDER BY o.fecha_orden;
```

**Datos extraídos:**
- **Total de registros:** 827 órdenes
- **Muestras entrenamiento:** 567 (70%)
- **Muestras prueba:** 142 (30%)
- **Features generados:** 33 características

**Features (variables) creadas para el modelo:**
- `dia_semana`: 0=Domingo, 1=Lunes, 2=Martes, ..., 6=Sábado
- `hora`: Hora del día (0-23)
- `es_fin_de_semana`: 1 si es sábado/domingo, 0 si no
- `es_hora_pico`: 1 si está entre 12-15h o 19-22h, 0 si no
- `productos_id`: ID del producto
- `precio_unitario`: Precio por unidad
- `total_orden`: Total de la orden
- Variables codificadas por producto (one-hot encoding)
- Promedios móviles y tendencias temporales

### 2. Compras Reales del Restaurante

Este es el archivo JSON que tú me proporcionaste con las **compras reales** que haces cada día:

```json
[
  {
    "Producto": "Pollo (tacos)",
    "Lunes": 200,
    "Martes": 160,
    "Miércoles": 160,
    "Jueves": 200,
    "Viernes": 200,
    "Sábado": 160
  },
  {
    "Producto": "Bistec (tacos)",
    "Lunes": 300,
    "Martes": 240,
    "Miércoles": 240,
    "Jueves": 300,
    "Viernes": 300,
    "Sábado": 200
  },
  {
    "Producto": "Longaniza (tacos)",
    "Lunes": 200,
    "Martes": 200,
    "Miércoles": 200,
    "Jueves": 200,
    "Viernes": 300,
    "Sábado": 200
  },
  {
    "Producto": "Chuleta (tacos)",
    "Lunes": 60,
    "Martes": 60,
    "Miércoles": 60,
    "Jueves": 60,
    "Viernes": 60,
    "Sábado": 60
  },
  {
    "Producto": "Arrachera (tacos)",
    "Lunes": 200,
    "Martes": 140,
    "Miércoles": 160,
    "Jueves": 200,
    "Viernes": 240,
    "Sábado": 160
  },
  {
    "Producto": "Costilla (tacos)",
    "Lunes": 120,
    "Martes": 100,
    "Miércoles": 100,
    "Jueves": 120,
    "Viernes": 160,
    "Sábado": 100
  },
  {
    "Producto": "Coca (piezas)",
    "Lunes": 92,
    "Martes": 66,
    "Miércoles": 66,
    "Jueves": 92,
    "Viernes": 100,
    "Sábado": 66
  },
  {
    "Producto": "Mundet (piezas)",
    "Lunes": 66,
    "Martes": 48,
    "Miércoles": 48,
    "Jueves": 66,
    "Viernes": 70,
    "Sábado": 48
  }
]
```

**Interpretación:**
- Estas cantidades representan **unidades listas para vender** (tacos ya preparados o bebidas)
- Las compras son **diarias** (cada día compras para ese día específico)
- Unidades ya convertidas: 1 kg de carne = 20 tacos
- **Total comprado semanalmente:** 6,948 unidades

---

## 🔢 Sistema de Factores de Escala

### El Problema Identificado

**Discrepancia entre predicciones base y realidad:**
- **Ventas registradas en BD:** ~1,087 unidades/semana
- **Compras reales:** ~6,948 unidades/semana
- **Factor de discrepancia:** ~6.4× subestimación

**Causa raíz:** No todas las ventas se registran en el sistema POS debido a:
- Alta afluencia de clientes en horas pico
- Operación rápida sin tiempo para registrar cada venta
- Ventas para llevar que se anotan manualmente

### La Solución: Factores de Escala

Implementamos un **sistema híbrido de factores de escala** que ajusta las predicciones del modelo base multiplicándolas por factores calculados históricamente.

### Método de Granularidad Híbrida

**Lógica de aplicación (en orden de preferencia):**

1. **Factor Producto + Día** (más específico):
   - Si hay ≥3 observaciones históricas para ese producto en ese día específico
   - Ejemplo: Factor para "Pollo los Lunes" = 2.63×
   
2. **Factor Producto** (fallback medio):
   - Si no hay suficientes datos para producto+día
   - Usa el promedio de todos los días de ese producto
   - Ejemplo: Factor promedio para "Pollo" = 5.88×

3. **Factor Global** (fallback final):
   - Si no hay datos suficientes del producto
   - Usa el promedio de todos los productos
   - Factor global = 5.61×

### Fórmula de Cálculo del Factor

Para cada combinación producto-día:

```
Factor_Escala = Compras_Reales / Ventas_Registradas
```

**Ejemplo: Pollo los Lunes**
- Ventas registradas: 76 tacos (de 34 órdenes)
- Compras reales: 200 tacos
- **Factor calculado: 200 / 76 = 2.63×**

**Interpretación:** Por cada taco de pollo registrado los lunes, en realidad se venden 2.63 tacos.

### Predicción Final

```
Predicción_Final = ((Predicción_Base_XGBoost × Factor_Escala) × Margen_Seguridad) 
                   redondeada a múltiplos de 10
```

Donde:
- `Predicción_Base_XGBoost`: Salida directa del modelo entrenado
- `Factor_Escala`: Factor híbrido seleccionado (producto+día, producto, o global)
- `Margen_Seguridad`: 1.15 (15% adicional para evitar desabasto)
- Redondeo a múltiplos de 10 para facilitar compras

---

## 📈 Factores de Escala Calculados

Aquí están los factores reales calculados por el sistema (archivo JSON que te adjunto):

### Resumen por Producto

| Producto | Factor Promedio | Rango de Factores | Interpretación |
|----------|----------------|-------------------|----------------|
| **Pollo** | 5.88× | 2.63× - 8.89× | Relativamente estable, buena cobertura |
| **Bistec** | 3.87× | 3.19× - 4.00× | **Muy estable** (mejor caso) |
| **Longaniza** | 66.67× | 28.57× - 66.67× | ⚠️ **EXTREMO** - pocas ventas registradas |
| **Chuleta** | 7.50× | 5.00× - 15.00× | Moderadamente variable |
| **Arrachera** | 10.91× | 6.25× - 16.67× | Alta variabilidad semanal |
| **Costilla** | 4.62× | 3.70× - 26.67× | Estable excepto viernes (26.67×) |
| **Coca** | 2.00× | 1.10× - 4.76× | Mejor registro (bebidas más fáciles) |
| **Mundet** | 5.33× | 3.68× - 6.60× | Estable |

### Estadísticas Globales

- **Media:** 13.24×
- **Mediana:** 5.88×
- **Desviación estándar:** 19.00 (alta variabilidad)
- **Rango:** 1.10× - 66.67×
- **Total de factores calculados:** 48 (8 productos × 6 días)

### Advertencias del Sistema

El sistema detectó **3 factores extremos** para Longaniza:

1. **Martes:** 66.67× (ventas registradas: 3, compras: 200)
2. **Miércoles:** 100.00× (ventas registradas: 2, compras: 200)
3. **Jueves:** 200.00× (ventas registradas: 1, compra: 200)

**Posibles causas:**
- Producto nuevo con pocas ventas históricas
- Casi ninguna venta se registra en el sistema
- Alta merma o desperdicio
- Error en los datos de compras

---

## 📊 Métricas de Evaluación

### Comparación: SIN Escala vs CON Escala

| Métrica | BASE (sin escala) | ESCALADA (con factores) | Mejora |
|---------|-------------------|--------------------------|--------|
| **MAE** (Error Absoluto Medio) | 122.10 unidades | 46.25 unidades | **+62.1%** |
| **MAPE** (Error Porcentual Medio) | 83.14% | 32.90% | **+60.4%** |
| **R² Score** | -2.20 | 0.12 | Mejora significativa |
| **RMSE** | ~145 | 71.86 | +50.4% |
| **Total Predicho** | 1,087 unidades | 6,840 unidades | 529.3% ajuste |
| **Precisión Total** | 15.6% del real | 98.4% del real | **Excelente** |

### Interpretación de Métricas

**MAE (Mean Absolute Error - Error Absoluto Medio):**
- Promedio de la diferencia absoluta entre predicción y realidad
- **46.25 unidades** significa que en promedio nos equivocamos por ~46 unidades por producto-día
- Métrica en las mismas unidades que los datos (fácil de interpretar)

**MAPE (Mean Absolute Percentage Error - Error Porcentual Absoluto Medio):**
- Promedio del error porcentual
- **32.90%** significa que en promedio nos equivocamos por ~33%
- Útil para comparar productos de diferentes escalas

**R² Score (Coeficiente de Determinación):**
- Mide qué tan bien el modelo explica la variabilidad de los datos
- Rango: -∞ a 1.0 (1.0 = perfecto)
- **0.12** indica que el modelo explica el 12% de la variabilidad
- Bajo pero aceptable dado el contexto (ventas con alta aleatoriedad)

**RMSE (Root Mean Squared Error):**
- Similar a MAE pero penaliza más los errores grandes
- **71.86** indica que los errores grandes siguen presentes pero controlados

---

## 🎯 Productos con Mejor/Peor Desempeño

### TOP 3 Mejores Predicciones (Error < 30%)

1. **Pollo (tacos):** 28.3% error - Factor 5.88× funciona bien
2. **Bistec (tacos):** 28.9% error - Factor más estable (3.87×)
3. **Arrachera (tacos):** 28.9% error - A pesar de alta variabilidad

### TOP 3 Productos a Mejorar (Error > 31%)

1. **Longaniza (tacos):** 49.2% error - Factor extremo (66.67×) por pocas ventas registradas
2. **Mundet (piezas):** 34.5% error - Bebida de menor consumo
3. **Coca (piezas):** 31.9% error - Aunque tiene mejor registro

---

## 🎨 Visualizaciones Generadas

El sistema genera 5 gráficos en formato PNG:

1. **prediccion_vs_real_productos.png:** Gráfico de barras comparando predicción escalada vs compras reales por producto (total semanal)

2. **tendencias_por_dia.png:** Gráficos de líneas mostrando tendencias para cada día de la semana (Lunes a Sábado)

3. **heatmap_errores.png:** Mapa de calor mostrando el error porcentual por producto y día (identifica patrones)

4. **distribucion_errores.png:** Histograma de la distribución de errores (verifica si son normales)

5. **mae_por_dia.png:** Gráfico de barras del MAE promedio por día de la semana (identifica días problemáticos)

---

## ❓ Preguntas para Gemini

Te adjunto:
1. **El archivo HTML completo del reporte** (`reporte_completo.html`)
2. **El archivo JSON con los factores de escala** (`factores_escala_calculados.json`)

Por favor, ayúdame a entender:

### 1. Interpretación de los Factores de Escala
- ¿Por qué ciertos productos tienen factores tan diferentes?
- ¿Es normal que Longaniza tenga un factor de 66.67×?
- ¿Los factores sugieren algún patrón de comportamiento de mis clientes o de mi operación?
- ¿Hay correlación entre el tipo de producto (carnes vs bebidas) y sus factores?

### 2. Impacto en los Resultados
- Con un MAPE de 32.90%, ¿es este sistema confiable para planificar compras?
- ¿Los factores extremos (Longaniza, Viernes Costilla) están afectando negativamente la métrica global?
- ¿Debería eliminar productos con factores extremos del sistema automatizado?
- ¿Qué significa que el modelo base tenga R²=-2.20 pero con escala sea 0.12?

### 3. Análisis de Patrones
- ¿Qué días de la semana son más difíciles de predecir y por qué?
- ¿Hay productos que claramente necesitan un sistema de registro mejorado?
- ¿Los factores de escala revelan problemas operacionales específicos?

### 4. Representación de Resultados
- ¿Cómo puedo presentar estos resultados a mi equipo de forma clara?
- ¿Qué gráficos adicionales me recomiendas crear?
- ¿Debería crear dashboards diferentes para productos estables vs problemáticos?
- ¿Qué KPIs debería monitorear semanalmente?

### 5. Mejoras al Sistema
- ¿Qué estrategias recomiendas para mejorar el registro de ventas?
- ¿Debería ajustar los umbrales del sistema (ej: umbral_datos_minimos=3)?
- ¿Hay features adicionales que debería agregar al modelo XGBoost?
- ¿Debería entrenar modelos separados por producto en vez de uno general?

### 6. Decisiones de Negocio
- ¿Puedo confiar en este sistema para automatizar compras?
- ¿Qué productos debería seguir comprando manualmente?
- ¿El margen de seguridad del 15% es apropiado o debería ajustarlo por producto?
- ¿Cómo manejo la alta variabilidad de productos como Arrachera y Costilla?

---

## 🔧 Configuración Técnica del Sistema

**Parámetros clave que puedo ajustar:**

```json
{
  "escala_params": {
    "aplicar_escala": true,
    "metodo_granularidad": "hibrido",  // Opciones: "producto_dia", "producto", "global", "hibrido"
    "umbral_datos_minimos": 3,         // Mínimo de observaciones para usar factor producto+día
    "factor_minimo": 0.5,              // Alerta si factor < 0.5
    "factor_maximo": 50.0,             // Alerta si factor > 50.0
    "guardar_factores_calculados": true
  },
  "redondeo": {
    "multiplo": 10,                    // Redondear a múltiplos de 10
    "aplicar": true
  },
  "margen_seguridad": 1.15              // 15% adicional
}
```

---

## 📝 Notas Adicionales

- El sistema está implementado en **Python 3.11** con bibliotecas: XGBoost, Pandas, NumPy, Scikit-learn, Matplotlib, Seaborn
- La base de datos es **PostgreSQL 14**
- El período de entrenamiento es de **solo 5 días** (29 sep - 3 oct 2025), lo cual es **limitado**
- Los datos históricos tienen **827 registros de ventas** para entrenar
- El sistema genera reportes automáticamente cada vez que se ejecuta

---

## 🎯 Objetivo Final

Quiero entender profundamente:
1. **¿Por qué el sistema tomó estas decisiones de factores?**
2. **¿Cómo afectan estos factores a mis resultados operacionales?**
3. **¿Cómo puedo representar y comunicar estos resultados efectivamente?**
4. **¿Qué acciones concretas debo tomar para mejorar el sistema?**

Gracias por tu análisis detallado.
