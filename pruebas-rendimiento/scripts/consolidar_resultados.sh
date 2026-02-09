#!/bin/bash
# Script de consolidación de resultados
# Genera CSVs consolidados y reporte ejecutivo

set -euo pipefail

# Cargar configuración
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config.env"
source "$SCRIPT_DIR/funciones_estadisticas.sh"

# ==============================================
# FUNCIONES DE LOGGING
# ==============================================
log_info() {
    echo -e "[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_INFO}INFO${COLOR_RESET}: $*"
}

log_success() {
    echo -e "[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_SUCCESS}✓${COLOR_RESET}: $*"
}

log_error() {
    echo -e "[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_ERROR}✗${COLOR_RESET}: $*" >&2
}

# ==============================================
# FUNCIÓN: Generar Resumen de Latencias por Módulo
# ==============================================
generar_resumen_latencias() {
    log_info "Generando resumen de latencias por módulo..."
    
    local output_file="$OUTPUT_DIR/RESUMEN_LATENCIAS_POR_MODULO_$TIMESTAMP.csv"
    
    # Crear header
    echo "\"Comunicación\",\"Promedio (s)\",\"Mínimo (s)\",\"Máximo (s)\",\"Desv. Estándar\",\"P95 (s)\",\"P99 (s)\",\"Iteraciones\",\"Descripción\"" > "$output_file"
    
    # Buscar todos los CSVs numerados
    for csv in "$OUTPUT_DIR"/[0-9][0-9]_*.csv; do
        if [ -f "$csv" ]; then
            local nombre_archivo=$(basename "$csv")
            local promedio=$(extraer_promedio_csv "$csv" 2>/dev/null || echo "N/A")
            local minimo=$(extraer_minimo_csv "$csv" 2>/dev/null || echo "N/A")
            local maximo=$(extraer_maximo_csv "$csv" 2>/dev/null || echo "N/A")
            local desvest=$(extraer_desvest_csv "$csv" 2>/dev/null || echo "N/A")
            local p95=$(extraer_p95_csv "$csv" 2>/dev/null || echo "N/A")
            local p99=$(extraer_p99_csv "$csv" 2>/dev/null || echo "N/A")
            
            # Extraer descripción del nombre de archivo
            local descripcion=$(echo "$nombre_archivo" | sed 's/^[0-9]*_//; s/_[0-9]*\.csv$//' | tr '_' ' ')
            
            echo "\"$descripcion\",\"$promedio\",\"$minimo\",\"$maximo\",\"$desvest\",\"$p95\",\"$p99\",\"$ITERATIONS\",\"Prueba de rendimiento\"" >> "$output_file"
        fi
    done
    
    log_success "Resumen generado: $output_file"
}

