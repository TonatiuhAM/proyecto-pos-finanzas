#!/bin/bash

# Script para construir el frontend con configuración de Cloudflare
# Autor: Sistema POS
# Fecha: $(date +%Y-%m-%d)

echo "🔄 Reconstruyendo frontend con configuración de Cloudflare..."

# Cambiar al directorio del proyecto
cd "$(dirname "$0")"/..

# Parar el contenedor del frontend actual
echo "⏹️ Deteniendo contenedor actual del frontend..."
docker stop pos_frontend 2>/dev/null || true
docker rm pos_frontend 2>/dev/null || true

# Remover la imagen anterior para forzar reconstrucción
echo "🗑️ Removiendo imagen anterior del frontend..."
docker rmi proyecto-pos-finanzas-frontend 2>/dev/null || true

# Copiar la configuración de Cloudflare como .env.production
echo "📄 Configurando variables de entorno para Cloudflare..."
cp frontend/.env.production.cloudflare frontend/.env.production

# Reconstruir solo el servicio frontend
echo "🔨 Reconstruyendo contenedor del frontend..."
docker compose build --no-cache frontend

# Levantar el servicio frontend
echo "🚀 Iniciando contenedor del frontend..."
docker compose up -d frontend

# Verificar que el contenedor esté ejecutándose
echo "✅ Verificando estado del frontend..."
docker compose ps frontend

echo ""
echo "🎉 ¡Frontend reconstruido exitosamente con configuración de Cloudflare!"
echo ""
echo "📋 Próximos pasos:"
echo "1. Configurar un tunnel de Cloudflare para el backend en: api.tonatiuham.dev -> 100.101.201.102:8084"
echo "2. Verificar que https://pos.tonatiuham.dev se conecte correctamente al backend"
echo "3. Probar el login en la aplicación web"
echo ""
echo "🔗 URLs:"
echo "   - Frontend (local): http://100.101.201.102:5173"
echo "   - Frontend (Cloudflare): https://pos.tonatiuham.dev"
echo "   - Backend (local): http://100.101.201.102:8084"
echo "   - Backend (requerido): https://api.tonatiuham.dev"