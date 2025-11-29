#!/usr/bin/env python3
"""
Script para entrenar modelos XGBoost con DATOS REALES de producción.
Reemplaza los datos sintéticos con ventas reales de la base de datos.
"""

import os
import json
import logging
import pandas as pd
import numpy as np
import xgboost as xgb
from datetime import datetime
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
import warnings

warnings.filterwarnings('ignore')

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def cargar_datos_reales():
    """Carga y procesa los datos reales de ventas."""
    logger.info("📂 Cargando datos reales de ventas...")
    
    df_ventas = pd.read_csv('datos_ventas_reales.csv')
    logger.info(f"✅ {len(df_ventas)} registros de ventas cargados")
    
    # Convertir fecha
    df_ventas['fecha_orden'] = pd.to_datetime(df_ventas['fecha_orden'])
    
    # Crear features temporales
    df_ventas['dia_semana'] = df_ventas['fecha_orden'].dt.dayofweek
    df_ventas['dia_mes'] = df_ventas['fecha_orden'].dt.day
    df_ventas['mes'] = df_ventas['fecha_orden'].dt.month
    df_ventas['trimestre'] = df_ventas['fecha_orden'].dt.quarter
    df_ventas['semana_año'] = df_ventas['fecha_orden'].dt.isocalendar().week
    
    # Features cíclicas
    df_ventas['dia_semana_sin'] = np.sin(2 * np.pi * df_ventas['dia_semana'] / 7)
    df_ventas['dia_semana_cos'] = np.cos(2 * np.pi * df_ventas['dia_semana'] / 7)
    df_ventas['mes_sin'] = np.sin(2 * np.pi * df_ventas['mes'] / 12)
    df_ventas['mes_cos'] = np.cos(2 * np.pi * df_ventas['mes'] / 12)
    
    # Features booleanas
    df_ventas['es_fin_semana'] = (df_ventas['dia_semana'] >= 5).astype(int)
    df_ventas['es_lunes'] = (df_ventas['dia_semana'] == 0).astype(int)
    df_ventas['es_viernes'] = (df_ventas['dia_semana'] == 4).astype(int)
    
    # Simular features climáticas (placeholder - podrían venir de API)
    df_ventas['es_feriado'] = 0  # TODO: Integrar calendario de feriados
    df_ventas['dia_antes_feriado'] = 0
    df_ventas['dia_despues_feriado'] = 0
    df_ventas['lluvia_simulada'] = 0
    df_ventas['temperatura_simulada'] = 22.0
    df_ventas['clima_caluroso'] = 0
    df_ventas['clima_frio'] = 0
    
    # Agregar por producto y fecha para crear dataset de entrenamiento
    df_agregado = df_ventas.groupby(['productos_id', 'producto_nombre', 'fecha_orden']).agg({
        'cantidad_pz': 'sum',
        'precio_venta': 'mean',
        'stock_actual_pz': 'first',
        'cantidad_minima': 'first',
        'cantidad_maxima': 'first',
        'dia_semana': 'first',
        'dia_mes': 'first',
        'mes': 'first',
        'trimestre': 'first',
        'semana_año': 'first',
        'dia_semana_sin': 'first',
        'dia_semana_cos': 'first',
        'mes_sin': 'first',
        'mes_cos': 'first',
        'es_fin_semana': 'first',
        'es_lunes': 'first',
        'es_viernes': 'first',
        'es_feriado': 'first',
        'dia_antes_feriado': 'first',
        'dia_despues_feriado': 'first',
        'lluvia_simulada': 'first',
        'temperatura_simulada': 'first',
        'clima_caluroso': 'first',
        'clima_frio': 'first'
    }).reset_index()
    
    # Renombrar columna de cantidad
    df_agregado.rename(columns={'cantidad_pz': 'cantidad_total'}, inplace=True)
    
    # Calcular prioridad de compra basada en ventas y stock
    df_agregado['stock_ratio'] = df_agregado['stock_actual_pz'] / (df_agregado['cantidad_maxima'] + 1)
    df_agregado['deficit'] = (df_agregado['cantidad_minima'] - df_agregado['stock_actual_pz']).clip(lower=0)
    
    # Prioridad: 0-5, mayor es más urgente
    df_agregado['prioridad_score'] = 3.0  # Base
    
    # Aumentar prioridad si hay déficit
    df_agregado.loc[df_agregado['deficit'] > 0, 'prioridad_score'] = 5.0
    
    # Reducir prioridad si hay exceso de stock
    df_agregado.loc[df_agregado['stock_ratio'] > 0.8, 'prioridad_score'] = 1.0
    
    # Prioridad media-alta si stock moderado y ventas altas
    df_agregado.loc[
        (df_agregado['stock_ratio'] < 0.5) & (df_agregado['cantidad_total'] > 2),
        'prioridad_score'
    ] = 4.0
    
    logger.info(f"✅ Dataset de entrenamiento preparado: {len(df_agregado)} muestras")
    logger.info(f"📊 Productos únicos: {df_agregado['productos_id'].nunique()}")
    logger.info(f"📅 Rango temporal: {df_agregado['fecha_orden'].min()} a {df_agregado['fecha_orden'].max()}")
    
    return df_agregado

