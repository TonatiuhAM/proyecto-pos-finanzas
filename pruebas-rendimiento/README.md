# Pruebas de Rendimiento - Sistema POS y Gestión Integral

## 📋 Descripción

Este directorio contiene un sistema completo de pruebas de rendimiento **NO INVASIVAS** para medir la latencia de comunicación entre los diferentes módulos del Sistema POS:

- **Frontend** (React en puerto 5173)
- **Backend** (Spring Boot en puerto 8084)
- **Base de Datos** (PostgreSQL en puerto 5433)
- **ML Service** (FastAPI en puerto 8004)

Las pruebas se ejecutan sobre las **instancias actuales corriendo en Docker** sin modificar ningún código del sistema.

## 🎯 Objetivos

1. **Medir Latencias**: Determinar tiempos de respuesta entre módulos
2. **Monitorear Recursos**: Capturar uso de CPU, RAM, Disco y Red
3. **Identificar Cuellos de Botella**: Detectar operaciones lentas o intensivas
4. **Analizar Procesos**: Top procesos por CPU/RAM dentro de cada contenedor
5. **Validar Performance**: Comparar con umbrales establecidos
6. **Generar Reportes**: Documentación automática con recomendaciones

## ⚙️ Configuración

### Requisitos Previos

- Docker con todos los servicios corriendo
- `bash`, `curl`, `awk` instalados
- Usuario de prueba: `Tona` / `123456`
- Espacio en disco: ~50MB para resultados

### Verificar Servicios

Antes de ejecutar las pruebas, verificar que todos los contenedores estén corriendo:

```bash
docker ps
```

Deben estar activos:
- `pos_backend` (puerto 8084)
- `pos_frontend` (puerto 5173)
- `pos_database` (puerto 5433)
- `pos_ml_prediction_api` (puerto 8004)

## 🚀 Ejecución Rápida

### Paso 1: Navegar al directorio de scripts

```bash
cd pruebas-rendimiento/scripts
```

### Paso 2: Ejecutar todas las pruebas

```bash
./benchmark.sh
```

Este comando ejecutará automáticamente:
- ✅ **10 pruebas de rendimiento** (20 iteraciones cada una)
- ✅ **Monitoreo de recursos** en segundo plano (cada 2 segundos)
- ✅ **Captura de procesos** (top 5 por CPU/RAM cada 10 segundos)
- ✅ **Consolidación automática** de resultados
- ✅ **Generación de reportes** ejecutivos

**Duración estimada**: ~30 segundos

### Paso 3: Ver los resultados

Los reportes se generan automáticamente en `resultados/`:

```bash
# Ver reporte de recursos
cat ../resultados/REPORTE_RECURSOS_*.txt

# Ver resumen de latencias
cat ../resultados/RESUMEN_LATENCIAS_POR_MODULO_*.csv

# Ver resumen de recursos
cat ../resultados/RESUMEN_RECURSOS_*.csv
```
```

### Paso 2: Ejecutar pruebas

```bash
./benchmark.sh
```

Esto ejecutará las **10 pruebas básicas** con **20 iteraciones cada una**. Tiempo estimado: ~15-20 minutos.

### Paso 3: Consolidar resultados

```bash
./consolidar_resultados.sh
```

Esto generará:
- `RESUMEN_LATENCIAS_POR_MODULO_<timestamp>.csv`
- `GLOSARIO_METRICAS_<timestamp>.csv`
- `REPORTE_EJECUTIVO_<timestamp>.txt`

### Paso 4: Ver resultados

```bash
# Ver reporte ejecutivo
cat ../resultados/REPORTE_EJECUTIVO_*.txt

