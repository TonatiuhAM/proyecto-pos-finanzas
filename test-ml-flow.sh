#!/bin/bash
# Test del flujo ML usando curl

echo "🧪 Probando flujo completo ML con curl..."

# Crear datos de prueba en formato JSON
cat > /tmp/test_ml_data.json << 'EOF'
{
  "ventas_historicas": [
    {
      "fecha_orden": "2024-10-01",
      "productos_id": "prod-001-cafe",
      "cantidad_pz": 15,
      "precio_venta": 25.50,
      "costo_compra": 17.85
    },
    {
      "fecha_orden": "2024-10-01",
      "productos_id": "prod-002-azucar",
      "cantidad_pz": 8,
      "precio_venta": 12.00,
      "costo_compra": 8.40
    },
    {
      "fecha_orden": "2024-10-02",
      "productos_id": "prod-001-cafe",
      "cantidad_pz": 12,
      "precio_venta": 25.50,
      "costo_compra": 17.85
    },
    {
      "fecha_orden": "2024-10-02",
      "productos_id": "prod-003-leche",
      "cantidad_pz": 20,
      "precio_venta": 18.75,
      "costo_compra": 13.13
    },
    {
      "fecha_orden": "2024-10-03",
      "productos_id": "prod-001-cafe",
      "cantidad_pz": 10,
      "precio_venta": 25.50,
      "costo_compra": 17.85
    }
  ],
  "productos_objetivo": ["prod-001-cafe", "prod-002-azucar", "prod-003-leche"]
}
EOF

echo "📊 Datos de prueba creados:"
echo "🏷️ Productos: prod-001-cafe, prod-002-azucar, prod-003-leche"

echo "📤 Enviando datos a ML API..."

# Enviar datos a ML API
response=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d @/tmp/test_ml_data.json \
  http://localhost:8002/predict)

echo "📥 Respuesta recibida:"
echo "$response" | jq .

echo ""
echo "🔍 Verificando productos en respuesta:"
productos_en_respuesta=$(echo "$response" | jq -r '.predicciones[]? | .productos_id')

if [ -n "$productos_en_respuesta" ]; then
  echo "$productos_en_respuesta" | while IFS= read -r producto; do
    if [[ "$producto" == prod-* ]]; then
      echo "  ✅ Producto real encontrado: $producto"
    elif [[ "$producto" == producto_* ]]; then
      echo "  ❌ Producto genérico encontrado: $producto"
    else
      echo "  ⚠️ Producto desconocido: $producto"
    fi
  done
else
  echo "  ❌ No se encontraron productos en la respuesta"
fi

# Limpiar archivo temporal
rm -f /tmp/test_ml_data.json

echo ""
echo "🎯 Test completado"