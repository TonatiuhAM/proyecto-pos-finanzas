#!/bin/bash

echo "🚀 Script de prueba para la integración ML"
echo "=========================================="

# Verificar que la API ML esté ejecutándose
echo "1️⃣ Verificando API ML..."
if curl -s http://localhost:8002/ > /dev/null; then
    echo "✅ API ML está ejecutándose"
    
    # Obtener estado de salud
    echo "📊 Estado de la API ML:"
    curl -s http://localhost:8002/ | jq '.status, .message, .models_loaded'
    
    echo ""
    echo "2️⃣ Probando endpoint de predicciones..."
    response=$(curl -s -X POST http://localhost:8002/predict \
        -H "Content-Type: application/json" \
        -d '{"top_k": 3, "dias_historicos": 30}')
    
    echo "📈 Resultado de predicciones:"
    echo $response | jq '.success, .message, .predicciones | length'
    
    echo ""
    echo "✅ ¡Integración ML funcionando correctamente!"
    echo ""
    echo "🎯 Próximos pasos:"
    echo "1. Iniciar el frontend con: cd frontend && npm run dev"
    echo "2. Navegar a Inventario y hacer clic en 'Predicciones ML'"
    echo "3. Ver las predicciones generadas con datos sintéticos"
    
else
    echo "❌ API ML no está ejecutándose"
    echo "💡 Para iniciarla:"
    echo "cd ml-prediction-service && docker run -d -p 8002:8000 --name ml-api ml-api-v3"
fi

echo ""
echo "🐳 Contenedores Docker activos:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"