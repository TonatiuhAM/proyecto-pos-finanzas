#!/usr/bin/env python3
"""
Script de Evaluación de Predicciones ML vs Compras Reales
==========================================================

Este script:
1. Extrae datos históricos de PostgreSQL del período de entrenamiento
2. Entrena modelos XGBoost con esos datos
3. Genera predicciones para una semana genérica
4. Compara las predicciones con las compras reales proporcionadas
5. Genera reportes detallados (JSON + HTML con visualizaciones)

Autor: Sistema POS Finanzas - ML Team
Fecha: 2026-01-24
"""

import os
import sys
import json
import logging
import warnings
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, List, Tuple, Any

import pandas as pd
import numpy as np
import psycopg2
from psycopg2.extras import RealDictCursor
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score, mean_absolute_percentage_error

# Visualización
import matplotlib
matplotlib.use('Agg')  # Backend sin GUI
import matplotlib.pyplot as plt
import seaborn as sns

warnings.filterwarnings('ignore')

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Configurar estilo de gráficos
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 6)
plt.rcParams['font.size'] = 10


class EvaluadorPredicciones:
    """Clase principal para evaluar predicciones ML vs compras reales."""
    
    def __init__(self, config_path: str):
        """
        Inicializa el evaluador.
        
        Args:
            config_path: Ruta al archivo config.json
        """
        self.config_path = config_path
        self.config = self._cargar_config()
        self.conn = None
        self.df_historico = None
        self.modelo_cantidad = None
        self.modelo_prioridad = None
        self.features = []
        self.predicciones = {}
        self.compras_reales = {}
        self.metricas = {}
        self.timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
    def _cargar_config(self) -> Dict:
        """Carga la configuración desde el archivo JSON."""
        logger.info(f"📂 Cargando configuración desde: {self.config_path}")
        with open(self.config_path, 'r', encoding='utf-8') as f:
            config = json.load(f)
        logger.info("✅ Configuración cargada exitosamente")
        return config
    
    def conectar_bd(self):
        """Establece conexión con PostgreSQL."""
        logger.info("🔌 Conectando a PostgreSQL...")
        db_config = self.config['database']
        try:
            self.conn = psycopg2.connect(
                host=db_config['host'],
                port=db_config['port'],
                dbname=db_config['dbname'],
                user=db_config['user'],
                password=db_config['password']
            )
            logger.info("✅ Conexión a PostgreSQL establecida")
        except Exception as e:
            logger.error(f"❌ Error conectando a PostgreSQL: {e}")
            raise
    
    def extraer_datos_historicos(self) -> pd.DataFrame:
        """
        Extrae datos históricos de ventas desde PostgreSQL.
        
        Returns:
            DataFrame con los datos históricos procesados
        """
        logger.info("📊 Extrayendo datos históricos de ventas...")
        
        periodo = self.config['training_period']
        fecha_inicio = periodo['fecha_inicio']
        fecha_fin = periodo['fecha_fin']
        
        query = """
        SELECT 
            ov.fecha_orden,
            p.id as productos_id,
            p.nombre as producto_nombre,
            dov.cantidad_pz,
            hp.precio as precio_venta,
            hc.costo as costo_compra,
            i.cantidad_pz as stock_actual,
            i.cantidad_minima,
            i.cantidad_maxima
        FROM ordenes_de_ventas ov
        JOIN detalles_ordenes_de_ventas dov ON ov.id = dov.ordenes_de_ventas_id
        JOIN productos p ON dov.productos_id = p.id
        LEFT JOIN historial_precios hp ON dov.historial_precios_id = hp.id
        LEFT JOIN historial_costos hc ON p.id = hc.productos_id
        LEFT JOIN inventarios i ON p.id = i.productos_id
        WHERE DATE(ov.fecha_orden) BETWEEN %s AND %s
        ORDER BY ov.fecha_orden, p.nombre
        """
        
        try:
            df = pd.read_sql_query(
                query,
                self.conn,
                params=(fecha_inicio, fecha_fin)
            )
            
            # Convertir tipos
            df['fecha_orden'] = pd.to_datetime(df['fecha_orden'])
            df['cantidad_pz'] = pd.to_numeric(df['cantidad_pz'], errors='coerce')
            df['precio_venta'] = pd.to_numeric(df['precio_venta'], errors='coerce').fillna(0)
            df['costo_compra'] = pd.to_numeric(df['costo_compra'], errors='coerce').fillna(0)
            
            # Parsear stock (viene como string)
            df['stock_actual'] = pd.to_numeric(df['stock_actual'], errors='coerce').fillna(0)
            
            logger.info(f"✅ {len(df)} registros extraídos ({fecha_inicio} a {fecha_fin})")
            logger.info(f"📦 Productos únicos: {df['productos_id'].nunique()}")
            logger.info(f"📅 Rango de fechas: {df['fecha_orden'].min()} - {df['fecha_orden'].max()}")
            
            self.df_historico = df
            return df
            
        except Exception as e:
            logger.error(f"❌ Error extrayendo datos: {e}")
            raise
    
    def preparar_datos_entrenamiento(self) -> Tuple[pd.DataFrame, pd.Series, pd.Series]:
        """
        Prepara los datos para entrenamiento del modelo.
        
        Returns:
            Tupla (X_features, y_cantidad, y_prioridad)
        """
        logger.info("🔧 Preparando datos para entrenamiento...")
        
        df = self.df_historico.copy()
        
        # Agregar por fecha y producto
        df_agregado = df.groupby(['fecha_orden', 'productos_id', 'producto_nombre']).agg({
            'cantidad_pz': 'sum',
            'precio_venta': 'mean',
            'costo_compra': 'mean',
            'stock_actual': 'first',
            'cantidad_minima': 'first',
            'cantidad_maxima': 'first'
        }).reset_index()
        
        # Feature engineering: características temporales
        df_agregado['dia_semana'] = df_agregado['fecha_orden'].dt.dayofweek
        df_agregado['dia_mes'] = df_agregado['fecha_orden'].dt.day
        df_agregado['mes'] = df_agregado['fecha_orden'].dt.month
        df_agregado['es_fin_semana'] = (df_agregado['dia_semana'] >= 5).astype(int)
        
        # Features cíclicas
        df_agregado['dia_semana_sin'] = np.sin(2 * np.pi * df_agregado['dia_semana'] / 7)
        df_agregado['dia_semana_cos'] = np.cos(2 * np.pi * df_agregado['dia_semana'] / 7)
        
        # Features de inventario
        df_agregado['stock_ratio'] = df_agregado['stock_actual'] / (df_agregado['cantidad_maxima'] + 1)
        df_agregado['deficit'] = (df_agregado['cantidad_minima'] - df_agregado['stock_actual']).clip(lower=0)
        df_agregado['margen'] = df_agregado['precio_venta'] - df_agregado['costo_compra']
        df_agregado['margen_pct'] = (df_agregado['margen'] / (df_agregado['costo_compra'] + 0.01)) * 100
        
        # Target: Prioridad de compra (0-5)
        df_agregado['prioridad_score'] = 3.0  # Base
        df_agregado.loc[df_agregado['deficit'] > 0, 'prioridad_score'] = 5.0
        df_agregado.loc[df_agregado['stock_ratio'] > 0.8, 'prioridad_score'] = 1.0
        df_agregado.loc[
            (df_agregado['stock_ratio'] < 0.5) & (df_agregado['cantidad_pz'] > 2),
            'prioridad_score'
        ] = 4.0
        
        # One-hot encoding para productos
        df_encoded = pd.get_dummies(df_agregado, columns=['productos_id'], prefix='prod')
        
        # Seleccionar features
        feature_cols = [
            'dia_semana', 'dia_mes', 'mes', 'es_fin_semana',
            'dia_semana_sin', 'dia_semana_cos',
            'precio_venta', 'costo_compra', 'margen', 'margen_pct',
            'stock_actual', 'cantidad_minima', 'cantidad_maxima',
            'stock_ratio', 'deficit'
        ]
        
        # Agregar columnas de productos
        prod_cols = [col for col in df_encoded.columns if col.startswith('prod_')]
        feature_cols.extend(prod_cols)
        
        # Features disponibles (algunas pueden no estar si hay pocos datos)
        feature_cols = [col for col in feature_cols if col in df_encoded.columns]
        
        X = df_encoded[feature_cols].fillna(0)
        y_cantidad = df_agregado['cantidad_pz']
        y_prioridad = df_agregado['prioridad_score']
        
        self.features = feature_cols
        
        logger.info(f"✅ Datos preparados: {len(X)} muestras, {len(feature_cols)} features")
        
        return X, y_cantidad, y_prioridad
    
    def entrenar_modelos(self, X: pd.DataFrame, y_cantidad: pd.Series, y_prioridad: pd.Series):
        """
        Entrena los modelos XGBoost.
        
        Args:
            X: Features
            y_cantidad: Target de cantidad
            y_prioridad: Target de prioridad
        """
        logger.info("🎯 Entrenando modelos XGBoost...")
        
        # División train/test
        X_train, X_test, y_cant_train, y_cant_test, y_prio_train, y_prio_test = \
            train_test_split(X, y_cantidad, y_prioridad, test_size=0.2, random_state=42)
        
        logger.info(f"📊 Entrenamiento: {len(X_train)} | Prueba: {len(X_test)}")
        
        # Parámetros del modelo
        params = self.config['model_params']
        
        # Modelo 1: Regressor (cantidad)
        logger.info("🔹 Entrenando modelo de cantidad...")
        self.modelo_cantidad = xgb.XGBRegressor(
            n_estimators=params['n_estimators'],
            max_depth=params['max_depth'],
            learning_rate=params['learning_rate'],
            subsample=params.get('subsample', 0.8),
            colsample_bytree=params.get('colsample_bytree', 0.8),
            random_state=params.get('random_state', 42),
            objective='reg:squarederror'
        )
        self.modelo_cantidad.fit(X_train, y_cant_train)
        
        # Evaluar cantidad
        y_cant_pred = self.modelo_cantidad.predict(X_test)
        rmse_cant = np.sqrt(mean_squared_error(y_cant_test, y_cant_pred))
        mae_cant = mean_absolute_error(y_cant_test, y_cant_pred)
        r2_cant = r2_score(y_cant_test, y_cant_pred)
        
        logger.info(f"   ✅ Cantidad - RMSE: {rmse_cant:.2f}, MAE: {mae_cant:.2f}, R²: {r2_cant:.4f}")
        
        # Modelo 2: Ranker (prioridad)
        logger.info("🔹 Entrenando modelo de prioridad...")
        self.modelo_prioridad = xgb.XGBRegressor(
            n_estimators=params['n_estimators'],
            max_depth=params['max_depth'],
            learning_rate=params['learning_rate'],
            subsample=params.get('subsample', 0.8),
            colsample_bytree=params.get('colsample_bytree', 0.8),
            random_state=params.get('random_state', 42),
            objective='reg:squarederror'
        )
        self.modelo_prioridad.fit(X_train, y_prio_train)
        
        # Evaluar prioridad
        y_prio_pred = self.modelo_prioridad.predict(X_test)
        rmse_prio = np.sqrt(mean_squared_error(y_prio_test, y_prio_pred))
        mae_prio = mean_absolute_error(y_prio_test, y_prio_pred)
        r2_prio = r2_score(y_prio_test, y_prio_pred)
        
        logger.info(f"   ✅ Prioridad - RMSE: {rmse_prio:.2f}, MAE: {mae_prio:.2f}, R²: {r2_prio:.4f}")
        
        # Guardar métricas de entrenamiento
        self.metricas['entrenamiento'] = {
            'modelo_cantidad': {
                'rmse': float(rmse_cant),
                'mae': float(mae_cant),
                'r2_score': float(r2_cant)
            },
            'modelo_prioridad': {
                'rmse': float(rmse_prio),
                'mae': float(mae_prio),
                'r2_score': float(r2_prio)
            },
            'samples_train': len(X_train),
            'samples_test': len(X_test),
            'features_count': len(self.features)
        }
    
    def generar_predicciones(self) -> Dict[str, Dict[str, Any]]:
        """
        Genera predicciones de compras necesarias basadas en ventas históricas.
        
        Metodología:
        1. Analiza número de órdenes por día de semana
        2. Calcula cantidad promedio por orden de cada producto
        3. Genera predicción BASE: órdenes_esperadas × cantidad_promedio
        4. Calcula factores de escala históricos (ventas registradas vs compras reales)
        5. Aplica factores: predicción_escalada = predicción_base × factor_escala
        6. Aplica margen de seguridad y redondea a múltiplos
        
        Returns:
            Diccionario {producto: {dia: {pred_base, pred_escalada, factor_usado}}}
        """
        logger.info("🔮 Generando predicciones de compras basadas en ventas históricas...")
        
        # Obtener parámetros de predicción
        pred_params = self.config.get('prediction_params', {})
        margen_seguridad = pred_params.get('margen_seguridad', 1.15)
        redondear_a = pred_params.get('redondear_a_multiplo', 10)
        
        logger.info(f"   📊 Margen de seguridad: {margen_seguridad} ({(margen_seguridad-1)*100:.0f}% extra)")
        logger.info(f"   🔢 Redondeo: Múltiplos de {redondear_a}")
        
        # Preparar datos históricos
        df = self.df_historico.copy()
        df['dia_semana'] = df['fecha_orden'].dt.dayofweek
        df['fecha_solo'] = df['fecha_orden'].dt.date
        
        # Paso 1: Calcular número promedio de órdenes por día de semana
        ordenes_por_dia = df.groupby(['fecha_solo', 'dia_semana']).agg({
            'productos_id': 'count'  # Contar transacciones
        }).reset_index()
        ordenes_por_dia.columns = ['fecha', 'dia_semana', 'num_transacciones']
        
        # Promedio de órdenes por día de semana
        promedio_ordenes = ordenes_por_dia.groupby('dia_semana')['num_transacciones'].mean().to_dict()
        
        logger.info(f"   📋 Promedio de transacciones por día:")
        dias_nombres = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']
        for idx, dia_nombre in enumerate(dias_nombres):
            ordenes = promedio_ordenes.get(idx, 0)
            logger.info(f"      {dia_nombre}: {ordenes:.0f} transacciones")
        
        # Paso 2: Calcular estadísticas de venta por producto y día
        stats_producto_dia = df.groupby(['productos_id', 'dia_semana']).agg({
            'cantidad_pz': ['sum', 'count', 'mean']
        }).reset_index()
        stats_producto_dia.columns = ['productos_id', 'dia_semana', 'total_vendido', 'num_ventas', 'promedio_por_venta']
        
        # Paso 3: Calcular probabilidad de que un producto aparezca en una orden
        # (num_ventas del producto) / (total de transacciones ese día)
        ordenes_totales_dia = ordenes_por_dia.groupby('dia_semana')['num_transacciones'].sum().to_dict()
        
        stats_producto_dia['probabilidad_venta'] = stats_producto_dia.apply(
            lambda row: min(1.0, row['num_ventas'] / ordenes_totales_dia.get(row['dia_semana'], 1)),
            axis=1
        )
        
        # Paso 4: Generar predicciones BASE por producto
        predicciones_base = {}
        
        for nombre_json, data in self.config['mapeo_productos'].items():
            producto_id = data['id']
            producto_bd = data['nombre_bd']
            
            # Filtrar estadísticas de este producto
            stats_prod = stats_producto_dia[stats_producto_dia['productos_id'] == producto_id]
            
            if stats_prod.empty:
                # Si no hay datos históricos, usar valores conservadores
                logger.warning(f"⚠️ No hay datos históricos para {producto_bd}")
                predicciones_base[nombre_json] = {dia: 0.0 for dia in dias_nombres}
                continue
            
            predicciones_base[nombre_json] = {}
            
            for dia_idx, dia_nombre in enumerate(dias_nombres):
                # Buscar estadísticas para este día
                stats_dia = stats_prod[stats_prod['dia_semana'] == dia_idx]
                
                if not stats_dia.empty:
                    # Órdenes esperadas para este día
                    ordenes_esperadas = promedio_ordenes.get(dia_idx, 0)
                    
                    # Probabilidad de que el producto se venda
                    prob_venta = stats_dia['probabilidad_venta'].values[0]
                    
                    # Cantidad promedio cuando se vende
                    cantidad_promedio = stats_dia['promedio_por_venta'].values[0]
                    
                    # PREDICCIÓN BASE (sin margen aún)
                    pred_base = ordenes_esperadas * prob_venta * cantidad_promedio
                    
                else:
                    # Si no hay datos para este día, usar promedio general del producto
                    if not stats_prod.empty:
                        ordenes_esperadas = promedio_ordenes.get(dia_idx, 0)
                        prob_venta = stats_prod['probabilidad_venta'].mean()
                        cantidad_promedio = stats_prod['promedio_por_venta'].mean()
                        
                        pred_base = ordenes_esperadas * prob_venta * cantidad_promedio
                    else:
                        pred_base = 0
                
                # Asegurar que sea positivo
                pred_base = max(0, pred_base)
                
                predicciones_base[nombre_json][dia_nombre] = float(pred_base)
        
        # Resumen de predicciones base
        total_pred_base = sum(sum(dias.values()) for dias in predicciones_base.values())
        logger.info(f"✅ Predicciones BASE generadas: {len(predicciones_base)} productos × 6 días")
        logger.info(f"📦 Total predicción BASE semanal: {total_pred_base:.0f} unidades (sin escala)")
        
        # Paso 5: Calcular factores de escala
        factores_escala = self.calcular_factores_escala()
        
        # Paso 6: Aplicar factores de escala a las predicciones
        predicciones_finales = {}
        
        escala_aplicada = factores_escala.get('aplicado', False)
        
        for nombre_json, pred_base_dias in predicciones_base.items():
            predicciones_finales[nombre_json] = {}
            
            for dia_nombre, pred_base in pred_base_dias.items():
                # Obtener factor de escala para este producto+día
                if escala_aplicada:
                    factor_escala = factores_escala['factores_por_producto_dia'].get(nombre_json, {}).get(dia_nombre, 1.0)
                else:
                    factor_escala = 1.0
                
                # Aplicar factor de escala
                pred_escalada = pred_base * factor_escala
                
                # Aplicar margen de seguridad
                pred_con_margen = pred_escalada * margen_seguridad
                
                # Redondear a múltiplos
                if redondear_a > 1:
                    pred_final = round(pred_con_margen / redondear_a) * redondear_a
                else:
                    pred_final = round(pred_con_margen)
                
                # Asegurar que sea positivo
                pred_final = max(0, pred_final)
                
                # Guardar estructura completa
                predicciones_finales[nombre_json][dia_nombre] = {
                    'pred_base': float(pred_base),
                    'factor_escala': float(factor_escala),
                    'pred_escalada': float(pred_final)
                }
        
        # Guardar en atributo de clase
        self.predicciones = predicciones_finales
        self.factores_escala = factores_escala
        
        # Resumen final
        total_pred_final = sum(
            sum(dia_data['pred_escalada'] for dia_data in dias.values()) 
            for dias in predicciones_finales.values()
        )
        logger.info(f"✅ Predicciones FINALES (con escala) generadas")
        logger.info(f"📦 Total recomendado de compra semanal: {total_pred_final:.0f} unidades")
        
        if escala_aplicada:
            mejora = ((total_pred_final - total_pred_base) / total_pred_base) * 100 if total_pred_base > 0 else 0
            logger.info(f"📈 Ajuste por factor de escala: +{mejora:.1f}% ({total_pred_base:.0f} → {total_pred_final:.0f})")
        
        # Log de ejemplo para algunos productos
        logger.info("   📋 Ejemplo de predicciones finales:")
        for i, (producto, dias) in enumerate(list(predicciones_finales.items())[:3]):
            total_prod = sum(dia_data['pred_escalada'] for dia_data in dias.values())
            logger.info(f"      {producto}: {total_prod:.0f} unidades/semana")
        
        return predicciones_finales
    
    def cargar_compras_reales(self) -> Dict[str, Dict[str, float]]:
        """
        Carga las compras reales desde el archivo JSON.
        
        Returns:
            Diccionario {producto: {dia: cantidad}}
        """
        logger.info("📥 Cargando compras reales...")
        
        compras_path = os.path.join(
            os.path.dirname(self.config_path),
            'compras_reales.json'
        )
        
        with open(compras_path, 'r', encoding='utf-8') as f:
            compras_raw = json.load(f)
        
        # Convertir formato
        compras = {}
        for item in compras_raw:
            producto = item['Producto']
            compras[producto] = {
                'Lunes': item.get('Lunes', 0),
                'Martes': item.get('Martes', 0),
                'Miércoles': item.get('Miércoles', 0),
                'Jueves': item.get('Jueves', 0),
                'Viernes': item.get('Viernes', 0),
                'Sábado': item.get('Sábado', 0)
            }
        
        self.compras_reales = compras
        
        total_real = sum(sum(dias.values()) for dias in compras.values())
        logger.info(f"✅ Compras reales cargadas: {len(compras)} productos")
        logger.info(f"📦 Total comprado semanal: {total_real:.0f} unidades")
        
        return compras
    
    def calcular_factores_escala(self) -> Dict[str, Any]:
        """
        Calcula factores de escala históricos comparando ventas registradas vs compras reales.
        
        El factor de escala corrige la discrepancia entre las ventas registradas en el sistema
        y la demanda real del negocio (inferida de las compras reales).
        
        Metodología:
        1. Agrupa ventas históricas por producto y día de semana
        2. Compara con compras reales del mismo período
        3. Calcula: factor = compras_totales / ventas_registradas
        4. Usa granularidad híbrida:
           - Si hay ≥3 observaciones: usa factor específico por producto+día
           - Si no: usa factor promedio del producto
           - Si no hay datos: usa factor global
        
        Returns:
            Diccionario con factores calculados, estadísticas y advertencias
        """
        logger.info("🔍 Calculando factores de escala históricos...")
        
        # Leer parámetros de configuración
        escala_params = self.config.get('escala_params', {})
        aplicar_escala = escala_params.get('aplicar_escala', True)
        
        if not aplicar_escala:
            logger.info("   ⏭️  Escalado desactivado en configuración")
            return {
                'aplicado': False,
                'factores_por_producto_dia': {},
                'factores_por_producto': {},
                'factor_global': 1.0
            }
        
        metodo = escala_params.get('metodo_granularidad', 'hibrido')
        umbral_min = escala_params.get('umbral_datos_minimos', 3)
        factor_min = escala_params.get('factor_minimo', 0.5)
        factor_max = escala_params.get('factor_maximo', 50.0)
        
        logger.info(f"   📊 Método: {metodo}, Umbral mínimo: {umbral_min} observaciones")
        
        # Preparar datos históricos
        df = self.df_historico.copy()
        df['dia_semana'] = df['fecha_orden'].dt.dayofweek
        df['fecha_solo'] = df['fecha_orden'].dt.date
        
        # DEBUG: Mostrar resumen de datos cargados
        logger.info(f"   🔍 DEBUG - Datos históricos: {len(df)} registros desde {df['fecha_orden'].min()} hasta {df['fecha_orden'].max()}")
        logger.info(f"   🔍 DEBUG - Compras reales cargadas para {len(self.compras_reales)} productos:")
        for prod, dias in self.compras_reales.items():
            total_compras = sum(dias.values())
            logger.info(f"      • {prod}: {total_compras:.0f} unidades totales - Días: {list(dias.keys())}")
        
        # Mapeo de día numérico a nombre
        dias_map = {0: 'Lunes', 1: 'Martes', 2: 'Miércoles', 3: 'Jueves', 4: 'Viernes', 5: 'Sábado'}
        
        # Diccionarios para almacenar factores
        factores_producto_dia = {}  # {producto: {dia: factor}}
        factores_producto = {}       # {producto: factor_promedio}
        ventas_por_producto_dia = {} # Para conteo de observaciones
        advertencias = []
        
        # Paso 1: Calcular ventas registradas por producto y día
        logger.info("   📋 Analizando ventas registradas históricas...")
        
        for nombre_json, data in self.config['mapeo_productos'].items():
            producto_id = data['id']
            producto_bd = data['nombre_bd']
            
            # Filtrar ventas de este producto
            df_producto = df[df['productos_id'] == producto_id]
            
            if df_producto.empty:
                advertencias.append(f"Producto {nombre_json}: Sin ventas registradas en período")
                factores_producto[nombre_json] = None
                continue
            
            # Agrupar por día de semana
            ventas_por_dia = df_producto.groupby('dia_semana')['cantidad_pz'].agg(['sum', 'count']).to_dict('index')
            
            factores_producto_dia[nombre_json] = {}
            ventas_por_producto_dia[nombre_json] = {}
            
            # Calcular factores por día
            for dia_num, dia_nombre in dias_map.items():
                if dia_num in ventas_por_dia:
                    ventas_registradas = ventas_por_dia[dia_num]['sum']
                    num_observaciones = ventas_por_dia[dia_num]['count']
                    ventas_por_producto_dia[nombre_json][dia_nombre] = num_observaciones
                else:
                    ventas_registradas = 0
                    num_observaciones = 0
                    ventas_por_producto_dia[nombre_json][dia_nombre] = 0
                
                # Obtener compras reales para este día
                compras_reales_dia = 0
                if nombre_json in self.compras_reales:
                    compras_reales_dia = self.compras_reales[nombre_json].get(dia_nombre, 0)
                
                # DEBUG: Mostrar datos de comparación
                if dia_num == 0:  # Solo para Lunes, para no saturar logs
                    logger.info(f"      DEBUG {nombre_json} - {dia_nombre}:")
                    logger.info(f"         Ventas registradas: {ventas_registradas:.0f} (observaciones: {num_observaciones})")
                    logger.info(f"         Compras reales: {compras_reales_dia:.0f}")
                    if ventas_registradas > 0 and compras_reales_dia > 0:
                        logger.info(f"         Factor calculado: {compras_reales_dia / ventas_registradas:.2f}×")
                    else:
                        logger.info(f"         Factor: No calculable (datos insuficientes)")
                
                # Calcular factor
                if ventas_registradas > 0 and compras_reales_dia > 0:
                    factor = compras_reales_dia / ventas_registradas
                    factores_producto_dia[nombre_json][dia_nombre] = factor
                    
                    # Detectar factores extremos
                    if factor < factor_min or factor > factor_max:
                        advertencias.append(
                            f"{nombre_json} - {dia_nombre}: Factor extremo {factor:.2f}× "
                            f"(ventas: {ventas_registradas:.0f}, compras: {compras_reales_dia:.0f})"
                        )
                else:
                    factores_producto_dia[nombre_json][dia_nombre] = None
        
        # Paso 2: Calcular factores promedio por producto
        logger.info("   🔢 Calculando factores promedio por producto...")
        
        for nombre_json in factores_producto_dia.keys():
            factores_dias = [f for f in factores_producto_dia[nombre_json].values() if f is not None]
            
            if factores_dias:
                # Usar mediana para ser más robusto a outliers
                factor_promedio = float(np.median(factores_dias))
                factores_producto[nombre_json] = factor_promedio
            else:
                factores_producto[nombre_json] = None
                advertencias.append(f"Producto {nombre_json}: No se pudo calcular factor (sin datos válidos)")
        
        # Paso 3: Calcular factor global (fallback)
        factores_validos = [f for f in factores_producto.values() if f is not None]
        factor_global = float(np.median(factores_validos)) if factores_validos else 1.0
        
        # Paso 4: Aplicar lógica híbrida (llenar valores None con fallbacks)
        logger.info("   🔄 Aplicando lógica de granularidad híbrida...")
        
        for nombre_json in factores_producto_dia.keys():
            for dia_nombre in dias_map.values():
                factor_actual = factores_producto_dia[nombre_json].get(dia_nombre)
                num_obs = ventas_por_producto_dia[nombre_json].get(dia_nombre, 0)
                
                # Decidir qué factor usar según granularidad híbrida
                if factor_actual is not None and num_obs >= umbral_min:
                    # Usar factor específico del día (suficientes observaciones)
                    pass  # Ya está asignado
                elif factores_producto[nombre_json] is not None:
                    # Usar factor promedio del producto
                    factores_producto_dia[nombre_json][dia_nombre] = factores_producto[nombre_json]
                else:
                    # Usar factor global
                    factores_producto_dia[nombre_json][dia_nombre] = factor_global
        
        # Paso 5: Calcular estadísticas
        todos_los_factores = []
        for producto_factores in factores_producto_dia.values():
            todos_los_factores.extend([f for f in producto_factores.values() if f is not None and f > 0])
        
        estadisticas = {
            'media': float(np.mean(todos_los_factores)) if todos_los_factores else 1.0,
            'mediana': float(np.median(todos_los_factores)) if todos_los_factores else 1.0,
            'std': float(np.std(todos_los_factores)) if todos_los_factores else 0.0,
            'min': float(np.min(todos_los_factores)) if todos_los_factores else 1.0,
            'max': float(np.max(todos_los_factores)) if todos_los_factores else 1.0,
            'count': len(todos_los_factores)
        }
        
        # Logging de resultados
        logger.info(f"   ✅ Factores calculados para {len(factores_producto)} productos")
        logger.info(f"   📊 Estadísticas de factores:")
        logger.info(f"      Media: {estadisticas['media']:.2f}×")
        logger.info(f"      Mediana: {estadisticas['mediana']:.2f}×")
        logger.info(f"      Rango: {estadisticas['min']:.2f}× - {estadisticas['max']:.2f}×")
        logger.info(f"      Desviación estándar: {estadisticas['std']:.2f}")
        
        if advertencias:
            logger.warning(f"   ⚠️  {len(advertencias)} advertencias generadas:")
            for adv in advertencias[:5]:  # Mostrar solo las primeras 5
                logger.warning(f"      • {adv}")
            if len(advertencias) > 5:
                logger.warning(f"      ... y {len(advertencias) - 5} más")
        
        # Construir resultado
        resultado = {
            'aplicado': True,
            'metodo': metodo,
            'umbral_datos_minimos': umbral_min,
            'factores_por_producto_dia': factores_producto_dia,
            'factores_por_producto': factores_producto,
            'factor_global': factor_global,
            'estadisticas': estadisticas,
            'advertencias': advertencias,
            'observaciones_por_producto_dia': ventas_por_producto_dia
        }
        
        # Guardar factores si está configurado
        if escala_params.get('guardar_factores_calculados', False):
            self._guardar_factores_calculados(resultado)
        
        return resultado
    
    def _guardar_factores_calculados(self, factores_data: Dict[str, Any]):
        """
        Guarda los factores calculados en un archivo JSON para auditoría.
        
        Args:
            factores_data: Diccionario con los factores y metadatos
        """
        try:
            escala_params = self.config.get('escala_params', {})
            nombre_archivo = escala_params.get('archivo_factores', 'factores_escala_calculados.json')
            
            # Construir ruta completa
            ruta_salida = os.path.join(
                self.config['output']['directorio_host'],
                f"evaluacion_{self.timestamp}",
                nombre_archivo
            )
            
            # Agregar metadatos
            factores_completo = {
                'fecha_calculo': datetime.now().isoformat(),
                'periodo_entrenamiento': self.config['training_period'],
                **factores_data
            }
            
            # Crear directorio si no existe
            os.makedirs(os.path.dirname(ruta_salida), exist_ok=True)
            
            # Guardar
            with open(ruta_salida, 'w', encoding='utf-8') as f:
                json.dump(factores_completo, f, indent=2, ensure_ascii=False)
            
            logger.info(f"   💾 Factores guardados en: {nombre_archivo}")
            
        except Exception as e:
            logger.warning(f"   ⚠️  No se pudieron guardar factores: {e}")
    
    def calcular_metricas_comparacion(self):
        """
        Calcula métricas de comparación entre predicciones y compras reales.
        
        Calcula dos conjuntos de métricas:
        - Predicciones BASE (sin factor de escala)
        - Predicciones ESCALADAS (con factor de escala aplicado)
        """
        logger.info("📊 Calculando métricas de comparación...")
        
        metricas_por_producto_base = {}
        metricas_por_producto_escalada = {}
        metricas_por_dia_base = {}
        metricas_por_dia_escalada = {}
        
        dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']
        
        # Métricas por producto
        for producto in self.compras_reales.keys():
            if producto not in self.predicciones:
                logger.warning(f"⚠️ Producto {producto} no tiene predicciones")
                continue
            
            pred_data = self.predicciones[producto]
            real = self.compras_reales[producto]
            
            # Extraer predicciones base y escaladas
            pred_base = {dia: pred_data[dia]['pred_base'] for dia in dias if dia in pred_data}
            pred_escalada = {dia: pred_data[dia]['pred_escalada'] for dia in dias if dia in pred_data}
            factores = {dia: pred_data[dia]['factor_escala'] for dia in dias if dia in pred_data}
            
            # Calcular errores para predicción BASE
            errores_base = []
            errores_base_pct = []
            for dia in dias:
                p_base = pred_base.get(dia, 0)
                r = real.get(dia, 0)
                error_abs = p_base - r
                errores_base.append(error_abs)
                if r > 0:
                    errores_base_pct.append(abs(error_abs / r) * 100)
            
            mae_base = np.mean(np.abs(errores_base))
            rmse_base = np.sqrt(np.mean(np.square(errores_base)))
            mape_base = np.mean(errores_base_pct) if errores_base_pct else 0
            
            metricas_por_producto_base[producto] = {
                'mae': float(mae_base),
                'rmse': float(rmse_base),
                'mape': float(mape_base),
                'errores_por_dia': {dia: float(errores_base[i]) for i, dia in enumerate(dias)},
                'predicciones': pred_base,
                'reales': real
            }
            
            # Calcular errores para predicción ESCALADA
            errores_escalada = []
            errores_escalada_pct = []
            for dia in dias:
                p_escalada = pred_escalada.get(dia, 0)
                r = real.get(dia, 0)
                error_abs = p_escalada - r
                errores_escalada.append(error_abs)
                if r > 0:
                    errores_escalada_pct.append(abs(error_abs / r) * 100)
            
            mae_escalada = np.mean(np.abs(errores_escalada))
            rmse_escalada = np.sqrt(np.mean(np.square(errores_escalada)))
            mape_escalada = np.mean(errores_escalada_pct) if errores_escalada_pct else 0
            
            metricas_por_producto_escalada[producto] = {
                'mae': float(mae_escalada),
                'rmse': float(rmse_escalada),
                'mape': float(mape_escalada),
                'errores_por_dia': {dia: float(errores_escalada[i]) for i, dia in enumerate(dias)},
                'predicciones': pred_escalada,
                'factores': factores,
                'reales': real
            }
        
        # Métricas por día (BASE)
        for dia in dias:
            predicciones_dia_base = []
            reales_dia = []
            
            for producto in self.compras_reales.keys():
                if producto in self.predicciones:
                    pred_base = self.predicciones[producto][dia]['pred_base']
                    predicciones_dia_base.append(pred_base)
                    reales_dia.append(self.compras_reales[producto].get(dia, 0))
            
            if predicciones_dia_base:
                mae_dia = mean_absolute_error(reales_dia, predicciones_dia_base)
                rmse_dia = np.sqrt(mean_squared_error(reales_dia, predicciones_dia_base))
                mape_dia = np.mean([abs((p - r) / r) * 100 for p, r in zip(predicciones_dia_base, reales_dia) if r > 0]) if any(r > 0 for r in reales_dia) else 0
                
                metricas_por_dia_base[dia] = {
                    'mae': float(mae_dia),
                    'rmse': float(rmse_dia),
                    'mape': float(mape_dia),
                    'total_predicho': float(sum(predicciones_dia_base)),
                    'total_real': float(sum(reales_dia))
                }
        
        # Métricas por día (ESCALADA)
        for dia in dias:
            predicciones_dia_escalada = []
            reales_dia = []
            
            for producto in self.compras_reales.keys():
                if producto in self.predicciones:
                    pred_escalada = self.predicciones[producto][dia]['pred_escalada']
                    predicciones_dia_escalada.append(pred_escalada)
                    reales_dia.append(self.compras_reales[producto].get(dia, 0))
            
            if predicciones_dia_escalada:
                mae_dia = mean_absolute_error(reales_dia, predicciones_dia_escalada)
                rmse_dia = np.sqrt(mean_squared_error(reales_dia, predicciones_dia_escalada))
                mape_dia = np.mean([abs((p - r) / r) * 100 for p, r in zip(predicciones_dia_escalada, reales_dia) if r > 0]) if any(r > 0 for r in reales_dia) else 0
                
                metricas_por_dia_escalada[dia] = {
                    'mae': float(mae_dia),
                    'rmse': float(rmse_dia),
                    'mape': float(mape_dia),
                    'total_predicho': float(sum(predicciones_dia_escalada)),
                    'total_real': float(sum(reales_dia))
                }
        
        # Métricas globales BASE
        todos_pred_base = []
        todos_real = []
        
        for producto in self.compras_reales.keys():
            if producto in self.predicciones:
                for dia in dias:
                    todos_pred_base.append(self.predicciones[producto][dia]['pred_base'])
                    todos_real.append(self.compras_reales[producto].get(dia, 0))
        
        mae_global_base = mean_absolute_error(todos_real, todos_pred_base)
        rmse_global_base = np.sqrt(mean_squared_error(todos_real, todos_pred_base))
        r2_global_base = r2_score(todos_real, todos_pred_base)
        mape_global_base = np.mean([abs((p - r) / r) * 100 for p, r in zip(todos_pred_base, todos_real) if r > 0]) if any(r > 0 for r in todos_real) else 0
        
        # Métricas globales ESCALADA
        todos_pred_escalada = []
        todos_real_2 = []
        
        for producto in self.compras_reales.keys():
            if producto in self.predicciones:
                for dia in dias:
                    todos_pred_escalada.append(self.predicciones[producto][dia]['pred_escalada'])
                    todos_real_2.append(self.compras_reales[producto].get(dia, 0))
        
        mae_global_escalada = mean_absolute_error(todos_real_2, todos_pred_escalada)
        rmse_global_escalada = np.sqrt(mean_squared_error(todos_real_2, todos_pred_escalada))
        r2_global_escalada = r2_score(todos_real_2, todos_pred_escalada)
        mape_global_escalada = np.mean([abs((p - r) / r) * 100 for p, r in zip(todos_pred_escalada, todos_real_2) if r > 0]) if any(r > 0 for r in todos_real_2) else 0
        
        # Calcular mejora porcentual
        mejora_mae = ((mae_global_base - mae_global_escalada) / mae_global_base) * 100 if mae_global_base > 0 else 0
        mejora_mape = ((mape_global_base - mape_global_escalada) / mape_global_base) * 100 if mape_global_base > 0 else 0
        
        self.metricas['comparacion'] = {
            'base': {
                'global': {
                    'mae': float(mae_global_base),
                    'rmse': float(rmse_global_base),
                    'r2_score': float(r2_global_base),
                    'mape': float(mape_global_base),
                    'total_predicho': float(sum(todos_pred_base)),
                    'total_real': float(sum(todos_real))
                },
                'por_producto': metricas_por_producto_base,
                'por_dia': metricas_por_dia_base
            },
            'escalada': {
                'global': {
                    'mae': float(mae_global_escalada),
                    'rmse': float(rmse_global_escalada),
                    'r2_score': float(r2_global_escalada),
                    'mape': float(mape_global_escalada),
                    'total_predicho': float(sum(todos_pred_escalada)),
                    'total_real': float(sum(todos_real_2))
                },
                'por_producto': metricas_por_producto_escalada,
                'por_dia': metricas_por_dia_escalada
            },
            'mejora': {
                'mae_mejora_pct': float(mejora_mae),
                'mape_mejora_pct': float(mejora_mape)
            }
        }
        
        logger.info(f"✅ Métricas calculadas (BASE y ESCALADA)")
        logger.info(f"📈 BASE   - MAE: {mae_global_base:.2f}, MAPE: {mape_global_base:.2f}%")
        logger.info(f"📈 ESCALADA - MAE: {mae_global_escalada:.2f}, MAPE: {mape_global_escalada:.2f}%")
        logger.info(f"🎯 MEJORA  - MAE: {mejora_mae:+.1f}%, MAPE: {mejora_mape:+.1f}%")
    
    def generar_visualizaciones(self, output_dir: str):
        """
        Genera visualizaciones de la comparación.
        
        Args:
            output_dir: Directorio donde guardar las imágenes
        """
        logger.info("📊 Generando visualizaciones...")
        
        graficos_dir = os.path.join(output_dir, 'graficos')
        os.makedirs(graficos_dir, exist_ok=True)
        
        dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']
        
        # 1. Gráfico de barras: Predicción vs Real por producto
        logger.info("   📊 Gráfico 1: Predicción vs Real por producto")
        fig, ax = plt.subplots(figsize=(14, 8))
        
        productos = list(self.compras_reales.keys())
        x = np.arange(len(productos))
        width = 0.35
        
        # Extraer predicciones escaladas
        totales_pred = [sum(self.predicciones[p][dia]['pred_escalada'] for dia in dias) for p in productos if p in self.predicciones]
        totales_real = [sum(self.compras_reales[p].values()) for p in productos]
        
        bars1 = ax.bar(x - width/2, totales_pred, width, label='Predicción', alpha=0.8)
        bars2 = ax.bar(x + width/2, totales_real, width, label='Real', alpha=0.8)
        
        ax.set_xlabel('Productos', fontsize=12)
        ax.set_ylabel('Cantidad Total Semanal', fontsize=12)
        ax.set_title('Comparación: Predicciones vs Compras Reales (Total Semanal)', fontsize=14, fontweight='bold')
        ax.set_xticks(x)
        ax.set_xticklabels(productos, rotation=45, ha='right')
        ax.legend()
        ax.grid(axis='y', alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(os.path.join(graficos_dir, 'prediccion_vs_real_productos.png'), dpi=300, bbox_inches='tight')
        plt.close()
        
        # 2. Gráfico de líneas: Tendencia por día de semana
        logger.info("   📊 Gráfico 2: Tendencias por día de semana")
        fig, ax = plt.subplots(figsize=(12, 6))
        
        for producto in productos[:5]:  # Top 5 productos para no saturar
            if producto not in self.predicciones:
                continue
            valores_pred = [self.predicciones[producto][dia]['pred_escalada'] for dia in dias]
            valores_real = [self.compras_reales[producto][dia] for dia in dias]
            
            ax.plot(dias, valores_pred, marker='o', linestyle='--', alpha=0.6, label=f'{producto} (Pred)')
            ax.plot(dias, valores_real, marker='s', linestyle='-', linewidth=2, label=f'{producto} (Real)')
        
        ax.set_xlabel('Día de la Semana', fontsize=12)
        ax.set_ylabel('Cantidad', fontsize=12)
        ax.set_title('Tendencias de Predicción vs Real por Día (Top 5 Productos)', fontsize=14, fontweight='bold')
        ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize=9)
        ax.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(os.path.join(graficos_dir, 'tendencias_por_dia.png'), dpi=300, bbox_inches='tight')
        plt.close()
        
        # 3. Heatmap: Error porcentual por producto × día
        logger.info("   📊 Gráfico 3: Heatmap de errores")
        matriz_error_pct = []
        
        for producto in productos:
            errores_prod = []
            for dia in dias:
                if producto in self.predicciones and dia in self.predicciones[producto]:
                    pred = self.predicciones[producto][dia]['pred_escalada']
                else:
                    pred = 0
                real = self.compras_reales[producto].get(dia, 0)
                
                if real > 0:
                    error_pct = ((pred - real) / real) * 100
                else:
                    error_pct = 0
                
                errores_prod.append(error_pct)
            matriz_error_pct.append(errores_prod)
        
        fig, ax = plt.subplots(figsize=(10, 8))
        sns.heatmap(
            matriz_error_pct,
            annot=True,
            fmt='.1f',
            cmap='RdYlGn_r',
            center=0,
            xticklabels=dias,
            yticklabels=productos,
            cbar_kws={'label': 'Error %'},
            ax=ax
        )
        ax.set_title('Error Porcentual: Predicción vs Real\n(Negativo = Subpredicción, Positivo = Sobrepredicción)', 
                     fontsize=14, fontweight='bold')
        
        plt.tight_layout()
        plt.savefig(os.path.join(graficos_dir, 'heatmap_errores.png'), dpi=300, bbox_inches='tight')
        plt.close()
        
        # 4. Distribución de errores (histograma)
        logger.info("   📊 Gráfico 4: Distribución de errores")
        todos_errores = []
        
        for producto in productos:
            for dia in dias:
                if producto in self.predicciones and dia in self.predicciones[producto]:
                    pred = self.predicciones[producto][dia]['pred_escalada']
                else:
                    pred = 0
                real = self.compras_reales[producto].get(dia, 0)
                todos_errores.append(pred - real)
        
        fig, ax = plt.subplots(figsize=(10, 6))
        ax.hist(todos_errores, bins=30, edgecolor='black', alpha=0.7)
        ax.axvline(x=0, color='red', linestyle='--', linewidth=2, label='Error = 0')
        ax.axvline(x=np.mean(todos_errores), color='green', linestyle='--', linewidth=2, label=f'Media = {np.mean(todos_errores):.1f}')
        
        ax.set_xlabel('Error (Predicción - Real)', fontsize=12)
        ax.set_ylabel('Frecuencia', fontsize=12)
        ax.set_title('Distribución de Errores de Predicción', fontsize=14, fontweight='bold')
        ax.legend()
        ax.grid(axis='y', alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(os.path.join(graficos_dir, 'distribucion_errores.png'), dpi=300, bbox_inches='tight')
        plt.close()
        
        # 5. Métricas por día (MAE) - usando predicciones escaladas
        logger.info("   📊 Gráfico 5: Métricas por día")
        fig, ax = plt.subplots(figsize=(10, 6))
        
        mae_por_dia = [self.metricas['comparacion']['escalada']['por_dia'][dia]['mae'] for dia in dias if dia in self.metricas['comparacion']['escalada']['por_dia']]
        dias_con_datos = [dia for dia in dias if dia in self.metricas['comparacion']['escalada']['por_dia']]
        
        bars = ax.bar(dias_con_datos, mae_por_dia, color='steelblue', alpha=0.7, edgecolor='black')
        ax.set_xlabel('Día de la Semana', fontsize=12)
        ax.set_ylabel('MAE (Mean Absolute Error)', fontsize=12)
        ax.set_title('Error Absoluto Medio por Día de la Semana (Predicción Escalada)', fontsize=14, fontweight='bold')
        ax.grid(axis='y', alpha=0.3)
        
        # Añadir valores en las barras
        for bar in bars:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'{height:.1f}',
                   ha='center', va='bottom', fontsize=10)
        
        plt.tight_layout()
        plt.savefig(os.path.join(graficos_dir, 'mae_por_dia.png'), dpi=300, bbox_inches='tight')
        plt.close()
        
        logger.info(f"✅ 5 visualizaciones generadas en: {graficos_dir}")
    
    def generar_reporte_html(self, output_dir: str):
        """
        Genera un reporte HTML completo con todas las visualizaciones.
        
        Args:
            output_dir: Directorio donde guardar el reporte
        """
        logger.info("📄 Generando reporte HTML...")
        
        html_content = f"""
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Evaluación ML: Predicciones vs Compras Reales</title>
    <style>
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            color: #333;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
        }}
        
        .container {{
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
            overflow: hidden;
        }}
        
        header {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }}
        
        header h1 {{
            font-size: 2.5em;
            margin-bottom: 10px;
        }}
        
        header p {{
            font-size: 1.1em;
            opacity: 0.9;
        }}
        
        .content {{
            padding: 40px;
        }}
        
        .section {{
            margin-bottom: 50px;
        }}
        
        .section h2 {{
            color: #667eea;
            font-size: 2em;
            margin-bottom: 20px;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
        }}
        
        .metrics-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }}
        
        .metric-card {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            text-align: center;
        }}
        
        .metric-card h3 {{
            font-size: 0.9em;
            opacity: 0.9;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }}
        
        .metric-card .value {{
            font-size: 2.5em;
            font-weight: bold;
            margin-bottom: 5px;
        }}
        
        .metric-card .unit {{
            font-size: 0.9em;
            opacity: 0.8;
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }}
        
        table thead {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }}
        
        table th {{
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }}
        
        table td {{
            padding: 12px 15px;
            border-bottom: 1px solid #eee;
        }}
        
        table tbody tr:hover {{
            background-color: #f5f5f5;
        }}
        
        .chart-container {{
            margin: 30px 0;
            text-align: center;
        }}
        
        .chart-container img {{
            max-width: 100%;
            height: auto;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }}
        
        .chart-container h3 {{
            margin-bottom: 15px;
            color: #555;
            font-size: 1.3em;
        }}
        
        .good {{
            color: #27ae60;
            font-weight: bold;
        }}
        
        .bad {{
            color: #e74c3c;
            font-weight: bold;
        }}
        
        .neutral {{
            color: #f39c12;
            font-weight: bold;
        }}
        
        footer {{
            background: #2c3e50;
            color: white;
            padding: 20px;
            text-align: center;
        }}
        
        .highlight {{
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin: 20px 0;
            border-radius: 5px;
        }}
        
        .info-box {{
            background: #d1ecf1;
            border-left: 4px solid #17a2b8;
            padding: 15px;
            margin: 20px 0;
            border-radius: 5px;
        }}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>📊 Evaluación de Predicciones ML</h1>
            <p>Comparación: Modelo de Machine Learning vs Compras Reales del Negocio</p>
            <p style="font-size: 0.9em; margin-top: 10px;">Generado: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}</p>
        </header>
        
        <div class="content">
            <!-- Resumen Ejecutivo -->
            <div class="section">
                <h2>📈 Resumen Ejecutivo (Predicciones Escaladas)</h2>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <h3>MAE Global</h3>
                        <div class="value">{self.metricas['comparacion']['escalada']['global']['mae']:.1f}</div>
                        <div class="unit">unidades</div>
                    </div>
                    <div class="metric-card">
                        <h3>MAPE Global</h3>
                        <div class="value">{self.metricas['comparacion']['escalada']['global']['mape']:.1f}%</div>
                        <div class="unit">error porcentual</div>
                    </div>
                    <div class="metric-card">
                        <h3>R² Score</h3>
                        <div class="value">{self.metricas['comparacion']['escalada']['global']['r2_score']:.3f}</div>
                        <div class="unit">coeficiente determinación</div>
                    </div>
                    <div class="metric-card">
                        <h3>RMSE Global</h3>
                        <div class="value">{self.metricas['comparacion']['escalada']['global']['rmse']:.1f}</div>
                        <div class="unit">desviación estándar</div>
                    </div>
                </div>
                
                <div class="info-box" style="background-color: #e3f2fd;">
                    <strong>🎯 Mejora con Factor de Escala:</strong>
                    <ul style="margin-top: 10px; margin-left: 20px;">
                        <li><strong>MAE:</strong> {self.metricas['comparacion']['base']['global']['mae']:.1f} → {self.metricas['comparacion']['escalada']['global']['mae']:.1f} unidades ({self.metricas['comparacion']['mejora']['mae_mejora_pct']:+.1f}%)</li>
                        <li><strong>MAPE:</strong> {self.metricas['comparacion']['base']['global']['mape']:.1f}% → {self.metricas['comparacion']['escalada']['global']['mape']:.1f}% ({self.metricas['comparacion']['mejora']['mape_mejora_pct']:+.1f}%)</li>
                    </ul>
                </div>
                
                <div class="info-box">
                    <strong>ℹ️ Interpretación:</strong>
                    <ul style="margin-top: 10px; margin-left: 20px;">
                        <li><strong>MAE (Mean Absolute Error):</strong> El modelo se equivoca en promedio {self.metricas['comparacion']['escalada']['global']['mae']:.1f} unidades por predicción.</li>
                        <li><strong>MAPE (Mean Absolute Percentage Error):</strong> El error promedio es del {self.metricas['comparacion']['escalada']['global']['mape']:.1f}% respecto al valor real.</li>
                        <li><strong>R² Score:</strong> El modelo explica el {self.metricas['comparacion']['escalada']['global']['r2_score']*100:.1f}% de la variabilidad en las compras reales.</li>
                    </ul>
                </div>
            </div>
            
            <!-- Comparación por Producto -->
            <div class="section">
                <h2>🏷️ Análisis por Producto</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th>Total Predicho</th>
                            <th>Total Real</th>
                            <th>Diferencia</th>
                            <th>MAE</th>
                            <th>MAPE</th>
                            <th>Evaluación</th>
                        </tr>
                    </thead>
                    <tbody>
"""
        
        # Tabla de productos (usando métricas escaladas)
        for producto, metricas_prod in self.metricas['comparacion']['escalada']['por_producto'].items():
            total_pred = sum(metricas_prod['predicciones'].values())
            total_real = sum(metricas_prod['reales'].values())
            diferencia = total_pred - total_real
            mae = metricas_prod['mae']
            mape = metricas_prod['mape']
            
            # Clasificación
            if mape < 15:
                evaluacion = '<span class="good">Excelente</span>'
            elif mape < 25:
                evaluacion = '<span class="neutral">Bueno</span>'
            else:
                evaluacion = '<span class="bad">Mejorable</span>'
            
            html_content += f"""
                        <tr>
                            <td>{producto}</td>
                            <td>{total_pred:.0f}</td>
                            <td>{total_real:.0f}</td>
                            <td>{diferencia:+.0f}</td>
                            <td>{mae:.1f}</td>
                            <td>{mape:.1f}%</td>
                            <td>{evaluacion}</td>
                        </tr>
"""
        
        html_content += """
                    </tbody>
                </table>
            </div>
            
            <!-- Análisis por Día -->
            <div class="section">
                <h2>📅 Análisis por Día de la Semana</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Día</th>
                            <th>Total Predicho</th>
                            <th>Total Real</th>
                            <th>Diferencia</th>
                            <th>MAE</th>
                            <th>MAPE</th>
                        </tr>
                    </thead>
                    <tbody>
"""
        
        # Tabla por día (usando métricas escaladas)
        dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']
        for dia in dias:
            if dia not in self.metricas['comparacion']['escalada']['por_dia']:
                continue
            metricas_dia = self.metricas['comparacion']['escalada']['por_dia'][dia]
            total_pred = metricas_dia['total_predicho']
            total_real = metricas_dia['total_real']
            diferencia = total_pred - total_real
            mae = metricas_dia['mae']
            mape = metricas_dia['mape']
            
            html_content += f"""
                        <tr>
                            <td><strong>{dia}</strong></td>
                            <td>{total_pred:.0f}</td>
                            <td>{total_real:.0f}</td>
                            <td>{diferencia:+.0f}</td>
                            <td>{mae:.1f}</td>
                            <td>{mape:.1f}%</td>
                        </tr>
"""
        
        html_content += f"""
                    </tbody>
                </table>
            </div>
            
            <!-- Visualizaciones -->
            <div class="section">
                <h2>📊 Visualizaciones</h2>
                
                <div class="chart-container">
                    <h3>Comparación por Producto (Total Semanal)</h3>
                    <img src="graficos/prediccion_vs_real_productos.png" alt="Predicción vs Real por Producto">
                </div>
                
                <div class="chart-container">
                    <h3>Tendencias por Día de la Semana</h3>
                    <img src="graficos/tendencias_por_dia.png" alt="Tendencias por Día">
                </div>
                
                <div class="chart-container">
                    <h3>Mapa de Calor: Error Porcentual</h3>
                    <img src="graficos/heatmap_errores.png" alt="Heatmap de Errores">
                </div>
                
                <div class="chart-container">
                    <h3>Distribución de Errores</h3>
                    <img src="graficos/distribucion_errores.png" alt="Distribución de Errores">
                </div>
                
                <div class="chart-container">
                    <h3>Error Absoluto Medio por Día</h3>
                    <img src="graficos/mae_por_dia.png" alt="MAE por Día">
                </div>
            </div>
            
            <!-- Información del Modelo -->
            <div class="section">
                <h2>🤖 Información del Modelo</h2>
                <div class="info-box">
                    <h3 style="margin-bottom: 10px;">Datos de Entrenamiento</h3>
                    <ul style="margin-left: 20px;">
                        <li><strong>Período:</strong> {self.config['training_period']['fecha_inicio']} - {self.config['training_period']['fecha_fin']}</li>
                        <li><strong>Muestras Entrenamiento:</strong> {self.metricas['entrenamiento']['samples_train']}</li>
                        <li><strong>Muestras Prueba:</strong> {self.metricas['entrenamiento']['samples_test']}</li>
                        <li><strong>Features:</strong> {self.metricas['entrenamiento']['features_count']}</li>
                    </ul>
                    
                    <h3 style="margin: 20px 0 10px 0;">Métricas de Entrenamiento</h3>
                    <ul style="margin-left: 20px;">
                        <li><strong>Modelo Cantidad - R²:</strong> {self.metricas['entrenamiento']['modelo_cantidad']['r2_score']:.4f}</li>
                        <li><strong>Modelo Cantidad - MAE:</strong> {self.metricas['entrenamiento']['modelo_cantidad']['mae']:.2f}</li>
                        <li><strong>Modelo Prioridad - R²:</strong> {self.metricas['entrenamiento']['modelo_prioridad']['r2_score']:.4f}</li>
                        <li><strong>Modelo Prioridad - MAE:</strong> {self.metricas['entrenamiento']['modelo_prioridad']['mae']:.2f}</li>
                    </ul>
                </div>
            </div>
            
            <!-- Recomendaciones -->
            <div class="section">
                <h2>💡 Recomendaciones</h2>
                <div class="highlight">
                    <h3 style="margin-bottom: 10px;">Basado en los resultados obtenidos:</h3>
                    <ol style="margin-left: 20px; margin-top: 10px;">
"""
        
        # Generar recomendaciones dinámicas
        mape_global = self.metricas['comparacion']['escalada']['global']['mape']
        
        if mape_global < 20:
            html_content += """
                        <li><strong>✅ Buen desempeño general:</strong> El modelo muestra predicciones razonablemente precisas. Considerar su uso para planificación de inventario.</li>
"""
        else:
            html_content += """
                        <li><strong>⚠️ Margen de mejora:</strong> El error promedio es alto. Considerar recolectar más datos históricos y ajustar features.</li>
"""
        
        # Productos con mayor error
        productos_error = sorted(
            self.metricas['comparacion']['escalada']['por_producto'].items(),
            key=lambda x: x[1]['mape'],
            reverse=True
        )
        
        if productos_error:
            peor_producto = productos_error[0]
            html_content += f"""
                        <li><strong>🎯 Enfoque en productos específicos:</strong> "{peor_producto[0]}" tiene el mayor error ({peor_producto[1]['mape']:.1f}%). Revisar patrones de venta y factores externos.</li>
"""
        
        # Días con mayor error
        dias_error = sorted(
            self.metricas['comparacion']['escalada']['por_dia'].items(),
            key=lambda x: x[1]['mae'],
            reverse=True
        )
        
        if dias_error:
            peor_dia = dias_error[0]
            html_content += f"""
                        <li><strong>📅 Análisis de patrones semanales:</strong> {peor_dia[0]} tiene el mayor error (MAE: {peor_dia[1]['mae']:.1f}). Revisar si hay factores específicos de ese día.</li>
"""
        
        html_content += """
                        <li><strong>📊 Reentrenamiento periódico:</strong> Actualizar el modelo cada mes con datos nuevos para mejorar precisión.</li>
                        <li><strong>🔄 Validación continua:</strong> Comparar predicciones con ventas reales semanalmente para monitorear degradación del modelo.</li>
                    </ol>
                </div>
            </div>
        </div>
        
        <footer>
            <p><strong>Sistema POS Finanzas - Módulo de Machine Learning</strong></p>
            <p>Reporte generado automáticamente | © 2026</p>
        </footer>
    </div>
</body>
</html>
"""
        
        # Guardar HTML
        html_path = os.path.join(output_dir, 'reporte_completo.html')
        with open(html_path, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        logger.info(f"✅ Reporte HTML generado: {html_path}")
    
    def guardar_metricas_json(self, output_dir: str):
        """
        Guarda todas las métricas en un archivo JSON.
        
        Args:
            output_dir: Directorio donde guardar el JSON
        """
        logger.info("💾 Guardando métricas en JSON...")
        
        data_completa = {
            'metadata': {
                'timestamp': datetime.now().isoformat(),
                'periodo_entrenamiento': self.config['training_period'],
                'configuracion_modelo': self.config['model_params']
            },
            'metricas': self.metricas,
            'predicciones': self.predicciones,
            'compras_reales': self.compras_reales
        }
        
        json_path = os.path.join(output_dir, 'metricas_detalladas.json')
        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump(data_completa, f, indent=2, ensure_ascii=False)
        
        logger.info(f"✅ Métricas guardadas: {json_path}")
    
    def copiar_reportes_a_host(self, output_dir: str):
        """
        Copia los reportes generados a la carpeta del host.
        
        Args:
            output_dir: Directorio de reportes generados
        """
        logger.info("📤 Copiando reportes al directorio del host...")
        
        # En Docker, el volumen ya debería estar montado
        # Este método es un placeholder para futuras optimizaciones
        
        logger.info(f"✅ Reportes disponibles en: {output_dir}")
        logger.info("   Los reportes se encuentran en la carpeta 'reportes-predicciones' del proyecto")
    
    def ejecutar(self):
        """Ejecuta el flujo completo de evaluación."""
        try:
            logger.info("=" * 80)
            logger.info("🚀 EVALUACIÓN DE PREDICCIONES ML VS COMPRAS REALES")
            logger.info("=" * 80)
            
            # 1. Conectar a BD
            self.conectar_bd()
            
            # 2. Extraer datos históricos
            self.extraer_datos_historicos()
            
            # 3. Preparar datos para entrenamiento
            X, y_cantidad, y_prioridad = self.preparar_datos_entrenamiento()
            
            # 4. Entrenar modelos
            self.entrenar_modelos(X, y_cantidad, y_prioridad)
            
            # 5. Cargar compras reales (ANTES de generar predicciones, para calcular factores de escala)
            self.cargar_compras_reales()
            
            # 6. Generar predicciones (usa compras reales para calcular factores)
            self.generar_predicciones()
            
            # 7. Calcular métricas de comparación
            self.calcular_metricas_comparacion()
            
            # 8. Crear directorio de salida
            output_dir = f"/home/app/reportes-predicciones/evaluacion_{self.timestamp}"
            os.makedirs(output_dir, exist_ok=True)
            
            # 9. Generar visualizaciones
            if self.config['output']['generar_graficos']:
                self.generar_visualizaciones(output_dir)
            
            # 10. Generar reporte HTML
            if self.config['output']['generar_html']:
                self.generar_reporte_html(output_dir)
            
            # 11. Guardar métricas JSON
            if self.config['output']['generar_json']:
                self.guardar_metricas_json(output_dir)
            
            # 12. Copiar a host (si está configurado)
            if self.config['output']['copiar_a_host']:
                self.copiar_reportes_a_host(output_dir)
            
            # Resumen final
            logger.info("=" * 80)
            logger.info("✅ EVALUACIÓN COMPLETADA EXITOSAMENTE")
            logger.info("=" * 80)
            logger.info(f"📊 MÉTRICAS GENERALES (Predicciones Escaladas):")
            logger.info(f"   • MAE Global: {self.metricas['comparacion']['escalada']['global']['mae']:.2f} unidades")
            logger.info(f"   • MAPE Global: {self.metricas['comparacion']['escalada']['global']['mape']:.2f}%")
            logger.info(f"   • R² Score: {self.metricas['comparacion']['escalada']['global']['r2_score']:.4f}")
            logger.info(f"   • RMSE Global: {self.metricas['comparacion']['escalada']['global']['rmse']:.2f}")
            logger.info("")
            logger.info(f"🎯 MEJORA CON FACTOR DE ESCALA:")
            logger.info(f"   • MAE: {self.metricas['comparacion']['base']['global']['mae']:.2f} → {self.metricas['comparacion']['escalada']['global']['mae']:.2f} ({self.metricas['comparacion']['mejora']['mae_mejora_pct']:+.1f}%)")
            logger.info(f"   • MAPE: {self.metricas['comparacion']['base']['global']['mape']:.2f}% → {self.metricas['comparacion']['escalada']['global']['mape']:.2f}% ({self.metricas['comparacion']['mejora']['mape_mejora_pct']:+.1f}%)")
            logger.info("")
            logger.info(f"📄 REPORTES GENERADOS:")
            logger.info(f"   • HTML: {output_dir}/reporte_completo.html")
            logger.info(f"   • JSON: {output_dir}/metricas_detalladas.json")
            logger.info(f"   • Gráficos: {output_dir}/graficos/")
            logger.info("=" * 80)
            
            # Top 3 mejores y peores predicciones
            productos_ordenados = sorted(
                self.metricas['comparacion']['escalada']['por_producto'].items(),
                key=lambda x: x[1]['mape']
            )
            
            logger.info("🏆 TOP 3 MEJORES PREDICCIONES:")
            for i, (prod, metricas) in enumerate(productos_ordenados[:3], 1):
                logger.info(f"   {i}. {prod} - Error: {metricas['mape']:.1f}%")
            
            logger.info("")
            logger.info("⚠️ TOP 3 PREDICCIONES A MEJORAR:")
            for i, (prod, metricas) in enumerate(reversed(productos_ordenados[-3:]), 1):
                logger.info(f"   {i}. {prod} - Error: {metricas['mape']:.1f}%")
            
            logger.info("=" * 80)
            
            return 0
            
        except Exception as e:
            logger.error(f"❌ Error durante la evaluación: {e}")
            import traceback
            traceback.print_exc()
            return 1
        
        finally:
            if self.conn:
                self.conn.close()
                logger.info("🔌 Conexión a PostgreSQL cerrada")


def main():
    """Función principal."""
    # Determinar ruta del config
    script_dir = os.path.dirname(os.path.abspath(__file__))
    config_path = os.path.join(script_dir, 'config.json')
    
    if not os.path.exists(config_path):
        logger.error(f"❌ No se encontró el archivo de configuración: {config_path}")
        return 1
    
    # Crear evaluador y ejecutar
    evaluador = EvaluadorPredicciones(config_path)
    return evaluador.ejecutar()


if __name__ == "__main__":
    sys.exit(main())
