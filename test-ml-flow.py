#!/usr/bin/env python3
"""
Script para probar el flujo completo ML:
1. Simula datos históricos como los que envía el backend
2. Los envía a la ML API
3. Verifica que la respuesta tenga IDs reales de productos
"""

import requests
import json
from datetime import datetime, timedelta

def test_ml_flow():
    print("🧪 Probando flujo completo ML...")
    
    # 1. Simular datos históricos como los del backend
    datos_historicos = []
    productos_reales = ["prod-001-cafe", "prod-002-azucar", "prod-003-leche", "prod-004-pan", "prod-005-agua"]
    
    # Generar datos históricos para los últimos 30 días
    for i in range(30):
        fecha = (datetime.now() - timedelta(days=i)).strftime("%Y-%m-%d")
        for producto_id in productos_reales:
            # Simular ventas aleatorias
            import random
            cantidad = random.randint(1, 20)
            precio = round(random.uniform(10.0, 100.0), 2)
            
            datos_historicos.append({
                "fecha_orden": fecha,
                "productos_id": producto_id,
                "cantidad_pz": cantidad,
                "precio_venta": precio,
                "costo_compra": round(precio * 0.7, 2)  # 70% del precio de venta
            })
    
    print(f"📊 Generados {len(datos_historicos)} registros históricos")
    print(f"🏷️ Productos en datos: {productos_reales}")
    
    # 2. Enviar a ML API
    payload = {
        "ventas_historicas": datos_historicos,
        "productos_objetivo": productos_reales[:3]  # Pedir predicción para primeros 3 productos
    }
    
    try:
        print("📤 Enviando datos a ML API...")
        response = requests.post("http://localhost:8002/predict", json=payload, timeout=30)
        
        if response.status_code == 200:
            resultado = response.json()
            print("✅ Respuesta exitosa de ML API")
            print(f"📈 Predicciones recibidas: {len(resultado['predicciones'])}")
            
            # 3. Verificar que los IDs de productos son reales
            print("\n🔍 Verificando productos en respuesta:")
            for i, pred in enumerate(resultado['predicciones']):
                producto_id = pred['productos_id']
                cantidad = pred['cantidad_recomendada']
                prioridad = pred['prioridad_score']
                
                print(f"  {i+1}. Producto: {producto_id}")
                print(f"     Cantidad: {cantidad:.2f}")
                print(f"     Prioridad: {prioridad:.2f}")
                
                # Verificar si es un ID real o genérico
                if producto_id in productos_reales or not producto_id.startswith("producto_"):
                    print(f"     ✅ ID válido")
                else:
                    print(f"     ❌ ID genérico - problema identificado")
                print()
            
            return True
            
        else:
            print(f"❌ Error en ML API: {response.status_code}")
            print(f"📝 Respuesta: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error conectando con ML API: {e}")
        return False

if __name__ == "__main__":
    success = test_ml_flow()
    print(f"\n🎯 Test {'EXITOSO' if success else 'FALLIDO'}")