def entrenar_modelos(df):
    """Entrena los modelos con los datos reales."""
    logger.info("🎯 Iniciando entrenamiento de modelos con datos reales...")
    
    # Definir features
    feature_cols = [
        'dia_semana', 'dia_mes', 'mes', 'trimestre', 'semana_año',
        'dia_semana_sin', 'dia_semana_cos', 'mes_sin', 'mes_cos',
        'es_fin_semana', 'es_lunes', 'es_viernes',
        'es_feriado', 'dia_antes_feriado', 'dia_despues_feriado',
        'lluvia_simulada', 'temperatura_simulada',
        'clima_caluroso', 'clima_frio',
        'precio_venta', 'stock_actual_pz', 'cantidad_minima', 'cantidad_maxima',
        'stock_ratio', 'deficit'
    ]
    
    X = df[feature_cols].fillna(0)
    y_cantidad = df['cantidad_total']
    y_prioridad = df['prioridad_score']
    
    # División train/test
    X_train, X_test, y_cantidad_train, y_cantidad_test, y_prioridad_train, y_prioridad_test = \
        train_test_split(X, y_cantidad, y_prioridad, test_size=0.2, random_state=42)
    
    logger.info(f"📊 Entrenamiento: {len(X_train)} registros")
    logger.info(f"📊 Prueba: {len(X_test)} registros")
    
    # 1. Modelo para cantidad
    logger.info("🎯 Entrenando modelo regressor (cantidad)...")
    regressor = xgb.XGBRegressor(
        n_estimators=100,
        max_depth=6,
        learning_rate=0.1,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=42,
        objective='reg:squarederror'
    )
    
    regressor.fit(X_train, y_cantidad_train)
    
    y_pred_cantidad = regressor.predict(X_test)
    rmse_cantidad = np.sqrt(mean_squared_error(y_cantidad_test, y_pred_cantidad))
    mae_cantidad = mean_absolute_error(y_cantidad_test, y_pred_cantidad)
    r2_cantidad = r2_score(y_cantidad_test, y_pred_cantidad)
    
    logger.info(f"📈 Regressor - RMSE: {rmse_cantidad:.4f}, MAE: {mae_cantidad:.4f}, R²: {r2_cantidad:.4f}")
    
    # 2. Modelo para prioridad
    logger.info("🏆 Entrenando modelo ranker (prioridad)...")
    ranker = xgb.XGBRegressor(
        n_estimators=100,
        max_depth=6,
        learning_rate=0.1,
        subsample=0.8,
        colsample_bytree=0.8,
        random_state=42,
        objective='reg:squarederror'
    )
    
    ranker.fit(X_train, y_prioridad_train)
    
    y_pred_prioridad = ranker.predict(X_test)
    rmse_prioridad = np.sqrt(mean_squared_error(y_prioridad_test, y_pred_prioridad))
    mae_prioridad = mean_absolute_error(y_prioridad_test, y_pred_prioridad)
    r2_prioridad = r2_score(y_prioridad_test, y_pred_prioridad)
    
    logger.info(f"📈 Ranker - RMSE: {rmse_prioridad:.4f}, MAE: {mae_prioridad:.4f}, R²: {r2_prioridad:.4f}")
    
    return regressor, ranker, feature_cols, {
        'regressor_metrics': {
            'rmse': float(rmse_cantidad),
            'mae': float(mae_cantidad),
            'r2_score': float(r2_cantidad)
        },
        'ranker_metrics': {
            'rmse': float(rmse_prioridad),
            'mae': float(mae_prioridad),
            'r2_score': float(r2_prioridad)
        },
        'training_samples': len(X_train),
        'test_samples': len(X_test),
        'features_count': len(feature_cols),
        'data_source': 'REAL_PRODUCTION_DATA',
        'date_range': {
            'min': str(df['fecha_orden'].min()),
            'max': str(df['fecha_orden'].max())
        }
    }

