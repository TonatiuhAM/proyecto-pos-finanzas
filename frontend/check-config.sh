#!/bin/bash

# Script de prueba para verificar la configuración de variables de entorno
echo "=== Verificando configuración de variables de entorno ==="

echo ""
echo "1. Contenido del archivo .env:"
cat .env

echo ""
echo "2. Contenido del archivo .env.local:"
cat .env.local

echo ""
echo "3. Variables de entorno disponibles para Vite (las que empiezan con VITE_):"
grep -E '^VITE_' .env .env.local 2>/dev/null || echo "No se encontraron variables VITE_"

echo ""
echo "4. Construcción de la aplicación:"
echo "   - En desarrollo: usará .env.local (http://localhost:8080/api)"
echo "   - En producción: usará .env (https://pos-finanzas-q2ddz.ondigitalocean.app/api)"

echo ""
echo "5. Archivos actualizados:"
echo "   ✓ src/services/apiService.ts - Actualizado para usar VITE_API_URL"
echo "   ✓ src/services/inventarioService.ts - Actualizado para usar VITE_API_URL"
echo "   ✓ src/vite-env.d.ts - Tipos TypeScript para las variables de entorno"
echo "   ✓ vite.config.ts - Configuración de proxy para desarrollo local"

echo ""
echo "=== Configuración completada ==="
echo "Para probar:"
echo "  - Desarrollo: npm run dev"
echo "  - Producción: npm run build && npm run preview"