# ==============================================
# FUNCIÓN: Generar Glosario de Métricas
# ==============================================
generar_glosario_metricas() {
    log_info "Generando glosario de métricas..."
    
    local output_file="$OUTPUT_DIR/GLOSARIO_METRICAS_$TIMESTAMP.csv"
    
    cat > "$output_file" << 'EOF'
"Métrica","Unidad","Descripción","Interpretación"
"Tiempo Total","segundos","Tiempo completo desde envío de request hasta recepción de respuesta","Menor es mejor. Representa la latencia percibida por el usuario"
"Tiempo de Conexión","segundos","Tiempo para establecer conexión TCP","Indica latencia de red. Debería ser < 0.01s en localhost"
"TTFB (Time To First Byte)","segundos","Tiempo hasta recibir el primer byte de respuesta","Indica tiempo de procesamiento del servidor"
"Planning Time (SQL)","milisegundos","Tiempo que PostgreSQL tarda en planificar la query","Tiempo de optimización de consulta"
"Execution Time (SQL)","milisegundos","Tiempo real de ejecución de la query","Tiempo de acceso a datos"
"Código HTTP","número","Código de respuesta HTTP (200, 401, 500, etc.)","200 = OK, 401 = No autorizado, 500 = Error del servidor"
"Tamaño Descarga","bytes","Tamaño de la respuesta HTTP en bytes","Mayor tamaño puede indicar mayor tiempo de transferencia"
"Promedio","segundos/ms","Media aritmética de todos los valores","Valor típico esperado para la operación"
"Mínimo","segundos/ms","Valor más bajo registrado","Mejor caso posible bajo condiciones ideales"
"Máximo","segundos/ms","Valor más alto registrado","Peor caso registrado, puede indicar outlier"
"Mediana","segundos/ms","Valor del medio de la distribución","Menos sensible a outliers que el promedio"
"Desv. Estándar","segundos/ms","Dispersión de los valores respecto al promedio","Menor valor = más consistente. Mayor valor = más variabilidad"
"Percentil 95 (P95)","segundos/ms","95% de los requests están por debajo de este tiempo","Útil para identificar el rendimiento típico sin outliers extremos"
"Percentil 99 (P99)","segundos/ms","99% de los requests están por debajo de este tiempo","Indica peor escenario sin considerar el 1% más lento"
"Throughput","requests/segundo","Número de requests procesados por segundo","Mayor es mejor. Indica capacidad del sistema"
"Degradación","porcentaje","Aumento de latencia bajo carga concurrente","Indica qué tan bien escala el sistema con múltiples usuarios"
EOF
    
    log_success "Glosario generado: $output_file"
}

# ==============================================
# FUNCIÓN: Generar Reporte Ejecutivo
# ==============================================
generar_reporte_ejecutivo() {
    log_info "Generando reporte ejecutivo..."
    
    local output_file="$OUTPUT_DIR/REPORTE_EJECUTIVO_$TIMESTAMP.txt"
    
    # Contar archivos CSV generados
    local num_csvs=$(ls -1 "$OUTPUT_DIR"/{01..20}_*.csv 2>/dev/null | wc -l)
    
    cat > "$output_file" << EOF
========================================
REPORTE DE PRUEBAS DE RENDIMIENTO
Sistema: POS y Gestión Integral
Fecha: $(date +'%d de %B de %Y %H:%M:%S')
========================================

CONFIGURACIÓN:
- Iteraciones por prueba: $ITERATIONS
- Iteraciones de warmup: $WARMUP_ITERATIONS
- Total de pruebas ejecutadas: $num_csvs
- Timestamp de ejecución: $TIMESTAMP

SERVICIOS EVALUADOS:
- Frontend: $FRONTEND_URL
- Backend: $BACKEND_URL
- ML Service: $ML_SERVICE_URL
- Base de Datos: PostgreSQL (contenedor: $DB_CONTAINER)

TIPOS DE PRUEBAS REALIZADAS:
1. Pruebas individuales (requests secuenciales)
2. Pruebas de consultas SQL (EXPLAIN ANALYZE)
3. Pruebas de endpoints Backend → BD
4. Pruebas de endpoints Backend → ML Service
5. Pruebas end-to-end (flujos completos)

ARCHIVOS GENERADOS:
EOF
    
    # Listar archivos CSV generados
    echo "" >> "$output_file"
    echo "Archivos CSV individuales:" >> "$output_file"
    ls -1 "$OUTPUT_DIR"/{01..20}_*.csv 2>/dev/null | while read csv; do
        echo "  - $(basename "$csv")" >> "$output_file"
    done
    
    echo "" >> "$output_file"
    echo "Archivos consolidados:" >> "$output_file"
    echo "  - RESUMEN_LATENCIAS_POR_MODULO_$TIMESTAMP.csv" >> "$output_file"
    echo "  - GLOSARIO_METRICAS_$TIMESTAMP.csv" >> "$output_file"
    echo "  - REPORTE_EJECUTIVO_$TIMESTAMP.txt (este archivo)" >> "$output_file"
    
    cat >> "$output_file" << EOF

========================================
ANÁLISIS DE RESULTADOS
========================================

LATENCIAS PROMEDIO (primeras 10 pruebas):
EOF
    
    # Extraer y mostrar latencias de las primeras pruebas
    for i in {01..10}; do
        local csv=$(ls "$OUTPUT_DIR"/${i}_*.csv 2>/dev/null | head -1)
        if [ -f "$csv" ]; then
            local nombre=$(basename "$csv" | sed "s/^${i}_//; s/_${TIMESTAMP}\.csv$//" | tr '_' ' ')
            local promedio=$(extraer_promedio_csv "$csv" 2>/dev/null || echo "N/A")
            printf "  %-50s %10ss\n" "$nombre:" "$promedio" >> "$output_file"
        fi
    done
    
    cat >> "$output_file" << EOF

========================================
INTERPRETACIÓN Y RECOMENDACIONES
========================================

Para interpretar los resultados:
1. Abra el archivo RESUMEN_LATENCIAS_POR_MODULO_$TIMESTAMP.csv
2. Compare los tiempos promedio entre diferentes módulos
3. Identifique las operaciones más lentas (mayor tiempo promedio)
4. Revise la desviación estándar para evaluar consistencia
5. Use P95 y P99 para entender el peor caso esperado

Métricas de Referencia (localhost):
- Frontend → Backend (HTML): < 10ms (excelente)
- Backend → BD (query simple): < 5ms (excelente)
- Backend → BD (query compleja): < 50ms (aceptable)
- Backend → ML (predicción): < 2000ms (aceptable para ML)

NOTA IMPORTANTE:
Estas pruebas se realizaron en localhost sin carga de producción.
Los tiempos en un entorno real pueden ser diferentes debido a:
- Latencia de red
- Carga concurrente de usuarios reales
- Recursos compartidos del servidor
- Tamaño de datos en producción

========================================
PRÓXIMOS PASOS
========================================

1. Revisar el archivo RESUMEN_LATENCIAS_POR_MODULO_$TIMESTAMP.csv
2. Identificar los 3 cuellos de botella más críticos
3. Consultar la documentación en docs/INTERPRETACION_RESULTADOS.md
4. Implementar optimizaciones según sea necesario
5. Re-ejecutar pruebas después de optimizaciones para comparar

========================================
FIN DEL REPORTE
Generado: $(date +'%Y-%m-%d %H:%M:%S')
========================================
EOF
    
    log_success "Reporte ejecutivo generado: $output_file"
}

