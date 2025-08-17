#!/bin/bash

# Script para probar las APIs del sistema de gestión de personas

echo "🔧 Probando Sistema Unificado de Gestión de Personas"
echo "=================================================="

# Test 1: Obtener categorías de personas
echo "📋 Test 1: Obtener categorías de personas"
echo "GET /api/categorias-personas"
curl -s -H "Content-Type: application/json" \
     -H "Origin: http://localhost:5173" \
     "http://localhost:8080/api/categorias-personas" | jq . || echo "Error o respuesta vacía"
echo ""

# Test 2: Obtener personas por categoría (ejemplo: categoría 1 - empleados)
echo "👥 Test 2: Obtener empleados (categoría 1)"
echo "GET /api/personas/categoria/1"
curl -s -H "Content-Type: application/json" \
     -H "Origin: http://localhost:5173" \
     "http://localhost:8080/api/personas/categoria/1" | jq . || echo "Error o respuesta vacía"
echo ""

# Test 3: Verificar endpoint de health
echo "💚 Test 3: Health check"
echo "GET /actuator/health"
curl -s "http://localhost:8080/actuator/health" | jq . || echo "Error o respuesta vacía"
echo ""

echo "✅ Pruebas completadas"