# 📊 Analizador de Calidad de Datos - Sistema POS

## Descripción

Script completo desarrollado por un **Ingeniero de Datos Senior** para análisis exhaustivo de calidad de datos en DataFrames post-feature engineering del sistema POS. Diseñado específicamente para validar la calidad de datos antes del entrenamiento de modelos XGBoost de predicción de compras.

## 🎯 Características Principales

### ✅ Análisis de Completitud
- Detección de valores faltantes por columna
- Cálculo de porcentajes de completitud
- Identificación de patrones de datos faltantes
- Alertas para columnas con alta tasa de valores faltantes (>5%)

### 🔢 Análisis de Tipos de Datos
- Clasificación automática de características (numéricas, categóricas, temporales)
- Detección de inconsistencias de tipado
- Identificación de columnas numéricas que podrían ser categóricas
- Verificación de columnas categóricas que podrían ser numéricas

### 🎯 Detección de Outliers
- Métodos disponibles: IQR, Z-Score, Isolation Forest
- Estadísticas detalladas por columna
- Identificación de valores extremos con sus índices
- Cálculo de porcentajes de outliers por característica

### 📈 Análisis de Distribuciones
- Estadísticas descriptivas completas
- Tests de normalidad (Shapiro-Wilk)
- Cálculo de asimetría y curtosis
- Clasificación automática del tipo de distribución
- Análisis de coeficientes de variación

### 🔗 Análisis de Correlaciones
- Matriz de correlación completa para variables numéricas
- Detección de correlaciones altas (umbral configurable)
- Identificación de problemas de multicolinealidad (>0.95)
- Correlaciones específicas con variables objetivo
- Top correlaciones positivas y negativas

### ⏰ Detección de Data Drift
- Análisis temporal de cambios en distribuciones
- Comparación entre múltiples períodos temporales
- Métricas de drift: coeficiente de variación, cambio relativo
- Detección automática de características con drift significativo
- Visualización de tendencias temporales

### 📋 Reportes Integrales
- Reporte JSON completo con todos los análisis
- Reporte HTML interactivo con visualizaciones
- Puntuación de calidad de datos (0-100)
- Resumen ejecutivo con flags de calidad
- Recomendaciones automatizadas basadas en hallazgos

## 🛠️ Instalación y Dependencias

```bash
# Instalar dependencias requeridas
pip install pandas numpy matplotlib seaborn scipy plotly
```

### Dependencias Principales:
- `pandas`: Manipulación de DataFrames
- `numpy`: Operaciones numéricas
- `matplotlib`: Visualizaciones básicas
- `seaborn`: Visualizaciones estadísticas
- `scipy`: Tests estadísticos
- `plotly`: Visualizaciones interactivas para HTML

## 📚 Uso Básico

### 1. Análisis Rápido con Datos Propios

```python
from data_quality_analyzer import DataQualityAnalyzer
import pandas as pd

# Cargar tu DataFrame post-feature engineering
df = pd.read_csv('tu_dataset_procesado.csv')

# Crear analizador
analyzer = DataQualityAnalyzer(
    df=df,
    target_columns=['cantidad_a_comprar', 'prioridad_compra']  # Opcional
)

# Generar reporte completo
report = analyzer.generate_full_report(
    output_file='reporte_calidad.json'
)

# Mostrar puntuación de calidad
print(f"Puntuación de Calidad: {report['executive_summary']['data_quality_score']}/100")
```

### 2. Análisis Componente por Componente

```python
# Análisis individual de cada componente
completeness = analyzer.analyze_completeness()
outliers = analyzer.analyze_outliers(method='iqr')
correlations = analyzer.analyze_correlations(threshold=0.8)
distributions = analyzer.analyze_distributions()
drift = analyzer.detect_data_drift()

print(f"Completitud general: {completeness['overall_completeness_pct']}%")
print(f"Outliers detectados: {outliers['summary']['total_outliers_detected']}")
print(f"Correlaciones altas: {correlations['summary']['high_correlation_pairs']}")
```

### 3. Generación de Datos de Prueba

```python
# Generar datos de prueba realistas basados en el sistema POS
df_test = DataQualityAnalyzer.generate_test_data(
    n_samples=2000,      # Número de registros
    n_features=30,       # Número de características
    include_issues=True  # Incluir problemas de calidad
)

print(f"Datos generados: {df_test.shape}")
print(f"Columnas: {list(df_test.columns)}")
```

### 4. Generación de Reporte HTML

```python
from data_quality_html_report import HTMLReportGenerator

# Generar reporte HTML interactivo
html_generator = HTMLReportGenerator(report)
html_content = html_generator.generate_html_report('reporte_calidad.html')

print("Reporte HTML generado exitosamente!")
```

## 🎮 Demo Completo

```bash
# Ejecutar demo completo con datos de prueba
python data_quality_analyzer.py
```

Este comando ejecutará:
1. Generación de datos de prueba (2000 muestras, 25 características)
2. Análisis completo de calidad
3. Generación de reporte JSON
4. Generación de reporte HTML
5. Visualización de resumen ejecutivo en consola

## 📊 Interpretación de Resultados

### Puntuación de Calidad (0-100)
- **80-100**: Excelente calidad, datos listos para ML
- **60-79**: Buena calidad, algunas mejoras menores
- **40-59**: Calidad regular, requiere atención
- **<40**: Problemas serios, requiere limpieza antes de usar

### Flags de Calidad
- `high_missing_data`: >5% de datos faltantes
- `excessive_outliers`: >5% de outliers en el dataset
- `multicollinearity_issues`: Correlaciones >0.95
- `data_drift_detected`: Cambios temporales significativos

