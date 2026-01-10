#!/bin/bash

# Script de validación para migración a Homelab
# Ejecuta este script para verificar que todos los servicios estén funcionando correctamente

echo "🚀 Validando migración a Homelab..."
echo "=================================="

# Verificar que Docker esté ejecutándose
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker no está ejecutándose"
    exit 1
fi

echo "✅ Docker está activo"

# Verificar que los directorios necesarios existan
if [ ! -d "database/data" ] || [ ! -d "database/backups" ] || [ ! -d "database/init" ]; then
    echo "❌ Directorios de base de datos no encontrados"
    echo "Ejecutando: mkdir -p database/data database/backups database/init"
    mkdir -p database/data database/backups database/init
fi

echo "✅ Directorios de base de datos presentes"

# Verificar archivo .env
if [ ! -f ".env" ]; then
    echo "⚠️  Archivo .env no encontrado"
    echo "Copiando .env.homelab como ejemplo..."
    if [ -f ".env.homelab" ]; then
        cp .env.homelab .env
        echo "✅ Archivo .env creado desde .env.homelab"
    else
        echo "❌ No se encontró .env.homelab. Crea tu archivo .env manualmente."
        exit 1
    fi
fi

echo "✅ Archivo .env presente"

# Iniciar servicios
echo ""
echo "🔄 Iniciando servicios..."
docker-compose up -d

# Esperar un momento para que los contenedores inicien
echo "⏳ Esperando 30 segundos para que los servicios inicien..."
sleep 30

# Verificar estado de contenedores
echo ""
echo "📋 Estado de contenedores:"
docker-compose ps

# Verificar conectividad de servicios
echo ""
echo "🌐 Verificando conectividad de servicios:"

# Base de datos
if docker exec pos_database pg_isready -U postgres > /dev/null 2>&1; then
    echo "✅ Base de datos PostgreSQL: ACTIVA"
else
    echo "❌ Base de datos PostgreSQL: NO RESPONDE"
fi

# Backend
if curl -f http://localhost:8084/api/auth/test > /dev/null 2>&1; then
    echo "✅ Backend API: ACTIVO"
elif curl -f http://localhost:8084/ > /dev/null 2>&1; then
    echo "⚠️  Backend API: PARCIALMENTE ACTIVO (verifica endpoints)"
else
    echo "❌ Backend API: NO RESPONDE"
    echo "   Logs del backend:"
    docker-compose logs --tail=10 backend
fi

# Frontend
if curl -f http://localhost:5173/ > /dev/null 2>&1; then
    echo "✅ Frontend: ACTIVO"
else
    echo "❌ Frontend: NO RESPONDE"
    echo "   Logs del frontend:"
    docker-compose logs --tail=10 frontend
fi

# ML Service
if curl -f http://localhost:8004/ > /dev/null 2>&1; then
    echo "✅ ML Service: ACTIVO"
else
    echo "❌ ML Service: NO RESPONDE"
    echo "   Logs del ML Service:"
    docker-compose logs --tail=10 ml-prediction
fi

echo ""
echo "📊 Resumen de puertos:"
echo "   - Frontend: http://localhost:5173"
echo "   - Backend API: http://localhost:8084"
echo "   - ML Service: http://localhost:8004"
echo "   - Base de datos: localhost:5433 (desde host), interno:5432"

echo ""
echo "📝 Próximos pasos:"
echo "1. Si todos los servicios están activos, sigue la guía en database/GUIA_BACKUP.md"
echo "2. Restaura tu backup de DigitalOcean"
echo "3. Actualiza las configuraciones CORS con tu dominio personalizado"
echo "4. Configura SSL/TLS si planeas acceso público"

echo ""
echo "🎉 Validación completada!"