#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de Análisis de Abastecimiento con XGBoost para Tesis
============================================================

Este script realiza un análisis completo de datos de ventas para demostrar
cómo el aumento del volumen de datos mejora la precisión de modelos XGBoost.

Autor: Sistema POS Finanzas
Fecha: 28 Enero 2026
"""

import os
import sys
import logging
from datetime import datetime, timedelta
from pathlib import Path

import numpy as np
import pandas as pd
import psycopg2
from psycopg2 import sql
from dotenv import load_dotenv
from scipy import stats
import pickle
import warnings

# Machine Learning
import xgboost as xgb
from sklearn.model_selection import train_test_split, learning_curve, cross_val_score
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

# Visualización
import matplotlib.pyplot as plt
import seaborn as sns
from tabulate import tabulate

# Configurar warnings
warnings.filterwarnings('ignore', category=FutureWarning)
warnings.filterwarnings('ignore', category=UserWarning)

# Configuración de logging
# Obtener el directorio raíz del proyecto (un nivel arriba del directorio scripts)
SCRIPT_DIR = Path(__file__).parent.absolute()
PROJECT_DIR = SCRIPT_DIR.parent.absolute()
RESULTS_DIR = PROJECT_DIR / 'results'
DATA_DIR = PROJECT_DIR / 'data'
MODELS_DIR = PROJECT_DIR / 'models'

# Crear directorio de resultados si no existe
RESULTS_DIR.mkdir(parents=True, exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(RESULTS_DIR / 'ejecucion.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)


# ============================================================================
# FASE 1: CONFIGURACIÓN Y EXTRACCIÓN DE DATOS REALES
# ============================================================================

def conectar_base_datos():
    """
    Establece conexión con la base de datos PostgreSQL usando variables de entorno.
    
    Returns:
        psycopg2.connection: Objeto de conexión a la base de datos
        
    Raises:
        Exception: Si hay error en la conexión
    """
    logger.info("=" * 80)
    logger.info("INICIANDO CONEXIÓN A BASE DE DATOS")
    logger.info("=" * 80)
    
    try:
        # Cargar variables de entorno
        load_dotenv()
        
        # Obtener credenciales
        db_url = os.getenv('DB_URL')
        db_user = os.getenv('DB_USER')
        db_pass = os.getenv('DB_PASS')
        
        if not all([db_url, db_user, db_pass]):
            raise ValueError("Faltan variables de entorno: DB_URL, DB_USER, DB_PASS")
        
        # Parsear DB_URL (soporta formatos: postgresql://host:port/database o jdbc:postgresql://host:port/database)
        if db_url.startswith('jdbc:postgresql://'):
            db_url = db_url.replace('jdbc:postgresql://', '')
        elif db_url.startswith('postgresql://'):
            db_url = db_url.replace('postgresql://', '')
        
        # Separar host:port/database
        parts = db_url.split('/')
        if len(parts) < 2:
            raise ValueError(f"Formato de DB_URL inválido: {db_url}. Debe ser postgresql://host:port/database")
        
        host_port = parts[0].split(':')
        host = host_port[0]
        port = host_port[1] if len(host_port) > 1 else '5432'
        database = parts[1].split('?')[0]
        
        logger.info(f"Conectando a: {host}:{port}/{database}")
        logger.info(f"Usuario: {db_user}")
        
        # Establecer conexión
        conn = psycopg2.connect(
            host=host,
            port=port,
            database=database,
            user=db_user,
            password=db_pass
        )
        
        logger.info("✓ Conexión exitosa a la base de datos")
        
        # Verificar conexión
        cursor = conn.cursor()
        cursor.execute("SELECT version();")
        version = cursor.fetchone()
        logger.info(f"Versión PostgreSQL: {version[0]}")
        cursor.close()
        
        return conn
        
    except Exception as e:
        logger.error(f"✗ Error al conectar a la base de datos: {str(e)}")
        raise


def explorar_fechas_con_datos(conn):
    """
    Explora la base de datos para encontrar fechas con muchos registros de ventas.
    
    Args:
        conn: Conexión a la base de datos
        
    Returns:
        pandas.DataFrame: Top 10 fechas con más registros
    """
    logger.info("\n" + "=" * 80)
    logger.info("EXPLORANDO FECHAS CON MAYOR VOLUMEN DE VENTAS")
    logger.info("=" * 80)
    
    try:
        query = """
        SELECT 
            DATE(fecha_orden) as fecha,
            COUNT(*) as num_registros,
            SUM(total_venta) as total_ventas,
            AVG(total_venta) as promedio_venta,
            MIN(total_venta) as min_venta,
            MAX(total_venta) as max_venta
        FROM ordenes_de_ventas
        WHERE fecha_orden IS NOT NULL
        GROUP BY DATE(fecha_orden)
        HAVING COUNT(*) >= 3
        ORDER BY num_registros DESC, fecha DESC
        LIMIT 20;
        """
        
        logger.info("Ejecutando consulta para encontrar fechas con datos...")
        df = pd.read_sql_query(query, conn)
        
        if df.empty:
            logger.warning("⚠ No se encontraron registros en ordenes_de_ventas")
            return df
        
        logger.info(f"✓ Se encontraron {len(df)} fechas con registros de ventas")
        logger.info("\nTop 10 fechas con mayor volumen de transacciones:")
        logger.info("-" * 80)
        
        for idx, row in df.head(10).iterrows():
            logger.info(f"{idx+1}. Fecha: {row['fecha']} | "
                       f"Transacciones: {row['num_registros']} | "
                       f"Total: ${row['total_ventas']:,.2f} | "
                       f"Promedio: ${row['promedio_venta']:,.2f}")
        
        return df
        
    except Exception as e:
        logger.error(f"✗ Error al explorar fechas: {str(e)}")
        raise


def extraer_ventas_5_dias(conn, fecha_inicio):
    """
    Extrae datos de ventas de 5 días consecutivos desde una fecha de inicio.
    
    Args:
        conn: Conexión a la base de datos
        fecha_inicio: Fecha de inicio (str o datetime)
        
    Returns:
        pandas.DataFrame: Datos de ventas agrupados por día
    """
    logger.info("\n" + "=" * 80)
    logger.info("EXTRAYENDO DATOS DE VENTAS DE 5 DÍAS CONSECUTIVOS")
    logger.info("=" * 80)
    
    try:
        # Convertir fecha_inicio a datetime si es string, o mantener si ya es date
        if isinstance(fecha_inicio, str):
            fecha_inicio = pd.to_datetime(fecha_inicio).date()
        elif hasattr(fecha_inicio, 'date') and callable(fecha_inicio.date):
            fecha_inicio = fecha_inicio.date()
        
        fecha_fin = fecha_inicio + timedelta(days=5)
        
        logger.info(f"Rango de fechas: {fecha_inicio} hasta {fecha_fin}")
        
        query = """
        SELECT 
            DATE(fecha_orden) as fecha,
            COUNT(*) as num_transacciones,
            SUM(total_venta) as total_ventas,
            AVG(total_venta) as promedio_venta,
            STDDEV(total_venta) as std_venta,
            MIN(total_venta) as min_venta,
            MAX(total_venta) as max_venta
        FROM ordenes_de_ventas
        WHERE fecha_orden >= %s AND fecha_orden < %s
        GROUP BY DATE(fecha_orden)
        ORDER BY fecha;
        """
        
        df = pd.read_sql_query(query, conn, params=(fecha_inicio, fecha_fin))
        
        if df.empty:
            logger.warning("⚠ No se encontraron datos en el rango especificado")
            return df
        
        logger.info(f"✓ Se extrajeron datos de {len(df)} días")
        logger.info("\nResumen de ventas por día:")
        logger.info("-" * 80)
        
        for idx, row in df.iterrows():
            logger.info(f"Día {idx+1} ({row['fecha']}): "
                       f"{row['num_transacciones']} transacciones, "
                       f"Total: ${row['total_ventas']:,.2f}, "
                       f"Promedio: ${row['promedio_venta']:,.2f}")
        
        # Guardar en CSV
        output_path = DATA_DIR / 'ventas_5_dias_reales.csv'
        df.to_csv(output_path, index=False)
        logger.info(f"\n✓ Datos guardados en: {output_path}")
        
        return df
        
    except Exception as e:
        logger.error(f"✗ Error al extraer ventas de 5 días: {str(e)}")
        raise


def analizar_tendencia_5_dias(df):
    """
    Realiza análisis descriptivo y de tendencia de los datos de 5 días.
    
    Args:
        df: DataFrame con datos de ventas de 5 días
        
    Returns:
        dict: Diccionario con estadísticas y métricas de tendencia
    """
    logger.info("\n" + "=" * 80)
    logger.info("ANÁLISIS DESCRIPTIVO Y DE TENDENCIA - 5 DÍAS REALES")
    logger.info("=" * 80)
    
    try:
        if df.empty:
            logger.error("✗ DataFrame vacío, no se puede realizar análisis")
            return {}
        
        # Preparar datos para análisis
        df = df.copy()
        df['dia_num'] = range(1, len(df) + 1)
        
        # Estadísticas básicas
        promedio_ventas = df['total_ventas'].mean()
        std_ventas = df['total_ventas'].std()
        promedio_transacciones = df['num_transacciones'].mean()
        total_5_dias = df['total_ventas'].sum()
        
        # Día con mayor y menor ventas
        dia_max = df.loc[df['total_ventas'].idxmax()]
        dia_min = df.loc[df['total_ventas'].idxmin()]
        
        # Análisis de tendencia usando regresión lineal
        x = df['dia_num'].values.reshape(-1, 1)
        y = df['total_ventas'].values
        
        # Realizar regresión lineal
        slope, intercept, r_value, p_value, std_err = stats.linregress(
            df['dia_num'], df['total_ventas']
        )
        
        # Tasa de crecimiento diario promedio
        tasa_crecimiento_diaria = (slope / promedio_ventas) * 100
        
        # Determinar tipo de tendencia
        if abs(r_value) < 0.3:
            tipo_tendencia = "ESTABLE (sin tendencia clara)"
        elif slope > 0:
            tipo_tendencia = "CRECIENTE"
        else:
            tipo_tendencia = "DECRECIENTE"
        
        # Crear reporte
        reporte = f"""
