#!/bin/bash

# Script para restaurar backup de PostgreSQL desde DigitalOcean
# Uso: ./restaurar-backup.sh <nombre_archivo_backup.pgsql>

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🗄️  Script de Restauración de Base de Datos${NC}"
echo "=============================================="

# Verificar que se proporcionó un archivo
if [ $# -eq 0 ]; then
    echo -e "${RED}❌ Error: Debes proporcionar el nombre del archivo de backup${NC}"
    echo -e "${YELLOW}Uso: ./restaurar-backup.sh <nombre_archivo_backup.pgsql>${NC}"
    echo ""
    echo "Archivos de backup disponibles:"
    ls -la database/backups/ 2>/dev/null || echo "No hay archivos en database/backups/"
    exit 1
fi

BACKUP_FILE="$1"
BACKUP_PATH="database/backups/$BACKUP_FILE"

# Verificar que el archivo existe
if [ ! -f "$BACKUP_PATH" ]; then
    echo -e "${RED}❌ Error: El archivo $BACKUP_PATH no existe${NC}"
    echo ""
    echo "Archivos de backup disponibles:"
    ls -la database/backups/ 2>/dev/null || echo "No hay archivos en database/backups/"
    exit 1
fi

# Verificar que el contenedor de base de datos esté corriendo
if ! docker compose ps database | grep -q "Up"; then
    echo -e "${YELLOW}⚠️  El contenedor de base de datos no está corriendo${NC}"
    echo -e "${YELLOW}🔄 Iniciando contenedor de base de datos...${NC}"
    docker compose up -d database
    
    # Esperar que la base de datos esté lista
    echo -e "${YELLOW}⏳ Esperando que PostgreSQL esté listo...${NC}"
    for i in {1..30}; do
        if docker exec pos_database pg_isready -U postgres >/dev/null 2>&1; then
            echo -e "${GREEN}✅ PostgreSQL está listo${NC}"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""
fi

# Verificar nuevamente que la base de datos esté lista
if ! docker exec pos_database pg_isready -U postgres >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: PostgreSQL no está respondiendo${NC}"
    echo "Revisa los logs con: docker compose logs database"
    exit 1
fi

echo -e "${GREEN}✅ Base de datos lista para restauración${NC}"
echo -e "${YELLOW}📥 Restaurando backup: $BACKUP_FILE${NC}"
echo ""

# Ejecutar la restauración
echo -e "${YELLOW}🔄 Ejecutando pg_restore...${NC}"
if docker exec -i pos_database pg_restore \
    --host=localhost \
    --port=5432 \
    --username=postgres \
    --dbname=pos_finanzas \
    --verbose \
    --clean \
    --if-exists \
    /backups/$BACKUP_FILE; then
    
    echo ""
    echo -e "${GREEN}🎉 ¡Backup restaurado exitosamente!${NC}"
    echo ""
    echo -e "${GREEN}📊 Verificando tablas restauradas:${NC}"
    docker exec pos_database psql -U postgres -d pos_finanzas -c "\dt"
    
else
    echo ""
    echo -e "${RED}❌ Error durante la restauración${NC}"
    echo "Revisa los logs arriba para más detalles"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ Proceso completado${NC}"
echo -e "${YELLOW}💡 Próximos pasos:${NC}"
echo "1. Verificar que los datos se restauraron correctamente"
echo "2. Iniciar todos los servicios: docker compose up -d"
echo "3. Probar el acceso al frontend: http://localhost:5173"