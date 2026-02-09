#!/bin/bash
# Script principal de pruebas de rendimiento
# Sistema POS y Gestión Integral
# 
# Uso: ./benchmark.sh [opción]
#   sin opciones: Ejecutar todas las pruebas
#   warmup: Solo ejecutar warmup
#   test <número>: Ejecutar solo una prueba específica

set -euo pipefail

# ==============================================
# CARGAR CONFIGURACIÓN Y FUNCIONES
# ==============================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/config.env"
source "$SCRIPT_DIR/funciones_estadisticas.sh"

# ==============================================
# FUNCIONES DE LOGGING
# ==============================================
log_info() {
    local msg="[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_INFO}INFO${COLOR_RESET}: $*"
    echo -e "$msg"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $*" >> "$LOG_FILE"
}

log_success() {
    local msg="[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_SUCCESS}✓${COLOR_RESET}: $*"
    echo -e "$msg"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] SUCCESS: $*" >> "$LOG_FILE"
}

log_warning() {
    local msg="[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_WARNING}⚠${COLOR_RESET}: $*"
    echo -e "$msg"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $*" >> "$LOG_FILE"
}

log_error() {
    local msg="[$(date +'%Y-%m-%d %H:%M:%S')] ${COLOR_ERROR}✗${COLOR_RESET}: $*"
    echo -e "$msg" >&2
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $*" >> "$LOG_FILE"
}

# ==============================================
# VERIFICACIÓN INICIAL
# ==============================================
verificar_servicios() {
    log_info "Verificando servicios Docker..."
    
    local servicios=("pos_backend" "pos_frontend" "pos_database" "pos_ml_prediction_api")
    local todos_corriendo=true
    
    for servicio in "${servicios[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${servicio}$"; then
            log_success "Servicio $servicio está corriendo"
        else
            log_error "Servicio $servicio NO está corriendo"
            todos_corriendo=false
        fi
    done
    
    if [ "$todos_corriendo" = false ]; then
        log_error "Algunos servicios no están corriendo. Ejecutar: docker-compose up -d"
        exit 1
    fi
    
    log_success "Todos los servicios están corriendo correctamente"
}

verificar_conectividad() {
    log_info "Verificando conectividad a servicios..."
    
    # Verificar frontend
    if curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL" | grep -q "200"; then
        log_success "Frontend accesible en $FRONTEND_URL"
    else
        log_error "Frontend no accesible en $FRONTEND_URL"
        exit 1
    fi
    
    # Verificar backend (esperamos 401 sin token)
    local backend_code=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/api/productos")
    if [ "$backend_code" = "401" ] || [ "$backend_code" = "200" ]; then
        log_success "Backend accesible en $BACKEND_URL"
    else
        log_error "Backend no accesible en $BACKEND_URL (código: $backend_code)"
        exit 1
    fi
    
    # Verificar ML Service
    if curl -s -o /dev/null -w "%{http_code}" "$ML_SERVICE_URL/health" | grep -q "200"; then
        log_success "ML Service accesible en $ML_SERVICE_URL"
    else
        log_error "ML Service no accesible en $ML_SERVICE_URL"
        exit 1
    fi
    
    # Verificar base de datos
    if docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" &> /dev/null; then
        log_success "Base de datos accesible"
    else
        log_error "Base de datos no accesible"
        exit 1
    fi
}

obtener_token_jwt() {
    log_info "Obteniendo token JWT para usuario: $TEST_USER"
    
    local response=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"nombre\":\"$TEST_USER\",\"contrasena\":\"$TEST_PASSWORD\"}")
    
    TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        log_error "No se pudo obtener token JWT"
        log_error "Respuesta del servidor: $response"
        exit 1
    fi
    
    log_success "Token JWT obtenido exitosamente (${#TOKEN} caracteres)"
}

# ==============================================
# WARMUP
# ==============================================
ejecutar_warmup() {
    log_info "========================================="
    log_info "EJECUTANDO WARMUP ($WARMUP_ITERATIONS iteraciones)"
    log_info "========================================="
    
    for i in $(seq 1 $WARMUP_ITERATIONS); do
        log_info "Warmup iteración $i/$WARMUP_ITERATIONS"
        curl -s -o /dev/null "$FRONTEND_URL" || true
        curl -s -o /dev/null "$BACKEND_URL/api/ml/test-connection" || true
        curl -s -o /dev/null "$ML_SERVICE_URL/health" || true
        sleep 0.2
    done
    
    log_success "Warmup completado"
}