# Ver resumen de latencias (formato CSV)
cat ../resultados/RESUMEN_LATENCIAS_POR_MODULO_*.csv
```

## 📊 Pruebas Incluidas

### Pruebas de Rendimiento (1-10)

| # | Prueba | Descripción | Módulos | Iteraciones |
|---|--------|-------------|---------|-------------|
| 01 | Frontend Carga | Tiempo de carga del HTML principal | Frontend | 20 |
| 02 | Login | Autenticación con JWT | Frontend → Backend | 20 |
| 03 | BD Query Simple | SELECT COUNT(*) | Backend → BD | 20 |
| 04 | BD Query JOIN | JOIN productos-proveedores | Backend → BD | 20 |
| 05 | BD Query Compleja | Agregaciones con GROUP BY | Backend → BD | 20 |
| 06 | Backend API Productos | GET /api/productos | Backend → BD | 20 |
| 07 | Backend API Producto ID | GET /api/productos/{id} | Backend → BD | 20 |
| 08 | ML Health Directo | Health check del ML Service | ML | 20 |
| 09 | Backend → ML Health | Health via proxy | Backend → ML | 20 |
| 10 | Backend → ML Predict | Predicción completa | Backend → ML | 20 |

### Monitoreo de Recursos

Durante la ejecución de las pruebas, se captura automáticamente:

#### Métricas por Contenedor (cada 2 segundos)
- **CPU**: % de uso promedio, mínimo, máximo y percentiles (P95, P99)
- **RAM**: MB usado, promedio, pico
- **Disco I/O**: MB leídos y escritos (delta acumulado)
- **Red I/O**: MB enviados y recibidos (delta acumulado)

#### Análisis de Procesos (cada 10 segundos)
- Top 5 procesos por **CPU** dentro de cada contenedor
- Top 5 procesos por **Memoria** dentro de cada contenedor
- Comando completo y usuario ejecutor

## 📁 Estructura de Archivos

```
pruebas-rendimiento/
├── README.md                          # Este archivo
├── RESUMEN_SESION.md                  # Resumen completo de implementación
├── METRICAS_REFERENCIA.md             # Baseline y valores de referencia
├── scripts/
│   ├── benchmark.sh                   # Script principal de pruebas
│   ├── monitor_recursos.sh            # Monitor de recursos (background)
│   ├── consolidar_resultados.sh       # Generador de reportes de rendimiento
│   ├── consolidar_recursos.sh         # Generador de reportes de recursos
│   ├── funciones_estadisticas.sh      # Funciones auxiliares de estadística
│   ├── funciones_recursos.sh          # Funciones auxiliares de recursos
│   └── config.env                     # Configuración centralizada
├── resultados/
│   ├── 01_frontend_carga_<timestamp>.csv          # Latencias prueba 1
│   ├── 02_login_<timestamp>.csv                   # Latencias prueba 2
│   ├── ... (más CSVs de pruebas)
│   ├── recursos_raw_<timestamp>.csv               # Recursos capturados (raw)
│   ├── procesos_raw_<timestamp>.csv               # Procesos capturados (raw)
│   ├── RESUMEN_LATENCIAS_POR_MODULO_<timestamp>.csv  # Resumen de latencias
│   ├── RESUMEN_RECURSOS_<timestamp>.csv           # Resumen de recursos
│   ├── TOP_PROCESOS_<timestamp>.csv               # Top procesos por contenedor
│   ├── GLOSARIO_METRICAS_<timestamp>.csv          # Definiciones de métricas
│   ├── REPORTE_EJECUTIVO_<timestamp>.txt          # Reporte de rendimiento
│   └── REPORTE_RECURSOS_<timestamp>.txt           # Reporte de recursos
├── logs/
│   └── ejecucion_<timestamp>.log
└── docs/
    ├── METODOLOGIA.md                 # Explicación técnica
    └── INTERPRETACION_RESULTADOS.md  # Guía de análisis
