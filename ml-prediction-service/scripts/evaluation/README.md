# 📊 Sistema de Evaluación de Predicciones ML vs Compras Reales

## 📝 Descripción

Este sistema permite comparar las predicciones del modelo de Machine Learning con las compras reales realizadas por un negocio. El objetivo es evaluar la precisión del modelo y generar reportes visuales detallados con métricas de comparación.

## 🎯 Características

- ✅ **Extracción automática** de datos históricos desde PostgreSQL
- ✅ **Entrenamiento automático** de modelos XGBoost con datos reales
- ✅ **Generación de predicciones** para una semana genérica
- ✅ **Comparación detallada** con compras reales del negocio
- ✅ **Métricas completas**: MAE, MAPE, RMSE, R²
- ✅ **Reportes visuales** en formato HTML con gráficos interactivos
- ✅ **Exportación de datos** en formato JSON para análisis adicional

## 📂 Estructura de Archivos

```
ml-prediction-service/
├── scripts/
│   └── evaluation/
│       ├── config.json                                   # Configuración del sistema
│       ├── compras_reales.json                          # Compras reales del negocio
│       ├── evaluar_predicciones.sh                      # Script de ejecución
│       └── evaluar_predicciones_vs_compras_reales.py   # Script principal Python
│
reportes-predicciones/                                    # Reportes generados
└── evaluacion_YYYYMMDD_HHMMSS/
    ├── reporte_completo.html                            # Reporte visual
    ├── metricas_detalladas.json                         # Datos en JSON
    └── graficos/                                        # Visualizaciones PNG
        ├── prediccion_vs_real_productos.png
        ├── tendencias_por_dia.png
        ├── heatmap_errores.png
        ├── distribucion_errores.png
        └── mae_por_dia.png
```

## 🚀 Uso

### Opción 1: Usando el script shell (Recomendado)

```bash
bash ml-prediction-service/scripts/evaluation/evaluar_predicciones.sh
```

Este script automáticamente:
1. Verifica que los contenedores estén corriendo
2. Instala dependencias necesarias
3. Ejecuta la evaluación completa
4. Muestra el resultado

### Opción 2: Ejecución manual

```bash
# Desde el contenedor ML
docker exec pos_ml_prediction_api python3 /home/app/scripts/evaluation/evaluar_predicciones_vs_compras_reales.py

# O desde tu máquina (asegúrate de tener Python y dependencias instaladas)
cd ml-prediction-service
python3 scripts/evaluation/evaluar_predicciones_vs_compras_reales.py
```

## ⚙️ Configuración

### config.json

El archivo `config.json` contiene toda la configuración del sistema:

```json
{
  "database": {
    "host": "database",
    "port": 5432,
    "dbname": "pos_finanzas",
    "user": "postgres",
    "password": "postgres"
  },
  "training_period": {
    "fecha_inicio": "2025-09-29",
    "fecha_fin": "2025-10-03",
    "descripcion": "Período de alta actividad identificado automáticamente"
  },
  "mapeo_productos": {
    "Pollo (tacos)": {
      "id": "98391e5f-abd1-4f87-893f-34447c3bf605",
      "nombre_bd": "Pollo"
    },
    ...
  },
  "model_params": {
    "n_estimators": 100,
    "max_depth": 6,
    "learning_rate": 0.1
  }
}
```

### compras_reales.json

