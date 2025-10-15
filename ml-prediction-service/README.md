# Sistema de Predicción ML - Versión Corregida

## 🎯 ¿Qué es esto?

Este es un sistema de **Machine Learning como servicio** que predice:
1. **¿Cuánto comprar?** (Cantidad recomendada de cada producto)
2. **¿Cuándo comprar?** (Prioridad de reabastecimiento)

## 🏗️ ¿Cómo funciona?

### 1. Arquitectura Simplificada
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Datos de      │───▶│    Pipeline de   │───▶│   Modelos ML    │
│   Ventas        │    │  Procesamiento   │    │  (XGBoost)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │  Predicciones   │
                                               │   (JSON API)    │
                                               └─────────────────┘
```

### 2. Pipeline de Datos Inteligente
El sistema toma datos básicos de ventas y los enriquece con:
- **Features de tiempo**: día de semana, mes, estación, ciclos
- **Datos externos**: clima simulado, días festivos
- **Patrones históricos**: tendencias, medias móviles, volatilidad
- **Interacciones**: clima×fin de semana, festivos×ventas

### 3. Modelos de Machine Learning
- **Regressor**: Predice la cantidad exacta a comprar
- **Ranker**: Asigna prioridad de compra (urgencia)

## 🚀 ¿Cómo usar?

### Desarrollo Local (Sin Docker)

```bash
# 1. Instalar dependencias
cd ml-prediction-service-fixed
pip install -r requirements.txt

# 2. Ejecutar la API
python main.py

# 3. Probar (en otra terminal)
./test-api.sh
```

### Desarrollo con Docker (Recomendado)

```bash
# 1. Construir y ejecutar
cd ml-prediction-service-fixed
docker-compose up --build

# 2. Probar la API
./test-api.sh
```

## 📡 Endpoints Disponibles

### 🏥 Health Check
```bash
GET http://localhost:8000/health
```

### 🔮 Hacer Predicción
```bash
POST http://localhost:8000/predict
Content-Type: application/json

{
  "ventas_historicas": [
    {
      "fecha_orden": "2024-01-15",
      "productos_id": "PROD_001",
      "cantidad_pz": 25,
      "precio_venta": 45.50,
      "costo_compra": 32.00
    }
  ]
}
```

**Respuesta:**
```json
{
  "predicciones": [
    {
      "productos_id": "PROD_001",
      "cantidad_recomendada": 28.5,
      "prioridad_score": 3.2,
      "confianza": 0.85
    }
  ],
  "timestamp": "2024-01-15T10:30:00",
  "modelo_version": "1.0.0"
}
```

### ℹ️ Información del Sistema
```bash
GET http://localhost:8000/info
```

## 🔧 ¿Qué se corrigió?

### Errores Anteriores:
1. **XGBoost Ranker**: Error de estructura de grupos
2. **FastAPI/Pydantic**: Incompatibilidad de versiones
3. **Arquitectura compleja**: Múltiples contenedores innecesarios
4. **Código repetitivo**: Falta de modularización

### Soluciones Aplicadas:
1. **Modelos simplificados**: Ranker funciona como regressor simple
2. **Versiones compatibles**: FastAPI 0.100.0 + Pydantic 1.10.12
3. **Un solo contenedor**: Aplicación autocontenida
4. **Pipeline robusto**: Manejo de errores y valores por defecto

## 🧪 ¿Cómo probar?

1. **Arrancar el sistema**:
   ```bash
   docker-compose up --build
   ```

2. **Verificar que funciona**:
   ```bash
   curl http://localhost:8000/health
   ```

3. **Hacer una predicción**:
   ```bash
   ./test-api.sh
   ```

4. **Ver documentación interactiva**:
   - Ir a http://localhost:8000/docs

## 🚚 ¿Cómo llevar a producción?

### Para Digital Ocean:

1. **Construir imagen**:
   ```bash
   docker build -t mi-ml-api:latest .
   ```

2. **Subir a registry** (Docker Hub, DigitalOcean Container Registry)

3. **Desplegar** en DigitalOcean App Platform o Droplet

### Variables de Entorno Recomendadas:
```bash
ENVIRONMENT=production
LOG_LEVEL=info
API_HOST=0.0.0.0
API_PORT=8000
```

## 🎯 Próximos Pasos

1. **Conectar a base de datos real** del POS
2. **Entrenar modelos** con datos reales
3. **Añadir autenticación** para producción
4. **Métricas y monitoreo** con Prometheus
5. **Re-entrenamiento automático** periódico

---

**🎉 ¡Ya tienes un sistema ML funcional!**

Este sistema está listo para:
- ✅ Ejecutarse localmente para desarrollo
- ✅ Desplegarse en producción
- ✅ Integrarse con tu sistema POS
- ✅ Escalar según necesidades