# ==============================================
# FUNCIÓN PRINCIPAL DE MEDICIÓN HTTP
# ==============================================
medir_http_request() {
    local url=$1
    local method=$2
    local extra_headers=$3
    local body=$4
    local output_csv=$5
    local descripcion=$6
    
    log_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info "Iniciando: $descripcion"
    log_info "URL: $url"
    log_info "Método: $method"
    log_info "Iteraciones: $ITERATIONS"
    
    # Crear CSV con header
    crear_header_http_csv > "$output_csv"
    
    # Array para almacenar tiempos
    declare -a tiempos_totales=()
    
    # Ejecutar iteraciones
    for i in $(seq 1 $ITERATIONS); do
        local resultado
        
        if [ -n "$body" ]; then
            if [ -n "$extra_headers" ]; then
                resultado=$(eval "curl -o /dev/null -s -w '%{time_total},%{time_connect},%{time_starttransfer},%{http_code},%{size_download}' \
                    -X '$method' \
                    -H 'Content-Type: application/json' \
                    $extra_headers \
                    -d '$body' \
                    '$url' 2>&1")
            else
                resultado=$(curl -o /dev/null -s -w "%{time_total},%{time_connect},%{time_starttransfer},%{http_code},%{size_download}" \
                    -X "$method" \
                    -H "Content-Type: application/json" \
                    -d "$body" \
                    "$url" 2>&1)
            fi
        else
            if [ -n "$extra_headers" ]; then
                resultado=$(eval "curl -o /dev/null -s -w '%{time_total},%{time_connect},%{time_starttransfer},%{http_code},%{size_download}' \
                    -X '$method' \
                    $extra_headers \
                    '$url' 2>&1")
            else
                resultado=$(curl -o /dev/null -s -w "%{time_total},%{time_connect},%{time_starttransfer},%{http_code},%{size_download}" \
                    -X "$method" \
                    "$url" 2>&1)
            fi
        fi
        
        IFS=',' read -r tiempo_total tiempo_conexion ttfb codigo tamanio <<< "$resultado"
        local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
        
        echo "\"$i\",\"$tiempo_total\",\"$tiempo_conexion\",\"$ttfb\",\"$codigo\",\"$tamanio\",\"$timestamp\"" >> "$output_csv"
        tiempos_totales+=("$tiempo_total")
        
        # Mostrar progreso cada 5 iteraciones
        if [ $((i % 5)) -eq 0 ]; then
            log_info "  Progreso: $i/$ITERATIONS iteraciones completadas"
        fi
        
        sleep $PAUSE_BETWEEN_TESTS
    done
    
    # Calcular y agregar estadísticas
    agregar_estadisticas_csv tiempos_totales "$output_csv"
    
    # Mostrar resumen
    local promedio=$(extraer_promedio_csv "$output_csv")
    local minimo=$(extraer_minimo_csv "$output_csv")
    local maximo=$(extraer_maximo_csv "$output_csv")
    
    log_success "Prueba completada"
    log_info "  Tiempo promedio: ${promedio}s"
    log_info "  Rango: ${minimo}s - ${maximo}s"
    log_info "  Archivo: $output_csv"
}

# ==============================================
# FUNCIÓN DE MEDICIÓN SQL
# ==============================================
medir_consulta_sql() {
    local query=$1
    local output_csv=$2
    local descripcion=$3
    
    log_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info "Iniciando: $descripcion"
    log_info "Query: ${query:0:80}..."
    log_info "Iteraciones: $ITERATIONS"
    
    # Crear CSV con header
    crear_header_sql_csv > "$output_csv"
    
    declare -a tiempos_totales=()
    
    for i in $(seq 1 $ITERATIONS); do
        # Ejecutar EXPLAIN ANALYZE y capturar resultado
        local resultado=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
            -c "EXPLAIN ANALYZE $query" 2>&1 | tail -2)
        
        local planning=$(echo "$resultado" | grep "Planning Time:" | awk '{print $3}')
        local execution=$(echo "$resultado" | grep "Execution Time:" | awk '{print $3}')
        
        # Si no se encontraron los valores, usar 0
        [ -z "$planning" ] && planning="0.0"
        [ -z "$execution" ] && execution="0.0"
        
        local total=$(awk "BEGIN {printf \"%.3f\", $planning + $execution}")
        local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
        
        echo "\"$i\",\"$planning\",\"$execution\",\"$total\",\"$timestamp\"" >> "$output_csv"
        tiempos_totales+=("$total")
        
        if [ $((i % 5)) -eq 0 ]; then
            log_info "  Progreso: $i/$ITERATIONS iteraciones completadas"
        fi
        
        sleep $PAUSE_BETWEEN_TESTS
    done
    
    # Agregar estadísticas
    agregar_estadisticas_sql_csv tiempos_totales "$output_csv"
    
    local promedio=$(extraer_promedio_csv "$output_csv")
    log_success "Prueba SQL completada"
    log_info "  Tiempo promedio: ${promedio}ms"
    log_info "  Archivo: $output_csv"
}