╔═══════════════════════════════════════════════════════════════════════════╗
║              REPORTE DE ANÁLISIS DESCRIPTIVO - 5 DÍAS REALES             ║
╚═══════════════════════════════════════════════════════════════════════════╝

📊 ESTADÍSTICAS GENERALES:
   • Total de días analizados: {len(df)}
   • Total acumulado 5 días: ${total_5_dias:,.2f}
   • Promedio de ventas diarias: ${promedio_ventas:,.2f}
   • Desviación estándar: ${std_ventas:,.2f}
   • Coeficiente de variación: {(std_ventas/promedio_ventas)*100:.2f}%
   • Promedio de transacciones diarias: {promedio_transacciones:.1f}

📈 ANÁLISIS DE TENDENCIA:
   • Tipo de tendencia: {tipo_tendencia}
   • Pendiente de regresión: ${slope:,.2f} por día
   • Coeficiente de correlación (R): {r_value:.4f}
   • R² (ajuste del modelo): {r_value**2:.4f}
   • Tasa de crecimiento diaria: {tasa_crecimiento_diaria:+.2f}%
   • P-value: {p_value:.4f}

🔝 DÍA CON MAYOR VENTAS:
   • Fecha: {dia_max['fecha']}
   • Total: ${dia_max['total_ventas']:,.2f}
   • Transacciones: {dia_max['num_transacciones']}
   • Promedio por transacción: ${dia_max['promedio_venta']:,.2f}

🔻 DÍA CON MENOR VENTAS:
   • Fecha: {dia_min['fecha']}
   • Total: ${dia_min['total_ventas']:,.2f}
   • Transacciones: {dia_min['num_transacciones']}
   • Promedio por transacción: ${dia_min['promedio_venta']:,.2f}

📉 VARIABILIDAD:
   • Rango de ventas: ${dia_min['total_ventas']:,.2f} - ${dia_max['total_ventas']:,.2f}
   • Diferencia: ${dia_max['total_ventas'] - dia_min['total_ventas']:,.2f}
   • Factor de variación: {dia_max['total_ventas'] / dia_min['total_ventas']:.2f}x

