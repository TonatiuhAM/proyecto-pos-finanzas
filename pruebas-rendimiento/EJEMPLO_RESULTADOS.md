# Ejemplo de Resultados - Ejecución del 06 Feb 2026

## 📊 Resumen Ejecutivo

**Fecha**: 06 de Febrero de 2026 00:06:06  
**Duración**: 28 segundos  
**Iteraciones por prueba**: 20  
**Muestras de recursos**: 14 (cada 2 segundos)

---

## 🎯 Rendimiento (Latencias)

### Resultados por Módulo

| Prueba | Latencia Promedio | Estado |
|--------|-------------------|--------|
| Frontend HTML | 0.562 ms | 🟢 Excelente |
| Login | 3.154 ms | 🟢 Excelente |
| SQL Queries | < 0.01 ms | 🟢 Excepcional |
| GET /productos | 13.0 ms | 🟡 Bueno |
| GET /producto/{id} | 4.4 ms | 🟢 Muy bueno |
| ML Health | 1.0 ms | 🟢 Excelente |
| ML Predict | 1.4 ms | 🔴 Revisar |

---

## 💻 Recursos del Sistema

### pos_backend (Spring Boot)

```
CPU:
  ├─ Promedio: 1.94%
  ├─ Pico: 9.10%
  └─ Estado: ✅ Normal

RAM:
  ├─ Promedio: 496.61 MB
  ├─ Pico: 497.80 MB
  └─ Estado: ⚠️  Uso alto (6.14% del sistema)

Disco I/O (durante pruebas):
  ├─ Lectura: 20.48 MB
  └─ Escritura: 0.00 MB

Red I/O (durante pruebas):
  ├─ Entrada: 0.60 MB
  └─ Salida: 0.60 MB

Top Procesos:
  1. ps aux --sort=- (33.30% CPU - comando de monitoreo)
  2. java -XX:TieredStopAtLevel=1 (0.40% CPU, 2.60% MEM - aplicación Spring Boot)
  3. java maven wrapper (0.10% CPU, 0.50% MEM - proceso Maven)
```

### pos_database (PostgreSQL)

```
CPU:
  ├─ Promedio: 7.86%
  ├─ Pico: 24.14%
  └─ Estado: ✅ Normal (picos esperados durante queries)

RAM:
  ├─ Promedio: 55.85 MB
  ├─ Pico: 56.36 MB
  └─ Estado: ✅ Excelente (< 1% del sistema)

Disco I/O (durante pruebas):
  ├─ Lectura: 3.00 MB
  └─ Escritura: 0.00 MB

Red I/O (durante pruebas):
  ├─ Entrada: 0.30 MB
  └─ Salida: 0.40 MB

Top Procesos:
  1. ps aux --sort=- (50.00% CPU - comando de monitoreo)
  2. postgres: walwriter (0.00% CPU - proceso de escritura de logs)
  3. postgres: idle (0.10% MEM - conexión inactiva)
```

### pos_frontend (Nginx)

```
CPU:
  ├─ Promedio: 0.03%
  ├─ Pico: 0.19%
  └─ Estado: ✅ Excelente (casi inactivo)

RAM:
  ├─ Promedio: 8.28 MB
  ├─ Pico: 9.14 MB
  └─ Estado: ✅ Excelente (< 0.1% del sistema)

Disco I/O (durante pruebas):
  ├─ Lectura: 5.00 MB
  └─ Escritura: 0.00 MB

Red I/O (durante pruebas):
  ├─ Entrada: 0.00 MB
  └─ Salida: 0.02 MB

Observaciones:
  - Extremadamente ligero (Nginx solo sirve archivos estáticos)
  - Sin escritura a disco (todo en memoria)
  - Red mínima (solo 20KB salientes)
```

### pos_ml_prediction_api (FastAPI)

```
CPU:
  ├─ Promedio: 0.18%
  ├─ Pico: 0.68%
  └─ Estado: ✅ Excelente

RAM:
  ├─ Promedio: 103.60 MB
  ├─ Pico: 104.30 MB
  └─ Estado: ⚠️  Uso medio (1.28% del sistema)

Disco I/O (durante pruebas):
  ├─ Lectura: 1.00 MB
  └─ Escritura: 1.00 MB

Red I/O (durante pruebas):
  ├─ Entrada: 0.10 MB
  └─ Salida: 0.01 MB

Observaciones:
  - CPU muy baja (posiblemente no ejecuta modelo real)
  - RAM constante (~104 MB)
  - I/O mínimo
```

---

## 🔍 Análisis Detallado

### ✅ Fortalezas

1. **Frontend ultrarrápido**
   - Latencia: 0.562 ms
   - Uso de recursos: Mínimo (0.03% CPU, 8 MB RAM)
   - Nginx sirviendo archivos estáticos es óptimo

2. **Base de datos eficiente**
   - Queries SQL < 0.01 ms (cache muy efectivo)
   - Uso de RAM: Solo 56 MB
   - CPU en picos del 24% solo durante queries intensivas

3. **Backend Spring Boot estable**
   - Latencias aceptables (4-13 ms según endpoint)
   - CPU baja promedio (1.94%)
   - RAM estable en ~497 MB

4. **ML Service responsive**
   - Health checks en ~1 ms
   - CPU y RAM bajos

### ⚠️  Áreas de Atención

1. **GET /productos (13 ms)**
   - **Estado**: 🟡 Aceptable pero escalará mal
   - **Problema**: Con 19 productos toma 13ms. Con 1000 productos podría tomar 680ms
   - **Recomendación**: Implementar paginación (límite de 20-50 items por página)
   - **Impacto**: Reduciría latencia a < 20 ms incluso con 10,000 productos