# ==============================================
# PRUEBAS BÁSICAS (FASE 4)
# ==============================================

# PRUEBA 01: Carga del Frontend
prueba_01_frontend_carga() {
    medir_http_request \
        "$FRONTEND_URL" \
        "GET" \
        "" \
        "" \
        "$OUTPUT_DIR/01_frontend_carga_$TIMESTAMP.csv" \
        "Prueba 01 - Carga del Frontend (HTML principal)"
}

# PRUEBA 02: Login
prueba_02_login() {
    medir_http_request \
        "$BACKEND_URL/api/auth/login" \
        "POST" \
        "" \
        "{\"nombre\":\"$TEST_USER\",\"contrasena\":\"$TEST_PASSWORD\"}" \
        "$OUTPUT_DIR/02_login_$TIMESTAMP.csv" \
        "Prueba 02 - Autenticación (POST /api/auth/login)"
}

# PRUEBA 03: BD - Consulta Simple
prueba_03_bd_consulta_simple() {
    medir_consulta_sql \
        "SELECT COUNT(*) FROM productos;" \
        "$OUTPUT_DIR/03_bd_consulta_simple_$TIMESTAMP.csv" \
        "Prueba 03 - Consulta simple COUNT en tabla productos"
}

# PRUEBA 04: BD - Consulta con JOIN
prueba_04_bd_consulta_join() {
    medir_consulta_sql \
        "SELECT p.id, p.nombre, p.precio_venta, pr.nombre as proveedor FROM productos p JOIN personas pr ON p.proveedor_id = pr.id LIMIT 20;" \
        "$OUTPUT_DIR/04_bd_consulta_join_$TIMESTAMP.csv" \
        "Prueba 04 - Consulta con JOIN de productos y proveedores"
}

# PRUEBA 05: BD - Consulta Compleja
prueba_05_bd_consulta_compleja() {
    medir_consulta_sql \
        "SELECT DATE(fecha_orden) as fecha, COUNT(*) as num_ordenes, SUM(total_venta) as total FROM ordenes_de_ventas GROUP BY DATE(fecha_orden) ORDER BY fecha DESC LIMIT 30;" \
        "$OUTPUT_DIR/05_bd_consulta_compleja_$TIMESTAMP.csv" \
        "Prueba 05 - Consulta compleja con agregaciones (SUM, GROUP BY)"
}

# PRUEBA 06: Backend API - GET Productos
prueba_06_backend_api_productos() {
    medir_http_request \
        "$BACKEND_URL/api/productos" \
        "GET" \
        "-H \"Authorization: Bearer $TOKEN\"" \
        "" \
        "$OUTPUT_DIR/06_backend_api_productos_$TIMESTAMP.csv" \
        "Prueba 06 - GET /api/productos (Backend → BD)"
}

# PRUEBA 07: Backend API - GET Producto por ID
prueba_07_backend_api_producto_by_id() {
    # Primero obtener un ID válido de producto
    local producto_id=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" \
        -t -c "SELECT id FROM productos LIMIT 1;" | tr -d ' ')
    
    if [ -z "$producto_id" ]; then
        log_warning "No se encontraron productos en la BD. Saltando prueba 07."
        return
    fi
    
    log_info "Usando producto ID: $producto_id"
    
    medir_http_request \
        "$BACKEND_URL/api/productos/$producto_id" \
        "GET" \
        "-H \"Authorization: Bearer $TOKEN\"" \
        "" \
        "$OUTPUT_DIR/07_backend_api_producto_by_id_$TIMESTAMP.csv" \
        "Prueba 07 - GET /api/productos/{id} (Backend → BD)"
}

# PRUEBA 08: ML Service - Health Check Directo
prueba_08_ml_health_directo() {
    medir_http_request \
        "$ML_SERVICE_URL/health" \
        "GET" \
        "" \
        "" \
        "$OUTPUT_DIR/08_ml_health_directo_$TIMESTAMP.csv" \
        "Prueba 08 - GET /health (ML Service directo)"
}

# PRUEBA 09: Backend → ML Health (Proxy)
prueba_09_backend_ml_health() {
    medir_http_request \
        "$BACKEND_URL/api/ml/health" \
        "GET" \
        "" \
        "" \
        "$OUTPUT_DIR/09_backend_ml_health_$TIMESTAMP.csv" \
        "Prueba 09 - GET /api/ml/health (Backend → ML via proxy)"
}

