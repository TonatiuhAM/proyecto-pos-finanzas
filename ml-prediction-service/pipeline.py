"""
Pipeline de procesamiento de datos mejorado para el sistema de predicción.
Incluye feature engineering, limpieza de datos y preparación para modelos ML.
"""

import pandas as pd
import numpy as np
import holidays
from datetime import datetime, timedelta
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)

def obtener_datos_climaticos(fecha: pd.Timestamp) -> Dict[str, float]:
    """
    Función placeholder para obtener datos climáticos.
    En producción, se conectaría a una API meteorológica real.
    """
    # Simulación de datos climáticos basados en la fecha
    dia_anio = fecha.dayofyear
    temp_base = 20 + 10 * np.sin(2 * np.pi * dia_anio / 365)  # Ciclo anual
    
    # Agregar algo de variación aleatoria pero reproducible
    np.random.seed(fecha.year * 1000 + dia_anio)
    
    return {
        'temperatura_max': temp_base + np.random.normal(0, 3),
        'temperatura_min': temp_base - 5 + np.random.normal(0, 2),
        'humedad': 50 + 30 * np.sin(2 * np.pi * dia_anio / 365) + np.random.normal(0, 5),
        'precipitacion': max(0, np.random.exponential(2) if np.random.random() < 0.3 else 0),
        'viento_velocidad': 5 + np.random.exponential(3),
        'presion_atmosferica': 1013 + np.random.normal(0, 10)
    }

def agregar_features_fecha(df: pd.DataFrame, columna_fecha: str = 'fecha_orden') -> pd.DataFrame:
    """
    Agrega features derivadas de fechas al DataFrame.
    """
    df = df.copy()
    fecha_col = pd.to_datetime(df[columna_fecha])
    
    # Features básicas de tiempo
    df['dia_de_la_semana'] = fecha_col.dt.dayofweek  # 0=Lunes, 6=Domingo
    df['es_fin_de_semana'] = df['dia_de_la_semana'].isin([5, 6]).astype(int)
    df['dia_del_mes'] = fecha_col.dt.day
    df['mes'] = fecha_col.dt.month
    df['trimestre'] = fecha_col.dt.quarter
    df['anio'] = fecha_col.dt.year
    df['semana_del_anio'] = fecha_col.dt.isocalendar().week
    
    # Estación del año (hemisferio norte)
    def obtener_estacion(mes):
        if mes in [12, 1, 2]:
            return 'invierno'
        elif mes in [3, 4, 5]:
            return 'primavera'
        elif mes in [6, 7, 8]:
            return 'verano'
        else:
            return 'otoño'
    
    df['estacion_del_año'] = df['mes'].apply(obtener_estacion)
    
    # Features cíclicas (para capturar la naturaleza cíclica del tiempo)
    df['sin_dia_semana'] = np.sin(2 * np.pi * df['dia_de_la_semana'] / 7)
    df['cos_dia_semana'] = np.cos(2 * np.pi * df['dia_de_la_semana'] / 7)
    df['sin_mes'] = np.sin(2 * np.pi * df['mes'] / 12)
    df['cos_mes'] = np.cos(2 * np.pi * df['mes'] / 12)
    df['sin_dia_anio'] = np.sin(2 * np.pi * fecha_col.dt.dayofyear / 365)
    df['cos_dia_anio'] = np.cos(2 * np.pi * fecha_col.dt.dayofyear / 365)
    
    return df

def agregar_features_feriados(df: pd.DataFrame, columna_fecha: str = 'fecha_orden') -> pd.DataFrame:
    """
    Agrega features relacionadas con días festivos usando la librería holidays.
    """
    df = df.copy()
    fecha_col = pd.to_datetime(df[columna_fecha])
    
    # Feriados de México
    mx_holidays = holidays.Mexico()
    
    # Verificar si es día festivo
    df['es_feriado'] = fecha_col.apply(lambda x: x.date() in mx_holidays).astype(int)
    
    # Días antes y después de feriados
    df['dias_hasta_feriado'] = 0
    df['dias_desde_feriado'] = 0
    
    for idx, fecha in enumerate(fecha_col):
        fecha_actual = fecha.date()
        
        # Buscar el próximo feriado
        for dias in range(1, 15):  # Buscar hasta 14 días adelante
            fecha_futura = fecha_actual + timedelta(days=dias)
            if fecha_futura in mx_holidays:
                df.loc[idx, 'dias_hasta_feriado'] = dias
                break
        
        # Buscar el feriado anterior
        for dias in range(1, 15):  # Buscar hasta 14 días atrás
            fecha_pasada = fecha_actual - timedelta(days=dias)
            if fecha_pasada in mx_holidays:
                df.loc[idx, 'dias_desde_feriado'] = dias
                break
    
    # Features adicionales de proximidad a feriados
    df['cerca_de_feriado'] = ((df['dias_hasta_feriado'] > 0) & (df['dias_hasta_feriado'] <= 3) |
                              (df['dias_desde_feriado'] > 0) & (df['dias_desde_feriado'] <= 3)).astype(int)
    
    return df

