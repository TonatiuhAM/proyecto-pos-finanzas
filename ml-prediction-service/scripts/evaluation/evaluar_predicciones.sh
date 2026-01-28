#!/bin/bash
###############################################################################
# Script para ejecutar la evaluación de predicciones ML vs compras reales
# 
# Uso: ./evaluar_predicciones.sh
# 
# Este script:
# 1. Verifica que el contenedor ML esté corriendo
# 2. Instala dependencias necesarias (matplotlib, seaborn)
# 3. Ejecuta el script de evaluación Python dentro del contenedor
# 4. Muestra el resultado
###############################################################################

set -e  # Salir si algún comando falla

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}🚀 EVALUACIÓN DE PREDICCIONES ML VS COMPRAS REALES${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo ""

# 1. Verificar que el contenedor esté corriendo
echo -e "${YELLOW}📦 Verificando estado del contenedor ML...${NC}"

if ! docker ps --format '{{.Names}}' | grep -q "pos_ml_prediction_api"; then
    echo -e "${RED}❌ Error: El contenedor 'pos_ml_prediction_api' no está corriendo${NC}"
    echo -e "${YELLOW}   Ejecuta: docker-compose up -d${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Contenedor ML está corriendo${NC}"
echo ""

# 2. Verificar que el contenedor de BD esté corriendo
echo -e "${YELLOW}📦 Verificando estado del contenedor de PostgreSQL...${NC}"

if ! docker ps --format '{{.Names}}' | grep -q "pos_database"; then
    echo -e "${RED}❌ Error: El contenedor 'pos_database' no está corriendo${NC}"
    echo -e "${YELLOW}   Ejecuta: docker-compose up -d${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Contenedor PostgreSQL está corriendo${NC}"
echo ""

# 3. Instalar dependencias de visualización (si no están ya instaladas)
echo -e "${YELLOW}📚 Verificando/instalando dependencias de visualización...${NC}"

docker exec pos_ml_prediction_api bash -c "
    pip install --quiet matplotlib seaborn 2>&1 | grep -v 'Requirement already satisfied' || true
" || echo -e "${YELLOW}   (Dependencias ya instaladas o error menor)${NC}"

echo -e "${GREEN}✅ Dependencias listas${NC}"
echo ""

# 4. Dar permisos de ejecución al script Python
echo -e "${YELLOW}🔧 Configurando permisos...${NC}"

docker exec pos_ml_prediction_api chmod +x /home/app/scripts/evaluation/evaluar_predicciones_vs_compras_reales.py 2>/dev/null || true

echo ""

# 5. Ejecutar el script de evaluación
echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}🎯 Ejecutando evaluación...${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
echo ""

docker exec pos_ml_prediction_api python3 /home/app/scripts/evaluation/evaluar_predicciones_vs_compras_reales.py

EVAL_EXIT_CODE=$?

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"

if [ $EVAL_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ EVALUACIÓN COMPLETADA EXITOSAMENTE${NC}"
    echo ""
    echo -e "${GREEN}📂 Los reportes se han generado en:${NC}"
    echo -e "   ${YELLOW}./reportes-predicciones/evaluacion_YYYYMMDD_HHMMSS/${NC}"
    echo ""
    echo -e "${GREEN}📄 Archivos generados:${NC}"
    echo -e "   • ${BLUE}reporte_completo.html${NC} - Reporte visual completo"
    echo -e "   • ${BLUE}metricas_detalladas.json${NC} - Datos en formato JSON"
    echo -e "   • ${BLUE}graficos/${NC} - Visualizaciones PNG"
    echo ""
    echo -e "${YELLOW}💡 Para ver el reporte HTML:${NC}"
    echo -e "   Abre el archivo reporte_completo.html en tu navegador"
else
    echo -e "${RED}❌ ERROR DURANTE LA EVALUACIÓN${NC}"
    echo -e "   Revisa los logs arriba para más detalles"
    echo ""
    echo -e "${YELLOW}💡 Posibles causas:${NC}"
    echo -e "   • No hay datos históricos en la BD"
    echo -e "   • Error de conexión a PostgreSQL"
    echo -e "   • Archivos de configuración faltantes"
fi

echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"

exit $EVAL_EXIT_CODE