def guardar_modelos(regressor, ranker, feature_cols, metrics, models_dir="./models"):
    """Guarda los modelos entrenados."""
    logger.info(f"💾 Guardando modelos en {models_dir}...")
    
    os.makedirs(models_dir, exist_ok=True)
    
    # Guardar regressor
    regressor_path = os.path.join(models_dir, "regressor_cantidad.json")
    regressor.save_model(regressor_path)
    regressor.save_model("./regressor_cantidad.json")
    logger.info(f"✅ Regressor guardado: {regressor_path}")
    
    # Guardar ranker
    ranker_path = os.path.join(models_dir, "ranker_prioridad.json")
    ranker.save_model(ranker_path)
    ranker.save_model("./ranker_prioridad.json")
    logger.info(f"✅ Ranker guardado: {ranker_path}")
    
    # Guardar features
    features_path = os.path.join(models_dir, "model_features.txt")
    with open(features_path, 'w') as f:
        f.write('\n'.join(feature_cols))
    with open("./model_features.txt", 'w') as f:
        f.write('\n'.join(feature_cols))
    logger.info(f"✅ Features guardadas: {features_path}")
    
    # Guardar metadata
    metadata = {
        'entrenamiento_fecha': datetime.now().isoformat(),
        'version': '2.0-DATOS-REALES',
        'xgboost_version': xgb.__version__,
        'modelo_tipo': 'XGBRegressor',
        'features': feature_cols,
        'metricas': metrics
    }
    
    metadata_path = os.path.join(models_dir, "model_metadata.json")
    with open(metadata_path, 'w') as f:
        json.dump(metadata, f, indent=2)
    with open("./model_metadata.json", 'w') as f:
        json.dump(metadata, f, indent=2)
    logger.info(f"✅ Metadata guardada: {metadata_path}")

def main():
    """Función principal."""
    logger.info("=" * 70)
    logger.info("🚀 ENTRENAMIENTO DE MODELOS ML CON DATOS REALES")
    logger.info("=" * 70)
    
    try:
        # Cargar datos reales
        df = cargar_datos_reales()
        
        # Entrenar modelos
        regressor, ranker, feature_cols, metrics = entrenar_modelos(df)
        
        # Guardar modelos
        guardar_modelos(regressor, ranker, feature_cols, metrics)
        
        logger.info("=" * 70)
        logger.info("✅ MODELOS ENTRENADOS Y GUARDADOS EXITOSAMENTE")
        logger.info("=" * 70)
        logger.info(f"📊 Resumen de Métricas:")
        logger.info(f"   Regressor - R²: {metrics['regressor_metrics']['r2_score']:.4f}")
        logger.info(f"   Ranker - R²: {metrics['ranker_metrics']['r2_score']:.4f}")
        logger.info(f"   Fuente de datos: {metrics['data_source']}")
        logger.info("=" * 70)
        
        return 0
        
    except Exception as e:
        logger.error(f"❌ Error durante el entrenamiento: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    import sys
    sys.exit(main())