def agregar_features_climaticas(df: pd.DataFrame, columna_fecha: str = 'fecha_orden') -> pd.DataFrame:
    """
    Agrega features climáticas simuladas al DataFrame.
    """
    df = df.copy()
    fecha_col = pd.to_datetime(df[columna_fecha])
    
    # Obtener datos climáticos para cada fecha única
    fechas_unicas = fecha_col.dt.date.unique()
    clima_dict = {}
    
    for fecha in fechas_unicas:
        fecha_pd = pd.Timestamp(fecha)
        clima_dict[fecha] = obtener_datos_climaticos(fecha_pd)
    
    # Agregar features climáticas al DataFrame
    clima_features = ['temperatura_max', 'temperatura_min', 'humedad', 
                      'precipitacion', 'viento_velocidad', 'presion_atmosferica']
    
    for feature in clima_features:
        df[f'clima_{feature}'] = fecha_col.dt.date.map(
            lambda x: clima_dict[x][feature]
        )
    
    # Features climáticas derivadas
    df['clima_rango_temperatura'] = df['clima_temperatura_max'] - df['clima_temperatura_min']
    df['clima_hay_lluvia'] = (df['clima_precipitacion'] > 0).astype(int)
    df['clima_lluvia_intensa'] = (df['clima_precipitacion'] > 10).astype(int)
    df['clima_viento_fuerte'] = (df['clima_viento_velocidad'] > 15).astype(int)
    
    return df

def agregar_features_lag(df: pd.DataFrame, 
                        columna_producto: str = 'productos_id',
                        columna_objetivo: str = 'cantidad_total',
                        lags: list = [1, 7, 14, 30]) -> pd.DataFrame:
    """
    Agrega features de lag (valores históricos) por producto.
    """
    df = df.copy()
    df = df.sort_values(['productos_id', 'fecha_orden'])
    
    for lag in lags:
        df[f'lag_{columna_objetivo}_{lag}d'] = df.groupby(columna_producto)[columna_objetivo].shift(lag)
    
    return df

def agregar_features_media_movil(df: pd.DataFrame,
                                columna_producto: str = 'productos_id',
                                columna_objetivo: str = 'cantidad_total',
                                ventanas: list = [7, 14, 30]) -> pd.DataFrame:
    """
    Agrega features de media móvil por producto.
    """
    df = df.copy()
    df = df.sort_values(['productos_id', 'fecha_orden'])
    
    for ventana in ventanas:
        # Media móvil (excluye el valor actual)
        df[f'media_movil_{columna_objetivo}_{ventana}d'] = (
            df.groupby(columna_producto)[columna_objetivo]
            .shift(1)
            .rolling(window=ventana, min_periods=1)
            .mean()
        )
        
        # Desviación estándar móvil
        df[f'std_movil_{columna_objetivo}_{ventana}d'] = (
            df.groupby(columna_producto)[columna_objetivo]
            .shift(1)
            .rolling(window=ventana, min_periods=1)
            .std()
        )
    
    return df

