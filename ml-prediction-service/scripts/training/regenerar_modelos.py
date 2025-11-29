#!/usr/bin/env python3
"""
Script para regenerar los modelos XGBoost con el formato correcto.
Soluciona el error de carga del modelo ranker.
Compatible con Python 3.13
"""

import os
import json
import logging
import pandas as pd
import numpy as np
import xgboost as xgb
from datetime import datetime, timedelta
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
import warnings

# Suprimir warnings de versiones
warnings.filterwarnings('ignore', category=FutureWarning)
warnings.filterwarnings('ignore', category=UserWarning)

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def generar_datos_sinteticos(n_samples=7300, n_productos=50):
    """
    Genera datos sintéticos realistas para entrenar los modelos.
    """
    logger.info(f"🔧 Generando {n_samples} muestras sintéticas para {n_productos} productos...")
    
    # Configurar semilla para reproducibilidad
    np.random.seed(42)
    
    # Fechas para el dataset (2 años de datos)
    start_date = datetime.now() - timedelta(days=730)
    dates = [start_date + timedelta(days=i) for i in range(n_samples)]
    
    data = []
    
    for i, fecha in enumerate(dates):
        # Simular entre 1 y 10 productos vendidos por día
        n_ventas_dia = np.random.randint(1, 11)
        
        for _ in range(n_ventas_dia):
            producto_id = f"PROD_{np.random.randint(1, n_productos+1):03d}"
            
            # Features básicas de tiempo
            dia_semana = fecha.weekday()  # 0=Monday, 6=Sunday
            dia_mes = fecha.day
            mes = fecha.month
            trimestre = (mes - 1) // 3 + 1
            semana_año = fecha.isocalendar()[1]
            
            # Features cíclicas
            dia_semana_sin = np.sin(2 * np.pi * dia_semana / 7)
            dia_semana_cos = np.cos(2 * np.pi * dia_semana / 7)
            mes_sin = np.sin(2 * np.pi * mes / 12)
            mes_cos = np.cos(2 * np.pi * mes / 12)
            
            # Features booleanas
            es_fin_semana = 1 if dia_semana >= 5 else 0
            es_lunes = 1 if dia_semana == 0 else 0
            es_viernes = 1 if dia_semana == 4 else 0
            
            # Simular feriados (aproximadamente 1 de cada 20 días)
            es_feriado = 1 if np.random.random() < 0.05 else 0
            dia_antes_feriado = 1 if np.random.random() < 0.05 else 0
            dia_despues_feriado = 1 if np.random.random() < 0.05 else 0
            
            # Features climáticas simuladas
            # Temperatura base con variación estacional
            dia_año = fecha.timetuple().tm_yday
            temp_base = 20 + 10 * np.sin(2 * np.pi * dia_año / 365)
            temperatura_simulada = temp_base + np.random.normal(0, 5)
            
            lluvia_simulada = 1 if np.random.random() < 0.3 else 0
            clima_caluroso = 1 if temperatura_simulada > 25 else 0
            clima_frio = 1 if temperatura_simulada < 15 else 0
            
            # Precio de venta base por producto (simulado)
            precio_base = 10 + (hash(producto_id) % 100)  # Entre 10 y 110
            precio_venta = precio_base + np.random.normal(0, 5)
            
            # Cantidad vendida (variable objetivo)
            # Factores que influyen en las ventas
            factor_dia_semana = 1.5 if es_fin_semana else 1.0
            factor_clima = 1.2 if lluvia_simulada else 1.0
            factor_feriado = 1.8 if es_feriado else 1.0
            factor_precio = max(0.5, 2.0 - (precio_venta / 100))  # Menos ventas con precio alto
            
            cantidad_base = np.random.poisson(10)  # Base de 10 unidades
            cantidad_total = max(1, int(cantidad_base * factor_dia_semana * factor_clima * factor_feriado * factor_precio))
            
            # Prioridad de compra (objetivo para el ranker)
            # Basada en la cantidad vendida y otros factores
            prioridad_score = min(5.0, cantidad_total / 10.0 + np.random.normal(0, 0.5))
            prioridad_score = max(0.0, prioridad_score)  # Entre 0 y 5
            
            data.append({
                'fecha_orden': fecha.strftime('%Y-%m-%d'),
                'productos_id': producto_id,
                'precio_venta': precio_venta,
                'cantidad_total': cantidad_total,
                'prioridad_score': prioridad_score,
                
                # Features para el modelo
                'dia_semana': dia_semana,
                'dia_mes': dia_mes,
                'mes': mes,
                'trimestre': trimestre,
                'semana_año': semana_año,
                'dia_semana_sin': dia_semana_sin,
                'dia_semana_cos': dia_semana_cos,
                'mes_sin': mes_sin,
                'mes_cos': mes_cos,
                'es_fin_semana': es_fin_semana,
                'es_lunes': es_lunes,
                'es_viernes': es_viernes,
                'es_feriado': es_feriado,
                'dia_antes_feriado': dia_antes_feriado,
                'dia_despues_feriado': dia_despues_feriado,
                'temperatura_simulada': temperatura_simulada,
                'lluvia_simulada': lluvia_simulada,
                'clima_caluroso': clima_caluroso,
                'clima_frio': clima_frio,
            })
    
    df = pd.DataFrame(data)
    
    # Agregar features de lag y medias móviles
    df = df.sort_values(['productos_id', 'fecha_orden'])
    
    # Features de lag (valores históricos)
    for lag in [7, 14, 30]:
        df[f'lag_cantidad_{lag}d'] = df.groupby('productos_id')['cantidad_total'].shift(lag).fillna(0)
    
    # Medias móviles
    for ventana in [7, 14, 30]:
        df[f'media_movil_{ventana}d'] = (
            df.groupby('productos_id')['cantidad_total']
            .shift(1)
            .rolling(window=ventana, min_periods=1)
            .mean()
            .fillna(0)
        )
    
    # Features derivadas
    df['tendencia_7d'] = df['lag_cantidad_7d'] - df['media_movil_7d']
    df['volatilidad_7d'] = (
        df.groupby('productos_id')['cantidad_total']
        .shift(1)
        .rolling(window=7, min_periods=1)
        .std()
        .fillna(0)
    )
    
    # Features de estación
    estaciones = []
    for _, row in df.iterrows():
        mes = row['mes']
        if mes in [12, 1, 2]:
            estacion = 'invierno'
        elif mes in [3, 4, 5]:
            estacion = 'primavera'
        elif mes in [6, 7, 8]:
            estacion = 'verano'
        else:
            estacion = 'otoño'
        estaciones.append(estacion)
    
    df['estacion'] = estaciones
    
    # One-hot encoding para estaciones
    df_estaciones = pd.get_dummies(df['estacion'], prefix='estacion')
    df = pd.concat([df, df_estaciones], axis=1)
    
    logger.info(f"✅ Datos sintéticos generados: {len(df)} registros")
    return df

