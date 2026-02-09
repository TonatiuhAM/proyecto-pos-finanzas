#!/bin/bash
# ============================================================
# FUNCIONES DE ANÁLISIS DE RECURSOS - Sistema POS
# ============================================================
# Funciones para calcular estadísticas de uso de recursos
# ============================================================

# ============================================================
# FUNCIONES DE CÁLCULO ESTADÍSTICO
# ============================================================

# Calcular estadísticas de una métrica para un contenedor específico
# Uso: calcular_estadisticas_recurso <CSV_FILE> <CONTENEDOR> <COLUMNA>
# Ejemplo: calcular_estadisticas_recurso recursos.csv "pos_backend" 4
calcular_estadisticas_recurso() {
    local csv_file=$1
    local contenedor=$2
    local columna=$3
    
    # Extraer valores del contenedor específico (saltar header)
    local valores=$(awk -F',' -v cont="$contenedor" -v col="$columna" '
        NR > 1 && $3 == cont {print $col}
    ' "$csv_file")
    
    if [ -z "$valores" ]; then
        echo "0,0,0,0,0,0,0"
        return
    fi
    
    # Ordenar valores para mediana y percentiles
    local valores_ordenados=$(echo "$valores" | sort -n)
    
    # Calcular estadísticas usando awk
    echo "$valores" | awk -v valores_ord="$valores_ordenados" '
    BEGIN {
        min = 999999999
        max = -999999999
        sum = 0
        count = 0
        # Parsear valores ordenados
        split(valores_ord, sorted, "\n")
    }
    {
        val = $1
        if (val+0 == val) {  # Verificar que es numérico
            sum += val
            count++
            if (val < min) min = val
            if (val > max) max = val
        }
    }
    END {
        if (count == 0) {
            print "0,0,0,0,0,0,0"
            exit
        }
        
        promedio = sum / count
        
        # Calcular mediana desde array ordenado
        if (count % 2 == 1) {
            mediana = sorted[int(count/2) + 1]
        } else {
            mediana = (sorted[count/2] + sorted[count/2 + 1]) / 2
        }
        
        # Calcular desviación estándar
        sum_sq_diff = 0
    }
    {
        val = $1
        if (val+0 == val) {
            diff = val - promedio
            sum_sq_diff += diff * diff
        }
    }
    END {
        desvest = sqrt(sum_sq_diff / count)
        
        # Calcular percentiles desde array ordenado
        p95_idx = int(count * 0.95)
        if (p95_idx < 1) p95_idx = 1
        p95 = sorted[p95_idx]
        
        p99_idx = int(count * 0.99)
        if (p99_idx < 1) p99_idx = 1
        p99 = sorted[p99_idx]
        
        printf "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d\n", min, max, promedio, mediana, desvest, p95, count
    }
    '
}

# Obtener el primer y último valor de una métrica (para calcular deltas)
# Uso: obtener_delta_recurso <CSV_FILE> <CONTENEDOR> <COLUMNA>
obtener_delta_recurso() {
    local csv_file=$1
    local contenedor=$2
    local columna=$3
    
    awk -F',' -v cont="$contenedor" -v col="$columna" '
        NR > 1 && $3 == cont {
            if (primero == "") primero = $col
            ultimo = $col
        }
        END {
            delta = ultimo - primero
            if (delta < 0) delta = 0  # Evitar negativos si hubo reset
            printf "%.2f,%.2f,%.2f\n", primero, ultimo, delta
        }
    ' "$csv_file"
}

# ============================================================
# FUNCIONES DE EXTRACCIÓN
# ============================================================

# Obtener lista de contenedores únicos en el CSV
obtener_contenedores() {
    local csv_file=$1
    awk -F',' 'NR > 1 {print $3}' "$csv_file" | sort -u
}

# Contar número de muestras capturadas
contar_muestras() {
    local csv_file=$1
    local contenedor=$2
    awk -F',' -v cont="$contenedor" 'NR > 1 && $3 == cont {count++} END {print count}' "$csv_file"
}

# Calcular duración total del monitoreo
calcular_duracion_monitoreo() {
    local csv_file=$1
    awk -F',' 'NR == 2 {inicio=$2} NR > 2 {fin=$2} END {print fin - inicio}' "$csv_file"
}

# ============================================================
# FUNCIONES DE ANÁLISIS DE PROCESOS
# ============================================================

# Obtener top N procesos por CPU promedio para un contenedor
# Uso: top_procesos_por_cpu <CSV_FILE> <CONTENEDOR> <N>
top_procesos_por_cpu() {
    local csv_file=$1
    local contenedor=$2
    local n=${3:-5}
    
    awk -F',' -v cont="$contenedor" '
        NR > 1 && $2 == cont {
            cmd = $7
            cpu = $5
            sum[cmd] += cpu
            count[cmd]++
        }
        END {
            for (cmd in sum) {
                avg = sum[cmd] / count[cmd]
                printf "%.2f,%s\n", avg, cmd
            }
        }
    ' "$csv_file" | sort -t',' -k1 -rn | head -n "$n"
}

# Obtener top N procesos por MEM promedio para un contenedor
# Uso: top_procesos_por_mem <CSV_FILE> <CONTENEDOR> <N>
top_procesos_por_mem() {
    local csv_file=$1
    local contenedor=$2
    local n=${3:-5}
    
    awk -F',' -v cont="$contenedor" '
        NR > 1 && $2 == cont {
            cmd = $7
            mem = $6
            sum[cmd] += mem
            count[cmd]++
        }
        END {
            for (cmd in sum) {
                avg = sum[cmd] / count[cmd]
                printf "%.2f,%s\n", avg, cmd
            }
        }
    ' "$csv_file" | sort -t',' -k1 -rn | head -n "$n"
}

# ============================================================
# FUNCIONES DE FORMATEO
# ============================================================

# Convertir MB a formato legible (MB, GB)
formatear_tamanio() {
    local mb=$1
    awk -v mb="$mb" 'BEGIN {
        if (mb >= 1024) {
            printf "%.2f GB", mb/1024
        } else {
            printf "%.2f MB", mb
        }
    }'
}

