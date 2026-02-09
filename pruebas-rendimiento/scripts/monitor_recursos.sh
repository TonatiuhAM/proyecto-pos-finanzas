#!/bin/bash
# ============================================================
# MONITOR DE RECURSOS - Sistema POS
# ============================================================
# Captura métricas de CPU, RAM, Disco y Red de contenedores
# Docker cada X segundos durante la ejecución de pruebas.
#
# Uso:
#   ./monitor_recursos.sh [DURACION_MAX_SEGUNDOS]
#
# Ejemplo:
#   ./monitor_recursos.sh 300  # Monitorear máximo 5 minutos
# ============================================================

set -euo pipefail

# Cargar configuración
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config.env"

# ============================================================
# CONFIGURACIÓN
# ============================================================

INTERVALO=2  # Capturar cada 2 segundos
DURACION_MAX=${1:-600}  # Duración máxima en segundos (default: 10 minutos)
TIMESTAMP=$(date +'%Y%m%d_%H%M%S')
OUTPUT_FILE="$OUTPUT_DIR/recursos_raw_${TIMESTAMP}.csv"
PROCESOS_FILE="$OUTPUT_DIR/procesos_raw_${TIMESTAMP}.csv"
CONTROL_FILE="/tmp/monitor_recursos_${TIMESTAMP}.pid"

# Contenedores a monitorear
CONTENEDORES=("pos_backend" "pos_frontend" "pos_database" "pos_ml_prediction_api")

# ============================================================
# FUNCIONES DE AYUDA
# ============================================================

log_monitor() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [MONITOR] $1" >&2
}

cleanup() {
    log_monitor "Deteniendo monitor de recursos..."
    rm -f "$CONTROL_FILE"
    exit 0
}

# Capturar señal de terminación
trap cleanup SIGTERM SIGINT

# ============================================================
# FUNCIÓN PRINCIPAL DE CAPTURA
# ============================================================

capturar_metricas() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    local timestamp_unix=$(date +%s)
    
    # Capturar docker stats de todos los contenedores
    docker stats --no-stream --format "{{.Container}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.BlockIO}},{{.NetIO}}" 2>/dev/null | while IFS=',' read -r container cpu mem_usage mem_perc block_io net_io; do
        
        # Obtener nombre del contenedor
        local container_name=$(docker ps --filter "id=$container" --format "{{.Names}}" 2>/dev/null)
        
        if [ -z "$container_name" ]; then
            continue
        fi
        
        # Parsear memoria usando awk (ej: "496.2MiB / 7.906GiB" -> 496.2 y 8096)
        local mem_usado=$(echo "$mem_usage" | awk '{
            val=$1; 
            if (index(val, "GiB")) {gsub(/GiB/, "", val); print val*1024} 
            else if (index(val, "MiB")) {gsub(/MiB/, "", val); print val}
            else if (index(val, "KiB")) {gsub(/KiB/, "", val); print val/1024}
            else {print 0}
        }')
        
        local mem_limite=$(echo "$mem_usage" | awk '{
            val=$3; 
            if (index(val, "GiB")) {gsub(/GiB/, "", val); print val*1024} 
            else if (index(val, "MiB")) {gsub(/MiB/, "", val); print val}
            else if (index(val, "KiB")) {gsub(/KiB/, "", val); print val/1024}
            else {print 0}
        }')
        
        # Parsear disco I/O usando awk (ej: "1.23GB / 2.18GB" -> 1230 y 2180 MB)
        local disco_read=$(echo "$block_io" | awk '{
            val=$1;
            if (index(val, "GB")) {gsub(/GB/, "", val); print val*1024}
            else if (index(val, "MB")) {gsub(/MB/, "", val); print val}
            else if (index(val, "kB")) {gsub(/kB/, "", val); print val/1024}
            else if (index(val, "B")) {gsub(/B/, "", val); print val/1048576}
            else {print 0}
        }')
        
        local disco_write=$(echo "$block_io" | awk '{
            val=$3;
            if (index(val, "GB")) {gsub(/GB/, "", val); print val*1024}
            else if (index(val, "MB")) {gsub(/MB/, "", val); print val}
            else if (index(val, "kB")) {gsub(/kB/, "", val); print val/1024}
            else if (index(val, "B")) {gsub(/B/, "", val); print val/1048576}
            else {print 0}
        }')
        
        # Parsear red I/O usando awk (ej: "50.4MB / 52.2MB" -> 50.4 y 52.2)
        local net_in=$(echo "$net_io" | awk '{
            val=$1;
            if (index(val, "GB")) {gsub(/GB/, "", val); print val*1024}
            else if (index(val, "MB")) {gsub(/MB/, "", val); print val}
            else if (index(val, "kB")) {gsub(/kB/, "", val); print val/1024}
            else if (index(val, "B")) {gsub(/B/, "", val); print val/1048576}
            else {print 0}
        }')
        
        local net_out=$(echo "$net_io" | awk '{
            val=$3;
            if (index(val, "GB")) {gsub(/GB/, "", val); print val*1024}
            else if (index(val, "MB")) {gsub(/MB/, "", val); print val}
            else if (index(val, "kB")) {gsub(/kB/, "", val); print val/1024}
            else if (index(val, "B")) {gsub(/B/, "", val); print val/1048576}
            else {print 0}
        }')
        
        # Limpiar porcentajes
        cpu=$(echo "$cpu" | sed 's/%//')
        mem_perc=$(echo "$mem_perc" | sed 's/%//')
        
        # Escribir al CSV
        echo "$timestamp,$timestamp_unix,$container_name,$cpu,$mem_usado,$mem_limite,$mem_perc,$disco_read,$disco_write,$net_in,$net_out"
    done >> "$OUTPUT_FILE"
}