# ==============================================
# FUNCIÓN PRINCIPAL
# ==============================================
main() {
    echo ""
    echo "========================================="
    echo "  CONSOLIDACIÓN DE RESULTADOS"
    echo "========================================="
    echo ""
    
    # Verificar que hay archivos CSV para procesar
    local csv_count=$(ls -1 "$OUTPUT_DIR"/{01..20}_*.csv 2>/dev/null | wc -l)
    
    if [ "$csv_count" -eq 0 ]; then
        log_error "No se encontraron archivos CSV para consolidar"
        log_error "Ejecutar primero: ./benchmark.sh"
        exit 1
    fi
    
    log_info "Encontrados $csv_count archivos CSV para procesar"
    
    # Generar reportes consolidados
    generar_resumen_latencias
    generar_glosario_metricas
    generar_reporte_ejecutivo
    
    echo ""
    log_success "========================================="
    log_success "CONSOLIDACIÓN COMPLETADA"
    log_success "========================================="
    echo ""
    log_info "Archivos generados en: $OUTPUT_DIR"
    echo ""
    log_info "Para ver el resumen ejecutivo:"
    echo "  cat $OUTPUT_DIR/REPORTE_EJECUTIVO_$TIMESTAMP.txt"
    echo ""
    log_info "Para analizar latencias detalladas:"
    echo "  cat $OUTPUT_DIR/RESUMEN_LATENCIAS_POR_MODULO_$TIMESTAMP.csv"
    echo ""
}

# Ejecutar
main
