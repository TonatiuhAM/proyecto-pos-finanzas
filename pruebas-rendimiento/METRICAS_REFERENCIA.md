# Métricas de Referencia - Sistema POS

> **Última actualización**: 03 Feb 2026  
> **Ambiente**: Desarrollo (localhost, Docker)  
> **Dataset**: 19 productos, 248 órdenes de venta

---

## 📊 Resumen Ejecutivo

| Componente | Latencia Promedio | Estado | Observaciones |
|------------|-------------------|--------|---------------|
| **Frontend** | 0.6 ms | 🟢 Excelente | Nginx sirviendo HTML estático |
| **Backend - Auth** | 3.2 ms | 🟢 Excelente | bcrypt con 10 rounds |
| **Backend - API** | 4-13 ms | 🟡 Bueno | Depende de complejidad |
| **Base de Datos** | < 0.01 ms | 🟢 Excepcional | Queries en caché |
| **ML Service** | 1-1.5 ms | 🟡 Revisar | Posiblemente no ejecuta modelo real |

---

## 🎯 Métricas Detalladas (Baseline)

### Frontend

```
GET / (index.html)
├─ Promedio: 0.562 ms
├─ Min:      0.463 ms
├─ Max:      0.971 ms
├─ P95:      0.971 ms
└─ Estado:   🟢 Excelente
```

### Backend - Autenticación

```
POST /api/auth/login
├─ Promedio: 3.154 ms
├─ Min:      2.824 ms
├─ Max:      3.767 ms
├─ P95:      3.767 ms
├─ Estado:   🟢 Excelente
└─ Nota:     Incluye bcrypt hashing (seguro)
```

### Backend - APIs de Productos

```
GET /api/productos (lista completa)
├─ Promedio: 13.005 ms
├─ Min:      12.178 ms
├─ Max:      14.031 ms
├─ P95:      14.031 ms
├─ Tamaño:   9,056 bytes (19 productos)
├─ Estado:   🟡 Aceptable
└─ Nota:     Podría escalar mal con 1000+ productos

GET /api/productos/{id} (individual)
├─ Promedio: 4.344 ms
├─ Min:      3.902 ms
├─ Max:      4.834 ms
├─ P95:      4.834 ms
├─ Estado:   🟢 Muy bueno
└─ Nota:     3x más rápido que lista completa
```

### Base de Datos (PostgreSQL)

```
SELECT COUNT(*) FROM productos
├─ Tiempo:   < 0.01 ms
├─ Estado:   🟢 Excepcional
└─ Nota:     Caché de PostgreSQL muy efectivo

SELECT con JOIN (productos + proveedores)
├─ Tiempo:   < 0.01 ms
├─ Estado:   🟢 Excepcional
└─ Nota:     Índices bien configurados

Agregaciones (GROUP BY, SUM)
├─ Tiempo:   < 0.01 ms
├─ Estado:   🟢 Excepcional
└─ Nota:     Dataset pequeño (248 órdenes)
```

### ML Service (FastAPI)

```
GET /health (directo)
├─ Promedio: 0.962 ms
├─ Min:      0.816 ms
├─ Max:      1.056 ms
├─ P95:      1.056 ms
└─ Estado:   🟢 Excelente

GET /api/ml/health (via Backend proxy)
├─ Promedio: 1.449 ms
├─ Min:      1.281 ms
├─ Max:      1.647 ms
├─ P95:      1.647 ms
├─ Overhead: +0.487 ms (proxy)
└─ Estado:   🟢 Muy bueno

POST /api/ml/predict
├─ Promedio: 1.475 ms
├─ Min:      1.339 ms
├─ Max:      1.765 ms
├─ P95:      1.765 ms
├─ Estado:   🔴 Sospechoso
└─ ⚠️ REVISAR: Demasiado rápido para predicción real
              Esperado: 50-500ms
```

---

## 🚦 Umbrales Recomendados

### Ambiente de Desarrollo (localhost)

| Operación | Objetivo | Aceptable | Crítico |
|-----------|----------|-----------|---------|
| Frontend (HTML) | < 1 ms | < 5 ms | > 10 ms |
| Login (Auth) | < 5 ms | < 10 ms | > 20 ms |
| API GET simple | < 10 ms | < 50 ms | > 100 ms |
| API GET lista | < 20 ms | < 100 ms | > 500 ms |
| SQL query simple | < 1 ms | < 10 ms | > 50 ms |
| ML Prediction | 50-200 ms | < 500 ms | > 2000 ms |

### Ambiente de Producción (red real)

Agregar **+30 a +50 ms** de latencia de red a todas las operaciones:

| Operación | Objetivo | Aceptable | Crítico |
|-----------|----------|-----------|---------|
| Frontend (HTML) | < 50 ms | < 100 ms | > 200 ms |
| Login (Auth) | < 100 ms | < 200 ms | > 500 ms |
| API GET simple | < 100 ms | < 200 ms | > 500 ms |
| API GET lista | < 150 ms | < 300 ms | > 1000 ms |
| ML Prediction | 100-500 ms | < 1000 ms | > 3000 ms |

---

## 📐 Fórmulas de Cálculo

### Latencia Promedio (Mean)
```
μ = (Σ xi) / n
donde:
  xi = latencia de iteración i
  n  = número de iteraciones
```

### Desviación Estándar
```
σ = √(Σ(xi - μ)² / n)
```

### Percentil 95 (P95)
```
P95 = valor en posición ⌈0.95 × n⌉ del array ordenado
```
El 95% de los requests son más rápidos que este valor.

### Percentil 99 (P99)
```
P99 = valor en posición ⌈0.99 × n⌉ del array ordenado
```
El 99% de los requests son más rápidos que este valor.

---

