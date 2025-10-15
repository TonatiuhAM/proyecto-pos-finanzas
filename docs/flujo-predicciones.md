# 🔄 Diagramas de Flujo: Proceso de Predicciones ML

## Diagrama General del Proceso

### **Flujo Principal (Vista Completa)**

```
[Inicio] → [Usuario hace clic] → [Frontend pide datos] → [Backend consulta BD] → [Frontend envía a ML] → [ML procesa] → [ML predice] → [Frontend formatea] → [Usuario ve resultados] → [Fin]
```
hola
---

## **PASO 1: Usuario Presiona "Predicciones ML"**

### Diagrama de Flujo:

```
┌─────────────────┐
│  INICIO         │ (Óvalo)
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Usuario en      │ (Rectángulo)
│ Frontend React  │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Clic botón      │ (Rectángulo)
│ "Predicciones"  │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Ejecuta función │ (Rectángulo)
│ loadPredictions │
└─────────────────┘
         │
         ▼
  (Conecta con Paso 2)
```

**Figuras necesarias:**
- **Óvalo**: "INICIO"
- **Rectángulo**: "Usuario en Frontend React"
- **Rectángulo**: "Clic botón Predicciones"
- **Rectángulo**: "Ejecuta función loadPredictions"

---

## **PASO 2: Frontend Pide Datos al Backend**

### Diagrama de Flujo:

```
(Viene del Paso 1)
         │
         ▼
┌─────────────────┐
│ Frontend envía  │ (Rectángulo)
│ GET /historial-ml│
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Backend recibe  │ (Rectángulo)
│ petición        │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Consulta BD     │ (Rectángulo)
│ últimos 90 días │
└─────────────────┘
         │
         ▼
◇─────────────────◇
│ ¿Hay ventas?    │ (Rombo)
└─────────────────┘
    │           │
   SÍ          NO
    │           │
    ▼           ▼
┌─────────┐  ┌─────────┐
│Convierte│  │ Envía   │ (Rectángulos)
│a formato│  │ lista   │
│   ML    │  │ vacía   │
└─────────┘  └─────────┘
    │           │
    └─────┬─────┘
          ▼
┌─────────────────┐
│ Envía JSON al   │ (Rectángulo)
│ Frontend        │
└─────────────────┘
         │
         ▼
  (Conecta con Paso 3)
```

**Figuras necesarias:**
- **Rectángulo**: "Frontend envía GET /historial-ml"
- **Rectángulo**: "Backend recibe petición"
- **Rectángulo**: "Consulta BD últimos 90 días"
- **Rombo**: "¿Hay ventas?"
- **Rectángulo**: "Convierte a formato ML"
- **Rectángulo**: "Envía lista vacía"
- **Rectángulo**: "Envía JSON al Frontend"

---

## **PASO 3: Frontend Envía Datos al Servicio ML**

### Diagrama de Flujo:

```
(Viene del Paso 2)
         │
         ▼
┌─────────────────┐
│ Frontend recibe │ (Rectángulo)
│ datos históricos│
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Prepara request │ (Rectángulo)
│ para ML API     │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ POST /predict   │ (Rectángulo)
│ puerto :8002    │
└─────────────────┘
         │
         ▼
◇─────────────────◇
│ ¿ML disponible? │ (Rombo)
└─────────────────┘
    │           │
   SÍ          NO
    │           │
    ▼           ▼
┌─────────┐  ┌─────────┐
│Continúa │  │ Error   │ (Rectángulos)
│proceso  │  │ conexión│
└─────────┘  └─────────┘
    │           │
    │           ▼
    │      ┌─────────┐
    │      │ FIN     │ (Óvalo)
    │      └─────────┘
    ▼
  (Conecta con Paso 4)
```

**Figuras necesarias:**
- **Rectángulo**: "Frontend recibe datos históricos"
- **Rectángulo**: "Prepara request para ML API"
- **Rectángulo**: "POST /predict puerto :8002"
- **Rombo**: "¿ML disponible?"
- **Rectángulo**: "Continúa proceso"
- **Rectángulo**: "Error conexión"
- **Óvalo**: "FIN"

---

## **PASO 4a: Enriquecimiento de Datos (Pipeline)**

### Diagrama de Flujo:

```
(Viene del Paso 3)
         │
         ▼
┌─────────────────┐
│ ML recibe datos │ (Rectángulo)
│ básicos         │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Agrega features │ (Proceso)
│ de tiempo       │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Agrega features │ (Proceso)
│ climáticas      │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Agrega features │ (Proceso)
│ de feriados     │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Agrega features │ (Proceso)
│ históricos (lag)│
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Calcula medias  │ (Proceso)
│ móviles         │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Crea features   │ (Proceso)
│ de interacción  │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Datos listos    │ (Rectángulo)
│ 60+ features    │
└─────────────────┘
         │
         ▼
  (Conecta con Paso 4b)
```

