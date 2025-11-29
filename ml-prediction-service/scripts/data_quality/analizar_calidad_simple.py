#!/usr/bin/env python3
"""
Análisis Simplificado de Calidad de Datos Reales
Sistema POS & Gestión Integral - Motor de Predicciones ML

Descripción:
    Script de análisis de calidad usando solo la biblioteca estándar de Python.
    No requiere pandas, numpy ni otras dependencias externas.

Uso:
    python3 analizar_calidad_simple.py
"""

import csv
import statistics
from collections import defaultdict, Counter
from datetime import datetime
import sys

def cargar_csv(archivo):
    """Carga un archivo CSV y retorna los datos como lista de diccionarios."""
    datos = []
    with open(archivo, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            datos.append(row)
    return datos

def analizar_completitud(datos, columnas):
    """Analiza la completitud de los datos."""
    total_celdas = len(datos) * len(columnas)
    celdas_vacias = 0
    faltantes_por_columna = defaultdict(int)
    
    for row in datos:
        for col in columnas:
            if not row.get(col) or row.get(col).strip() == '':
                celdas_vacias += 1
                faltantes_por_columna[col] += 1
    
    completitud = ((total_celdas - celdas_vacias) / total_celdas) * 100
    
    return {
        'completitud_pct': completitud,
        'total_celdas': total_celdas,
        'celdas_vacias': celdas_vacias,
        'faltantes_por_columna': dict(faltantes_por_columna)
    }

def analizar_columna_numerica(valores):
    """Analiza estadísticas de una columna numérica."""
    valores_numericos = []
    for v in valores:
        try:
            valores_numericos.append(float(v))
        except (ValueError, TypeError):
            pass
    
    if not valores_numericos:
        return None
    
    valores_numericos.sort()
    n = len(valores_numericos)
    
    # Estadísticas básicas
    minimo = min(valores_numericos)
    maximo = max(valores_numericos)
    media = statistics.mean(valores_numericos)
    mediana = statistics.median(valores_numericos)
    
    try:
        desv_std = statistics.stdev(valores_numericos)
    except:
        desv_std = 0
    
    # Detectar outliers usando IQR
    q1_idx = n // 4
    q3_idx = 3 * n // 4
    q1 = valores_numericos[q1_idx]
    q3 = valores_numericos[q3_idx]
    iqr = q3 - q1
    
    limite_inferior = q1 - 1.5 * iqr
    limite_superior = q3 + 1.5 * iqr
    
    outliers = sum(1 for v in valores_numericos if v < limite_inferior or v > limite_superior)
    outliers_pct = (outliers / n) * 100 if n > 0 else 0
    
    return {
        'count': n,
        'min': minimo,
        'max': maximo,
        'mean': media,
        'median': mediana,
        'std': desv_std,
        'outliers': outliers,
        'outliers_pct': outliers_pct
    }

def analizar_productos(datos):
    """Analiza ventas por producto."""
    ventas_por_producto = defaultdict(lambda: {
        'cantidad_total': 0,
        'num_ordenes': 0,
        'ingresos_totales': 0,
        'precios': []
    })
    
    for row in datos:
        producto = row.get('producto_nombre', 'Desconocido')
        
        try:
            cantidad = float(row.get('cantidad_pz', 0))
            precio = float(row.get('precio_venta', 0))
            total = float(row.get('total_venta', 0))
        except (ValueError, TypeError):
            continue
        
        ventas_por_producto[producto]['cantidad_total'] += cantidad
        ventas_por_producto[producto]['num_ordenes'] += 1
        ventas_por_producto[producto]['ingresos_totales'] += total
        ventas_por_producto[producto]['precios'].append(precio)
    
    # Ordenar por cantidad vendida
    productos_ordenados = sorted(
        ventas_por_producto.items(),
        key=lambda x: x[1]['cantidad_total'],
        reverse=True
    )
    
    return productos_ordenados

def calcular_puntuacion_calidad(datos, resultados_completitud):
    """Calcula una puntuación de calidad estimada."""
    
    # 1. Completitud (40% del peso)
    score_completitud = resultados_completitud['completitud_pct']
    
    # 2. Volumen de datos (30% del peso)
    # Consideramos 1000+ registros como ideal
    num_registros = len(datos)
    score_volumen = min(100, (num_registros / 1000) * 100)
    
    # 3. Consistencia de datos (30% del peso)
    # Verificar varianza de precios por producto
    productos = analizar_productos(datos)
    score_consistencia = 90  # Base
    
    productos_con_alta_varianza = 0
    for nombre, stats in productos:
        if len(stats['precios']) > 1:
            try:
                std_precio = statistics.stdev(stats['precios'])
                mean_precio = statistics.mean(stats['precios'])
                cv = (std_precio / mean_precio) * 100 if mean_precio > 0 else 0
                
                # Penalizar si el coeficiente de variación es > 10%
                if cv > 10:
                    productos_con_alta_varianza += 1
            except:
                pass
    
    # Penalizar por varianza alta (máximo 30 puntos de penalización)
    penalizacion = min(30, productos_con_alta_varianza * 3)
    score_consistencia -= penalizacion
    
    # 4. Calcular score total ponderado
    score_total = (
        score_completitud * 0.40 +
        score_volumen * 0.30 +
        score_consistencia * 0.30
    )
    
    return {
        'completitud': score_completitud,
        'volumen': score_volumen,
        'consistencia': score_consistencia,
        'total': score_total
    }

def main():
    """Función principal."""
    print("=" * 70)
    print("🎯 ANÁLISIS DE CALIDAD DE DATOS REALES")
    print("    Sistema POS & Gestión Integral - Motor de Predicciones ML")
    print("=" * 70)
    
    try:
        # Cargar datos
        print("\n📂 Cargando datos_ventas_reales.csv...")
        datos = cargar_csv('datos_ventas_reales.csv')
        print(f"✅ {len(datos)} registros cargados")
        
        if not datos:
            print("❌ No se encontraron datos en el archivo")
            return 1
        
        columnas = list(datos[0].keys())
        print(f"📊 {len(columnas)} columnas: {', '.join(columnas)}")
        
        # Análisis de completitud
        print("\n" + "=" * 70)
        print("📋 ANÁLISIS DE COMPLETITUD")
        print("=" * 70)
        
        resultados_completitud = analizar_completitud(datos, columnas)
        print(f"✅ Completitud Total: {resultados_completitud['completitud_pct']:.2f}%")
        print(f"   Total de celdas: {resultados_completitud['total_celdas']}")
        print(f"   Celdas vacías: {resultados_completitud['celdas_vacias']}")
        
        if resultados_completitud['faltantes_por_columna']:
            print(f"\n📊 Columnas con valores faltantes:")
            for col, count in resultados_completitud['faltantes_por_columna'].items():
                pct = (count / len(datos)) * 100
                print(f"   • {col}: {count} ({pct:.2f}%)")
        
        # Análisis de rango temporal
        print("\n" + "=" * 70)
        print("📅 ANÁLISIS TEMPORAL")
        print("=" * 70)
        
        fechas = [row.get('fecha_orden', '') for row in datos if row.get('fecha_orden')]
        if fechas:
            print(f"   Fecha mínima: {min(fechas)[:10]}")
            print(f"   Fecha máxima: {max(fechas)[:10]}")
            
            # Calcular días aproximados
            try:
                fecha_min = datetime.fromisoformat(min(fechas).replace('+00', ''))
                fecha_max = datetime.fromisoformat(max(fechas).replace('+00', ''))
                dias = (fecha_max - fecha_min).days
                print(f"   Rango: ~{dias} días (~{dias/30:.1f} meses)")
            except:
                pass
        
        # Análisis de productos
        print("\n" + "=" * 70)
        print("🛍️ TOP 5 PRODUCTOS MÁS VENDIDOS")
        print("=" * 70)
        
        productos = analizar_productos(datos)
        for i, (nombre, stats) in enumerate(productos[:5], 1):
            print(f"\n{i}. {nombre}")
            print(f"   Cantidad vendida: {stats['cantidad_total']:.0f} unidades")
            print(f"   Órdenes: {stats['num_ordenes']}")
            print(f"   Ingresos: ${stats['ingresos_totales']:.2f}")
            if stats['precios']:
                print(f"   Precio promedio: ${statistics.mean(stats['precios']):.2f}")
        
        # Análisis de columnas numéricas
        print("\n" + "=" * 70)
        print("📈 ANÁLISIS DE COLUMNAS NUMÉRICAS")
        print("=" * 70)
        
        columnas_numericas = ['cantidad_pz', 'precio_venta', 'stock_actual_pz', 'total_venta']
        
        for col in columnas_numericas:
            if col in columnas:
                valores = [row.get(col) for row in datos if row.get(col)]
                stats = analizar_columna_numerica(valores)
                
                if stats:
                    print(f"\n📊 {col}:")
                    print(f"   Registros: {stats['count']}")
                    print(f"   Min: {stats['min']:.2f}")
                    print(f"   Max: {stats['max']:.2f}")
                    print(f"   Media: {stats['mean']:.2f}")
                    print(f"   Mediana: {stats['median']:.2f}")
                    print(f"   Desv. Std: {stats['std']:.2f}")
                    print(f"   Outliers: {stats['outliers']} ({stats['outliers_pct']:.2f}%)")
        
        # Cálculo de puntuación de calidad
        print("\n" + "=" * 70)
        print("🎯 PUNTUACIÓN DE CALIDAD DE DATOS")
        print("=" * 70)
        
        scores = calcular_puntuacion_calidad(datos, resultados_completitud)
        
        print(f"\n📊 Desglose por Componente:")
        print(f"   • Completitud:    {scores['completitud']:.2f}/100 (40% peso)")
        print(f"   • Volumen:        {scores['volumen']:.2f}/100 (30% peso)")
        print(f"   • Consistencia:   {scores['consistencia']:.2f}/100 (30% peso)")
        
        print(f"\n⭐ PUNTUACIÓN TOTAL: {scores['total']:.2f}/100")
        
        # Comparación con datos sintéticos
        print("\n" + "=" * 70)
        print("📊 COMPARACIÓN CON DATOS SINTÉTICOS")
        print("=" * 70)
        
        score_sinteticos = 48.82
        mejora = scores['total'] - score_sinteticos
        
        print(f"   Datos Reales:     {scores['total']:.2f}/100")
        print(f"   Datos Sintéticos: {score_sinteticos}/100")
        print(f"   Mejora:           {'+' if mejora >= 0 else ''}{mejora:.2f} puntos")
        
        if mejora > 0:
            print(f"\n✅ Los datos reales son {mejora:.1f}% mejores que los sintéticos")
        else:
            print(f"\n⚠️ Los datos reales necesitan mejoras")
        
        print("\n" + "=" * 70)
        print("✅ Análisis completado exitosamente")
        print("=" * 70)
        
        return 0
        
    except FileNotFoundError as e:
        print(f"\n❌ Error: No se encontró el archivo 'datos_ventas_reales.csv'")
        print(f"💡 Asegúrate de ejecutar primero './extraer_datos_reales.sh'")
        return 1
    except Exception as e:
        print(f"\n❌ Error durante el análisis: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    sys.exit(main())