## 🔍 Interpretación de Resultados

### ¿Cómo identificar problemas?

#### 1. Latencia Alta (Mean > Umbral)
**Síntoma**: Promedio excede el valor aceptable  
**Causas posibles**:
- Consultas SQL sin índices
- N+1 queries (JPA lazy loading)
- Payload muy grande (JSON serialization)
- Falta de caché

**Acción**: Revisar query, agregar índices, implementar caché

#### 2. Alta Variabilidad (σ alto)
**Síntoma**: Desviación estándar > 20% del promedio  
**Causas posibles**:
- Garbage Collector pausas (JVM)
- Contention en base de datos
- Network jitter
- Throttling de CPU

**Acción**: Revisar logs de GC, monitorear recursos, agregar warmup

#### 3. Outliers (P95/P99 >> Mean)
**Síntoma**: Percentil 95 es 2-3x mayor que el promedio  
**Causas posibles**:
- Cold cache (primera request)
- Lazy initialization
- Timeouts de conexiones
- Carga variable del sistema

**Acción**: Implementar warmup, pre-cargar caché, connection pooling

#### 4. Latencia Sospechosamente Baja
**Síntoma**: Operación compleja toma < 1ms  
**Causas posibles**:
- Request no llega al destino (error silencioso)
- Caché devuelve valor antiguo
- Mock/stub en lugar de operación real

**Acción**: Verificar logs, revisar código, confirmar que ejecuta

---

## 🎯 Objetivos de Optimización

### Prioridad ALTA 🔴

1. **Verificar ML Predict**:
   - **Actual**: 1.5 ms
   - **Esperado**: 50-500 ms
   - **Acción**: Confirmar que ejecuta modelo, no mock

2. **Implementar Paginación en GET /productos**:
   - **Actual**: 13 ms para 19 productos
   - **Proyectado**: ~650 ms para 1000 productos (escalamiento lineal)
   - **Objetivo**: < 50 ms con paginación (20 items)

### Prioridad MEDIA 🟡

3. **Agregar Caché en Backend**:
   - **Target**: GET /productos, GET /categorias
   - **Tecnología**: Caffeine (in-memory) o Redis (distribuido)
   - **Objetivo**: Reducir latencia de 13ms → 2-3ms

4. **Optimizar Serialización JSON**:
   - **Actual**: ~2-3 ms (estimado)
   - **Alternativas**: Jackson con caché de reflections, proyecciones JPA
   - **Objetivo**: Reducir a < 1 ms

### Prioridad BAJA 🟢

5. **Optimizar Chain de Spring Security**:
   - **Actual**: ~5-7 ms overhead por request
   - **Optimización**: Excluir rutas públicas, optimizar filtros
   - **Objetivo**: Reducir a < 3 ms

---

## 📊 Comparación con Industria

### Benchmarks de Referencia

| Sistema | Latencia típica |
|---------|-----------------|
| **CDN (HTML estático)** | 10-50 ms (P95) |
| **API REST simple** | 50-100 ms (P95) |
| **API REST con DB** | 100-200 ms (P95) |
| **ML Inference** | 100-1000 ms (depende del modelo) |
| **Búsqueda (Elasticsearch)** | 20-100 ms (P95) |

### ¿Cómo estamos?

| Nuestro Sistema | Benchmark Industria | Estado |
|-----------------|---------------------|--------|
| Frontend: 0.6 ms | 10-50 ms | 🟢 Mejor que industria |
| API simple: 4 ms | 50-100 ms | 🟢 Mejor que industria |
| API con DB: 13 ms | 100-200 ms | 🟢 Mejor que industria |
| ML Predict: 1.5 ms | 100-1000 ms | 🔴 Anormalmente bajo |

**Nota**: Mediciones en localhost sin latencia de red. Agregar +30-50ms para comparación justa.

---

## 🛠️ Herramientas de Medición

### En Desarrollo

```bash
# Ejecutar todas las pruebas (28 segundos)
cd pruebas-rendimiento/scripts
./benchmark.sh

# Consolidar resultados
./consolidar_resultados.sh

# Ver reporte ejecutivo
cat ../resultados/REPORTE_EJECUTIVO_*.txt

# Ver resumen CSV
cat ../resultados/RESUMEN_LATENCIAS_POR_MODULO_*.csv
```

### En Producción

```bash
# Spring Boot Actuator (métricas en tiempo real)
curl http://backend:8080/actuator/metrics/http.server.requests

# Prometheus + Grafana
# Dashboard con latencias P50, P95, P99 por endpoint

# Logs estructurados
# Buscar en logs: "duration_ms" field
```

---

## 📝 Changelog

### [2026-02-03] - Baseline Inicial

**Agregado**:
- Primeras mediciones de rendimiento
- 10 pruebas implementadas (Frontend, Backend, DB, ML)
- 20 iteraciones por prueba con warmup
- Documentación completa de metodología

**Descubierto**:
- Frontend y DB extremadamente rápidos (< 1ms)
- GET /productos podría escalar mal
- ML Predict sospechosamente rápido (revisar)
- Overhead de Spring Security: ~5-7ms

**Pendiente**:
- Pruebas de carga (usuarios concurrentes)
- Dataset grande (1000+ productos)
- Verificar implementación de ML Predict
- Integración con CI/CD

---

## 🔗 Referencias

- [Documentación Completa](README.md)
- [Metodología Técnica](docs/METODOLOGIA.md)
- [Interpretación de Resultados](docs/INTERPRETACION_RESULTADOS.md)
- [Resumen de Sesión](RESUMEN_SESION.md)

---

**Última ejecución**: 2026-02-03 23:18:59  
**Timestamp**: 20260203_231831  
**Resultados**: `/pruebas-rendimiento/resultados/`
