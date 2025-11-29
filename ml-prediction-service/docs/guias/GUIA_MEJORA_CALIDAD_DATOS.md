# 📊 Guía para Mejorar la Calidad de Datos - Sistema ML POS

**Calificación Actual: 48.82/100** ❌  
**Objetivo: >80/100** ✅

---

## 🔍 Problemas Identificados en el Reporte

### 1. **OUTLIERS EXCESIVOS** (Impacto Alto - 35% de la calificación)

#### Problema:
- **503 outliers** detectados (25% del dataset)
- **100% de columnas** numéricas afectadas
- Columnas más problemáticas:
  - `precio_producto`: 9.40% outliers (94 registros)
  - `costo_producto`: 5.30% outliers (53 registros)
  - `dias_desde_ultima_venta`: 5.30% (53 registros)
  - `tiempo_entrega_promedio`: 4.60% (46 registros)

#### Solución 1: **Capping (Winsorización)** ⭐ RECOMENDADO
```python
# Limitar outliers a percentiles 1-99
for col in numeric_columns:
    lower = df[col].quantile(0.01)
    upper = df[col].quantile(0.99)
    df[col] = np.clip(df[col], lower, upper)
```

**Ventajas:**
- ✅ Preserva todos los registros
- ✅ Reduce impacto de valores extremos
- ✅ Mejora distribuciones
- ✅ Compatible con XGBoost

#### Solución 2: **Transformación Log**
```python
# Para columnas con distribuciones log-normales
df['precio_producto_log'] = np.log1p(df['precio_producto'])
df['costo_producto_log'] = np.log1p(df['costo_producto'])
```

**Ventajas:**
- ✅ Normaliza distribuciones asimétricas
- ✅ Reduce outliers naturalmente
- ✅ Mejora interpretabilidad

#### Solución 3: **Eliminación Selectiva** (ÚLTIMA OPCIÓN)
```python
# Solo si >10% son outliers y son errores de datos
from scipy import stats
z_scores = np.abs(stats.zscore(df[numeric_columns]))
df_clean = df[(z_scores < 3).all(axis=1)]
```

**⚠️ Desventaja:** Pérdida de datos (no recomendado con <2000 registros)

---

### 2. **MULTICOLINEALIDAD EXTREMA** (Impacto Alto - 25% de la calificación)

#### Problema:
- `precio_producto` ↔ `costo_producto`: **correlación 0.9929**
- Esto causa inestabilidad en el modelo

#### Solución 1: **Eliminar una variable** ⭐ RECOMENDADO
```python
# Eliminar la variable menos informativa
df.drop(columns=['costo_producto'], inplace=True)

# O crear una variable derivada más útil
df['margen_ganancia_pct'] = (df['precio_producto'] - df['costo_producto']) / df['precio_producto']
df.drop(columns=['precio_producto', 'costo_producto'], inplace=True)
```

**Ventajas:**
- ✅ Elimina redundancia
- ✅ Mejora estabilidad del modelo
- ✅ Reduce dimensionalidad

#### Solución 2: **Análisis de Componentes Principales (PCA)**
```python
from sklearn.decomposition import PCA

# Combinar variables correlacionadas
pca = PCA(n_components=1)
df['precio_costo_pca'] = pca.fit_transform(df[['precio_producto', 'costo_producto']])
df.drop(columns=['precio_producto', 'costo_producto'], inplace=True)
```

---

### 3. **VALORES FALTANTES** (Impacto Medio - 20% de la calificación)

#### Problema:
- `patron_estacional`: 90.20% completitud (9.8% faltante)
- `estacionalidad_dia_semana`: 90.30%
- `stock_minimo`: 90.50%
- `rotacion_inventario`: 92.00%

#### Solución 1: **Imputación Inteligente** ⭐ RECOMENDADO
```python
# Para variables numéricas asimétricas: usar MEDIANA
df['patron_estacional'].fillna(df['patron_estacional'].median(), inplace=True)

# Para variables numéricas normales: usar MEDIA
df['stock_minimo'].fillna(df['stock_minimo'].mean(), inplace=True)

# Para variables categóricas: usar MODA
df['categoria'].fillna(df['categoria'].mode()[0], inplace=True)
```