### Umbrales de Alerta
- **Completitud**: <95% requiere atención
- **Outliers**: >5% del dataset indica problemas
- **Correlaciones**: >0.8 alta, >0.95 multicolinealidad
- **Drift**: Coef. variación >0.1 o cambio >20%

## 🧪 Características de los Datos de Prueba

Los datos de prueba simulan el sistema POS real con:

### Características del Producto
- `precio_producto`: Precios con distribución log-normal
- `stock_actual`: Inventario actual (Poisson)
- `rotacion_inventario`: Rotación de productos

### Características Temporales
- `ventas_ultimos_7_dias`: Ventas recientes
- `tendencia_ventas`: Tendencia histórica
- `estacionalidad_mes`: Patrones estacionales

### Características de Proveedor
- `confiabilidad_proveedor`: Rating de proveedores
- `tiempo_entrega_promedio`: Tiempos de entrega

### Variables Objetivo
- `cantidad_a_comprar`: Cantidad predicha (entero positivo)
- `prioridad_compra`: Prioridad 1-5 (categórica ordinal)

## 🔧 Personalización y Extensión

### Añadir Nuevos Métodos de Detección de Outliers

```python
def analyze_outliers_custom(self, method='isolation_forest'):
    if method == 'isolation_forest':
        from sklearn.ensemble import IsolationForest
        # Implementar lógica personalizada
    # ... resto de la implementación
```

### Personalizar Umbrales

```python
# Personalizar umbrales en el constructor
analyzer = DataQualityAnalyzer(df, target_columns=['target'])

# Modificar umbrales después de la inicialización
outliers = analyzer.analyze_outliers()  # IQR por defecto
correlations = analyzer.analyze_correlations(threshold=0.7)  # Umbral personalizado
```

### Añadir Nuevas Métricas

```python
def analyze_feature_importance(self) -> Dict[str, Any]:
    """Añadir análisis de importancia de características."""
    # Tu implementación personalizada
    pass
```

## 📈 Casos de Uso Específicos

### 1. Validación Pre-Entrenamiento
```python
# Antes de entrenar modelo XGBoost
analyzer = DataQualityAnalyzer(df_features)
report = analyzer.generate_full_report()

if report['executive_summary']['data_quality_score'] < 70:
    print("⚠️ Calidad insuficiente para entrenamiento")
    # Implementar limpieza de datos
else:
    print("✅ Datos listos para entrenamiento")
```

### 2. Monitoreo en Producción
```python
# Comparar calidad entre datasets
analyzer_train = DataQualityAnalyzer(df_train)
analyzer_prod = DataQualityAnalyzer(df_production)

# Detectar drift entre entrenamiento y producción
drift_analysis = analyzer_prod.detect_data_drift()
if drift_analysis['summary']['features_with_drift'] > 0:
    print("🚨 Drift detectado - considerar reentrenamiento")
```

### 3. Auditoría de Calidad Periódica
```python
# Script para ejecutar semanalmente
def weekly_quality_audit(data_path):
    df = pd.read_csv(data_path)
    analyzer = DataQualityAnalyzer(df)
    
    report = analyzer.generate_full_report(
        output_file=f'audit_{datetime.now().strftime("%Y%m%d")}.json'
    )
    
    # Enviar alerta si calidad < umbral
    if report['executive_summary']['data_quality_score'] < 80:
        send_quality_alert(report)
```

## 🚀 Mejores Prácticas

### 1. Frecuencia de Análisis
- **Pre-entrenamiento**: Siempre antes de entrenar modelos
- **Producción**: Semanal o cuando lleguen nuevos datos
- **Post-cambios**: Después de modificar pipeline de datos

### 2. Automatización
```python
# Integrar en pipeline de MLOps
def validate_data_quality(df, min_score=75):
    analyzer = DataQualityAnalyzer(df)
    report = analyzer.generate_full_report()
    
    score = report['executive_summary']['data_quality_score']
    if score < min_score:
        raise ValueError(f"Calidad insuficiente: {score}<{min_score}")
    
    return report
```

### 3. Almacenamiento de Reportes
```python
# Guardar reportes con timestamp para seguimiento histórico
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
output_file = f'reports/quality_report_{timestamp}.json'
report = analyzer.generate_full_report(output_file)
```

## 🔍 Troubleshooting

### Error: "No se encontró columna temporal"
```python
# Especificar columna temporal manualmente
drift = analyzer.detect_data_drift(temporal_column='fecha_venta')
```

### Error: "Datos insuficientes"
```python
# Verificar tamaño mínimo del dataset
if len(df) < 100:
    print("Dataset muy pequeño para análisis completo")
```

### Warning: "Muchos valores faltantes"
```python
# Filtrar columnas con pocos valores antes del análisis
df_clean = df.dropna(thresh=len(df)*0.7, axis=1)  # Mantener columnas con <30% faltantes
```

## 📞 Soporte y Contribuciones

Este script está diseñado para el sistema POS específico pero es fácilmente adaptable a otros proyectos de ML. Para casos de uso específicos o extensiones, consultar con el equipo de datos.

### Estructura de Archivos Generados:
```
ml-prediction-service/
├── data_quality_analyzer.py          # Script principal
├── data_quality_html_report.py       # Generador de HTML
├── data_quality_report.json          # Reporte JSON detallado
├── data_quality_report.html          # Reporte HTML interactivo
└── README_data_quality.md            # Esta documentación
```

---

**Desarrollado por**: Ingeniero de Datos Senior  
**Versión**: 1.0.0  
**Fecha**: 14 de octubre de 2025  
**Sistema**: POS & Gestión Integral - Motor de Predicciones ML