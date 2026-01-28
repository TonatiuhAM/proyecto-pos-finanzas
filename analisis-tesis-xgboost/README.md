# 📊 Análisis de Abastecimiento con XGBoost para Tesis

Sistema de análisis de datos de ventas que demuestra cómo el aumento del volumen de datos mejora la precisión de modelos de Machine Learning (XGBoost) para predicción de abastecimiento en restaurantes.

## 🎯 Objetivo

Demostrar visualmente mediante **curvas de aprendizaje** cómo la expansión del dataset de entrenamiento mejora la capacidad predictiva del modelo, comparando:

- ✅ **Modelo 1**: Entrenado con **5 días reales** de ventas extraídos de la base de datos
- ✅ **Modelo 2**: Entrenado con **6 meses de datos sintéticos** generados con estacionalidad, ruido y tendencia

## 📁 Estructura del Proyecto

```
analisis-tesis-xgboost/
├── README.md                    # Este archivo
├── requirements.txt             # Dependencias de Python
├── setup.sh                     # Script de instalación
├── .env.example                 # Plantilla de configuración
├── .gitignore                   # Archivos ignorados por git
├── scripts/
│   └── analisis_abastecimiento_xgboost.py  # Script principal
├── notebooks/
│   └── (notebooks Jupyter interactivos)
├── data/
│   ├── ventas_5_dias_reales.csv           # Datos extraídos de BD
│   └── ventas_6_meses_sinteticas.csv      # Datos sintéticos generados
├── models/
│   ├── modelo_xgboost_5dias.pkl           # Modelo entrenado con 5 días
│   ├── modelo_xgboost_6meses.pkl          # Modelo entrenado con 6 meses
│   └── scaler.pkl                          # Normalizador de features
└── results/
    ├── analisis_descriptivo_5_dias.txt    # Reporte de análisis inicial
    ├── comparacion_metricas.txt           # Tabla comparativa MAE/RMSE
    ├── interpretacion_resultados.txt      # Análisis de resultados
    ├── learning_curves_comparacion.png    # Gráfica de curvas de aprendizaje
    ├── learning_curves_comparacion.pdf    # Versión PDF para documentos
    ├── comparacion_errores.png            # Gráfica comparativa de errores
    ├── REPORTE_ANALISIS_XGBOOST.md       # Reporte ejecutivo completo
    └── ejecucion.log                      # Log de ejecución
```

## 🔧 Requisitos Previos

- **Python 3.8+**
- **PostgreSQL** con base de datos del sistema POS
- **pip** (gestor de paquetes de Python)
- **venv** (módulo de entornos virtuales de Python)

## 🚀 Instalación

### 1. Clonar o navegar al directorio del proyecto

```bash
cd analisis-tesis-xgboost
```

### 2. Ejecutar script de instalación

```bash
bash setup.sh
```

Este script automáticamente:
- ✅ Crea un entorno virtual Python
- ✅ Instala todas las dependencias necesarias
- ✅ Configura la estructura de carpetas

### 3. Configurar credenciales de base de datos

```bash
# Copiar plantilla de configuración
cp .env.example .env

# Editar con tus credenciales
nano .env
```

**Contenido del archivo `.env`:**

```env
DB_URL=jdbc:postgresql://localhost:5432/pos_fin
DB_USER=tu_usuario
DB_PASS=tu_contraseña
```

### 4. Activar entorno virtual

```bash
source venv/bin/activate
```

## 📊 Uso

### Ejecutar análisis completo (Fase 1)

```bash
python scripts/analisis_abastecimiento_xgboost.py
```

### Resultados generados

Al finalizar la ejecución, se generarán:

1. **`data/ventas_5_dias_reales.csv`**: Dataset extraído de la base de datos
2. **`results/analisis_descriptivo_5_dias.txt`**: Reporte detallado con:
   - Estadísticas generales (promedio, desviación estándar, etc.)
   - Análisis de tendencia (creciente/decreciente/estable)
   - Día con mayor y menor ventas
   - Métricas de variabilidad

3. **`results/ejecucion.log`**: Log completo de la ejecución

### Verificar resultados

