#!/bin/bash
# Funciones estadísticas para análisis de resultados
# Sistema POS y Gestión Integral

# ==============================================
# FUNCIÓN: Calcular estadísticas de un array
# ==============================================
# Input: Array de valores numéricos
# Output: Variables globales con estadísticas
calcular_estadisticas() {
    local -n arr=$1
    local count=${#arr[@]}
    
    if [ $count -eq 0 ]; then
        echo "0,0,0,0,0,0,0"
        return
    fi
    
    # Convertir array a string separado por espacios para awk
    local valores=$(printf "%s\n" "${arr[@]}")
    
    # Calcular todas las estadísticas con awk
    echo "$valores" | awk '
    BEGIN {
        min = 999999999
        max = -999999999
        sum = 0
        count = 0
    }
    {
        values[count++] = $1
        sum += $1
        if ($1 < min) min = $1
        if ($1 > max) max = $1
    }
    END {
        # Promedio
        mean = sum / count
        
        # Calcular desviación estándar
        sum_sq_diff = 0
        for (i = 0; i < count; i++) {
            diff = values[i] - mean
            sum_sq_diff += diff * diff
        }
        std_dev = sqrt(sum_sq_diff / count)
        
        # Ordenar array para calcular mediana y percentiles
        for (i = 0; i < count; i++) {
            for (j = i + 1; j < count; j++) {
                if (values[i] > values[j]) {
                    temp = values[i]
                    values[i] = values[j]
                    values[j] = temp
                }
            }
        }
        
        # Mediana
        if (count % 2 == 0) {
            median = (values[count/2 - 1] + values[count/2]) / 2
        } else {
            median = values[int(count/2)]
        }
        
        # Percentil 95
        p95_idx = int(count * 0.95)
        if (p95_idx >= count) p95_idx = count - 1
        p95 = values[p95_idx]
        
        # Percentil 99
        p99_idx = int(count * 0.99)
        if (p99_idx >= count) p99_idx = count - 1
        p99 = values[p99_idx]
        
        # Imprimir resultados separados por coma
        printf "%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n", min, max, mean, median, std_dev, p95, p99
    }
    '
}

# ==============================================
# FUNCIÓN: Agregar fila de estadísticas a CSV
# ==============================================
# Input: Array de tiempos, archivo CSV
agregar_estadisticas_csv() {
    local -n tiempos=$1
    local csv_file=$2
    
    # Calcular estadísticas
    local stats=$(calcular_estadisticas tiempos)
    IFS=',' read -r min max mean median std_dev p95 p99 <<< "$stats"
    
    # Agregar línea separadora
    echo "" >> "$csv_file"
    echo "\"ESTADÍSTICAS\",\"\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    
    # Agregar estadísticas
    echo "\"Mínimo\",\"$min\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Máximo\",\"$max\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Promedio\",\"$mean\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Mediana\",\"$median\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Desv. Estándar\",\"$std_dev\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Percentil 95\",\"$p95\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Percentil 99\",\"$p99\",\"\",\"\",\"\",\"\",\"\"" >> "$csv_file"
}

# ==============================================
# FUNCIÓN: Agregar estadísticas SQL a CSV
# ==============================================
agregar_estadisticas_sql_csv() {
    local -n tiempos=$1
    local csv_file=$2
    
    local stats=$(calcular_estadisticas tiempos)
    IFS=',' read -r min max mean median std_dev p95 p99 <<< "$stats"
    
    echo "" >> "$csv_file"
    echo "\"ESTADÍSTICAS\",\"\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Mínimo\",\"$min\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Máximo\",\"$max\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Promedio\",\"$mean\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Mediana\",\"$median\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Desv. Estándar\",\"$std_dev\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Percentil 95\",\"$p95\",\"\",\"\",\"\"" >> "$csv_file"
    echo "\"Percentil 99\",\"$p99\",\"\",\"\",\"\"" >> "$csv_file"
}

# ==============================================
# FUNCIÓN: Formatear tiempo en segundos
# ==============================================
formatear_tiempo() {
    local tiempo=$1
    printf "%.6f" "$tiempo"
}

# ==============================================
# FUNCIÓN: Extraer valor promedio de CSV
# ==============================================
extraer_promedio_csv() {
    local csv_file=$1
    grep "^\"Promedio\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Extraer valor mínimo de CSV
# ==============================================
extraer_minimo_csv() {
    local csv_file=$1
    grep "^\"Mínimo\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Extraer valor máximo de CSV
# ==============================================
extraer_maximo_csv() {
    local csv_file=$1
    grep "^\"Máximo\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Extraer desviación estándar de CSV
# ==============================================
extraer_desvest_csv() {
    local csv_file=$1
    grep "^\"Desv. Estándar\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Extraer percentil 95 de CSV
# ==============================================
extraer_p95_csv() {
    local csv_file=$1
    grep "^\"Percentil 95\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Extraer percentil 99 de CSV
# ==============================================
extraer_p99_csv() {
    local csv_file=$1
    grep "^\"Percentil 99\"" "$csv_file" | cut -d',' -f2 | tr -d '"'
}

# ==============================================
# FUNCIÓN: Calcular mejora porcentual
# ==============================================
# Input: valor_anterior, valor_nuevo
# Output: porcentaje de mejora (positivo = mejor, negativo = peor)
calcular_mejora_porcentual() {
    local anterior=$1
    local nuevo=$2
    
    awk "BEGIN {printf \"%.2f\", (($anterior - $nuevo) / $anterior) * 100}"
}

# ==============================================
# FUNCIÓN: Calcular degradación porcentual
# ==============================================
# Input: valor_individual, valor_concurrente
# Output: porcentaje de degradación
calcular_degradacion_porcentual() {
    local individual=$1
    local concurrente=$2
    
    awk "BEGIN {printf \"%.2f\", (($concurrente - $individual) / $individual) * 100}"
}

# ==============================================
# FUNCIÓN: Validar que un archivo CSV existe
# ==============================================
validar_csv_existe() {
    local csv_file=$1
    
    if [ ! -f "$csv_file" ]; then
        return 1
    fi
    
    return 0
}

# ==============================================
# FUNCIÓN: Contar líneas de datos en CSV
# ==============================================
# Excluye header y estadísticas
contar_datos_csv() {
    local csv_file=$1
    
    # Contar líneas entre header y "ESTADÍSTICAS"
    awk '/^"Iteración"/,/^"ESTADÍSTICAS"/ {count++} END {print count-2}' "$csv_file"
}

# ==============================================
# FUNCIÓN: Crear header CSV para pruebas HTTP
# ==============================================
crear_header_http_csv() {
    echo "\"Iteración\",\"Tiempo Total (s)\",\"Tiempo Conexión (s)\",\"TTFB (s)\",\"Código HTTP\",\"Tamaño Descarga (bytes)\",\"Timestamp\""
}

# ==============================================
# FUNCIÓN: Crear header CSV para pruebas SQL
# ==============================================
crear_header_sql_csv() {
    echo "\"Iteración\",\"Planning Time (ms)\",\"Execution Time (ms)\",\"Total Time (ms)\",\"Timestamp\""
}

# ==============================================
# FUNCIÓN: Crear header CSV para pruebas concurrentes
# ==============================================
crear_header_concurrente_csv() {
    echo "\"Iteración\",\"Usuario\",\"Tiempo Respuesta (s)\",\"Código HTTP\",\"Timestamp\""
}

# ==============================================
# FUNCIÓN: Verificar herramientas necesarias
# ==============================================
verificar_herramientas() {
    if ! command -v awk &> /dev/null; then
        echo "ERROR: 'awk' no está instalado."
        return 1
    fi
    
    if ! command -v curl &> /dev/null; then
        echo "ERROR: 'curl' no está instalado."
        return 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo "ERROR: 'docker' no está instalado."
        return 1
    fi
    
    return 0
}