Archivo con las compras reales del negocio por día:

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
  ...
]
```

## 📊 Reportes Generados

### 1. Reporte HTML Completo

Archivo: `reporte_completo.html`

Contiene:
- **Resumen ejecutivo** con métricas globales
- **Tablas comparativas** por producto y día
- **5 visualizaciones** interactivas
- **Recomendaciones** basadas en resultados
- **Información del modelo** y métricas de entrenamiento

### 2. Datos JSON

Archivo: `metricas_detalladas.json`

Incluye:
- Métricas globales
- Métricas por producto
- Métricas por día
- Predicciones completas
- Compras reales
- Metadata del modelo

### 3. Visualizaciones

5 gráficos PNG de alta resolución:

1. **prediccion_vs_real_productos.png**: Comparación de totales semanales
2. **tendencias_por_dia.png**: Evolución diaria (top 5 productos)
3. **heatmap_errores.png**: Mapa de calor de errores porcentuales
4. **distribucion_errores.png**: Histograma de distribución de errores
5. **mae_por_dia.png**: Error absoluto medio por día de la semana

## 📈 Métricas Explicadas

### MAE (Mean Absolute Error)
Error promedio en unidades. Ejemplo: MAE de 28.5 significa que el modelo se equivoca en promedio 28.5 unidades.

### MAPE (Mean Absolute Percentage Error)
Error promedio en porcentaje. Ejemplo: MAPE de 18% significa un error del 18% respecto al valor real.

### RMSE (Root Mean Squared Error)
Desviación estándar del error. Penaliza más los errores grandes.

### R² Score
Coeficiente de determinación. Valores de 0 a 1, donde 1 es predicción perfecta.
- **> 0.7**: Excelente
- **0.5 - 0.7**: Bueno
- **< 0.5**: Mejorable

## 🔧 Personalización

### Cambiar el período de entrenamiento

Edita `config.json`:

```json
"training_period": {
  "fecha_inicio": "2025-10-01",
  "fecha_fin": "2025-10-31"
}
```

### Ajustar parámetros del modelo

Edita `config.json`:

```json
"model_params": {
  "n_estimators": 200,
  "max_depth": 8,
  "learning_rate": 0.05
}
```

### Actualizar compras reales

Reemplaza el contenido de `compras_reales.json` con tus propios datos.

## 🐛 Solución de Problemas

### Error: Contenedores no están corriendo

```bash
docker compose up -d
```

### Error: No hay datos históricos

Verifica que haya ventas registradas en el período configurado:

```bash
docker exec pos_database psql -U postgres -d pos_finanzas -c "SELECT COUNT(*) FROM ordenes_de_ventas WHERE fecha_orden BETWEEN '2025-09-29' AND '2025-10-03';"
```

### Error: Producto no encontrado

Verifica el mapeo de productos en `config.json`. Los IDs deben coincidir con la base de datos.

### Permisos de archivos

Si hay problemas de permisos con los reportes:

```bash
sudo chown -R $USER:$USER reportes-predicciones/
```

## 📚 Dependencias

El script requiere las siguientes librerías Python (se instalan automáticamente):

- pandas >= 2.1.0
- numpy >= 1.25.0
- xgboost >= 2.0.0
- scikit-learn >= 1.3.0
- psycopg2-binary >= 2.9.6
- matplotlib >= 3.8.0
- seaborn >= 0.13.0

## 🔄 Flujo del Sistema

```
1. Conectar a PostgreSQL
   ↓
2. Extraer datos históricos (período configurado)
   ↓
3. Feature engineering automático
   ↓
4. Entrenar modelos XGBoost (cantidad + prioridad)
   ↓
5. Generar predicciones para semana genérica
   ↓
6. Cargar compras reales desde JSON
   ↓
7. Calcular métricas de comparación
   ↓
8. Generar visualizaciones (5 gráficos)
   ↓
9. Crear reporte HTML completo
   ↓
10. Exportar métricas a JSON
   ↓
11. Copiar reportes a carpeta del host
```

## 💡 Recomendaciones

1. **Ejecuta la evaluación semanalmente** para monitorear la precisión del modelo
2. **Actualiza las compras reales** cada vez que tengas nuevos datos
3. **Reentrena el modelo mensualmente** con datos frescos
4. **Revisa los productos con mayor error** y ajusta features si es necesario
5. **Compara reportes históricos** para detectar degradación del modelo

## 📞 Soporte

Para problemas o preguntas:
- Revisa los logs en la consola
- Consulta el archivo `metricas_detalladas.json` para más detalles
- Verifica la configuración en `config.json`

## 📝 Notas Importantes

- ⚠️ El script requiere que los contenedores `pos_database` y `pos_ml_prediction_api` estén corriendo
- ⚠️ Los reportes se generan en carpetas con timestamp para no sobrescribir evaluaciones anteriores
- ⚠️ Las predicciones se basan en patrones históricos, no en eventos futuros no predecibles
- ⚠️ Un error alto puede indicar: datos insuficientes, eventos atípicos, o necesidad de más features

---

**Desarrollado para el Sistema POS Finanzas | Módulo de Machine Learning**
