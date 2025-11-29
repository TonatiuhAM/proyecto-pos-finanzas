#!/bin/bash

# Script para regenerar modelos XGBoost ejecutándose DENTRO del contenedor Docker
# Este script construye el contenedor, ejecuta la regeneración y luego reinicia el servicio

set -e

echo "🐳 Regenerando modelos XGBoost en contenedor Docker"
echo "=================================================="

# Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: No se encontró docker-compose.yml"
    echo "   Ejecuta este script desde la raíz del proyecto"
    exit 1
fi

# Verificar si el contenedor ML existe (usar nombre del servicio: ml-prediction)
ML_CONTAINER=$(docker-compose ps -q ml-prediction 2>/dev/null || echo "")

echo "🛠️ Paso 1: Construir contenedor actualizado..."
docker-compose build ml-prediction

echo "🚀 Paso 2: Ejecutar regeneración de modelos..."
if [ -n "$ML_CONTAINER" ]; then
    echo "   ⏹️ Deteniendo contenedor existente..."
    docker-compose stop ml-prediction
fi

echo "   🧠 Ejecutando regeneración en contenedor temporal..."
docker-compose run --rm ml-prediction python regenerar_modelos.py

echo "🔄 Paso 3: Reiniciar servicio ML..."
docker-compose up -d ml-prediction

echo "⏳ Esperando a que el servicio esté listo..."
sleep 5

echo "🔍 Paso 4: Verificar estado del servicio..."
if docker-compose ps ml-prediction | grep -q "Up"; then
    echo "✅ Servicio ML funcionando correctamente"
    
    # Probar la API (puerto 8002 según docker-compose.yml)
    echo "🧪 Probando API..."
    if curl -s http://localhost:8002/health >/dev/null 2>&1; then
        echo "✅ API responde correctamente"
    else
        echo "⚠️ API no responde aún (puede tardar unos segundos más)"
    fi
else
    echo "❌ Error: El servicio ML no está funcionando"
    echo "📋 Logs del contenedor:"
    docker-compose logs --tail=20 ml-prediction
fi

echo ""
echo "🎉 ¡Proceso completado!"
echo "💡 Los nuevos modelos XGBoost están activos en el contenedor"
echo "🔬 Para probar la API completa ejecuta: bash test-ml-integration.sh"

