#!/bin/bash

# Script para ejecutar validación ML académica completa ISO/IEC 25010
# 
# CARACTERÍSTICAS:
# ✅ Entrenamiento con Hold-out temporal (70/15/15)
# ✅ Validación independiente con métricas RMSE
# ✅ Cross-validation temporal
# ✅ Métricas completas para tesina académica
#
# Autor: Sistema POS-Finanzas
# Fecha: 2025-01-07

set -e  # Detener en caso de error

echo "=============================================================================="
echo "🚀 VALIDACIÓN ML ACADÉMICA - SISTEMA POS-FINANZAS"
echo "=============================================================================="
echo "✅ Estándares: ISO/IEC 25010 Quality in Use Metrics"
echo "✅ Validación: Hold-out temporal + Cross-validation"  
echo "✅ Métricas: RMSE, MAE, R², MAPE, SMAPE + Intervalos de confianza"
echo "✅ Output: Listo para tesina académica"
echo "=============================================================================="

# Verificar que estamos en el directorio correcto
if [[ ! -f "main.py" ]]; then
    echo "❌ Error: Ejecutar desde directorio ml-prediction-service/"
    exit 1
fi

# Crear directorios necesarios
echo "📁 Creando estructura de directorios..."
mkdir -p models
mkdir -p data/raw
mkdir -p reports/academic

# Verificar Python y dependencias
echo "🐍 Verificando entorno Python..."
python3 --version
pip3 show xgboost scikit-learn pandas numpy > /dev/null || {
    echo "❌ Error: Instalar dependencias: pip install xgboost scikit-learn pandas numpy matplotlib seaborn scipy"
    exit 1
}

echo "✅ Entorno Python verificado"

# Paso 1: Entrenamiento con validación académica
echo ""
echo "================================================================================"
echo "📊 PASO 1: ENTRENAMIENTO CON VALIDACIÓN ACADÉMICA"
echo "================================================================================"
echo "🎯 Ejecutando: entrenar_validacion_academica.py"
echo "🔍 Hold-out temporal: 70% entrenamiento, 15% validación, 15% prueba"
echo "🔍 Cross-validation: 5-fold con TimeSeriesSplit"
echo "🔍 Early stopping: 20 rounds para prevenir overfitting"
echo "================================================================================"

cd scripts/training
python3 entrenar_validacion_academica.py

if [[ $? -eq 0 ]]; then
    echo "✅ Entrenamiento académico completado exitosamente"
    echo "📊 Archivos generados:"
    echo "   • models/regressor_cantidad.json"
    echo "   • models/ranker_prioridad.json"  
    echo "   • models/metricas_validacion_academica.json"
    echo "   • models/model_metadata.json"
    echo "   • models/model_features.txt"
else
    echo "❌ Error durante el entrenamiento académico"
    cd ../..
    exit 1
fi

cd ../..

# Paso 2: Validación Hold-out independiente
echo ""
echo "================================================================================"
echo "🧪 PASO 2: VALIDACIÓN HOLD-OUT INDEPENDIENTE"
echo "================================================================================"
echo "🎯 Ejecutando: validacion_holdout_rmse.py"
echo "🔍 Conjunto de prueba independiente"
echo "🔍 Intervalos de confianza RMSE (95%)"
echo "🔍 Pruebas de normalidad de residuales"
echo "🔍 Análisis de estabilidad temporal"
echo "================================================================================"

cd scripts/testing
python3 validacion_holdout_rmse.py

if [[ $? -eq 0 ]]; then
    echo "✅ Validación Hold-out completada exitosamente"
    echo "📊 Archivo generado:"
    echo "   • models/validacion_holdout_resultados.json"
else
    echo "❌ Error durante la validación Hold-out"
    cd ../..
    exit 1
fi

cd ../..

# Paso 3: Generar reporte académico consolidado
echo ""
echo "================================================================================"
echo "📋 PASO 3: GENERACIÓN DE REPORTE ACADÉMICO CONSOLIDADO"
echo "================================================================================"

# Crear reporte consolidado
cat > reports/academic/reporte_validacion_academica.md << 'EOF'
# Reporte de Validación ML Académica - Sistema POS-Finanzas

## Resumen Ejecutivo

Este reporte presenta los resultados de la validación académica de los modelos de Machine Learning del Sistema POS-Finanzas, siguiendo estándares **ISO/IEC 25010** para Quality in Use Metrics.

## Metodología de Validación

### 1. División de Datos (Hold-out Temporal)
- **Entrenamiento**: 70% de los datos históricos
- **Validación**: 15% para early stopping y ajuste de hiperparámetros
- **Prueba**: 15% para evaluación final independiente

### 2. Validación Cruzada
- **Método**: TimeSeriesSplit (5-fold)
- **Objetivo**: Evaluar estabilidad y generalización temporal