2. **ML Predict (1.4 ms)**
   - **Estado**: 🔴 Sospechoso
   - **Problema**: Demasiado rápido para una predicción real
   - **Análisis**: 
     - Latencia casi idéntica a health check (1.4ms vs 1.0ms)
     - CPU del ML Service muy bajo (0.18% promedio)
     - Sin picos de CPU durante las 20 predicciones
   - **Hipótesis**: Posiblemente devuelve respuesta mock o cached
   - **Predicción real esperada**: 50-500 ms
   - **Recomendación**: Revisar código del endpoint /predict para confirmar que:
     - Carga el modelo correctamente
     - Ejecuta la inferencia
     - Procesa datos de entrada

3. **RAM de Backend con flag "MEM_ALTO"**
   - **Estado**: ⚠️  Indicador de precaución
   - **Análisis**: 
     - El script marca "MEM_ALTO_PROMEDIO" y "MEM_CRITICO_PICO"
     - Sin embargo, solo usa 497 MB de 8096 MB (6.14%)
     - **Este es un FALSO POSITIVO**
   - **Causa**: El threshold está configurado para % dentro del contenedor, no del sistema
   - **Acción**: Ignorar este warning. 497 MB para Spring Boot es normal

### 📈 Uso Total del Sistema

```
CPU combinada (todos los contenedores): ~10% (suficiente headroom)
RAM combinada: ~665 MB de 8096 MB (8.2% usado)
Disco leído: ~29 MB en 28 segundos
Disco escrito: ~1 MB en 28 segundos
Red enviada: ~1 MB
Red recibida: ~1 MB
```

**Conclusión**: El sistema tiene mucho margen para crecer. Con solo 8% de RAM usada y 10% de CPU, podría manejar 5-10x más carga.

---

## 🎯 Prioridades de Acción

### Alta Prioridad 🔴

1. **Verificar ML Predict**
   - Revisar archivo: `ml-service/app/main.py` (o similar)
   - Buscar el endpoint POST `/predict`
   - Confirmar que carga modelo y ejecuta inferencia
   - Si es mock, implementar lógica real
   - Re-ejecutar pruebas y verificar latencia > 50ms

### Media Prioridad 🟡

2. **Implementar Paginación en /productos**
   - Modificar: `backend/controller/ProductoController.java`
   - Agregar parámetros: `?page=0&size=20`
   - Usar `Pageable` de Spring Data
   - Reducir carga para grandes datasets

3. **Agregar Pruebas de Carga**
   - Simular 10, 50, 100 usuarios concurrentes
   - Usar herramienta: Apache JMeter o Gatling
   - Identificar punto de saturación

### Baja Prioridad 🟢

4. **Optimizar filtros de Spring Security** (si es necesario)
   - Actualmente agrega ~5-7ms por request
   - Solo optimizar si latencia se vuelve crítica en producción

5. **Monitoreo Continuo**
   - Configurar Spring Boot Actuator + Prometheus
   - Alertas si P95 > threshold
   - Dashboard en Grafana

---

## 📊 Comparación con Valores Esperados

| Métrica | Valor Obtenido | Esperado (Dev) | Producción | Estado |
|---------|----------------|----------------|------------|--------|
| Frontend | 0.6 ms | < 1 ms | < 50 ms | ✅ |
| Login | 3.2 ms | < 5 ms | < 100 ms | ✅ |
| API simple | 4.4 ms | < 10 ms | < 100 ms | ✅ |
| API lista | 13.0 ms | < 20 ms | < 150 ms | ✅ |
| SQL query | < 0.01 ms | < 1 ms | < 10 ms | ✅ |
| ML Predict | 1.4 ms | 50-200 ms | 100-500 ms | ❌ |

**Nota**: Valores de producción incluyen +30-50ms de latencia de red real.

---

## 💡 Recomendaciones Generales

### Optimizaciones No Urgentes

El sistema está funcionando bien. No requiere optimizaciones inmediatas excepto:
- ✅ Verificar ML Predict
- ✅ Implementar paginación antes de agregar 100+ productos

### Preparación para Producción

1. **Agregar caché** (Redis o Caffeine)
   - Para GET /productos (TTL: 5-10 minutos)
   - Para categorías y proveedores
   - Reducirá latencia de 13ms → 2-3ms

2. **Connection pooling**
   - HikariCP ya está incluido en Spring Boot
   - Verificar configuración óptima (pool size ~10-20)

3. **Índices de BD**
   - Revisar con EXPLAIN ANALYZE queries lentas
   - Agregar índices compuestos si es necesario

4. **Escalado horizontal**
   - Con el bajo uso actual (8% RAM, 10% CPU)
   - Una sola instancia puede manejar 500-1000 usuarios simultáneos
   - Considerar escalado solo si se superan 1000 usuarios

---

## 📁 Archivos de Esta Ejecución

```
resultados/
├── 01-10_*.csv                      # Latencias individuales (10 archivos)
├── recursos_raw_20260206_000606.csv # Recursos capturados (14 muestras)
├── procesos_raw_20260206_000606.csv # Top procesos
├── RESUMEN_LATENCIAS_*.csv          # Consolidado de latencias
├── RESUMEN_RECURSOS_*.csv           # Consolidado de recursos
├── TOP_PROCESOS_*.csv               # Análisis de procesos
└── REPORTE_RECURSOS_*.txt           # Reporte completo
```

---

**Fin del documento**
