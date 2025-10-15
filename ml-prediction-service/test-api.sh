#!/bin/bash
# Script para probar la API de ML

echo "🧪 Probando API de ML..."

# Verificar que la API esté corriendo
echo "📡 Verificando health check..."
curl -X GET "http://localhost:8000/health" | jq

echo -e "\n📊 Obteniendo información de la API..."
curl -X GET "http://localhost:8000/info" | jq

# Datos de prueba
echo -e "\n🔮 Enviando predicción de prueba..."
curl -X POST "http://localhost:8000/predict" \
  -H "Content-Type: application/json" \
  -d '{
    "ventas_historicas": [
      {
        "fecha_orden": "2024-01-01",
        "productos_id": "PROD_001",
        "cantidad_pz": 10,
        "precio_venta": 25.50,
        "costo_compra": 18.00
      },
      {
        "fecha_orden": "2024-01-02",
        "productos_id": "PROD_001",
        "cantidad_pz": 15,
        "precio_venta": 25.50,
        "costo_compra": 18.00
      },
      {
        "fecha_orden": "2024-01-03",
        "productos_id": "PROD_002",
        "cantidad_pz": 8,
        "precio_venta": 42.00,
        "costo_compra": 30.00
      }
    ]
  }' | jq

echo -e "\n✅ Pruebas completadas!"