capturar_procesos() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    
    for contenedor in "${CONTENEDORES[@]}"; do
        # Verificar si el contenedor existe y está corriendo
        if ! docker ps --format "{{.Names}}" | grep -q "^${contenedor}$"; then
            continue
        fi
        
        # Capturar top 5 procesos por CPU
        docker exec "$contenedor" ps aux --sort=-%cpu 2>/dev/null | head -6 | tail -5 | while read -r line; do
            local user=$(echo "$line" | awk '{print $1}')
            local pid=$(echo "$line" | awk '{print $2}')
            local cpu=$(echo "$line" | awk '{print $3}')
            local mem=$(echo "$line" | awk '{print $4}')
            local command=$(echo "$line" | awk '{for(i=11;i<=NF;i++) printf $i" "; print ""}' | sed 's/ $//')
            
            echo "$timestamp,$contenedor,$pid,$user,$cpu,$mem,$command"
        done >> "$PROCESOS_FILE" 2>/dev/null || true
    done
}

# ============================================================
# INICIALIZACIÓN
# ============================================================

log_monitor "Iniciando monitor de recursos..."
log_monitor "Intervalo: ${INTERVALO}s | Duración máxima: ${DURACION_MAX}s"
log_monitor "Archivo de salida: $OUTPUT_FILE"
log_monitor "Archivo de procesos: $PROCESOS_FILE"

# Guardar PID para control externo
echo $$ > "$CONTROL_FILE"

# Crear header del CSV de recursos
echo "Timestamp,Timestamp_Unix,Contenedor,CPU_Porc,RAM_MB,RAM_Limite_MB,RAM_Porc,Disco_Read_MB,Disco_Write_MB,Net_In_MB,Net_Out_MB" > "$OUTPUT_FILE"

# Crear header del CSV de procesos
echo "Timestamp,Contenedor,PID,Usuario,CPU_Porc,MEM_Porc,Comando" > "$PROCESOS_FILE"

# ============================================================
# BUCLE PRINCIPAL
# ============================================================

inicio=$(date +%s)
iteracion=0

while true; do
    iteracion=$((iteracion + 1))
    actual=$(date +%s)
    transcurrido=$((actual - inicio))
    
    # Verificar si se alcanzó la duración máxima
    if [ $transcurrido -ge $DURACION_MAX ]; then
        log_monitor "Duración máxima alcanzada (${DURACION_MAX}s). Finalizando..."
        break
    fi
    
    # Capturar métricas
    capturar_metricas
    
    # Capturar procesos cada 10 segundos (reduce overhead)
    if [ $((iteracion % 5)) -eq 0 ]; then
        capturar_procesos
    fi
    
    # Log cada 30 segundos
    if [ $((transcurrido % 30)) -eq 0 ] && [ $transcurrido -gt 0 ]; then
        log_monitor "Capturando métricas... (${transcurrido}s transcurridos, iteración ${iteracion})"
    fi
    
    # Esperar antes de la siguiente captura
    sleep $INTERVALO
done

log_monitor "Monitor finalizado. Métricas guardadas en:"
log_monitor "  - $OUTPUT_FILE"
log_monitor "  - $PROCESOS_FILE"

cleanup