# PRUEBA 10: Backend → ML Predict
prueba_10_backend_ml_predict() {
    # Obtener historial real de ventas de la BD
    log_info "Obteniendo historial de ventas para predicción..."
    
    local historial=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c \
        "SELECT json_agg(json_build_object('fecha_orden', fecha_orden::text, 'productos_id', (SELECT id FROM productos LIMIT 1), 'cantidad_pz', 10)) 
         FROM (SELECT fecha_orden FROM ordenes_de_ventas ORDER BY fecha_orden DESC LIMIT 30) sub;")
    
    if [ -z "$historial" ] || [ "$historial" = " " ]; then
        log_warning "No hay suficientes datos de ventas. Usando datos sintéticos."
        historial='[{"fecha_orden":"2026-01-01","productos_id":"test-id","cantidad_pz":10}]'
    fi
    
    medir_http_request \
        "$BACKEND_URL/api/ml/predict" \
        "POST" \
        "" \
        "{\"historial_ventas\":$historial}" \
        "$OUTPUT_DIR/10_backend_ml_predict_$TIMESTAMP.csv" \
        "Prueba 10 - POST /api/ml/predict (Backend → ML Service)"
}

# ==============================================
# FUNCIÓN PRINCIPAL
# ==============================================
main() {
    echo ""
    echo "========================================="
    echo "  PRUEBAS DE RENDIMIENTO - SISTEMA POS  "
    echo "========================================="
    echo "Timestamp: $TIMESTAMP"
    echo "Iteraciones por prueba: $ITERATIONS"
    echo "========================================="
    echo ""
    
    # Crear directorios si no existen
    mkdir -p "$OUTPUT_DIR" "$LOGS_DIR"
    
    # Iniciar log
    echo "=== INICIO DE PRUEBAS DE RENDIMIENTO ===" > "$LOG_FILE"
    echo "Timestamp: $TIMESTAMP" >> "$LOG_FILE"
    echo "Usuario: $TEST_USER" >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
    
    # Iniciar monitor de recursos en background
    log_info "Iniciando monitor de recursos en background..."
    "$SCRIPT_DIR/monitor_recursos.sh" 300 &  # Máximo 5 minutos
    MONITOR_PID=$!
    log_info "Monitor de recursos iniciado (PID: $MONITOR_PID)"
    
    # Función de limpieza al salir
    cleanup_monitor() {
        if [ -n "${MONITOR_PID:-}" ]; then
            log_info "Deteniendo monitor de recursos (PID: $MONITOR_PID)..."
            kill $MONITOR_PID 2>/dev/null || true
            wait $MONITOR_PID 2>/dev/null || true
        fi
    }
    trap cleanup_monitor EXIT INT TERM
    
    # Verificaciones iniciales
    log_info "FASE 1: Verificaciones Iniciales"
    verificar_herramientas || exit 1
    verificar_servicios
    verificar_conectividad
    obtener_token_jwt
    ejecutar_warmup
    
    echo ""
    log_info "========================================="
    log_info "FASE 4: Pruebas Básicas (1-10)"
    log_info "========================================="
    echo ""
    
    prueba_01_frontend_carga
    prueba_02_login
    prueba_03_bd_consulta_simple
    prueba_04_bd_consulta_join
    prueba_05_bd_consulta_compleja
    prueba_06_backend_api_productos
    prueba_07_backend_api_producto_by_id
    prueba_08_ml_health_directo
    prueba_09_backend_ml_health
    prueba_10_backend_ml_predict
    
    echo ""
    log_success "========================================="
    log_success "TODAS LAS PRUEBAS BÁSICAS COMPLETADAS"
    log_success "========================================="
    log_info "Resultados guardados en: $OUTPUT_DIR"
    log_info "Log completo en: $LOG_FILE"
    echo ""
    
    # Detener monitor de recursos
    cleanup_monitor
    trap - EXIT INT TERM  # Remover trap
    
    # Consolidar resultados de rendimiento
    log_info "Consolidando resultados de rendimiento..."
    "$SCRIPT_DIR/consolidar_resultados.sh" 2>&1 | grep -E "(INFO|✓|⚠)" || true
    
    # Consolidar resultados de recursos
    log_info "Consolidando resultados de recursos..."
    "$SCRIPT_DIR/consolidar_recursos.sh" 2>&1 | grep -E "(INFO|✓|⚠)" || true
    
    echo ""
    log_success "========================================="
    log_success "TODOS LOS REPORTES GENERADOS"
    log_success "========================================="
    echo ""
    log_info "Reportes disponibles en: $OUTPUT_DIR"
    log_info "  - Rendimiento: RESUMEN_LATENCIAS_POR_MODULO_*.csv"
    log_info "  - Recursos: RESUMEN_RECURSOS_*.csv"
    log_info "  - Reporte completo: REPORTE_RECURSOS_*.txt"
    echo ""
}

# Ejecutar función principal
main