def entrenar_modelos(df):
    """
    Entrena los modelos XGBoost regressor y ranker.
    """
    logger.info("🧠 Entrenando modelos XGBoost...")
    
    # Features para los modelos (excluir columnas no numéricas y objetivos)
    feature_cols = [
        'precio_venta', 'dia_semana', 'dia_mes', 'mes', 'trimestre', 'semana_año',
        'dia_semana_sin', 'dia_semana_cos', 'mes_sin', 'mes_cos',
        'es_fin_semana', 'es_lunes', 'es_viernes', 'es_feriado',
        'dia_antes_feriado', 'dia_despues_feriado', 'temperatura_simulada',
        'lluvia_simulada', 'clima_caluroso', 'clima_frio',
        'lag_cantidad_7d', 'lag_cantidad_14d', 'lag_cantidad_30d',
        'media_movil_7d', 'media_movil_14d', 'media_movil_30d',
        'tendencia_7d', 'volatilidad_7d', 'estacion_invierno',
        'estacion_otoño', 'estacion_primavera', 'estacion_verano'
    ]
    
    X = df[feature_cols].fillna(0)
    y_cantidad = df['cantidad_total']
    y_prioridad = df['prioridad_score']
    
    # División train/test
    X_train, X_test, y_cantidad_train, y_cantidad_test, y_prioridad_train, y_prioridad_test = \
        train_test_split(X, y_cantidad, y_prioridad, test_size=0.2, random_state=42)
    
    logger.info(f"📊 Datos de entrenamiento: {len(X_train)} registros")
    logger.info(f"📊 Datos de prueba: {len(X_test)} registros")
    
    # 1. Entrenar modelo regressor para cantidad
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
    
    # Evaluar regressor
    y_pred_cantidad = regressor.predict(X_test)
    rmse_cantidad = np.sqrt(mean_squared_error(y_cantidad_test, y_pred_cantidad))
    mae_cantidad = mean_absolute_error(y_cantidad_test, y_pred_cantidad)
    r2_cantidad = r2_score(y_cantidad_test, y_pred_cantidad)
    
    logger.info(f"📈 Regressor - RMSE: {rmse_cantidad:.4f}, MAE: {mae_cantidad:.4f}, R²: {r2_cantidad:.4f}")
    
    # 2. Entrenar modelo para prioridad (usando XGBRegressor en lugar de XGBRanker)
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
    
    # Evaluar ranker
    y_pred_prioridad = ranker.predict(X_test)
    rmse_prioridad = np.sqrt(mean_squared_error(y_prioridad_test, y_pred_prioridad))
    mae_prioridad = mean_absolute_error(y_prioridad_test, y_pred_prioridad)
    r2_prioridad = r2_score(y_prioridad_test, y_pred_prioridad)
    
    logger.info(f"📈 Ranker - RMSE: {rmse_prioridad:.4f}, MAE: {mae_prioridad:.4f}, R²: {r2_prioridad:.4f}")
    
    return regressor, ranker, feature_cols, {
        'regressor_metrics': {
            'rmse': rmse_cantidad,
            'mae': mae_cantidad,
            'r2_score': r2_cantidad
        },
        'ranker_metrics': {
            'rmse': rmse_prioridad,
            'mae': mae_prioridad,
            'r2_score': r2_prioridad
        },
        'training_samples': len(X_train),
        'test_samples': len(X_test),
        'features_count': len(feature_cols)
    }

