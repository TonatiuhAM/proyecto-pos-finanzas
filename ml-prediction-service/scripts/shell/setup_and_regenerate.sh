#!/bin/bash

# Script para regenerar modelos XGBoost con Python 3.13
# Compatible con macOS ARM64 y Python moderno

set -e  # Salir si cualquier comando falla

echo "🐍 Regenerando modelos XGBoost con Python 3.13"
echo "==============================================="

# Detectar la versión de Python
PYTHON_VERSION=$(python3 --version 2>&1 | cut -d' ' -f2)
echo "📋 Versión de Python: $PYTHON_VERSION"

# Crear directorio del entorno virtual si no existe
VENV_DIR="venv_ml"
if [ ! -d "$VENV_DIR" ]; then
    echo "📦 Creando entorno virtual en $VENV_DIR..."
    python3 -m venv "$VENV_DIR"
fi

# Activar el entorno virtual
echo "🔧 Activando entorno virtual..."
source "$VENV_DIR/bin/activate"

# Actualizar pip y herramientas base
echo "⬆️  Actualizando pip..."
python -m pip install --upgrade pip

# Instalar dependencias una por una para mejor control de errores
echo "📦 Instalando dependencias esenciales..."

# Instalar numpy primero (base para todo)
echo "  📊 Instalando numpy..."
pip install "numpy>=1.25.0"

# Instalar pandas
echo "  📋 Instalando pandas..."
pip install "pandas>=2.1.0"

# Instalar scikit-learn
echo "  🤖 Instalando scikit-learn..."
pip install "scikit-learn>=1.3.0"

# Instalar XGBoost (versión más reciente compatible)
echo "  🚀 Instalando XGBoost..."
pip install "xgboost>=2.0.0"

# Instalar otras dependencias necesarias
echo "  🛠️ Instalando dependencias adicionales..."
pip install "python-dateutil>=2.8.2"

# Verificar que XGBoost se instaló correctamente
echo "✅ Verificando instalación..."
python3 -c "
import sys
print(f'Python: {sys.version}')
import numpy as np
print(f'NumPy: {np.__version__}')
import pandas as pd
print(f'Pandas: {pd.__version__}')
import sklearn
print(f'Scikit-learn: {sklearn.__version__}')
import xgboost as xgb
print(f'XGBoost: {xgb.__version__}')
print('✅ Todas las dependencias instaladas correctamente')
"

# Ejecutar regeneración de modelos
echo ""
echo "🧠 Ejecutando regeneración de modelos..."
echo "========================================"
python3 regenerar_modelos.py

# Verificar que los archivos se generaron correctamente
echo ""
echo "🔍 Verificando archivos generados..."
echo "===================================="

# Función para verificar archivo
check_file() {
    if [ -f "$1" ]; then
        echo "✅ $1 - Generado correctamente ($(du -h "$1" | cut -f1))"
    else
        echo "❌ $1 - No encontrado"
    fi
}

check_file "models/regressor_cantidad.json"
check_file "models/ranker_prioridad.json"
check_file "models/model_metadata.json"
check_file "models/model_features.txt"

echo ""
echo "🎉 Proceso completado! Los modelos están listos para usar."
echo "💡 Para aplicar los cambios:"
echo "   docker-compose down"
echo "   docker-compose up --build -d"
echo ""
echo "🔬 Para probar la API:"
echo "   bash test-api.sh"

# Desactivar entorno virtual
deactivate