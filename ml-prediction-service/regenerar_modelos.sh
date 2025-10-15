#!/bin/bash
# Script para regenerar los modelos XGBoost

echo "🔧 Regenerando modelos XGBoost..."
echo "=================================="

# Verificar que estamos en el directorio correcto
if [ ! -f "requirements.txt" ]; then
    echo "❌ Error: Este script debe ejecutarse desde el directorio ml-prediction-service"
    exit 1
fi

# Verificar si Python está disponible
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python3 no está instalado"
    exit 1
fi

# Instalar dependencias si es necesario
echo "📦 Verificando dependencias..."
pip3 install -r requirements.txt

# Ejecutar script de regeneración
echo "🧠 Ejecutando regeneración de modelos..."
python3 regenerar_modelos.py

if [ $? -eq 0 ]; then
    echo "✅ ¡Modelos regenerados exitosamente!"
    echo ""
    echo "📋 Próximos pasos:"
    echo "1. Reinicia el contenedor Docker:"
    echo "   docker-compose down && docker-compose up --build"
    echo ""
    echo "2. Verifica que los modelos se carguen correctamente:"
    echo "   docker logs <nombre-contenedor>"
    echo ""
    echo "3. Prueba la API:"
    echo "   ./test-api.sh"
else
    echo "❌ Error durante la regeneración de modelos"
    exit 1
fi