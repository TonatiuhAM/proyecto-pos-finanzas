#!/usr/bin/env python3
"""
Análisis de Calidad de Datos Reales
Sistema POS & Gestión Integral - Motor de Predicciones ML

Descripción:
    Script para analizar la calidad de los datos reales extraídos de la base de datos
    de producción y generar un reporte de calidad detallado.

Uso:
    python analizar_calidad_datos_reales.py
"""

import pandas as pd
import numpy as np
from datetime import datetime
from pathlib import Path
import sys

# Importar el analizador de calidad
from data_quality_analyzer import DataQualityAnalyzer

def cargar_datos_reales():
    """
    Carga los datos reales desde el CSV extraído de la base de datos.
    
    Returns:
        pd.DataFrame: DataFrame con los datos de ventas reales
    """
    print("📂 Cargando datos reales desde CSV...")
    
    # Cargar el CSV de datos de ventas
    df_ventas = pd.read_csv('datos_ventas_reales.csv')
    
    print(f"✅ Datos cargados: {len(df_ventas)} registros")
    print(f"📊 Columnas: {df_ventas.columns.tolist()}")
    print(f"📅 Rango de fechas: {df_ventas['fecha_orden'].min()} a {df_ventas['fecha_orden'].max()}")
    
    return df_ventas

def preparar_datos_para_analisis(df_ventas):
    """
    Transforma los datos de ventas al formato esperado por el analizador
    (similar al formato post-feature engineering del pipeline ML).
    
    Args:
        df_ventas (pd.DataFrame): Datos de ventas crudos
        
    Returns:
        pd.DataFrame: DataFrame preparado para análisis de calidad
    """
    print("\n🔧 Preparando datos para análisis...")
    
    # Convertir fecha a datetime
    df_ventas['fecha_orden'] = pd.to_datetime(df_ventas['fecha_orden'])
    
    # Agrupar por producto para crear features agregadas
    # (similar a lo que hace el feature engineering)
    df_analisis = df_ventas.groupby('productos_id').agg({
        'cantidad_pz': ['sum', 'mean', 'std', 'count'],
        'cantidad_kg': ['sum', 'mean'],
        'precio_venta': ['mean', 'std', 'min', 'max'],
        'total_venta': 'sum',
        'stock_actual_pz': 'first',  # Tomar el más reciente
        'cantidad_minima': 'first',
        'cantidad_maxima': 'first',
        'producto_nombre': 'first',
        'proveedor_id': 'first'
    }).reset_index()
    
    # Aplanar columnas multinivel
    df_analisis.columns = [
        'producto_id',
        'ventas_totales_pz', 'venta_promedio_pz', 'venta_std_pz', 'num_ordenes',
        'ventas_totales_kg', 'venta_promedio_kg',
        'precio_promedio', 'precio_std', 'precio_min', 'precio_max',
        'ingresos_totales',
        'stock_actual',
        'stock_minimo',
        'stock_maximo',
        'nombre_producto',
        'proveedor_id'
    ]
    
    # Calcular features derivadas (como en el pipeline ML)
    df_analisis['rotacion_inventario'] = df_analisis['ventas_totales_pz'] / (df_analisis['stock_actual'] + 1)
    df_analisis['nivel_stock_pct'] = (df_analisis['stock_actual'] / df_analisis['stock_maximo']) * 100
    df_analisis['deficit_stock'] = df_analisis['stock_minimo'] - df_analisis['stock_actual']
    df_analisis['deficit_stock'] = df_analisis['deficit_stock'].clip(lower=0)
    df_analisis['tasa_demanda_diaria'] = df_analisis['ventas_totales_pz'] / 60  # ~2 meses
    df_analisis['variabilidad_precio'] = (df_analisis['precio_std'] / df_analisis['precio_promedio']) * 100
    
    # Target variable simulada (cantidad a comprar basada en déficit y demanda)
    df_analisis['cantidad_a_comprar'] = np.maximum(
        df_analisis['deficit_stock'],
        df_analisis['tasa_demanda_diaria'] * 7  # Una semana de inventario
    ).round().astype(int)
    
    # Prioridad de compra (1-5, siendo 5 más urgente)
    df_analisis['prioridad_compra'] = 3  # Prioridad media por defecto
    
    # Mayor prioridad si hay déficit de stock
    df_analisis.loc[df_analisis['deficit_stock'] > 0, 'prioridad_compra'] = 5
    
    # Menor prioridad si hay exceso de stock
    df_analisis.loc[df_analisis['nivel_stock_pct'] > 80, 'prioridad_compra'] = 1
    
    # Prioridad alta si rotación es alta y stock bajo
    df_analisis.loc[
        (df_analisis['rotacion_inventario'] > 2) & (df_analisis['nivel_stock_pct'] < 50),
        'prioridad_compra'
    ] = 4
    
    print(f"✅ Datos preparados: {len(df_analisis)} productos")
    print(f"📊 Features creadas: {len(df_analisis.columns)} columnas")
    
    # Seleccionar solo columnas numéricas y relevantes para el análisis
    columnas_analisis = [
        'ventas_totales_pz', 'venta_promedio_pz', 'venta_std_pz', 'num_ordenes',
        'precio_promedio', 'precio_std', 'precio_min', 'precio_max',
        'ingresos_totales', 'stock_actual', 'stock_minimo', 'stock_maximo',
        'rotacion_inventario', 'nivel_stock_pct', 'deficit_stock',
        'tasa_demanda_diaria', 'variabilidad_precio',
        'cantidad_a_comprar', 'prioridad_compra'
    ]
    
    return df_analisis[columnas_analisis]