═══════════════════════════════════════════════════════════════════════════
"""
        
        logger.info(reporte)
        
        # Guardar reporte
        output_path = RESULTS_DIR / 'analisis_descriptivo_5_dias.txt'
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(reporte)
        logger.info(f"✓ Reporte guardado en: {output_path}")
        
        # Retornar diccionario con métricas
        metricas = {
            'promedio_ventas_diarias': promedio_ventas,
            'std_ventas': std_ventas,
            'promedio_transacciones': promedio_transacciones,
            'total_5_dias': total_5_dias,
            'tasa_crecimiento_diaria': tasa_crecimiento_diaria,
            'tipo_tendencia': tipo_tendencia,
            'r_squared': r_value**2,
            'pendiente': slope,
            'intercepto': intercept,
            'dia_max': dia_max.to_dict(),
            'dia_min': dia_min.to_dict()
        }
        
        return metricas
        
    except Exception as e:
        logger.error(f"✗ Error al analizar tendencia: {str(e)}")
        raise


# ============================================================================
# FASE 2: GENERACIÓN DE DATOS SINTÉTICOS (6 MESES)
# ============================================================================

def generar_datos_sinteticos_6_meses(df_base, fecha_inicio, meses=6):
    """
    Genera datos sintéticos de ventas para 6 meses con estacionalidad, 
    ruido y tendencia de crecimiento.
    
    Args:
        df_base: DataFrame con estadísticas de los días reales
        fecha_inicio: Fecha donde comienza la simulación
        meses: Número de meses a generar (default: 6)
        
    Returns:
        pandas.DataFrame: Dataset sintético con ventas y features temporales
    """
    logger.info("\n" + "=" * 80)
    logger.info("GENERACIÓN DE DATOS SINTÉTICOS - 6 MESES")
    logger.info("=" * 80)
    
    try:
        # Convertir fecha_inicio a datetime si es necesario
        if isinstance(fecha_inicio, str):
            fecha_inicio = pd.to_datetime(fecha_inicio)
        
        # Calcular estadísticas base desde los datos reales
        venta_promedio_diaria = df_base['total_ventas'].mean()
        num_transacciones_promedio = df_base['num_transacciones'].mean()
        std_ventas = df_base['total_ventas'].std()
        promedio_venta_transaccion = df_base['promedio_venta'].mean()
        
        logger.info(f"📊 Estadísticas base (de {len(df_base)} días reales):")
        logger.info(f"   • Venta promedio diaria: ${venta_promedio_diaria:,.2f}")
        logger.info(f"   • Número de transacciones promedio: {num_transacciones_promedio:.1f}")
        logger.info(f"   • Desviación estándar: ${std_ventas:,.2f}")
        logger.info(f"   • Promedio por transacción: ${promedio_venta_transaccion:,.2f}")
        
        # Parámetros de generación
        dias_total = meses * 30  # Aproximadamente 6 meses = 180 días
        tasa_crecimiento_mensual = 0.02  # 2% de crecimiento mensual
        
        logger.info(f"\n🔧 Parámetros de generación:")
        logger.info(f"   • Días a generar: {dias_total}")
        logger.info(f"   • Tasa de crecimiento mensual: {tasa_crecimiento_mensual*100:.1f}%")
        logger.info(f"   • Estacionalidad: Domingos sin ventas")
        logger.info(f"   • Ruido: Gaussiano con σ = {std_ventas*0.15:,.2f}")
        
        # Generar secuencia de fechas
        logger.info(f"\n📅 Generando secuencia de fechas...")
        fechas = pd.date_range(start=fecha_inicio, periods=dias_total, freq='D')
        logger.info(f"   ✓ Rango: {fechas[0].date()} hasta {fechas[-1].date()}")
        
        # Factores de estacionalidad semanal
        factores_estacionalidad = {
            0: 0.85,  # Lunes
            1: 1.00,  # Martes
            2: 1.00,  # Miércoles
            3: 1.00,  # Jueves
            4: 1.15,  # Viernes
            5: 1.20,  # Sábado
            6: 0.00   # Domingo (NO SE TRABAJA)
        }
        
        logger.info(f"\n📈 Aplicando factores de estacionalidad semanal:")
        for dia, factor in factores_estacionalidad.items():
            dia_nombre = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'][dia]
            logger.info(f"   • {dia_nombre}: {factor:.2f}x")
        
        # Listas para almacenar datos generados
        datos_sinteticos = []
        
        logger.info(f"\n🎲 Generando datos día por día...")
        
        for idx, fecha in enumerate(fechas):
            # Convertir Timestamp a datetime.date para compatibilidad
            fecha_date = fecha.date() if hasattr(fecha, 'date') else fecha
            if isinstance(fecha_inicio, pd.Timestamp):
                fecha_inicio_date = fecha_inicio.date()
            else:
                fecha_inicio_date = fecha_inicio
            
            # Calcular mes desde el inicio (para tendencia)
            dias_desde_inicio = (fecha_date - fecha_inicio_date).days
            mes_actual = dias_desde_inicio // 30
            
            # Factor de tendencia (crecimiento compuesto)
            factor_tendencia = (1 + tasa_crecimiento_mensual) ** mes_actual
            
            # Día de la semana (0=Lunes, 6=Domingo)
            dia_semana = fecha.weekday()
            
            # Factor de estacionalidad
            factor_estacional = factores_estacionalidad[dia_semana]
            
            # Si es domingo, no hay ventas
            if dia_semana == 6:
                total_ventas = 0.0
                num_transacciones = 0
                promedio_venta = 0.0
            else:
                # Calcular ventas base con tendencia y estacionalidad
                ventas_base = venta_promedio_diaria * factor_tendencia * factor_estacional
                
                # Añadir ruido gaussiano (15% de la desviación estándar)
                ruido = np.random.normal(0, std_ventas * 0.15)
                total_ventas = max(0, ventas_base + ruido)  # No puede ser negativo
                
                # Calcular número de transacciones (también con ruido)
                trans_base = num_transacciones_promedio * factor_tendencia * factor_estacional
                ruido_trans = np.random.normal(0, num_transacciones_promedio * 0.1)
                num_transacciones = max(1, int(trans_base + ruido_trans))
                
                # Promedio por transacción
                promedio_venta = total_ventas / num_transacciones if num_transacciones > 0 else 0
            
            # Añadir features temporales para XGBoost
            datos_sinteticos.append({
                'fecha': fecha_date,
                'dia_semana': dia_semana,
                'dia_mes': fecha_date.day,
                'mes': fecha_date.month,
                'semana_año': fecha_date.isocalendar()[1],
                'es_fin_de_semana': 1 if dia_semana in [5, 6] else 0,
                'es_domingo': 1 if dia_semana == 6 else 0,
                'dias_desde_inicio': dias_desde_inicio,
                'mes_desde_inicio': mes_actual,
                'factor_tendencia': factor_tendencia,
                'factor_estacional': factor_estacional,
                'num_transacciones': num_transacciones,
                'total_ventas': total_ventas,
                'promedio_venta': promedio_venta
            })
            
            # Mostrar progreso cada 30 días
            if (idx + 1) % 30 == 0:
                logger.info(f"   ✓ Mes {(idx+1)//30}: {idx+1} días generados")
        
        # Crear DataFrame
        df_sintetico = pd.DataFrame(datos_sinteticos)
        
        logger.info(f"\n✓ Dataset sintético generado exitosamente")
        logger.info(f"   • Total de días: {len(df_sintetico)}")
        logger.info(f"   • Días con ventas: {(df_sintetico['total_ventas'] > 0).sum()}")
        logger.info(f"   • Domingos sin ventas: {(df_sintetico['es_domingo'] == 1).sum()}")
        
        # Estadísticas del dataset sintético
        logger.info(f"\n📊 Estadísticas del dataset sintético:")
        logger.info(f"   • Total ventas 6 meses: ${df_sintetico['total_ventas'].sum():,.2f}")
        logger.info(f"   • Promedio diario (excluyendo domingos): ${df_sintetico[df_sintetico['es_domingo']==0]['total_ventas'].mean():,.2f}")
        logger.info(f"   • Desviación estándar: ${df_sintetico['total_ventas'].std():,.2f}")
        logger.info(f"   • Total transacciones: {df_sintetico['num_transacciones'].sum()}")
        
        # Verificar crecimiento mensual
        logger.info(f"\n📈 Verificación de tendencia de crecimiento:")
        for mes in range(meses):
            datos_mes = df_sintetico[df_sintetico['mes_desde_inicio'] == mes]
            total_mes = datos_mes['total_ventas'].sum()
            dias_trabajados = (datos_mes['es_domingo'] == 0).sum()
            promedio_mes = total_mes / dias_trabajados if dias_trabajados > 0 else 0
            logger.info(f"   • Mes {mes+1}: ${promedio_mes:,.2f}/día (en {dias_trabajados} días laborables)")
        
        # Guardar en CSV
        output_path = DATA_DIR / 'ventas_6_meses_sinteticas.csv'
        df_sintetico.to_csv(output_path, index=False)
        logger.info(f"\n✓ Dataset guardado en: {output_path}")
        
        return df_sintetico
        
    except Exception as e:
        logger.error(f"✗ Error al generar datos sintéticos: {str(e)}")
        raise


def validar_datos_sinteticos(df_sintetico):
    """
    Valida que los datos sintéticos cumplan con los requisitos esperados.
    
    Args:
        df_sintetico: DataFrame con datos sintéticos generados
        
    Returns:
        dict: Diccionario con resultados de validación
    """
    logger.info("\n" + "=" * 80)
    logger.info("VALIDACIÓN DE DATOS SINTÉTICOS")
    logger.info("=" * 80)
    
    try:
        validaciones = {
            'total_registros': len(df_sintetico),
            'domingos_sin_ventas': True,
            'tendencia_creciente': False,
            'variabilidad_presente': False,
            'features_completas': False
        }
        
        # Validación 1: Domingos sin ventas
        domingos = df_sintetico[df_sintetico['es_domingo'] == 1]
        domingos_con_ventas = (domingos['total_ventas'] > 0).sum()
        validaciones['domingos_sin_ventas'] = (domingos_con_ventas == 0)
        
        logger.info(f"\n✓ Validación 1: Domingos sin ventas")
        logger.info(f"   • Total domingos: {len(domingos)}")
        logger.info(f"   • Domingos con ventas = 0: {len(domingos) - domingos_con_ventas}")
        logger.info(f"   • Estado: {'✓ CORRECTO' if validaciones['domingos_sin_ventas'] else '✗ ERROR'}")
        
        # Validación 2: Tendencia creciente
        primer_mes = df_sintetico[df_sintetico['mes_desde_inicio'] == 0]
        ultimo_mes = df_sintetico[df_sintetico['mes_desde_inicio'] == 5]
        
        promedio_primer_mes = primer_mes[primer_mes['es_domingo'] == 0]['total_ventas'].mean()
        promedio_ultimo_mes = ultimo_mes[ultimo_mes['es_domingo'] == 0]['total_ventas'].mean()
        
        crecimiento_total = ((promedio_ultimo_mes - promedio_primer_mes) / promedio_primer_mes) * 100
        validaciones['tendencia_creciente'] = (crecimiento_total > 8)  # Al menos 8% en 6 meses
        
        logger.info(f"\n✓ Validación 2: Tendencia de crecimiento")
        logger.info(f"   • Promedio mes 1: ${promedio_primer_mes:,.2f}")
        logger.info(f"   • Promedio mes 6: ${promedio_ultimo_mes:,.2f}")
        logger.info(f"   • Crecimiento total: {crecimiento_total:+.2f}%")
        logger.info(f"   • Estado: {'✓ CORRECTO' if validaciones['tendencia_creciente'] else '⚠ REVISAR'}")
        
        # Validación 3: Variabilidad (ruido presente)
        dias_laborables = df_sintetico[df_sintetico['es_domingo'] == 0]
        coef_variacion = (dias_laborables['total_ventas'].std() / dias_laborables['total_ventas'].mean()) * 100
        validaciones['variabilidad_presente'] = (coef_variacion > 5)  # Al menos 5% de variación
        
        logger.info(f"\n✓ Validación 3: Variabilidad en los datos")
        logger.info(f"   • Coeficiente de variación: {coef_variacion:.2f}%")
        logger.info(f"   • Estado: {'✓ CORRECTO' if validaciones['variabilidad_presente'] else '⚠ REVISAR'}")
        
        # Validación 4: Features completas
        features_requeridas = ['dia_semana', 'dia_mes', 'mes', 'semana_año', 
                               'es_fin_de_semana', 'es_domingo', 'dias_desde_inicio']
        features_presentes = all(col in df_sintetico.columns for col in features_requeridas)
        validaciones['features_completas'] = features_presentes
        
        logger.info(f"\n✓ Validación 4: Features temporales")
        logger.info(f"   • Features requeridas: {len(features_requeridas)}")
        logger.info(f"   • Features presentes: {sum(col in df_sintetico.columns for col in features_requeridas)}")
        logger.info(f"   • Estado: {'✓ CORRECTO' if validaciones['features_completas'] else '✗ ERROR'}")
        
        # Resumen de validación
        validaciones_ok = sum(validaciones.values())
        total_validaciones = len(validaciones) - 1  # Restar 'total_registros'
        
        logger.info(f"\n" + "=" * 80)
        logger.info(f"RESUMEN DE VALIDACIÓN: {validaciones_ok}/{total_validaciones} validaciones correctas")
        logger.info("=" * 80)
        
        if validaciones_ok == total_validaciones:
            logger.info("✓ Todos los datos sintéticos son válidos y están listos para entrenamiento")
        else:
            logger.warning("⚠ Algunas validaciones fallaron. Revisar datos generados.")
        
        return validaciones
        
    except Exception as e:
        logger.error(f"✗ Error al validar datos sintéticos: {str(e)}")
        raise


# ============================================================================
# FASE 3: PREPARACIÓN DE DATASETS PARA ENTRENAMIENTO
# ============================================================================

def preparar_dataset_para_modelo(df, nombre_dataset="Dataset"):
    """
    Prepara dataset para entrenamiento del modelo XGBoost.
    Separa features (X) y target (y).
    
    Args:
        df: DataFrame con datos de ventas
        nombre_dataset: Nombre del dataset para logging
        
    Returns:
        tuple: (X, y) Features y target separados
    """
    logger.info(f"\n📦 Preparando {nombre_dataset} para modelo XGBoost...")
    
    try:
        df = df.copy()
        
        # Si el dataset no tiene las features temporales, agregarlas
        if 'dia_semana' not in df.columns:
            logger.info(f"   • Añadiendo features temporales...")
            df['fecha'] = pd.to_datetime(df['fecha'])
            df['dia_semana'] = df['fecha'].dt.weekday
            df['dia_mes'] = df['fecha'].dt.day
            df['mes'] = df['fecha'].dt.month
            df['semana_año'] = df['fecha'].dt.isocalendar().week
            df['es_fin_de_semana'] = (df['dia_semana'] >= 5).astype(int)
            df['es_domingo'] = (df['dia_semana'] == 6).astype(int)
            
            # Calcular días desde inicio
            fecha_inicio = df['fecha'].min()
            df['dias_desde_inicio'] = (df['fecha'] - fecha_inicio).dt.days
        
        # Seleccionar features para el modelo
        features = [
            'dia_semana',
            'dia_mes', 
            'mes',
            'es_fin_de_semana',
            'dias_desde_inicio',
            'num_transacciones'
        ]
        
        # Verificar que todas las features existen
        features_faltantes = [f for f in features if f not in df.columns]
        if features_faltantes:
            raise ValueError(f"Features faltantes: {features_faltantes}")
        
        # Separar X (features) y y (target)
        X = df[features].copy()
        y = df['total_ventas'].copy()
        
        # Remover domingos (ventas = 0) para no sesgar el modelo
        mask_no_domingo = df['es_domingo'] == 0
        X = X[mask_no_domingo]
        y = y[mask_no_domingo]
        
        logger.info(f"   ✓ Dataset preparado:")
        logger.info(f"     • Features: {features}")
        logger.info(f"     • Registros totales: {len(df)}")
        logger.info(f"     • Registros útiles (sin domingos): {len(X)}")
        logger.info(f"     • Target: total_ventas")
        logger.info(f"     • Rango target: ${y.min():,.2f} - ${y.max():,.2f}")
        
        return X, y
        
    except Exception as e:
        logger.error(f"✗ Error al preparar dataset: {str(e)}")
        raise


def normalizar_features(X_train, X_test=None, X_adicional=None):
    """
    Normaliza features usando StandardScaler.
    
    Args:
        X_train: Features de entrenamiento
        X_test: Features de test (opcional)
        X_adicional: Features adicionales para transformar (opcional)
        
    Returns:
        tuple: (scaler, X_train_scaled, X_test_scaled, X_adicional_scaled)
    """
    logger.info(f"\n🔧 Normalizando features con StandardScaler...")
    
    try:
        scaler = StandardScaler()
        
        # Ajustar y transformar train
        X_train_scaled = scaler.fit_transform(X_train)
        X_train_scaled = pd.DataFrame(X_train_scaled, columns=X_train.columns, index=X_train.index)
        logger.info(f"   ✓ Train escalado: {X_train_scaled.shape}")
        
        # Transformar test si existe
        X_test_scaled = None
        if X_test is not None:
            X_test_scaled = scaler.transform(X_test)
            X_test_scaled = pd.DataFrame(X_test_scaled, columns=X_test.columns, index=X_test.index)
            logger.info(f"   ✓ Test escalado: {X_test_scaled.shape}")
        
        # Transformar adicional si existe
        X_adicional_scaled = None
        if X_adicional is not None:
            X_adicional_scaled = scaler.transform(X_adicional)
            X_adicional_scaled = pd.DataFrame(X_adicional_scaled, columns=X_adicional.columns, index=X_adicional.index)
            logger.info(f"   ✓ Dataset adicional escalado: {X_adicional_scaled.shape}")
        
        # Guardar scaler
        scaler_path = MODELS_DIR / 'scaler.pkl'
        with open(scaler_path, 'wb') as f:
            pickle.dump(scaler, f)
        logger.info(f"   ✓ Scaler guardado en: {scaler_path}")
        
        return scaler, X_train_scaled, X_test_scaled, X_adicional_scaled
        
    except Exception as e:
        logger.error(f"✗ Error al normalizar features: {str(e)}")
        raise


# ============================================================================
# FASE 4: ENTRENAMIENTO DE MODELOS XGBOOST
# ============================================================================

def entrenar_modelo_xgboost(X_train, y_train, X_test=None, y_test=None, 
                            nombre_modelo="XGBoost", params=None):
    """
    Entrena un modelo XGBoost y calcula métricas.
    
    Args:
        X_train: Features de entrenamiento
        y_train: Target de entrenamiento
        X_test: Features de test (opcional)
        y_test: Target de test (opcional)
        nombre_modelo: Nombre del modelo
        params: Diccionario de hiperparámetros
        
    Returns:
        dict: Diccionario con modelo y métricas
    """
    logger.info(f"\n🤖 Entrenando modelo: {nombre_modelo}")
    logger.info("-" * 80)
    
    try:
        # Parámetros por defecto si no se especifican
        if params is None:
            params = {
                'objective': 'reg:squarederror',
                'max_depth': 6,
                'learning_rate': 0.1,
                'n_estimators': 100,
                'subsample': 0.8,
                'colsample_bytree': 0.8,
                'random_state': 42,
                'verbosity': 0
            }
        
        logger.info(f"📋 Hiperparámetros:")
        for key, value in params.items():
            logger.info(f"   • {key}: {value}")
        
        # Crear y entrenar modelo
        logger.info(f"\n🔄 Entrenando modelo...")
        modelo = xgb.XGBRegressor(**params)
        modelo.fit(X_train, y_train)
        logger.info(f"   ✓ Entrenamiento completado")
        
        # Predicciones y métricas
        resultados = {
            'modelo': modelo,
            'nombre': nombre_modelo,
            'params': params,
            'num_datos_entrenamiento': len(X_train)
        }
        
        # Métricas en entrenamiento
        y_train_pred = modelo.predict(X_train)
        mae_train = mean_absolute_error(y_train, y_train_pred)
        rmse_train = np.sqrt(mean_squared_error(y_train, y_train_pred))
        r2_train = r2_score(y_train, y_train_pred)
        
        resultados['mae_train'] = mae_train
        resultados['rmse_train'] = rmse_train
        resultados['r2_train'] = r2_train
        
        logger.info(f"\n📊 Métricas en Entrenamiento:")
        logger.info(f"   • MAE:  ${mae_train:,.2f}")
        logger.info(f"   • RMSE: ${rmse_train:,.2f}")
        logger.info(f"   • R²:   {r2_train:.4f}")
        
        # Métricas en test si existe
        if X_test is not None and y_test is not None:
            y_test_pred = modelo.predict(X_test)
            mae_test = mean_absolute_error(y_test, y_test_pred)
            rmse_test = np.sqrt(mean_squared_error(y_test, y_test_pred))
            r2_test = r2_score(y_test, y_test_pred)
            
            resultados['mae_test'] = mae_test
            resultados['rmse_test'] = rmse_test
            resultados['r2_test'] = r2_test
            resultados['num_datos_test'] = len(X_test)
            
            logger.info(f"\n📊 Métricas en Test:")
            logger.info(f"   • MAE:  ${mae_test:,.2f}")
            logger.info(f"   • RMSE: ${rmse_test:,.2f}")
            logger.info(f"   • R²:   {r2_test:.4f}")
        
        # Validación cruzada
        logger.info(f"\n🔄 Validación cruzada (5-fold)...")
        cv_scores = cross_val_score(modelo, X_train, y_train, 
                                     cv=min(5, len(X_train)), 
                                     scoring='neg_mean_absolute_error',
                                     n_jobs=-1)
        cv_mae = -cv_scores.mean()
        cv_std = cv_scores.std()
        
        resultados['cv_mae'] = cv_mae
        resultados['cv_std'] = cv_std
        
        logger.info(f"   • CV MAE: ${cv_mae:,.2f} (±${cv_std:,.2f})")
        
        # Feature importance
        importance = modelo.feature_importances_
        feature_names = X_train.columns
        feature_importance = pd.DataFrame({
            'feature': feature_names,
            'importance': importance
        }).sort_values('importance', ascending=False)
        
        resultados['feature_importance'] = feature_importance
        
        logger.info(f"\n📊 Importancia de Features:")
        for idx, row in feature_importance.iterrows():
            logger.info(f"   • {row['feature']}: {row['importance']:.4f}")
        
        logger.info(f"\n✓ Modelo {nombre_modelo} entrenado exitosamente")
        
        return resultados
        
    except Exception as e:
        logger.error(f"✗ Error al entrenar modelo: {str(e)}")
        raise


def guardar_modelo(modelo, nombre_archivo):
    """
    Guarda modelo entrenado en disco.
    
    Args:
        modelo: Modelo XGBoost entrenado
        nombre_archivo: Nombre del archivo (sin ruta)
    """
    try:
        ruta_completa = fMODELS_DIR / '{nombre_archivo}'
        with open(ruta_completa, 'wb') as f:
            pickle.dump(modelo, f)
        logger.info(f"   ✓ Modelo guardado en: {ruta_completa}")
    except Exception as e:
        logger.error(f"✗ Error al guardar modelo: {str(e)}")


# ============================================================================
# FASE 5: COMPARACIÓN DE MÉTRICAS
# ============================================================================

def comparar_metricas(resultados_5dias, resultados_6meses):
    """
    Compara métricas entre ambos modelos y genera reporte.
    
    Args:
        resultados_5dias: Resultados del modelo de 5 días
        resultados_6meses: Resultados del modelo de 6 meses
        
    Returns:
        pandas.DataFrame: Tabla comparativa
    """
    logger.info("\n" + "=" * 80)
    logger.info("COMPARACIÓN DE MÉTRICAS - MODELO 5 DÍAS VS 6 MESES")
    logger.info("=" * 80)
    
    try:
        # Crear tabla comparativa
        datos_comparacion = {
            'Modelo': [
                'Modelo 5 Días Reales',
                'Modelo 6 Meses Sintéticos'
            ],
            'Datos Entrenamiento': [
                resultados_5dias['num_datos_entrenamiento'],
                resultados_6meses['num_datos_entrenamiento']
            ],
            'MAE Train': [
                f"${resultados_5dias['mae_train']:,.2f}",
                f"${resultados_6meses['mae_train']:,.2f}"
            ],
            'RMSE Train': [
                f"${resultados_5dias['rmse_train']:,.2f}",
                f"${resultados_6meses['rmse_train']:,.2f}"
            ],
            'R² Train': [
                f"{resultados_5dias['r2_train']:.4f}",
                f"{resultados_6meses['r2_train']:.4f}"
            ]
        }
        
        # Añadir métricas de test si existen
        if 'mae_test' in resultados_6meses:
            datos_comparacion['MAE Test'] = [
                'N/A',
                f"${resultados_6meses['mae_test']:,.2f}"
            ]
            datos_comparacion['RMSE Test'] = [
                'N/A',
                f"${resultados_6meses['rmse_test']:,.2f}"
            ]
        
        df_comparacion = pd.DataFrame(datos_comparacion)
        
        # Mostrar tabla
        logger.info("\n" + tabulate(df_comparacion, headers='keys', tablefmt='grid', showindex=False))
        
        # Calcular mejoras
        mejora_mae = ((resultados_5dias['mae_train'] - resultados_6meses['mae_train']) 
                      / resultados_5dias['mae_train']) * 100
        mejora_rmse = ((resultados_5dias['rmse_train'] - resultados_6meses['rmse_train']) 
                       / resultados_5dias['rmse_train']) * 100
        
        logger.info(f"\n📈 ANÁLISIS DE MEJORA:")
        logger.info(f"   • Mejora en MAE:  {mejora_mae:+.2f}% {'✓' if mejora_mae > 0 else '✗'}")
        logger.info(f"   • Mejora en RMSE: {mejora_rmse:+.2f}% {'✓' if mejora_rmse > 0 else '✗'}")
        logger.info(f"   • Factor de datos: {resultados_6meses['num_datos_entrenamiento'] / resultados_5dias['num_datos_entrenamiento']:.1f}x más datos")
        
        # Interpretación
        logger.info(f"\n💡 INTERPRETACIÓN:")
        if mejora_mae > 0 and mejora_rmse > 0:
            logger.info(f"   ✓ El modelo entrenado con 6 meses de datos sintéticos tiene MEJOR")
            logger.info(f"     rendimiento que el modelo con 5 días reales.")
            logger.info(f"   ✓ Esto demuestra que aumentar el volumen de datos mejora la precisión")
            logger.info(f"     del modelo, incluso usando datos sintéticos bien generados.")
        else:
            logger.info(f"   ⚠ El modelo de 6 meses no superó al de 5 días en todas las métricas.")
            logger.info(f"   ⚠ Esto puede deberse a:")
            logger.info(f"     - Datos sintéticos no suficientemente realistas")
            logger.info(f"     - Overfitting en el modelo de 5 días")
            logger.info(f"     - Necesidad de ajustar hiperparámetros")
        
        # Guardar reporte
        reporte_path = RESULTS_DIR / 'comparacion_metricas.txt'
        with open(reporte_path, 'w', encoding='utf-8') as f:
            f.write("=" * 80 + "\n")
            f.write("COMPARACIÓN DE MÉTRICAS - MODELO 5 DÍAS VS 6 MESES\n")
            f.write("=" * 80 + "\n\n")
            f.write(tabulate(df_comparacion, headers='keys', tablefmt='grid', showindex=False))
            f.write(f"\n\nMejora en MAE: {mejora_mae:+.2f}%\n")
            f.write(f"Mejora en RMSE: {mejora_rmse:+.2f}%\n")
        
        logger.info(f"\n✓ Reporte guardado en: {reporte_path}")
        
        return df_comparacion
        
    except Exception as e:
        logger.error(f"✗ Error al comparar métricas: {str(e)}")
        raise


# ============================================================================
# FASE 6: CURVAS DE APRENDIZAJE (LEARNING CURVES)
# ============================================================================

def generar_learning_curve(modelo, X, y, nombre_modelo="Modelo"):
    """
    Genera datos de curva de aprendizaje para un modelo.
    
    Args:
        modelo: Modelo XGBoost
        X: Features
        y: Target
        nombre_modelo: Nombre del modelo
        
    Returns:
        dict: Datos de la curva de aprendizaje
    """
    logger.info(f"\n📈 Generando curva de aprendizaje para: {nombre_modelo}")
    
    try:
        # Definir tamaños de entrenamiento
        train_sizes = np.linspace(0.1, 1.0, 10)
        
        logger.info(f"   • Calculando scores para 10 tamaños de muestra...")
        logger.info(f"   • Validación cruzada: 3-fold")
        
        # Calcular learning curve
        train_sizes_abs, train_scores, val_scores = learning_curve(
            estimator=modelo,
            X=X,
            y=y,
            train_sizes=train_sizes,
            cv=min(3, len(X) // 2),  # Mínimo 3-fold o mitad de datos
            scoring='neg_mean_absolute_error',
            n_jobs=-1,
            random_state=42
        )
        
        # Convertir a valores positivos (MAE)
        train_scores_mean = -train_scores.mean(axis=1)
        train_scores_std = train_scores.std(axis=1)
        val_scores_mean = -val_scores.mean(axis=1)
        val_scores_std = val_scores.std(axis=1)
        
        logger.info(f"   ✓ Curva de aprendizaje generada")
        logger.info(f"   • Error inicial (10% datos): ${val_scores_mean[0]:,.2f}")
        logger.info(f"   • Error final (100% datos): ${val_scores_mean[-1]:,.2f}")
        logger.info(f"   • Mejora: {((val_scores_mean[0] - val_scores_mean[-1]) / val_scores_mean[0] * 100):.2f}%")
        
        return {
            'nombre': nombre_modelo,
            'train_sizes': train_sizes_abs,
            'train_scores_mean': train_scores_mean,
            'train_scores_std': train_scores_std,
            'val_scores_mean': val_scores_mean,
            'val_scores_std': val_scores_std
        }
        
    except Exception as e:
        logger.error(f"✗ Error al generar curva de aprendizaje: {str(e)}")
        raise


# ============================================================================
# FASE 7: VISUALIZACIÓN CON MATPLOTLIB
# ============================================================================

def crear_grafica_learning_curves(datos_5dias, datos_6meses):
    """
    Crea gráfica comparativa de curvas de aprendizaje.
    
    Args:
        datos_5dias: Datos de learning curve del modelo de 5 días
        datos_6meses: Datos de learning curve del modelo de 6 meses
    """
    logger.info("\n🎨 Creando gráfica de curvas de aprendizaje...")
    
    try:
        # Configurar estilo
        plt.style.use('seaborn-v0_8-darkgrid')
        sns.set_palette("husl")
        
        # Crear figura con 2 subplots
        fig, axes = plt.subplots(1, 2, figsize=(16, 6))
        fig.suptitle('Curvas de Aprendizaje - Comparación de Modelos XGBoost', 
                     fontsize=16, fontweight='bold', y=1.02)
        
        # ====== SUBPLOT 1: Modelo 5 Días ======
        ax1 = axes[0]
        
        # Plot training score
        ax1.plot(datos_5dias['train_sizes'], datos_5dias['train_scores_mean'], 
                'o-', color='#2E86AB', linewidth=2, markersize=6, label='Error Entrenamiento')
        ax1.fill_between(datos_5dias['train_sizes'], 
                         datos_5dias['train_scores_mean'] - datos_5dias['train_scores_std'],
                         datos_5dias['train_scores_mean'] + datos_5dias['train_scores_std'],
                         alpha=0.15, color='#2E86AB')
        
        # Plot validation score
        ax1.plot(datos_5dias['train_sizes'], datos_5dias['val_scores_mean'], 
                'o-', color='#A23B72', linewidth=2, markersize=6, label='Error Validación')
        ax1.fill_between(datos_5dias['train_sizes'],
                         datos_5dias['val_scores_mean'] - datos_5dias['val_scores_std'],
                         datos_5dias['val_scores_mean'] + datos_5dias['val_scores_std'],
                         alpha=0.15, color='#A23B72')
        
        ax1.set_xlabel('Número de Muestras de Entrenamiento', fontsize=11, fontweight='bold')
        ax1.set_ylabel('MAE (Mean Absolute Error) $', fontsize=11, fontweight='bold')
        ax1.set_title(f'{datos_5dias["nombre"]}\n({datos_5dias["train_sizes"][-1]} muestras)', 
                     fontsize=13, fontweight='bold', pad=10)
        ax1.legend(loc='best', frameon=True, shadow=True)
        ax1.grid(True, alpha=0.3)
        
        # ====== SUBPLOT 2: Modelo 6 Meses ======
        ax2 = axes[1]
        
        # Plot training score
        ax2.plot(datos_6meses['train_sizes'], datos_6meses['train_scores_mean'],
                'o-', color='#2E86AB', linewidth=2, markersize=6, label='Error Entrenamiento')
        ax2.fill_between(datos_6meses['train_sizes'],
                         datos_6meses['train_scores_mean'] - datos_6meses['train_scores_std'],
                         datos_6meses['train_scores_mean'] + datos_6meses['train_scores_std'],
                         alpha=0.15, color='#2E86AB')
        
        # Plot validation score
        ax2.plot(datos_6meses['train_sizes'], datos_6meses['val_scores_mean'],
                'o-', color='#A23B72', linewidth=2, markersize=6, label='Error Validación')
        ax2.fill_between(datos_6meses['train_sizes'],
                         datos_6meses['val_scores_mean'] - datos_6meses['val_scores_std'],
                         datos_6meses['val_scores_mean'] + datos_6meses['val_scores_std'],
                         alpha=0.15, color='#A23B72')
        
        ax2.set_xlabel('Número de Muestras de Entrenamiento', fontsize=11, fontweight='bold')
        ax2.set_ylabel('MAE (Mean Absolute Error) $', fontsize=11, fontweight='bold')
        ax2.set_title(f'{datos_6meses["nombre"]}\n({datos_6meses["train_sizes"][-1]} muestras)',
                     fontsize=13, fontweight='bold', pad=10)
        ax2.legend(loc='best', frameon=True, shadow=True)
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        
        # Guardar gráfica
        output_png = RESULTS_DIR / 'learning_curves_comparacion.png'
        output_pdf = RESULTS_DIR / 'learning_curves_comparacion.pdf'
        
        plt.savefig(output_png, dpi=300, bbox_inches='tight')
        plt.savefig(output_pdf, bbox_inches='tight')
        
        logger.info(f"   ✓ Gráfica guardada en:")
        logger.info(f"     - {output_png}")
        logger.info(f"     - {output_pdf}")
        
        plt.close()
        
    except Exception as e:
        logger.error(f"✗ Error al crear gráfica de learning curves: {str(e)}")
        raise


def crear_grafica_comparacion_errores(resultados_5dias, resultados_6meses):
    """
    Crea gráfica de barras comparando errores MAE y RMSE.
    
    Args:
        resultados_5dias: Resultados del modelo de 5 días
        resultados_6meses: Resultados del modelo de 6 meses
    """
    logger.info("\n🎨 Creando gráfica de comparación de errores...")
    
    try:
        # Configurar estilo
        plt.style.use('seaborn-v0_8-whitegrid')
        
        # Datos para la gráfica
        modelos = ['5 Días\nReales', '6 Meses\nSintéticos']
        mae_values = [resultados_5dias['mae_train'], resultados_6meses['mae_train']]
        rmse_values = [resultados_5dias['rmse_train'], resultados_6meses['rmse_train']]
        
        # Crear figura
        fig, ax = plt.subplots(figsize=(10, 6))
        
        x = np.arange(len(modelos))
        width = 0.35
        
        # Barras
        bars1 = ax.bar(x - width/2, mae_values, width, label='MAE', 
                       color='#FF6B6B', edgecolor='black', linewidth=1.2)
        bars2 = ax.bar(x + width/2, rmse_values, width, label='RMSE',
                       color='#4ECDC4', edgecolor='black', linewidth=1.2)
        
        # Añadir valores encima de las barras
        for bar in bars1:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'${height:,.0f}', ha='center', va='bottom', 
                   fontweight='bold', fontsize=10)
        
        for bar in bars2:
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                   f'${height:,.0f}', ha='center', va='bottom',
                   fontweight='bold', fontsize=10)
        
        # Labels y título
        ax.set_xlabel('Modelo', fontsize=12, fontweight='bold')
        ax.set_ylabel('Error ($)', fontsize=12, fontweight='bold')
        ax.set_title('Comparación de Errores: MAE y RMSE\nModelo 5 Días vs 6 Meses',
                    fontsize=14, fontweight='bold', pad=15)
        ax.set_xticks(x)
        ax.set_xticklabels(modelos, fontsize=11, fontweight='bold')
        ax.legend(loc='upper right', frameon=True, shadow=True, fontsize=11)
        ax.grid(axis='y', alpha=0.3)
        
        plt.tight_layout()
        
        # Guardar gráfica
        output_path = RESULTS_DIR / 'comparacion_errores.png'
        plt.savefig(output_path, dpi=300, bbox_inches='tight')
        
        logger.info(f"   ✓ Gráfica guardada en: {output_path}")
        
        plt.close()
        
    except Exception as e:
        logger.error(f"✗ Error al crear gráfica de comparación: {str(e)}")
        raise


# ============================================================================
# FUNCIÓN PRINCIPAL
# ============================================================================

def main():
    """
    Función principal que ejecuta todas las fases del análisis (1-7).
    """
    logger.info("\n")
    logger.info("╔" + "═" * 78 + "╗")
    logger.info("║" + " " * 78 + "║")
    logger.info("║" + "  ANÁLISIS COMPLETO DE ABASTECIMIENTO CON XGBOOST".center(78) + "║")
    logger.info("║" + "  Fases 1-7: Extracción, Generación, Entrenamiento y Visualización".center(78) + "║")
    logger.info("║" + " " * 78 + "║")
    logger.info("╚" + "═" * 78 + "╝")
    logger.info(f"\nFecha de ejecución: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    conn = None
    
    try:
        # ====================================================================
        # FASE 1: EXTRACCIÓN Y ANÁLISIS DE DATOS REALES
        # ====================================================================
        
        logger.info("\n" + "🔵" * 40)
        logger.info("INICIANDO FASE 1: EXTRACCIÓN DE DATOS REALES")
        logger.info("🔵" * 40 + "\n")
        
        conn = conectar_base_datos()
        df_fechas = explorar_fechas_con_datos(conn)
        
        if df_fechas.empty:
            logger.error("✗ No hay datos suficientes en la base de datos para continuar")
            return
        
        fecha_inicio = df_fechas.iloc[0]['fecha']
        logger.info(f"\n📅 Fecha seleccionada para análisis: {fecha_inicio}")
        
        df_5_dias = extraer_ventas_5_dias(conn, fecha_inicio)
        
        if df_5_dias.empty or len(df_5_dias) < 3:
            logger.warning(f"⚠ Solo se encontraron {len(df_5_dias)} días con datos")
        
        metricas = analizar_tendencia_5_dias(df_5_dias)
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 1 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        if conn:
            conn.close()
            conn = None
        
        # ====================================================================
        # FASE 2: GENERACIÓN DE DATOS SINTÉTICOS
        # ====================================================================
        
        logger.info("\n" + "🟢" * 40)
        logger.info("INICIANDO FASE 2: GENERACIÓN DE DATOS SINTÉTICOS")
        logger.info("🟢" * 40 + "\n")
        
        fecha_inicio_simulacion = df_5_dias.iloc[-1]['fecha']
        df_sintetico = generar_datos_sinteticos_6_meses(
            df_base=df_5_dias,
            fecha_inicio=fecha_inicio_simulacion,
            meses=6
        )
        
        validaciones = validar_datos_sinteticos(df_sintetico)
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 2 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # FASE 3: PREPARACIÓN DE DATASETS
        # ====================================================================
        
        logger.info("\n" + "🟡" * 40)
        logger.info("INICIANDO FASE 3: PREPARACIÓN DE DATASETS")
        logger.info("🟡" * 40 + "\n")
        
        # Preparar dataset de 5 días
        X_5dias, y_5dias = preparar_dataset_para_modelo(df_5_dias, "5 Días Reales")
        
        # Preparar dataset de 6 meses y dividir en train/test
        X_6meses, y_6meses = preparar_dataset_para_modelo(df_sintetico, "6 Meses Sintéticos")
        
        logger.info(f"\n🔀 Dividiendo dataset de 6 meses en train/test (80%-20%)...")
        X_train_6m, X_test_6m, y_train_6m, y_test_6m = train_test_split(
            X_6meses, y_6meses, test_size=0.2, random_state=42
        )
        logger.info(f"   ✓ Train: {len(X_train_6m)} muestras")
        logger.info(f"   ✓ Test: {len(X_test_6m)} muestras")
        
        # Normalizar features
        scaler, X_train_6m_scaled, X_test_6m_scaled, X_5dias_scaled = normalizar_features(
            X_train_6m, X_test_6m, X_5dias
        )
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 3 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # FASE 4: ENTRENAMIENTO DE MODELOS
        # ====================================================================
        
        logger.info("\n" + "🟠" * 40)
        logger.info("INICIANDO FASE 4: ENTRENAMIENTO DE MODELOS XGBOOST")
        logger.info("🟠" * 40 + "\n")
        
        # Parámetros de XGBoost
        params_xgb = {
            'objective': 'reg:squarederror',
            'max_depth': 6,
            'learning_rate': 0.1,
            'n_estimators': 100,
            'subsample': 0.8,
            'colsample_bytree': 0.8,
            'random_state': 42,
            'verbosity': 0
        }
        
        # Entrenar modelo con 5 días
        logger.info(f"\n{'='*80}")
        resultados_5dias = entrenar_modelo_xgboost(
            X_5dias_scaled, y_5dias,
            nombre_modelo="Modelo 5 Días Reales",
            params=params_xgb
        )
        guardar_modelo(resultados_5dias['modelo'], 'modelo_xgboost_5dias.pkl')
        
        # Entrenar modelo con 6 meses
        logger.info(f"\n{'='*80}")
        resultados_6meses = entrenar_modelo_xgboost(
            X_train_6m_scaled, y_train_6m,
            X_test_6m_scaled, y_test_6m,
            nombre_modelo="Modelo 6 Meses Sintéticos",
            params=params_xgb
        )
        guardar_modelo(resultados_6meses['modelo'], 'modelo_xgboost_6meses.pkl')
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 4 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # FASE 5: COMPARACIÓN DE MÉTRICAS
        # ====================================================================
        
        logger.info("\n" + "🟣" * 40)
        logger.info("INICIANDO FASE 5: COMPARACIÓN DE MÉTRICAS")
        logger.info("🟣" * 40 + "\n")
        
        df_comparacion = comparar_metricas(resultados_5dias, resultados_6meses)
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 5 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # FASE 6: CURVAS DE APRENDIZAJE
        # ====================================================================
        
        logger.info("\n" + "🔴" * 40)
        logger.info("INICIANDO FASE 6: GENERACIÓN DE CURVAS DE APRENDIZAJE")
        logger.info("🔴" * 40 + "\n")
        
        # Learning curve para modelo 5 días
        modelo_5dias_nuevo = xgb.XGBRegressor(**params_xgb)
        datos_lc_5dias = generar_learning_curve(
            modelo_5dias_nuevo, X_5dias_scaled, y_5dias,
            nombre_modelo="Modelo 5 Días Reales"
        )
        
        # Learning curve para modelo 6 meses
        modelo_6meses_nuevo = xgb.XGBRegressor(**params_xgb)
        datos_lc_6meses = generar_learning_curve(
            modelo_6meses_nuevo, X_train_6m_scaled, y_train_6m,
            nombre_modelo="Modelo 6 Meses Sintéticos"
        )
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 6 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # FASE 7: VISUALIZACIÓN
        # ====================================================================
        
        logger.info("\n" + "⚪" * 40)
        logger.info("INICIANDO FASE 7: GENERACIÓN DE VISUALIZACIONES")
        logger.info("⚪" * 40 + "\n")
        
        # Crear gráfica de learning curves
        crear_grafica_learning_curves(datos_lc_5dias, datos_lc_6meses)
        
        # Crear gráfica de comparación de errores
        crear_grafica_comparacion_errores(resultados_5dias, resultados_6meses)
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ FASE 7 COMPLETADA")
        logger.info("=" * 80 + "\n")
        
        # ====================================================================
        # RESUMEN FINAL
        # ====================================================================
        
        logger.info("\n" + "🎉" * 40)
        logger.info("ANÁLISIS COMPLETADO - TODAS LAS FASES (1-7)")
        logger.info("🎉" * 40 + "\n")
        
        logger.info("📊 RESUMEN COMPLETO:")
        logger.info("=" * 80)
        logger.info("\n📁 ARCHIVOS GENERADOS:")
        logger.info("   Datos:")
        logger.info("   • data/ventas_5_dias_reales.csv")
        logger.info("   • data/ventas_6_meses_sinteticas.csv")
        logger.info("")
        logger.info("   Modelos:")
        logger.info("   • models/modelo_xgboost_5dias.pkl")
        logger.info("   • models/modelo_xgboost_6meses.pkl")
        logger.info("   • models/scaler.pkl")
        logger.info("")
        logger.info("   Reportes:")
        logger.info("   • results/analisis_descriptivo_5_dias.txt")
        logger.info("   • results/comparacion_metricas.txt")
        logger.info("")
        logger.info("   Visualizaciones:")
        logger.info("   • results/learning_curves_comparacion.png")
        logger.info("   • results/learning_curves_comparacion.pdf")
        logger.info("   • results/comparacion_errores.png")
        logger.info("   • results/ejecucion.log")
        
        logger.info("\n📈 MÉTRICAS FINALES:")
        logger.info(f"   Modelo 5 Días:")
        logger.info(f"   • MAE:  ${resultados_5dias['mae_train']:,.2f}")
        logger.info(f"   • RMSE: ${resultados_5dias['rmse_train']:,.2f}")
        logger.info(f"   • R²:   {resultados_5dias['r2_train']:.4f}")
        logger.info(f"")
        logger.info(f"   Modelo 6 Meses:")
        logger.info(f"   • MAE Test:  ${resultados_6meses.get('mae_test', 0):,.2f}")
        logger.info(f"   • RMSE Test: ${resultados_6meses.get('rmse_test', 0):,.2f}")
        logger.info(f"   • R² Test:   {resultados_6meses.get('r2_test', 0):.4f}")
        
        # Calcular mejora
        mejora = ((resultados_5dias['mae_train'] - resultados_6meses['mae_train']) 
                  / resultados_5dias['mae_train']) * 100
        
        logger.info(f"\n💡 CONCLUSIÓN:")
        if mejora > 0:
            logger.info(f"   ✓ El modelo con MÁS DATOS (6 meses) tiene {mejora:.2f}% MEJOR")
            logger.info(f"     rendimiento que el modelo con MENOS DATOS (5 días).")
            logger.info(f"   ✓ Esto demuestra empíricamente que AUMENTAR EL VOLUMEN DE DATOS")
            logger.info(f"     mejora la capacidad predictiva del modelo XGBoost.")
        else:
            logger.info(f"   ⚠ El modelo de 5 días tuvo mejor rendimiento")
            logger.info(f"   ⚠ Posibles razones: overfitting, datos sintéticos no realistas")
        
        logger.info("\n" + "=" * 80)
        logger.info("✓ ¡ANÁLISIS FINALIZADO CON ÉXITO!")
        logger.info("=" * 80 + "\n")
        
    except Exception as e:
        logger.error(f"\n✗ Error en la ejecución: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        sys.exit(1)
        
    finally:
        if conn:
            conn.close()
            logger.info("✓ Conexión a base de datos cerrada")


if __name__ == "__main__":
    main()