#### Solución 2: **Imputación por KNN**
```python
from sklearn.impute import KNNImputer

imputer = KNNImputer(n_neighbors=5)
df[numeric_cols] = imputer.fit_transform(df[numeric_cols])
```

**Ventajas:**
- ✅ Considera patrones entre variables
- ✅ Más preciso que media/mediana

#### Solución 3: **Eliminar columnas >30% faltante**
```python
# Solo para columnas con muchísimos valores faltantes
threshold = 0.3
df = df.loc[:, df.isnull().mean() < threshold]
```

---

### 4. **DATA DRIFT TEMPORAL** (Impacto Medio - 15% de la calificación)

#### Problema:
- `costo_producto`: CV = 0.1064
- `margen_ganancia`: CV = 0.1344, cambio -17.68%

#### Solución 1: **Normalización por Períodos**
```python
# Normalizar cada período de tiempo independientemente
df['periodo'] = pd.to_datetime(df['timestamp']).dt.to_period('M')

for periodo in df['periodo'].unique():
    mask = df['periodo'] == periodo
    df.loc[mask, 'costo_producto'] = (
        df.loc[mask, 'costo_producto'] - df.loc[mask, 'costo_producto'].mean()
    ) / df.loc[mask, 'costo_producto'].std()
```

#### Solución 2: **Features de Drift como Input**
```python
# En lugar de eliminar drift, usarlo como señal
df['mes'] = pd.to_datetime(df['timestamp']).dt.month
df['trimestre'] = pd.to_datetime(df['timestamp']).dt.quarter

# El modelo aprenderá patrones temporales
```

⭐ **RECOMENDADO para ML:** XGBoost puede manejar drift si incluyes features temporales.

---

### 5. **DISTRIBUCIONES ASIMÉTRICAS** (Impacto Bajo - 5% de la calificación)

#### Problema:
- Solo 40% de distribuciones normales
- 8 características con skewness extremo (>1.5)

#### Solución: **Transformaciones**
```python
from scipy import stats

# Log Transform para asimetría positiva
df['precio_log'] = np.log1p(df['precio_producto'])  # log(x+1)

# Box-Cox para cualquier asimetría
from scipy.stats import boxcox
df['ventas_boxcox'], lambda_param = boxcox(df['ventas_ultimos_7_dias'] + 1)

# Square Root para distribuciones Poisson-like
df['stock_sqrt'] = np.sqrt(df['stock_actual'])
```

**Nota:** XGBoost **no requiere** normalidad, así que esto es opcional.

---

## 🚀 Plan de Acción Paso a Paso

### **FASE 1: Limpieza Básica (30 min)**

```python
import pandas as pd
import numpy as np

# 1. Cargar datos
df = pd.read_csv('tu_dataset.csv')

# 2. Eliminar duplicados
df = df.drop_duplicates()

# 3. Manejar valores faltantes
for col in df.columns:
    if df[col].isnull().sum() > 0:
        if df[col].dtype in ['float64', 'int64']:
            df[col].fillna(df[col].median(), inplace=True)
        else:
            df[col].fillna(df[col].mode()[0], inplace=True)

print(f"✅ Fase 1 completada: {df.shape}")
```

**Mejora esperada:** +15 puntos

---

### **FASE 2: Manejo de Outliers (45 min)**

```python
from scipy import stats

# Aplicar capping a todas las columnas numéricas
numeric_cols = df.select_dtypes(include=[np.number]).columns

for col in numeric_cols:
    Q1 = df[col].quantile(0.25)
    Q3 = df[col].quantile(0.75)
    IQR = Q3 - Q1
    
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR
    
    # Capping
    df[col] = np.clip(df[col], lower_bound, upper_bound)

print(f"✅ Fase 2 completada: outliers reducidos")
```

**Mejora esperada:** +20 puntos

---

### **FASE 3: Eliminar Multicolinealidad (20 min)**