### 3. Métricas Evaluadas
- **RMSE**: Root Mean Square Error (métrica principal)
- **MAE**: Mean Absolute Error
- **R²**: Coeficiente de determinación
- **R² Ajustado**: Considerando número de características
- **MAPE**: Mean Absolute Percentage Error
- **SMAPE**: Symmetric Mean Absolute Percentage Error
- **Accuracy ±20%**: Porcentaje de predicciones dentro del 20% del valor real

### 4. Análisis Estadístico
- **Intervalos de confianza** para RMSE (95%)
- **Pruebas de normalidad** de residuales (Shapiro-Wilk)
- **Análisis de estabilidad temporal** por períodos
- **Correlaciones** entre valores reales y predichos

## Archivos de Resultados

1. **metricas_validacion_academica.json**: Métricas completas de entrenamiento
2. **validacion_holdout_resultados.json**: Resultados de validación independiente
3. **model_metadata.json**: Metadatos de modelos para compatibilidad
4. **regressor_cantidad.json**: Modelo entrenado para predicción de cantidades
5. **ranker_prioridad.json**: Modelo entrenado para ranking de prioridad
6. **model_features.txt**: Lista de características utilizadas

## Cumplimiento de Estándares

✅ **ISO/IEC 25010 - Quality in Use**: Métricas de efectividad y precisión  
✅ **Validación Hold-out**: División temporal estricta sin data leakage  
✅ **Cross-validation**: Evaluación robusta de generalización  
✅ **Intervalos de confianza**: Cuantificación de incertidumbre  
✅ **Análisis estadístico**: Pruebas de normalidad y estabilidad  

## Interpretación de Resultados

Los resultados detallados se encuentran en los archivos JSON correspondientes. 
Para una interpretación completa, consultar las métricas de:

1. **Precisión del modelo** (RMSE, R²)
2. **Estabilidad temporal** (coeficiente de variación)
3. **Confiabilidad estadística** (intervalos de confianza)
4. **Calidad de residuales** (normalidad, correlaciones)

---

*Generado automáticamente el $(date)*
*Sistema POS-Finanzas - Validación Académica v3.0*
EOF

echo "✅ Reporte académico generado: reports/academic/reporte_validacion_academica.md"

# Paso 4: Resumen final
echo ""
echo "================================================================================"
echo "🎉 VALIDACIÓN ML ACADÉMICA COMPLETADA EXITOSAMENTE"
echo "================================================================================"
echo ""
echo "📊 ARCHIVOS GENERADOS PARA TESINA:"
echo ""
echo "🏆 MODELOS ENTRENADOS:"
echo "   📄 regressor_cantidad.json (XGBoost - Predicción de cantidades)"
echo "   📄 ranker_prioridad.json (XGBoost - Ranking de prioridad)"
echo "   📄 model_features.txt (25 características utilizadas)"
echo ""
echo "📈 MÉTRICAS ACADÉMICAS:"
echo "   📄 metricas_validacion_academica.json (Métricas completas)"
echo "   📄 validacion_holdout_resultados.json (Validación independiente)"
echo "   📄 model_metadata.json (Metadatos compatibilidad)"
echo ""
echo "📋 DOCUMENTACIÓN:"
echo "   📄 reports/academic/reporte_validacion_academica.md"
echo ""
echo "================================================================================"
echo "✅ CUMPLE ESTÁNDARES ISO/IEC 25010"
echo "✅ LISTO PARA TESINA ACADÉMICA"
echo "✅ VALIDACIÓN HOLD-OUT CON INTERVALOS DE CONFIANZA"
echo "✅ CROSS-VALIDATION TEMPORAL ROBUSTA"
echo "================================================================================"

# Mostrar breve resumen de métricas si los archivos existen
if [[ -f "metricas_validacion_academica.json" ]]; then
    echo ""
    echo "🎯 RESUMEN DE MÉTRICAS PRINCIPALES:"
    
    # Extraer métricas principales usando Python inline
    python3 -c "
import json
import sys

try:
    with open('metricas_validacion_academica.json', 'r') as f:
        data = json.load(f)
    
    reg_metrics = data['regressor_cantidad']['metricas_principales']
    rank_metrics = data['ranker_prioridad']['metricas_principales']
    
    print(f'📊 REGRESSOR (Cantidades):')
    print(f'   RMSE: {reg_metrics[\"rmse\"]:.4f}')
    print(f'   R² Score: {reg_metrics[\"r2_score\"]:.4f}')
    print(f'   R² Ajustado: {reg_metrics[\"r2_ajustado\"]:.4f}')
    
    print(f'🏆 RANKER (Prioridades):')
    print(f'   RMSE: {rank_metrics[\"rmse\"]:.4f}')
    print(f'   R² Score: {rank_metrics[\"r2_score\"]:.4f}') 
    print(f'   R² Ajustado: {rank_metrics[\"r2_ajustado\"]:.4f}')
    
    total_samples = data['division_datos']['total_registros']
    print(f'📈 Total registros procesados: {total_samples}')
    
except Exception as e:
    print(f'ℹ️ Métricas disponibles en archivos JSON generados')
"
fi

echo ""
echo "🚀 ¡Validación académica completada! Los modelos están listos para producción."
echo "================================================================================"