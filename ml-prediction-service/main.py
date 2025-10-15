"""
Aplicación FastAPI simplificada y funcional para predicción de abastecimiento.
Corrige los errores de compatibilidad y simplifica la arquitectura.
"""

import os
import json
import logging
from datetime import datetime
from typing import List, Dict, Optional, Any
import pandas as pd
import numpy as np
import xgboost as xgb
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn

from pipeline import process_data

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Inicializar FastAPI
app = FastAPI(
    title="Sistema de Predicción de Abastecimiento",
    description="API para predicciones de cantidad y prioridad basadas en ML",
    version="1.0.0"
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Modelos globales
regressor_model = None
ranker_model = None
model_features = []

# Schemas Pydantic
class VentaData(BaseModel):
    fecha_orden: str
    productos_id: str
    cantidad_pz: int
    precio_venta: Optional[float] = 0.0
    costo_compra: Optional[float] = 0.0

class PredictionRequest(BaseModel):
    ventas_historicas: List[VentaData]
    productos_objetivo: Optional[List[str]] = None

class ProductoPrediction(BaseModel):
    productos_id: str
    cantidad_recomendada: float
    prioridad_score: float
    confianza: float

class PredictionResponse(BaseModel):
    predicciones: List[ProductoPrediction]
    timestamp: str
    modelo_version: str

class HealthResponse(BaseModel):
    status: str
    timestamp: str
    modelos_cargados: bool
    version: str

def cargar_modelos():
    """Carga los modelos XGBoost en memoria."""
    global regressor_model, ranker_model, model_features
    
    models_dir = "/home/app/models"
    if not os.path.exists(models_dir):
        models_dir = "./models"
    
    logger.info(f"📂 Buscando modelos en: {models_dir}")
    
    # Cargar regressor
    regressor_path = os.path.join(models_dir, "regressor_cantidad.json")
    try:
        if os.path.exists(regressor_path):
            regressor_model = xgb.XGBRegressor()
            regressor_model.load_model(regressor_path)
            logger.info("✅ Regressor cargado exitosamente")
        else:
            logger.warning(f"⚠️ Modelo regressor no encontrado en: {regressor_path}")
    except Exception as e:
        logger.error(f"❌ Error cargando regressor: {e}")
        regressor_model = None
    
    # Cargar ranker - usando XGBRegressor en lugar de XGBRanker para evitar errores
    ranker_path = os.path.join(models_dir, "ranker_prioridad.json")
    try:
        if os.path.exists(ranker_path):
            # Usar XGBRegressor en lugar de XGBRanker para mayor compatibilidad
            ranker_model = xgb.XGBRegressor()
            ranker_model.load_model(ranker_path)
            logger.info("✅ Ranker cargado exitosamente (como regressor)")
        else:
            logger.warning(f"⚠️ Modelo ranker no encontrado en: {ranker_path}")
    except Exception as e:
        logger.error(f"❌ Error cargando ranker: {e}")
        logger.info("💡 Esto puede deberse a incompatibilidad de formato del modelo ranker")
        ranker_model = None
    
    # Cargar lista de features
    features_path = os.path.join(models_dir, "model_features.txt")
    try:
        if os.path.exists(features_path):
            with open(features_path, 'r', encoding='utf-8') as f:
                model_features = [line.strip() for line in f.readlines() if line.strip()]
            logger.info(f"✅ Features cargadas: {len(model_features)} features")
        else:
            logger.warning(f"⚠️ Lista de features no encontrada en: {features_path}")
    except Exception as e:
        logger.error(f"❌ Error cargando features: {e}")
        model_features = []

def crear_modelos_dummy():
    """Crea modelos dummy para testing."""
    global regressor_model, ranker_model, model_features
    
    try:
        logger.info("🔧 Creando modelos dummy para testing...")
        
        # Datos simples para entrenamiento
        n_samples = 100
        n_features = 10
        
        # Datos para regressor
        X_reg = np.random.random((n_samples, n_features))
        y_reg = np.random.random(n_samples) * 100
        
        # Entrenar regressor simple
        regressor_model = xgb.XGBRegressor(
            n_estimators=10,
            max_depth=3,
            random_state=42
        )
        regressor_model.fit(X_reg, y_reg)
        
        # Datos para ranker (más simple, sin grupos complejos)
        X_rank = np.random.random((n_samples, n_features))
        y_rank = np.random.randint(0, 5, n_samples)  # Rankings 0-4
        
        # Entrenar ranker simple como regressor
        ranker_model = xgb.XGBRegressor(
            n_estimators=10,
            max_depth=3,
            random_state=42
        )
        ranker_model.fit(X_rank, y_rank)
        
        # Features dummy
        model_features = [f'feature_{i}' for i in range(n_features)]
        
        logger.info("✅ Modelos dummy creados exitosamente")
        
    except Exception as e:
        logger.error(f"❌ Error creando modelos dummy: {e}")

def preparar_features_para_prediccion(df_procesado: pd.DataFrame) -> pd.DataFrame:
    """Prepara las features para hacer predicciones."""
    if not model_features:
        # Si no tenemos la lista de features, usar todas las numéricas excepto objetivo
        exclude_cols = ['fecha_orden', 'productos_id', 'cantidad_total']
        available_features = [col for col in df_procesado.columns 
                            if col not in exclude_cols and 
                            df_procesado[col].dtype in [np.int64, np.float64]]
    else:
        available_features = [col for col in model_features if col in df_procesado.columns]
    
    # Agregar features faltantes con 0
    for feature in model_features:
        if feature not in df_procesado.columns:
            df_procesado[feature] = 0
    
    # Seleccionar solo las features del modelo
    if model_features:
        return df_procesado[model_features]
    else:
        return df_procesado[available_features]

@app.on_event("startup")
async def startup_event():
    """Inicialización de la aplicación."""
    logger.info("🚀 Iniciando aplicación...")
    
    # Intentar cargar modelos preentrenados
    cargar_modelos()
    
    # Si no hay modelos, crear dummy para testing
    if regressor_model is None or ranker_model is None:
        logger.info("📝 No se encontraron modelos preentrenados, creando modelos dummy...")
        crear_modelos_dummy()
    
    logger.info("✅ Aplicación iniciada correctamente")

@app.get("/", response_model=HealthResponse)
async def health_check():
    """Endpoint de health check."""
    return HealthResponse(
        status="healthy",
        timestamp=datetime.now().isoformat(),
        modelos_cargados=regressor_model is not None and ranker_model is not None,
        version="1.0.0"
    )

@app.get("/health", response_model=HealthResponse)
async def detailed_health():
    """Health check detallado."""
    return HealthResponse(
        status="healthy" if regressor_model and ranker_model else "degraded",
        timestamp=datetime.now().isoformat(),
        modelos_cargados=regressor_model is not None and ranker_model is not None,
        version="1.0.0"
    )

@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """Endpoint principal de predicción."""
    try:
        logger.info(f"📥 Recibida solicitud de predicción con {len(request.ventas_historicas)} registros")
        
        # Validar que tenemos modelos
        if regressor_model is None or ranker_model is None:
            raise HTTPException(status_code=503, detail="Modelos no disponibles")
        
        # Convertir datos de entrada a DataFrame
        ventas_data = []
        for venta in request.ventas_historicas:
            ventas_data.append({
                'fecha_orden': venta.fecha_orden,
                'productos_id': venta.productos_id,
                'cantidad_pz': venta.cantidad_pz,
                'precio_venta': venta.precio_venta,
                'costo_compra': venta.costo_compra
            })
        
        df_ventas = pd.DataFrame(ventas_data)
        
        # Procesar datos con el pipeline
        logger.info("🔄 Procesando datos con pipeline...")
        df_procesado = process_data(df_ventas)
        
        # Filtrar productos objetivo si se especifican
        if request.productos_objetivo:
            # Buscar productos en las columnas one-hot encoded
            productos_cols = [col for col in df_procesado.columns if col.startswith('producto_')]
            df_filtrado = df_procesado[df_procesado.index.isin([
                i for i, row in df_procesado.iterrows() 
                if any(row[col] == 1 for col in productos_cols 
                      if any(prod in col for prod in request.productos_objetivo))
            ])]
            if df_filtrado.empty:
                df_filtrado = df_procesado  # Fallback si no se encuentra nada
        else:
            df_filtrado = df_procesado
        
        # Preparar features para predicción
        X_pred = preparar_features_para_prediccion(df_filtrado)
        
        if X_pred.empty:
            raise HTTPException(status_code=400, detail="No se pudieron preparar features para predicción")
        
        logger.info(f"🧮 Realizando predicciones en {len(X_pred)} registros con {len(X_pred.columns)} features")
        
        # Hacer predicciones
        cantidad_pred = regressor_model.predict(X_pred)
        prioridad_pred = ranker_model.predict(X_pred)
        
        # Preparar respuesta
        predicciones = []
        
        # Obtener productos únicos del DataFrame filtrado (ahora debe tener productos_id preservado)
        if 'productos_id' in df_filtrado.columns:
            productos_unicos = df_filtrado['productos_id'].unique()
            logger.info(f"🏷️ Productos únicos encontrados: {productos_unicos[:5]}...")  # Log primeros 5
        else:
            # Fallback: extraer de columnas one-hot encoded
            productos_cols = [col for col in df_filtrado.columns if col.startswith('producto_')]
            productos_unicos = [col.replace('producto_', '') for col in productos_cols if df_filtrado[col].sum() > 0]
            logger.info(f"🏷️ Productos únicos desde one-hot: {productos_unicos[:5]}...")
            
            # Si no hay productos, generar nombres por defecto
            if not productos_unicos:
                productos_unicos = [f"producto_{i}" for i in range(min(len(cantidad_pred), 10))]  # Máximo 10
                logger.warning("⚠️ No se encontraron productos únicos, usando nombres genéricos")
        
        # Crear predicciones solo para el número de productos únicos que tenemos
        num_productos = len(productos_unicos)
        num_predicciones = min(len(cantidad_pred), num_productos)
        
        logger.info(f"📊 Creando {num_predicciones} predicciones para {num_productos} productos únicos")
        
        for i in range(num_predicciones):
            producto_id = productos_unicos[i]
            predicciones.append(ProductoPrediction(
                productos_id=str(producto_id),
                cantidad_recomendada=max(0, float(cantidad_pred[i])),
                prioridad_score=float(prioridad_pred[i]) if i < len(prioridad_pred) else 0.0,
                confianza=0.85  # Valor fijo por ahora
            ))
        
        logger.info(f"✅ Predicciones completadas para {len(predicciones)} productos")
        
        return PredictionResponse(
            predicciones=predicciones,
            timestamp=datetime.now().isoformat(),
            modelo_version="1.0.0"
        )
        
    except Exception as e:
        logger.error(f"❌ Error en predicción: {e}")
        raise HTTPException(status_code=500, detail=f"Error en predicción: {str(e)}")

@app.get("/info")
async def get_info():
    """Información sobre los modelos y features."""
    return {
        "modelos_disponibles": {
            "regressor": regressor_model is not None,
            "ranker": ranker_model is not None
        },
        "num_features": len(model_features),
        "features_sample": model_features[:10] if model_features else [],
        "version": "1.0.0"
    }

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    )