```python
# Calcular correlaciones
corr_matrix = df[numeric_cols].corr().abs()

# Encontrar pares >0.95
upper_triangle = corr_matrix.where(
    np.triu(np.ones(corr_matrix.shape), k=1).astype(bool)
)

to_drop = [col for col in upper_triangle.columns 
           if any(upper_triangle[col] > 0.95)]

# Eliminar columnas redundantes
df.drop(columns=to_drop, inplace=True)

print(f"✅ Fase 3 completada: {len(to_drop)} columnas eliminadas")
```

**Mejora esperada:** +15 puntos

---

### **FASE 4: Transformar Distribuciones (30 min)**

```python
# Transformar solo las muy asimétricas (skew > 2)
for col in numeric_cols:
    if col in df.columns:  # Por si fue eliminada
        skewness = df[col].skew()
        
        if abs(skewness) > 2 and df[col].min() > 0:
            df[col] = np.log1p(df[col])
            print(f"   Transformado {col}: skew {skewness:.2f} → {df[col].skew():.2f}")

print(f"✅ Fase 4 completada: distribuciones mejoradas")
```

**Mejora esperada:** +5-10 puntos

---

## 📈 Calificación Esperada Después de Mejoras

| Fase | Acción | Puntos Ganados | Calificación |
|------|--------|----------------|--------------|
| Inicial | - | - | **48.82** ❌ |
| Fase 1 | Limpieza básica | +15 | **63.82** |
| Fase 2 | Manejo outliers | +20 | **83.82** ✅ |
| Fase 3 | Multicolinealidad | +15 | **98.82** ⭐ |
| Fase 4 | Transformaciones | +5 | **103.82** (cap 100) |

**Calificación Final Esperada: 85-95/100** 🎉

---

## 🛠️ Uso del Script Automatizado

Ya creamos un script que hace todo automáticamente:

```bash
# 1. Ejecutar el mejorador
cd ml-prediction-service
python mejorar_calidad_datos.py

# 2. Ver resultados
# - datos_mejorados.csv (dataset limpio)
# - reporte_calidad_mejorado.json (nuevo reporte)
# - reporte_calidad_mejorado.html (visualización)
```

---

## ⚠️ Advertencias Importantes

### 1. **NO Normalices para XGBoost**
```python
# ❌ NO HACER esto para XGBoost:
from sklearn.preprocessing import StandardScaler
scaler = StandardScaler()
df_scaled = scaler.fit_transform(df)
```

**Razón:** XGBoost es invariante a escala. La normalización puede **empeorar** el rendimiento.

### 2. **Preserva Relaciones Originales**
```python
# ✅ BIEN: Transformar pero mantener orden
df['precio_log'] = np.log1p(df['precio'])

# ❌ MAL: Normalización que destruye interpretabilidad
df['precio_z'] = (df['precio'] - df['precio'].mean()) / df['precio'].std()
```

### 3. **Valida con Datos de Producción**
```python
# Siempre probar con datos reales antes de producción
df_train_cleaned = improve_quality(df_train)
df_prod_cleaned = improve_quality(df_production)

# Verificar que la transformación sea consistente
assert df_train_cleaned.columns.tolist() == df_prod_cleaned.columns.tolist()
```

---

## 📊 Checklist de Validación

Después de aplicar mejoras, verifica:

- [ ] Completitud >98%
- [ ] Outliers <3% del dataset
- [ ] Sin correlaciones >0.95
- [ ] Data drift CV <0.1
- [ ] Al menos 60% distribuciones aproximadamente normales
- [ ] Sin columnas con >30% valores faltantes
- [ ] Calificación >80/100

---

## 🎯 Próximos Pasos

1. **Ejecutar el script de mejora**
   ```bash
   python mejorar_calidad_datos.py
   ```

2. **Revisar el nuevo reporte HTML**
   ```bash
   open ml-prediction-service/reporte_calidad_mejorado.html
   ```

3. **Regenerar modelos con datos limpios**
   ```bash
   python regenerar_modelos.py --input datos_mejorados.csv
   ```

4. **Validar predicciones**
   ```bash
   ./test-api.sh
   ```

5. **Monitorear en producción**
   - Ejecutar análisis de calidad semanalmente
   - Alertar si calificación <75

---

**Autor:** OpenCode AI  
**Fecha:** 29 de Noviembre de 2025  
**Sistema:** POS & Gestión Integral - Motor de Predicciones ML