```bash
# Ver reporte de análisis
cat results/analisis_descriptivo_5_dias.txt

# Ver datos extraídos
head -n 10 data/ventas_5_dias_reales.csv

# Ver log de ejecución
tail -n 50 results/ejecucion.log
```

## 🧪 Flujo de Trabajo

### Fase 1: Extracción y Análisis de Datos Reales ✅

1. **Conexión a Base de Datos**: Se conecta a PostgreSQL usando credenciales del `.env`
2. **Exploración de Fechas**: Encuentra las fechas con mayor volumen de transacciones
3. **Extracción de 5 Días**: Extrae datos de ventas de 5 días consecutivos
4. **Análisis Descriptivo**: Calcula tendencias, promedios y estadísticas

### Fase 2: Generación de Datos Sintéticos (Próximamente)

- Simulación de 6 meses de ventas
- Estacionalidad semanal (domingos sin ventas)
- Tendencia de crecimiento del 2% mensual
- Ruido aleatorio para variabilidad

### Fase 3-7: Modelado y Visualización (Próximamente)

- Entrenamiento de modelos XGBoost
- Comparación de métricas (MAE, RMSE)
- Generación de curvas de aprendizaje
- Visualización con Matplotlib

## 📦 Dependencias Principales

| Librería | Versión | Propósito |
|----------|---------|-----------|
| `pandas` | 2.0.3 | Manipulación de datos |
| `numpy` | 1.24.3 | Cálculos numéricos |
| `xgboost` | 2.0.0 | Modelo de Machine Learning |
| `scikit-learn` | 1.3.0 | Preprocesamiento y métricas |
| `matplotlib` | 3.7.2 | Visualización de gráficas |
| `psycopg2-binary` | 2.9.7 | Conexión a PostgreSQL |
| `python-dotenv` | 1.0.0 | Gestión de variables de entorno |

## 🐛 Troubleshooting

### Error: "Faltan variables de entorno"

**Solución**: Verifica que el archivo `.env` existe y contiene las credenciales correctas.

```bash
ls -la .env
cat .env
```

### Error: "No se puede conectar a la base de datos"

**Soluciones**:
1. Verifica que PostgreSQL está corriendo: `sudo systemctl status postgresql`
2. Verifica las credenciales en `.env`
3. Verifica que el firewall permite conexiones al puerto 5432

### Error: "ModuleNotFoundError"

**Solución**: Asegúrate de haber activado el entorno virtual y ejecutado la instalación.

```bash
source venv/bin/activate
pip install -r requirements.txt
```

### No se encontraron datos en la base de datos

**Solución**: Verifica que la tabla `ordenes_de_ventas` tiene registros:

```sql
SELECT COUNT(*) FROM ordenes_de_ventas;
SELECT MIN(fecha_orden), MAX(fecha_orden) FROM ordenes_de_ventas;
```

## 📖 Documentación Adicional

- **Reporte Completo**: `results/REPORTE_ANALISIS_XGBOOST.md` (se generará al completar todas las fases)
- **Logs de Ejecución**: `results/ejecucion.log`
- **Datos Procesados**: `data/` (archivos CSV)

## 👨‍💻 Desarrollo

### Estructura del código

El script principal (`analisis_abastecimiento_xgboost.py`) está organizado en fases:

```python
# FASE 1: Extracción de datos reales
conectar_base_datos()
explorar_fechas_con_datos()
extraer_ventas_5_dias()
analizar_tendencia_5_dias()

# FASE 2: Generación de datos sintéticos (próximamente)
# FASE 3: Preparación de datasets (próximamente)
# FASE 4: Entrenamiento de modelos (próximamente)
# FASE 5-7: Análisis y visualización (próximamente)
```

### Logging

El sistema usa el módulo `logging` de Python para registrar eventos. Los logs se muestran en:
- ✅ **Consola**: Salida estándar con colores
- ✅ **Archivo**: `results/ejecucion.log`

## 🤝 Contribuciones

Este proyecto es parte de una tesis de grado. Para consultas o sugerencias, contactar al autor.

## 📄 Licencia

Sistema POS Finanzas - 2026

---

**Fecha de creación**: 28 Enero 2026  
**Última actualización**: 28 Enero 2026  
**Estado**: Fase 1 completada ✅