# Formatear porcentaje con 2 decimales
formatear_porcentaje() {
    local valor=$1
    awk -v val="$valor" 'BEGIN {printf "%.2f%%", val}'
}

# Formatear duración en formato HH:MM:SS
formatear_duracion() {
    local segundos=$1
    awk -v seg="$segundos" 'BEGIN {
        horas = int(seg / 3600)
        minutos = int((seg % 3600) / 60)
        segundos = seg % 60
        printf "%02d:%02d:%02d", horas, minutos, segundos
    }'
}

# ============================================================
# FUNCIONES DE VALIDACIÓN
# ============================================================

# Verificar si el archivo CSV existe y tiene datos
validar_csv_recursos() {
    local csv_file=$1
    
    if [ ! -f "$csv_file" ]; then
        echo "ERROR: Archivo no encontrado: $csv_file" >&2
        return 1
    fi
    
    local lineas=$(wc -l < "$csv_file")
    if [ "$lineas" -le 1 ]; then
        echo "ERROR: CSV sin datos (solo header): $csv_file" >&2
        return 1
    fi
    
    return 0
}

# ============================================================
# FUNCIONES DE COMPARACIÓN
# ============================================================

# Calcular diferencia porcentual entre dos valores
# Uso: calcular_diferencia_porcentual <VALOR_BASE> <VALOR_NUEVO>
calcular_diferencia_porcentual() {
    local base=$1
    local nuevo=$2
    
    awk -v base="$base" -v nuevo="$nuevo" 'BEGIN {
        if (base == 0) {
            if (nuevo == 0) print "0.00"
            else print "100.00"
        } else {
            diff = ((nuevo - base) / base) * 100
            printf "%.2f", diff
        }
    }'
}

# Identificar si hay un problema de recursos (thresholds)
identificar_problemas() {
    local cpu_prom=$1
    local cpu_max=$2
    local mem_prom=$3
    local mem_max=$4
    
    local problemas=""
    
    # CPU promedio > 80% es alto
    if awk -v val="$cpu_prom" 'BEGIN {exit (val > 80) ? 0 : 1}'; then
        problemas="${problemas}CPU_ALTO_PROMEDIO,"
    fi
    
    # CPU max > 95% es crítico
    if awk -v val="$cpu_max" 'BEGIN {exit (val > 95) ? 0 : 1}'; then
        problemas="${problemas}CPU_CRITICO_PICO,"
    fi
    
    # Memoria > 80% es alto
    if awk -v val="$mem_prom" 'BEGIN {exit (val > 80) ? 0 : 1}'; then
        problemas="${problemas}MEM_ALTO_PROMEDIO,"
    fi
    
    # Memoria max > 90% es crítico
    if awk -v val="$mem_max" 'BEGIN {exit (val > 90) ? 0 : 1}'; then
        problemas="${problemas}MEM_CRITICO_PICO,"
    fi
    
    if [ -z "$problemas" ]; then
        echo "OK"
    else
        echo "${problemas%,}"  # Remover última coma
    fi
}

# ============================================================
# EXPORTAR FUNCIONES
# ============================================================

export -f calcular_estadisticas_recurso
export -f obtener_delta_recurso
export -f obtener_contenedores
export -f contar_muestras
export -f calcular_duracion_monitoreo
export -f top_procesos_por_cpu
export -f top_procesos_por_mem
export -f formatear_tamanio
export -f formatear_porcentaje
export -f formatear_duracion
export -f validar_csv_recursos
export -f calcular_diferencia_porcentual
export -f identificar_problemas
