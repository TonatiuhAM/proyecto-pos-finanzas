#!/bin/bash
# ============================================================
# CONSOLIDAR RECURSOS - Sistema POS
# ============================================================
# Genera resúmenes y reportes de los datos capturados
# por monitor_recursos.sh
#
# Uso:
#   ./consolidar_recursos.sh [ARCHIVO_CSV_RAW]
#
# Si no se especifica archivo, busca el más reciente
# ============================================================

set -euo pipefail

# Cargar configuración y funciones
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config.env"
source "$SCRIPT_DIR/funciones_recursos.sh"

# ============================================================
# CONFIGURACIÓN
# ============================================================

TIMESTAMP=$(date +'%Y%m%d_%H%M%S')

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;36m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC}: $1"
}

log_success() {
    echo -e "${GREEN}✓${NC}: $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC}: $1"
}

log_error() {
    echo -e "${RED}✗${NC}: $1"
}

# ============================================================
# BUSCAR ARCHIVO CSV
# ============================================================

if [ $# -ge 1 ]; then
    CSV_RAW="$1"
else
    # Buscar el archivo más reciente
    CSV_RAW=$(ls -t "$OUTPUT_DIR"/recursos_raw_*.csv 2>/dev/null | head -1)
    
    if [ -z "$CSV_RAW" ]; then
        log_error "No se encontraron archivos recursos_raw_*.csv en $OUTPUT_DIR"
        exit 1
    fi
    
    log_info "Usando archivo más reciente: $(basename "$CSV_RAW")"
fi

# Validar CSV
if ! validar_csv_recursos "$CSV_RAW"; then
    exit 1
fi

# Buscar archivo de procesos correspondiente
CSV_PROCESOS="${CSV_RAW/recursos_raw/procesos_raw}"
if [ ! -f "$CSV_PROCESOS" ]; then
    log_warning "No se encontró archivo de procesos: $(basename "$CSV_PROCESOS")"
    CSV_PROCESOS=""
fi

# ============================================================
# GENERAR RESUMEN DE RECURSOS
# ============================================================

echo ""
echo "========================================="
echo "  CONSOLIDACIÓN DE RECURSOS"
echo "========================================="
echo ""

RESUMEN_FILE="$OUTPUT_DIR/RESUMEN_RECURSOS_${TIMESTAMP}.csv"
REPORTE_FILE="$OUTPUT_DIR/REPORTE_RECURSOS_${TIMESTAMP}.txt"
PROCESOS_RESUMEN_FILE="$OUTPUT_DIR/TOP_PROCESOS_${TIMESTAMP}.csv"

log_info "Generando resumen de recursos por contenedor..."

# Crear header del resumen
echo "\"Contenedor\",\"Muestras\",\"CPU_Min_%\",\"CPU_Max_%\",\"CPU_Prom_%\",\"CPU_P95_%\",\"RAM_Min_MB\",\"RAM_Max_MB\",\"RAM_Prom_MB\",\"RAM_P95_MB\",\"Disco_Read_Inicial_MB\",\"Disco_Read_Final_MB\",\"Disco_Read_Delta_MB\",\"Disco_Write_Inicial_MB\",\"Disco_Write_Final_MB\",\"Disco_Write_Delta_MB\",\"Net_In_Delta_MB\",\"Net_Out_Delta_MB\",\"Estado\"" > "$RESUMEN_FILE"

# Obtener lista de contenedores
CONTENEDORES=$(obtener_contenedores "$CSV_RAW")

for contenedor in $CONTENEDORES; do
    log_info "  Procesando: $contenedor"
    
    # Contar muestras
    muestras=$(contar_muestras "$CSV_RAW" "$contenedor")
    
    # CPU (columna 4)
    cpu_stats=$(calcular_estadisticas_recurso "$CSV_RAW" "$contenedor" 4)
    IFS=',' read -r cpu_min cpu_max cpu_prom cpu_med cpu_desv cpu_p95 cpu_count <<< "$cpu_stats"
    
    # RAM en MB (columna 5)
    ram_stats=$(calcular_estadisticas_recurso "$CSV_RAW" "$contenedor" 5)
    IFS=',' read -r ram_min ram_max ram_prom ram_med ram_desv ram_p95 ram_count <<< "$ram_stats"
    
    # Disco Read (columna 8) - calcular delta
    disco_read_delta=$(obtener_delta_recurso "$CSV_RAW" "$contenedor" 8)
    IFS=',' read -r disco_read_ini disco_read_fin disco_read_dlt <<< "$disco_read_delta"
    
    # Disco Write (columna 9) - calcular delta
    disco_write_delta=$(obtener_delta_recurso "$CSV_RAW" "$contenedor" 9)
    IFS=',' read -r disco_write_ini disco_write_fin disco_write_dlt <<< "$disco_write_delta"
    
    # Red In (columna 10) - calcular delta
    net_in_delta=$(obtener_delta_recurso "$CSV_RAW" "$contenedor" 10)
    IFS=',' read -r net_in_ini net_in_fin net_in_dlt <<< "$net_in_delta"
    
    # Red Out (columna 11) - calcular delta
    net_out_delta=$(obtener_delta_recurso "$CSV_RAW" "$contenedor" 11)
    IFS=',' read -r net_out_ini net_out_fin net_out_dlt <<< "$net_out_delta"
    
    # Identificar problemas
    estado=$(identificar_problemas "$cpu_prom" "$cpu_max" "$ram_prom" "$ram_max")
    
    # Escribir al CSV
    echo "\"$contenedor\",$muestras,$cpu_min,$cpu_max,$cpu_prom,$cpu_p95,$ram_min,$ram_max,$ram_prom,$ram_p95,$disco_read_ini,$disco_read_fin,$disco_read_dlt,$disco_write_ini,$disco_write_fin,$disco_write_dlt,$net_in_dlt,$net_out_dlt,\"$estado\"" >> "$RESUMEN_FILE"
done

log_success "Resumen generado: $(basename "$RESUMEN_FILE")"

# ============================================================
# GENERAR RESUMEN DE PROCESOS (si existe)
# ============================================================

if [ -n "$CSV_PROCESOS" ] && [ -f "$CSV_PROCESOS" ]; then
    log_info "Generando resumen de procesos..."
    
    echo "\"Contenedor\",\"Tipo\",\"Métrica_Promedio\",\"Comando\"" > "$PROCESOS_RESUMEN_FILE"
    
    for contenedor in $CONTENEDORES; do
        # Top 5 por CPU
        top_procesos_por_cpu "$CSV_PROCESOS" "$contenedor" 5 | while IFS=',' read -r cpu_prom comando; do
            echo "\"$contenedor\",\"CPU\",$cpu_prom,\"$comando\"" >> "$PROCESOS_RESUMEN_FILE"
        done
        
        # Top 5 por MEM
        top_procesos_por_mem "$CSV_PROCESOS" "$contenedor" 5 | while IFS=',' read -r mem_prom comando; do
            echo "\"$contenedor\",\"MEM\",$mem_prom,\"$comando\"" >> "$PROCESOS_RESUMEN_FILE"
        done
    done
    
    log_success "Resumen de procesos generado: $(basename "$PROCESOS_RESUMEN_FILE")"
fi

# ============================================================
# GENERAR REPORTE EJECUTIVO
# ============================================================

log_info "Generando reporte ejecutivo..."

# Calcular duración total
duracion_seg=$(calcular_duracion_monitoreo "$CSV_RAW")
duracion_fmt=$(formatear_duracion "$duracion_seg")

# Iniciar reporte
cat > "$REPORTE_FILE" << EOF
========================================
REPORTE DE USO DE RECURSOS
Sistema: POS y Gestión Integral
Fecha: $(date +'%d de %B de %Y %H:%M:%S')
========================================

CONFIGURACIÓN:
- Archivo de datos: $(basename "$CSV_RAW")
- Duración del monitoreo: $duracion_fmt ($duracion_seg segundos)
- Intervalo de captura: 2 segundos
- Total de contenedores: $(echo "$CONTENEDORES" | wc -w)

CONTENEDORES MONITOREADOS:
EOF

for contenedor in $CONTENEDORES; do
    echo "  - $contenedor" >> "$REPORTE_FILE"
done

echo "" >> "$REPORTE_FILE"
echo "========================================" >> "$REPORTE_FILE"
echo "ANÁLISIS POR CONTENEDOR" >> "$REPORTE_FILE"
echo "========================================" >> "$REPORTE_FILE"

# Leer el resumen y generar reporte detallado
tail -n +2 "$RESUMEN_FILE" | while IFS=',' read -r contenedor muestras cpu_min cpu_max cpu_prom cpu_p95 ram_min ram_max ram_prom ram_p95 disco_r_ini disco_r_fin disco_r_dlt disco_w_ini disco_w_fin disco_w_dlt net_in net_out estado; do
    
    # Limpiar comillas
    contenedor=$(echo "$contenedor" | tr -d '"')
    estado=$(echo "$estado" | tr -d '"')
    
    cat >> "$REPORTE_FILE" << EOF

CONTENEDOR: $contenedor
$(printf '=%.0s' {1..60})

  ESTADO: $estado
  Muestras capturadas: $muestras

  CPU:
    Promedio: ${cpu_prom}%
    Rango: ${cpu_min}% - ${cpu_max}%
    P95: ${cpu_p95}%
    $([ $(awk -v val="$cpu_max" 'BEGIN {print (val > 80) ? 1 : 0}') -eq 1 ] && echo "    ⚠️  Pico alto detectado (> 80%)")

  MEMORIA (RAM):
    Promedio: $(formatear_tamanio "$ram_prom")
    Rango: $(formatear_tamanio "$ram_min") - $(formatear_tamanio "$ram_max")
    P95: $(formatear_tamanio "$ram_p95")

  DISCO I/O (durante el monitoreo):
    Lectura: $(formatear_tamanio "$disco_r_dlt")
    Escritura: $(formatear_tamanio "$disco_w_dlt")

  RED I/O (durante el monitoreo):
    Entrada: $(formatear_tamanio "$net_in")
    Salida: $(formatear_tamanio "$net_out")

EOF

    # Agregar top procesos si está disponible
    if [ -n "$CSV_PROCESOS" ] && [ -f "$CSV_PROCESOS" ]; then
        echo "  TOP 3 PROCESOS POR CPU:" >> "$REPORTE_FILE"
        top_procesos_por_cpu "$CSV_PROCESOS" "$contenedor" 3 | while IFS=',' read -r cpu_avg cmd; do
            printf "    - %.2f%%: %s\n" "$cpu_avg" "$cmd" >> "$REPORTE_FILE"
        done
        
        echo "" >> "$REPORTE_FILE"
        echo "  TOP 3 PROCESOS POR MEMORIA:" >> "$REPORTE_FILE"
        top_procesos_por_mem "$CSV_PROCESOS" "$contenedor" 3 | while IFS=',' read -r mem_avg cmd; do
            printf "    - %.2f%%: %s\n" "$mem_avg" "$cmd" >> "$REPORTE_FILE"
        done
    fi

done

# ============================================================
# RESUMEN GENERAL DEL SISTEMA
# ============================================================

cat >> "$REPORTE_FILE" << EOF


========================================
RESUMEN GENERAL DEL SISTEMA
========================================

EOF

# Calcular totales
total_cpu_prom=$(awk -F',' 'NR > 1 {sum += $5; count++} END {printf "%.2f", sum}' "$RESUMEN_FILE")
total_ram_prom=$(awk -F',' 'NR > 1 {sum += $9; count++} END {printf "%.2f", sum}' "$RESUMEN_FILE")
total_disco_read=$(awk -F',' 'NR > 1 {sum += $13} END {printf "%.2f", sum}' "$RESUMEN_FILE")
total_disco_write=$(awk -F',' 'NR > 1 {sum += $16} END {printf "%.2f", sum}' "$RESUMEN_FILE")
total_net_in=$(awk -F',' 'NR > 1 {sum += $17} END {printf "%.2f", sum}' "$RESUMEN_FILE")
total_net_out=$(awk -F',' 'NR > 1 {sum += $18} END {printf "%.2f", sum}' "$RESUMEN_FILE")

cat >> "$REPORTE_FILE" << EOF
USO COMBINADO DE TODOS LOS CONTENEDORES:
  CPU total (promedio): ${total_cpu_prom}%
  RAM total (promedio): $(formatear_tamanio "$total_ram_prom")
  Disco leído: $(formatear_tamanio "$total_disco_read")
  Disco escrito: $(formatear_tamanio "$total_disco_write")
  Red entrada: $(formatear_tamanio "$total_net_in")
  Red salida: $(formatear_tamanio "$total_net_out")

SISTEMA HOST:
  Núcleos de CPU disponibles: $(nproc)
  RAM total: $(free -m | awk 'NR==2 {print $2}') MB
  Disco total: $(df -h / | awk 'NR==2 {print $2}')
  Disco usado: $(df -h / | awk 'NR==2 {print $3}')
  Disco disponible: $(df -h / | awk 'NR==2 {print $4}')

========================================
RECOMENDACIONES
========================================

EOF

# Generar recomendaciones basadas en los datos
problemas_encontrados=0

while IFS=',' read -r contenedor estado; do
    contenedor=$(echo "$contenedor" | tr -d '"')
    estado=$(echo "$estado" | tr -d '"')
    
    if [ "$estado" != "OK" ]; then
        problemas_encontrados=1
        echo "⚠️  $contenedor:" >> "$REPORTE_FILE"
        
        if [[ "$estado" == *"CPU_ALTO"* ]]; then
            echo "    - CPU promedio alto (> 80%). Considerar optimización o escalado horizontal." >> "$REPORTE_FILE"
        fi
        
        if [[ "$estado" == *"CPU_CRITICO"* ]]; then
            echo "    - Picos de CPU críticos (> 95%). Revisar procesos intensivos." >> "$REPORTE_FILE"
        fi
        
        if [[ "$estado" == *"MEM_ALTO"* ]]; then
            echo "    - Uso de memoria alto (> 80%). Aumentar límite o revisar memory leaks." >> "$REPORTE_FILE"
        fi
        
        if [[ "$estado" == *"MEM_CRITICO"* ]]; then
            echo "    - Picos de memoria críticos (> 90%). Riesgo de OOM." >> "$REPORTE_FILE"
        fi
        
        echo "" >> "$REPORTE_FILE"
    fi
done < <(tail -n +2 "$RESUMEN_FILE" | awk -F',' '{print $1","$19}')

if [ $problemas_encontrados -eq 0 ]; then
    echo "✓ No se detectaron problemas de recursos." >> "$REPORTE_FILE"
    echo "  Todos los contenedores operan dentro de rangos normales." >> "$REPORTE_FILE"
fi

cat >> "$REPORTE_FILE" << EOF

========================================
ARCHIVOS GENERADOS
========================================

- Datos crudos: $(basename "$CSV_RAW")
EOF

if [ -n "$CSV_PROCESOS" ]; then
    echo "- Procesos crudos: $(basename "$CSV_PROCESOS")" >> "$REPORTE_FILE"
fi

cat >> "$REPORTE_FILE" << EOF
- Resumen de recursos: $(basename "$RESUMEN_FILE")
EOF

if [ -n "$CSV_PROCESOS" ]; then
    echo "- Top procesos: $(basename "$PROCESOS_RESUMEN_FILE")" >> "$REPORTE_FILE"
fi

cat >> "$REPORTE_FILE" << EOF
- Este reporte: $(basename "$REPORTE_FILE")

========================================
Fin del reporte
========================================
EOF

log_success "Reporte ejecutivo generado: $(basename "$REPORTE_FILE")"

# ============================================================
# RESUMEN FINAL
# ============================================================

echo ""
echo "========================================="
echo "  CONSOLIDACIÓN COMPLETADA"
echo "========================================="
echo ""
echo "Archivos generados:"
echo "  - $(basename "$RESUMEN_FILE")"
if [ -n "$CSV_PROCESOS" ]; then
    echo "  - $(basename "$PROCESOS_RESUMEN_FILE")"
fi
echo "  - $(basename "$REPORTE_FILE")"
echo ""
echo "Para ver el reporte ejecutivo:"
echo "  cat $(basename "$REPORTE_FILE")"
echo ""