def guardar_modelos(regressor, ranker, feature_cols, metrics, models_dir="./models"):
    """
    Guarda los modelos entrenados en formato JSON compatible.
    Los guarda tanto en ./models/ como en la raíz para compatibilidad.
    """
    logger.info(f"💾 Guardando modelos en {models_dir} y raíz...")
    
    # Crear directorio si no existe
    os.makedirs(models_dir, exist_ok=True)
    
    # Guardar regressor en ambas ubicaciones
    regressor_path_models = os.path.join(models_dir, "regressor_cantidad.json")
    regressor_path_root = "./regressor_cantidad.json"
    regressor.save_model(regressor_path_models)
    regressor.save_model(regressor_path_root)
    logger.info(f"✅ Regressor guardado: {regressor_path_models} y {regressor_path_root}")
    
    # Guardar ranker en ambas ubicaciones
    ranker_path_models = os.path.join(models_dir, "ranker_prioridad.json")
    ranker_path_root = "./ranker_prioridad.json"
    ranker.save_model(ranker_path_models)
    ranker.save_model(ranker_path_root)
    logger.info(f"✅ Ranker guardado: {ranker_path_models} y {ranker_path_root}")
    
    # Guardar lista de features en ambas ubicaciones
    features_path_models = os.path.join(models_dir, "model_features.txt")
    features_path_root = "./model_features.txt"
    
    for features_path in [features_path_models, features_path_root]:
        with open(features_path, 'w', encoding='utf-8') as f:
            for feature in feature_cols:
                f.write(f"{feature}\n")
    logger.info(f"✅ Features guardadas: {features_path_models} y {features_path_root}")
    
    # Guardar metadata en ambas ubicaciones
    metadata = {
        'created_date': datetime.now().isoformat(),
        'model_type': 'production_models',
        'xgboost_version': xgb.__version__,
        'python_version': f"{os.sys.version_info.major}.{os.sys.version_info.minor}.{os.sys.version_info.micro}",
        'features': feature_cols,
        'metrics': metrics,
        'model_files': {
            'regressor': 'regressor_cantidad.json',
            'ranker': 'ranker_prioridad.json',
            'features': 'model_features.txt'
        }
    }
    
    metadata_path_models = os.path.join(models_dir, "model_metadata.json")
    metadata_path_root = "./model_metadata.json"
    
    for metadata_path in [metadata_path_models, metadata_path_root]:
        with open(metadata_path, 'w', encoding='utf-8') as f:
            json.dump(metadata, f, indent=2, ensure_ascii=False)
    logger.info(f"✅ Metadata guardada: {metadata_path_models} y {metadata_path_root}")
    
    return {
        'regressor_path': regressor_path_root,
        'ranker_path': ranker_path_root,
        'features_path': features_path_root,
        'metadata_path': metadata_path_root
    }

def main():
    """
    Función principal que regenera los modelos.
    """
    logger.info("🚀 Iniciando regeneración de modelos XGBoost...")
    logger.info(f"📦 Versión de XGBoost: {xgb.__version__}")
    
    try:
        # 1. Generar datos sintéticos
        df = generar_datos_sinteticos()
        
        # 2. Entrenar modelos
        regressor, ranker, feature_cols, metrics = entrenar_modelos(df)
        
        # 3. Guardar modelos
        paths = guardar_modelos(regressor, ranker, feature_cols, metrics)
        
        logger.info("🎉 ¡Modelos regenerados exitosamente!")
        logger.info("📁 Archivos generados:")
        for key, path in paths.items():
            logger.info(f"   {key}: {path}")
        logger.info("💡 Ahora puedes reiniciar el contenedor para usar los nuevos modelos.")
        
    except Exception as e:
        logger.error(f"❌ Error durante la regeneración: {e}")
        raise

if __name__ == "__main__":
    main()