# Metodología de Pruebas de Rendimiento

## 📋 Descripción General

Este documento describe la metodología técnica utilizada para realizar las pruebas de rendimiento del Sistema POS. Las pruebas son **no invasivas** y se ejecutan sobre las instancias actuales sin modificar código.

## 🔬 Enfoque Técnico

### Herramientas Utilizadas

1. **curl**: Cliente HTTP para medir tiempos de respuesta
2. **docker exec**: Para ejecutar consultas SQL directamente en PostgreSQL
3. **psql**: Cliente de PostgreSQL con EXPLAIN ANALYZE
4. **awk**: Para cálculos estadísticos avanzados
5. **bash**: Orquestación y automatización

### Ventajas del Enfoque

- ✅ No requiere modificaciones de código
- ✅ Pruebas reproducibles y automatizadas
- ✅ Mediciones precisas con curl -w
- ✅ Estadísticas detalladas (min, max, promedio, P95, P99)
- ✅ Resultados exportables en CSV

## 📊 Tipos de Pruebas

### 1. Pruebas HTTP (Frontend ↔ Backend)

**Comando curl con timing:**

```bash
curl -o /dev/null -s -w "%{time_total},%{time_connect},%{time_starttransfer},%{http_code},%{size_download}" \
  -X GET "http://localhost:5173"
```

**Métricas capturadas:**
- `time_total`: Tiempo total del request (segundos)
- `time_connect`: Tiempo de establecer conexión TCP (segundos)
- `time_starttransfer`: TTFB - Tiempo hasta primer byte (segundos)
- `http_code`: Código de respuesta HTTP
- `size_download`: Tamaño de la respuesta (bytes)

### 2. Pruebas SQL (Backend ↔ Base de Datos)

**Comando psql con EXPLAIN ANALYZE:**

```bash
docker exec pos_database psql -U postgres -d pos_finanzas \
  -c "EXPLAIN ANALYZE SELECT COUNT(*) FROM productos;"
```

**Métricas capturadas:**
- `Planning Time`: Tiempo de optimización de la query (ms)
- `Execution Time`: Tiempo de ejecución real (ms)
- `Total Time`: Planning + Execution (ms)

**Ejemplo de salida:**

```
Planning Time: 0.123 ms
Execution Time: 1.456 ms
```

### 3. Pruebas de API (Backend ↔ ML Service)

**Request con autenticación JWT:**

```bash
curl -o /dev/null -s -w "%{time_total}" \
  -X POST "$BACKEND_URL/api/ml/predict" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"historial_ventas": [...]}'
```

## 🔢 Cálculos Estadísticos

Todas las estadísticas se calculan con `awk` para máxima precisión:

### Promedio (Media Aritmética)

```awk
mean = sum / count
```

### Desviación Estándar

```awk
sum_sq_diff = 0
for (i = 0; i < count; i++) {
    diff = values[i] - mean
    sum_sq_diff += diff * diff
}
std_dev = sqrt(sum_sq_diff / count)
```

### Mediana

1. Ordenar array de valores
2. Si count es par: mediana = (valores[n/2-1] + valores[n/2]) / 2
3. Si count es impar: mediana = valores[n/2]

### Percentil 95 (P95)

```awk
p95_idx = int(count * 0.95)
if (p95_idx >= count) p95_idx = count - 1
p95 = sorted_values[p95_idx]
```

### Percentil 99 (P99)

```awk
p99_idx = int(count * 0.99)
if (p99_idx >= count) p99_idx = count - 1
p99 = sorted_values[p99_idx]
```

## 🔄 Flujo de Ejecución

### Fase 1: Verificación Inicial

```
1. Verificar servicios Docker corriendo
2. Verificar conectividad a cada servicio
3. Obtener token JWT para autenticación
4. Ejecutar warmup (3 iteraciones descartadas)
```

### Fase 2: Ejecución de Pruebas

Para cada prueba:

```
FOR i = 1 TO 20:
    1. Ejecutar request/query
    2. Capturar métricas de timing
    3. Guardar resultado en CSV
    4. Pausa de 0.1 segundos
END FOR
```

### Fase 3: Cálculo de Estadísticas

```
1. Ordenar array de tiempos
2. Calcular min, max, promedio, mediana
3. Calcular desviación estándar
4. Calcular P95 y P99
5. Agregar al CSV
```

### Fase 4: Consolidación

```
1. Leer todos los CSVs individuales
2. Extraer estadísticas de cada uno
3. Generar resumen consolidado
4. Generar reporte ejecutivo
```

## 🎯 Estrategia de Warmup

**Objetivo**: Evitar medir cold start de servicios.

**Implementación**:
```bash
for i in 1..3:
    curl frontend
    curl backend/ml/test-connection
    curl ml-service/health
    sleep 0.2
```