def ejecutar_analisis_calidad(df_analisis):
    """
    Ejecuta el análisis de calidad usando DataQualityAnalyzer.
    
    Args:
        df_analisis (pd.DataFrame): DataFrame preparado para análisis
        
    Returns:
        dict: Reporte de calidad generado
    """
    print("\n🔍 Ejecutando análisis de calidad de datos...")
    
    # Crear instancia del analizador
    analyzer = DataQualityAnalyzer(
        df_analisis,
        target_columns=['cantidad_a_comprar', 'prioridad_compra']
    )
    
    # Generar reporte completo
    print("\n📊 Generando reporte de calidad...")
    reporte = analyzer.generate_full_report()
    
    return reporte

def main():
    """Función principal del script."""
    print("=" * 70)
    print("🎯 ANÁLISIS DE CALIDAD DE DATOS REALES")
    print("    Sistema POS & Gestión Integral - Motor de Predicciones ML")
    print("=" * 70)
    
    try:
        # 1. Cargar datos reales
        df_ventas = cargar_datos_reales()
        
        # 2. Preparar datos para análisis
        df_analisis = preparar_datos_para_analisis(df_ventas)
        
        # 3. Mostrar resumen de los datos preparados
        print("\n📋 RESUMEN DE DATOS PREPARADOS:")
        print("-" * 70)
        print(df_analisis.describe())
        
        # 4. Ejecutar análisis de calidad
        reporte = ejecutar_analisis_calidad(df_analisis)
        
        # 5. Mostrar puntuación de calidad
        print("\n" + "=" * 70)
        print("📈 PUNTUACIÓN FINAL DE CALIDAD DE DATOS")
        print("=" * 70)
        print(f"🎯 Puntuación Total: {reporte['overall_score']:.2f}/100")
        print("-" * 70)
        
        if 'component_scores' in reporte:
            print("📊 Desglose por Componente:")
            for componente, score in reporte['component_scores'].items():
                print(f"   • {componente}: {score:.2f}/100")
        
        print("\n✅ Análisis completado exitosamente")
        print(f"📄 Reporte HTML generado en: ml-prediction-service/")
        
        # Comparación con datos sintéticos
        print("\n" + "=" * 70)
        print("📊 COMPARACIÓN CON DATOS SINTÉTICOS")
        print("=" * 70)
        print(f"   Datos Reales:     {reporte['overall_score']:.2f}/100")
        print(f"   Datos Sintéticos: 48.82/100")
        print(f"   Mejora:           +{reporte['overall_score'] - 48.82:.2f} puntos")
        print("=" * 70)
        
        return 0
        
    except FileNotFoundError as e:
        print(f"❌ Error: No se encontró el archivo de datos - {e}")
        print("💡 Asegúrate de ejecutar primero 'extraer_datos_reales.sh'")
        return 1
    except Exception as e:
        print(f"❌ Error durante el análisis: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    sys.exit(main())