def process_data(df: pd.DataFrame) -> pd.DataFrame:
    """
    Pipeline principal de procesamiento de datos.
    Aplica todas las transformaciones y feature engineering.
    
    Args:
        df: DataFrame con columnas mínimas: fecha_orden, productos_id, cantidad_pz
        
    Returns:
        DataFrame procesado listo para modelos ML
    """
    logger.info("🚀 Iniciando pipeline de procesamiento de datos...")
    
    # 1. Validación inicial
    columnas_requeridas = ['fecha_orden', 'productos_id']
    for col in columnas_requeridas:
        if col not in df.columns:
            raise ValueError(f"Columna requerida '{col}' no encontrada en el DataFrame")
    
    # 2. Limpieza básica
    df = df.copy()
    df['fecha_orden'] = pd.to_datetime(df['fecha_orden'])
    df = df.dropna(subset=['fecha_orden', 'productos_id'])
    
    # Si no existe cantidad_total, asumir que viene como cantidad_pz
    if 'cantidad_total' not in df.columns:
        if 'cantidad_pz' in df.columns:
            df['cantidad_total'] = df['cantidad_pz']
        else:
            df['cantidad_total'] = 1  # Valor por defecto
    
    # 3. Agregación por día y producto (si hay múltiples registros por día)
    df_agregado = df.groupby(['fecha_orden', 'productos_id']).agg({
        'cantidad_total': 'sum',
        **{col: 'first' for col in df.columns if col not in ['fecha_orden', 'productos_id', 'cantidad_total']}
    }).reset_index()
    
    logger.info(f"📊 Datos agregados: {len(df_agregado)} registros")
    
    # 4. Feature Engineering
    logger.info("🔧 Aplicando feature engineering...")
    
    # Features de fecha
    df_procesado = agregar_features_fecha(df_agregado)
    
    # Features de feriados
    df_procesado = agregar_features_feriados(df_procesado)
    
    # Features climáticas
    df_procesado = agregar_features_climaticas(df_procesado)
    
    # Features de lag y media móvil
    df_procesado = agregar_features_lag(df_procesado)
    df_procesado = agregar_features_media_movil(df_procesado)
    
    # 5. Codificación de variables categóricas
    logger.info("🏷️ Codificando variables categóricas...")
    
    # One-hot encoding para estación del año
    df_procesado = pd.get_dummies(df_procesado, columns=['estacion_del_año'], prefix='estacion')
    
    # Preservar la columna productos_id original antes de encoding
    productos_id_original = df_procesado['productos_id'].copy()
    
    # Label encoding para productos (si hay muchos productos)
    if df_procesado['productos_id'].nunique() > 50:
        from sklearn.preprocessing import LabelEncoder
        le_producto = LabelEncoder()
        df_procesado['productos_id_encoded'] = le_producto.fit_transform(df_procesado['productos_id'])
        # Eliminar la columna original después de crear la codificada
        df_procesado = df_procesado.drop('productos_id', axis=1)
    else:
        df_procesado = pd.get_dummies(df_procesado, columns=['productos_id'], prefix='producto')
    
    # Restaurar la columna original para preservar la información
    df_procesado['productos_id'] = productos_id_original
    
    # 6. Manejo de valores faltantes
    logger.info("🧹 Limpiando valores faltantes...")
    
    # Rellenar NaN con 0 para features de lag y media móvil
    lag_cols = [col for col in df_procesado.columns if 'lag_' in col or 'media_movil_' in col or 'std_movil_' in col]
    df_procesado[lag_cols] = df_procesado[lag_cols].fillna(0)
    
    # Rellenar otros NaN
    df_procesado = df_procesado.fillna(method='ffill').fillna(0)
    
    # 7. Features adicionales de ingeniería
    logger.info("🎯 Creando features adicionales...")
    
    # Tendencia de ventas
    if 'lag_cantidad_total_7d' in df_procesado.columns and 'media_movil_cantidad_total_7d' in df_procesado.columns:
        df_procesado['tendencia_ventas'] = df_procesado['lag_cantidad_total_7d'] - df_procesado['media_movil_cantidad_total_7d']
    
    # Volatilidad de ventas
    if 'std_movil_cantidad_total_7d' in df_procesado.columns:
        df_procesado['volatilidad_ventas'] = df_procesado['std_movil_cantidad_total_7d'].fillna(0)
    
    # Interacciones importantes
    df_procesado['temperatura_x_humedad'] = df_procesado['clima_temperatura_max'] * df_procesado['clima_humedad'] / 100
    df_procesado['lluvia_x_fin_semana'] = df_procesado['clima_hay_lluvia'] * df_procesado['es_fin_de_semana']
    df_procesado['feriado_x_fin_semana'] = df_procesado['es_feriado'] * df_procesado['es_fin_de_semana']
    
    logger.info(f"✅ Pipeline completado. Forma final: {df_procesado.shape}")
    logger.info(f"📋 Features creadas: {len(df_procesado.columns)} columnas")
    
    return df_procesado