**Razón**: 
- JVM (Spring Boot) necesita tiempo para cargar clases
- Nginx (frontend) cachea archivos estáticos
- PostgreSQL cachea queries frecuentes
- ML Service carga modelos en memoria

Descartamos las primeras 3 iteraciones para medir solo el estado "caliente" del sistema.

## 📏 Número de Iteraciones

**Configuración**: 20 iteraciones por prueba

**Justificación**:
- **< 10 iteraciones**: Muestra muy pequeña, estadísticas poco confiables
- **20-30 iteraciones**: Balance entre tiempo de ejecución y confiabilidad
- **> 50 iteraciones**: Tiempo excesivo sin mejora significativa en precisión

**Distribución normal**: Con 20 muestras, la distribución se aproxima a normal según Teorema del Límite Central.

## ⏱️ Pausas entre Requests

**Configuración**:
- `PAUSE_BETWEEN_TESTS = 0.1s` (entre requests individuales)
- `PAUSE_BETWEEN_ITERATIONS = 0.5s` (entre iteraciones concurrentes)

**Razón**:
- Evitar saturación de servicios
- Permitir que servicios completen tareas pendientes
- Reducir interferencia entre mediciones
- Simular ritmo más realista

## 🔐 Autenticación

**Proceso**:

1. **Obtener Token JWT**:
```bash
TOKEN=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Tona","contrasena":"123456"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

2. **Usar Token en Requests**:
```bash
curl -H "Authorization: Bearer $TOKEN" "$BACKEND_URL/api/productos"
```

3. **Validez**: Token válido por 24 horas (configuración del backend)

## 📊 Formato de Datos

### CSV Individual

```csv
"Iteración","Tiempo Total (s)","Tiempo Conexión (s)","TTFB (s)","Código HTTP","Tamaño Descarga (bytes)","Timestamp"
1,0.0042,0.0001,0.0041,200,1115,"2026-02-03 15:23:01"
...
"ESTADÍSTICAS","","","","",""
"Promedio",0.0042,...
```

**Ventajas**:
- Compatible con Excel/LibreOffice
- Fácil de parsear con scripts
- Incluye timestamps para análisis temporal
- Estadísticas integradas

### CSV Consolidado

```csv
"Comunicación","Promedio (s)","Mínimo (s)","Máximo (s)","Desv. Estándar","P95 (s)","P99 (s)","Iteraciones","Descripción"
```

**Ventajas**:
- Vista general de todas las pruebas
- Fácil comparación entre módulos
- Listo para generar gráficas

## ⚠️ Consideraciones y Limitaciones

### Entorno de Prueba

- **Localhost**: Latencia de red mínima (~0.1ms)
- **Docker**: Overhead de virtualización (~1-5%)
- **Recursos compartidos**: CPU/RAM/Disco compartidos entre servicios

### Factores que Afectan Resultados

1. **Carga de la máquina host**
   - Otros procesos consumiendo CPU
   - Memoria RAM disponible
   - I/O de disco

2. **Estado de los servicios**
   - Caché de PostgreSQL
   - JVM garbage collection
   - Modelos ML cargados en memoria

3. **Variabilidad natural**
   - Scheduling del sistema operativo
   - Context switching
   - Background tasks

### Mitigaciones Implementadas

✅ **Warmup**: 3 iteraciones descartadas  
✅ **Múltiples iteraciones**: 20 por prueba  
✅ **Estadísticas robustas**: P95, P99 menos sensibles a outliers  
✅ **Pausas entre requests**: Evitar saturación  
✅ **Desviación estándar**: Medir consistencia

## 🎓 Referencias

- **curl man page**: Documentación de opciones de timing
- **PostgreSQL EXPLAIN ANALYZE**: [Documentación oficial](https://www.postgresql.org/docs/current/sql-explain.html)
- **HTTP timing**: [W3C Navigation Timing](https://www.w3.org/TR/navigation-timing/)
- **Percentiles**: Método de interpolación lineal

## 📝 Validación de Resultados

### Valores Esperados (localhost)

| Operación | Esperado | Excelente | Aceptable | Problemático |
|-----------|----------|-----------|-----------|--------------|
| Frontend HTML | < 10ms | < 5ms | 5-20ms | > 20ms |
| Login | < 50ms | < 20ms | 20-100ms | > 100ms |
| Query Simple | < 5ms | < 2ms | 2-10ms | > 10ms |
| Query Compleja | < 50ms | < 20ms | 20-100ms | > 100ms |
| ML Predicción | < 2000ms | < 1000ms | 1000-3000ms | > 3000ms |

### Detección de Anomalías

🚨 **Indicadores de problema**:
- Desviación estándar > 50% del promedio
- P99 > 3x el promedio
- Códigos HTTP diferentes de 200
- Tiempos negativos o cero
- Diferencia significativa entre iteraciones

---

**Última actualización**: 03 de Febrero de 2026  
**Versión**: 1.0.0  
**Autor**: Equipo de Desarrollo POS Finanzas