```

## 📈 Formato de Resultados

### CSV Individual de Rendimiento

Cada prueba genera un CSV con este formato:

```csv
"Iteración","Tiempo Total (s)","Tiempo Conexión (s)","TTFB (s)","Código HTTP","Tamaño Descarga (bytes)","Timestamp"
1,0.0042,0.0001,0.0041,200,1115,"2026-02-03 15:23:01"
2,0.0038,0.0001,0.0037,200,1115,"2026-02-03 15:23:02"
...
20,0.0045,0.0001,0.0044,200,1115,"2026-02-03 15:23:20"
"ESTADÍSTICAS","","","","",""
"Promedio",0.0042,...
"Mínimo",0.0038,...
"Máximo",0.0051,...
"Desv. Estándar",0.0003,...
"Percentil 95",0.0048,...
"Percentil 99",0.0050,...
```

### CSV de Recursos (Raw)

Captura de métricas cada 2 segundos:

```csv
"Timestamp","Timestamp_Unix","Contenedor","CPU_Porc","RAM_MB","RAM_Limite_MB","RAM_Porc","Disco_Read_MB","Disco_Write_MB","Net_In_MB","Net_Out_MB"
"2026-02-06 00:06:06",1770336366,"pos_backend",1.62,496.00,8095.74,6.14,1259.52,2232.32,50.40,52.20
"2026-02-06 00:06:08",1770336368,"pos_backend",2.15,496.50,8095.74,6.14,1270.00,2232.32,50.80,52.60
...
```

### CSV Resumen de Recursos

Estadísticas consolidadas por contenedor:

```csv
"Contenedor","Muestras","CPU_Min_%","CPU_Max_%","CPU_Prom_%","CPU_P95_%","RAM_Min_MB","RAM_Max_MB","RAM_Prom_MB","RAM_P95_MB","Disco_Read_Delta_MB","Disco_Write_Delta_MB","Net_In_Delta_MB","Net_Out_Delta_MB","Estado"
"pos_backend",14,0.48,9.10,1.94,8.50,496.00,497.80,496.61,497.50,20.48,0.00,0.60,0.60,"OK"
"pos_database",14,0.00,24.14,7.86,19.96,54.90,56.36,55.85,56.20,3.00,0.00,0.30,0.40,"OK"
...
```

### CSV Consolidado de Latencias

El resumen contiene todas las pruebas en un solo archivo:

```csv
"Comunicación","Promedio (s)","Mínimo (s)","Máximo (s)","Desv. Estándar","P95 (s)","P99 (s)","Iteraciones","Descripción"
"frontend carga","0.0042","0.0038","0.0051","0.0003","0.0048","0.0050","20","Prueba de rendimiento"
...
```

## 🔍 Interpretación de Métricas

### Métricas de Rendimiento (HTTP)

- **Tiempo Total**: Tiempo completo del request (envío + procesamiento + respuesta)
- **Tiempo Conexión**: Tiempo para establecer TCP (debe ser < 0.01s en localhost)
- **TTFB**: Time To First Byte, indica tiempo de procesamiento del servidor
- **Código HTTP**: 200 = OK, 401 = No autorizado, 500 = Error

### Métricas SQL

- **Planning Time**: Tiempo que tarda PostgreSQL en optimizar la query
- **Execution Time**: Tiempo real de ejecución de la consulta
- **Total Time**: Planning + Execution

### Métricas de Recursos

#### CPU
- **CPU_Porc**: Porcentaje de uso de CPU del contenedor
- **CPU_Prom**: Promedio durante todo el monitoreo
- **CPU_Max**: Pico máximo alcanzado
- **CPU_P95**: El 95% del tiempo estuvo por debajo de este valor

#### Memoria (RAM)
- **RAM_MB**: Megabytes de RAM usados
- **RAM_Porc**: Porcentaje del límite del contenedor
- **RAM_Prom**: Promedio durante el monitoreo
- **RAM_Max**: Pico máximo alcanzado

#### Disco I/O
- **Disco_Read_MB**: Megabytes leídos del disco (acumulado)
- **Disco_Write_MB**: Megabytes escritos al disco (acumulado)
- **Delta**: Incremento durante el monitoreo

#### Red I/O
- **Net_In_MB**: Megabytes recibidos por red (acumulado)
- **Net_Out_MB**: Megabytes enviados por red (acumulado)
- **Delta**: Incremento durante el monitoreo

### Estados de Recursos

- **OK**: Todos los recursos dentro de rangos normales
- **CPU_ALTO_PROMEDIO**: CPU promedio > 80%
- **CPU_CRITICO_PICO**: CPU pico > 95%
- **MEM_ALTO_PROMEDIO**: RAM promedio > 80%
- **MEM_CRITICO_PICO**: RAM pico > 90%

### Estadísticas

- **Promedio**: Valor típico esperado
- **Mínimo/Máximo**: Mejor y peor caso registrado
- **Mediana**: Valor del medio, menos sensible a outliers
- **Desv. Estándar**: Consistencia (menor = más predecible)
- **P95/P99**: El 95%/99% de requests están por debajo de este tiempo

## ⚠️ Limitaciones

1. **Entorno Local**: Las pruebas se ejecutan en localhost, los tiempos de red son mínimos
2. **Sin carga real**: No hay usuarios concurrentes ni carga de producción
3. **Datos limitados**: Solo 19 productos en la BD
4. **Recursos compartidos**: Todos los servicios en la misma máquina host

Los tiempos medidos son **indicativos** y pueden variar en producción debido a latencia de red, carga concurrente y volumen de datos.

## 🔧 Configuración Avanzada

### Modificar Número de Iteraciones

Editar `scripts/config.env`:

```bash
ITERATIONS=20           # Cambiar a 50, 100, etc.
WARMUP_ITERATIONS=3     # Iteraciones de calentamiento
```

### Cambiar Usuario de Prueba

Editar `scripts/config.env`:

```bash
TEST_USER="OtroUsuario"
TEST_PASSWORD="otraContraseña"
```

### Modificar URLs de Servicios

Editar `scripts/config.env`:

```bash
FRONTEND_URL="http://localhost:5173"
BACKEND_URL="http://localhost:8084"
ML_SERVICE_URL="http://localhost:8004"
```

## 🐛 Troubleshooting

### Error: "No se pudo obtener token JWT"

**Solución**: Verificar que las credenciales sean correctas y que el usuario exista en la BD.

```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Tona","contrasena":"123456"}'
```

### Error: "Servicio X NO está corriendo"

**Solución**: Levantar los servicios Docker:

```bash
cd /home/tona/dev/proyecto-pos-finanzas
docker-compose up -d
```

### Error: "awk no está instalado"

**Solución**: Instalar herramientas necesarias:

```bash
sudo apt-get update
sudo apt-get install gawk
```

### Resultados con tiempos negativos o cero

**Solución**: Puede indicar error en la ejecución del request. Revisar el log:

```bash
cat ../logs/ejecucion_*.log
```

## 📚 Documentación Adicional

- **METODOLOGIA.md**: Explicación técnica de cómo funcionan las pruebas
- **INTERPRETACION_RESULTADOS.md**: Guía detallada de análisis
- **GLOSARIO_METRICAS.csv**: Definiciones de todas las métricas

## 📞 Soporte

Para reportar problemas o sugerencias:
1. Revisar el log de ejecución en `logs/ejecucion_<timestamp>.log`
2. Verificar que todos los servicios estén corriendo
3. Contactar al equipo de desarrollo con el log completo

---

**Última actualización**: 03 de Febrero de 2026  
**Versión**: 1.0.0  
**Autor**: Equipo de Desarrollo POS Finanzas