**Figuras necesarias:**
- **Rectángulo**: "ML recibe datos básicos"
- **Proceso (Hexágono)**: "Agrega features de tiempo"
- **Proceso (Hexágono)**: "Agrega features climáticas"
- **Proceso (Hexágono)**: "Agrega features de feriados"
- **Proceso (Hexágono)**: "Agrega features históricos (lag)"
- **Proceso (Hexágono)**: "Calcula medias móviles"
- **Proceso (Hexágono)**: "Crea features de interacción"
- **Rectángulo**: "Datos listos 60+ features"

---

## **PASO 4b: Predicción con Gradient Boosting**

### Diagrama de Flujo:

```
(Viene del Paso 4a)
         │
         ▼
┌─────────────────┐
│ Datos con 60+   │ (Rectángulo)
│ features listos │
└─────────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐
│ Modelo  │ │ Modelo  │ (Procesos)
│Regressor│ │ Ranker  │
│(cantidad│ │(prioridad│
└─────────┘ └─────────┘
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐
│Predice  │ │Predice  │ (Rectángulos)
│28.5 unid│ │score 3.2│
└─────────┘ └─────────┘
    │         │
    └────┬────┘
         ▼
┌─────────────────┐
│ Combina         │ (Proceso)
│ predicciones    │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Calcula         │ (Proceso)
│ confianza       │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ JSON respuesta  │ (Rectángulo)
│ generado        │
└─────────────────┘
         │
         ▼
  (Conecta con Paso 5)
```

**Figuras necesarias:**
- **Rectángulo**: "Datos con 60+ features listos"
- **Proceso (Hexágono)**: "Modelo Regressor (cantidad)"
- **Proceso (Hexágono)**: "Modelo Ranker (prioridad)"
- **Rectángulo**: "Predice 28.5 unid"
- **Rectángulo**: "Predice score 3.2"
- **Proceso (Hexágono)**: "Combina predicciones"
- **Proceso (Hexágono)**: "Calcula confianza"
- **Rectángulo**: "JSON respuesta generado"

---

## **PASO 5: ML Regresa Predicciones**

### Diagrama de Flujo:

```
(Viene del Paso 4b)
         │
         ▼
┌─────────────────┐
│ ML envía JSON   │ (Rectángulo)
│ al Frontend     │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Frontend recibe │ (Rectángulo)
│ predicciones    │
└─────────────────┘
         │
         ▼
◇─────────────────◇
│ ¿Datos válidos? │ (Rombo)
└─────────────────┘
    │           │
   SÍ          NO
    │           │
    ▼           ▼
┌─────────┐  ┌─────────┐
│Continúa │  │ Muestra │ (Rectángulos)
│proceso  │  │ error   │
└─────────┘  └─────────┘
    │           │
    │           ▼
    │      ┌─────────┐
    │      │ FIN     │ (Óvalo)
    │      └─────────┘
    ▼
  (Conecta con Paso 6)
```

**Figuras necesarias:**
- **Rectángulo**: "ML envía JSON al Frontend"
- **Rectángulo**: "Frontend recibe predicciones"
- **Rombo**: "¿Datos válidos?"
- **Rectángulo**: "Continúa proceso"
- **Rectángulo**: "Muestra error"
- **Óvalo**: "FIN"

---

## **PASO 6: Frontend Formatea y Usuario Ve Resultados**

### Diagrama de Flujo:

```
(Viene del Paso 5)
         │
         ▼
┌─────────────────┐
│ Obtiene info    │ (Proceso)
│ productos BD    │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Convierte score │ (Proceso)
│ a prioridad     │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Calcula días    │ (Proceso)
│ de stock        │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Genera          │ (Proceso)
│ recomendaciones │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Formatea para   │ (Rectángulo)
│ mostrar en UI   │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Usuario ve      │ (Rectángulo)
│ tabla resultados│
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ FIN PROCESO     │ (Óvalo)
└─────────────────┘
```

**Figuras necesarias:**
- **Proceso (Hexágono)**: "Obtiene info productos BD"
- **Proceso (Hexágono)**: "Convierte score a prioridad"
- **Proceso (Hexágono)**: "Calcula días de stock"
- **Proceso (Hexágono)**: "Genera recomendaciones"
- **Rectángulo**: "Formatea para mostrar en UI"
- **Rectángulo**: "Usuario ve tabla resultados"
- **Óvalo**: "FIN PROCESO"

---

## 📝 **Leyenda de Figuras:**

- **Óvalo**: Inicio/Fin del proceso
- **Rectángulo**: Acción o proceso simple
- **Hexágono**: Proceso de transformación/cálculo
- **Rombo**: Decisión (Sí/No)
- **Flecha**: Dirección del flujo

## 🎯 **Resumen del Flujo:**

1. **Clic** → Inicia proceso
2. **Datos** → Backend obtiene ventas
3. **Envío** → Frontend llama ML API
4. **Pipeline** → Enriquece datos con features
5. **ML** → Gradient Boosting predice
6. **Formato** → Convierte a interfaz amigable
7. **Resultado** → Usuario ve recomendaciones

**Tiempo total estimado**: 2-5 segundos
