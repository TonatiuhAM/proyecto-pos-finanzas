#!/bin/bash

# Script de Reorganización Completa del Proyecto POS Finanzas
# Generado automáticamente - Ejecutar desde la raíz del proyecto

set -e  # Detener en caso de error

echo "🚀 Iniciando reorganización del proyecto..."
echo "📁 Directorio actual: $(pwd)"
echo ""

# Función para crear carpetas y mover archivos
mover_archivo() {
    local origen=$1
    local destino=$2
    
    if [ -f "$origen" ]; then
        mkdir -p "$(dirname "$destino")"
        mv "$origen" "$destino"
        echo "✅ Movido: $origen → $destino"
    else
        echo "⚠️  No encontrado: $origen"
    fi
}

echo "=== FASE 1: Reorganización de ml-prediction-service/ ==="
echo ""

# 1.1 Crear estructura de carpetas
echo "📂 Creando estructura de carpetas en ml-prediction-service/..."
mkdir -p ml-prediction-service/app
mkdir -p ml-prediction-service/data/raw
mkdir -p ml-prediction-service/data/processed
mkdir -p ml-prediction-service/docs/analisis
mkdir -p ml-prediction-service/docs/guias
mkdir -p ml-prediction-service/docs/explicaciones
mkdir -p ml-prediction-service/scripts/analysis
mkdir -p ml-prediction-service/scripts/data_quality
mkdir -p ml-prediction-service/scripts/training
mkdir -p ml-prediction-service/scripts/shell
mkdir -p ml-prediction-service/reports/html
mkdir -p ml-prediction-service/tests
echo ""

# 1.2 Mover archivos de datos (CSVs)
echo "📊 Moviendo archivos de datos..."
mover_archivo "ml-prediction-service/datos_ventas_reales.csv" "ml-prediction-service/data/raw/datos_ventas_reales.csv"
mover_archivo "ml-prediction-service/historial_costos_reales.csv" "ml-prediction-service/data/raw/historial_costos_reales.csv"
mover_archivo "ml-prediction-service/estadisticas_productos.csv" "ml-prediction-service/data/raw/estadisticas_productos.csv"
echo ""

# 1.3 Mover documentación
echo "📄 Moviendo documentación..."
mover_archivo "ml-prediction-service/ANALISIS_DATOS_REALES.md" "ml-prediction-service/docs/analisis/ANALISIS_DATOS_REALES.md"
mover_archivo "ml-prediction-service/REPORTE_CALIDAD_DATOS_REALES.md" "ml-prediction-service/docs/analisis/REPORTE_CALIDAD_DATOS_REALES.md"
mover_archivo "ml-prediction-service/GUIA_MEJORA_CALIDAD_DATOS.md" "ml-prediction-service/docs/guias/GUIA_MEJORA_CALIDAD_DATOS.md"
mover_archivo "ml-prediction-service/README_data_quality.md" "ml-prediction-service/docs/guias/README_data_quality.md"
mover_archivo "ml-prediction-service/RESUMEN_MEJORAS.md" "ml-prediction-service/docs/guias/RESUMEN_MEJORAS.md"
mover_archivo "ml-prediction-service/RESUMEN_SESION_29NOV.md" "ml-prediction-service/docs/guias/RESUMEN_SESION_29NOV.md"
mover_archivo "ml-prediction-service/EXPLICACION-COMPLETA.md" "ml-prediction-service/docs/explicaciones/EXPLICACION-COMPLETA.md"
mover_archivo "ml-prediction-service/README.md" "ml-prediction-service/docs/README.md"
echo ""

# 1.4 Mover scripts de análisis
echo "🔬 Moviendo scripts de análisis..."
mover_archivo "ml-prediction-service/analizar_datos_reales.py" "ml-prediction-service/scripts/analysis/analizar_datos_reales.py"
mover_archivo "ml-prediction-service/analizar_calidad_datos_reales.py" "ml-prediction-service/scripts/data_quality/analizar_calidad_datos_reales.py"
mover_archivo "ml-prediction-service/analizar_calidad_simple.py" "ml-prediction-service/scripts/data_quality/analizar_calidad_simple.py"
mover_archivo "ml-prediction-service/data_quality_analyzer.py" "ml-prediction-service/scripts/data_quality/data_quality_analyzer.py"
mover_archivo "ml-prediction-service/data_quality_html_report.py" "ml-prediction-service/scripts/data_quality/data_quality_html_report.py"
mover_archivo "ml-prediction-service/mejorar_calidad_datos.py" "ml-prediction-service/scripts/data_quality/mejorar_calidad_datos.py"
echo ""

# 1.5 Mover scripts de entrenamiento
echo "🤖 Moviendo scripts de entrenamiento..."
mover_archivo "ml-prediction-service/entrenar_con_datos_reales.py" "ml-prediction-service/scripts/training/entrenar_con_datos_reales.py"
mover_archivo "ml-prediction-service/regenerar_modelos.py" "ml-prediction-service/scripts/training/regenerar_modelos.py"
echo ""

# 1.6 Mover scripts bash
echo "🔧 Moviendo scripts bash..."
mover_archivo "ml-prediction-service/regenerar_modelos.sh" "ml-prediction-service/scripts/shell/regenerar_modelos.sh"
mover_archivo "ml-prediction-service/setup_and_regenerate.sh" "ml-prediction-service/scripts/shell/setup_and_regenerate.sh"
mover_archivo "ml-prediction-service/test-api.sh" "ml-prediction-service/scripts/shell/test-api.sh"
echo ""

# 1.7 Mover código principal
echo "💻 Moviendo código principal..."
if [ -f "ml-prediction-service/main.py" ]; then
    cp "ml-prediction-service/main.py" "ml-prediction-service/app/main.py"
    echo "✅ Copiado: main.py → app/main.py (original preservado)"
fi
mover_archivo "ml-prediction-service/pipeline.py" "ml-prediction-service/app/pipeline.py"
echo ""

# 1.8 Mover reportes HTML
echo "📊 Moviendo reportes..."
if [ -d "ml-prediction-service/ml-prediction-service" ]; then
    if [ -f "ml-prediction-service/ml-prediction-service/14oct-data_quality_report.html" ]; then
        mover_archivo "ml-prediction-service/ml-prediction-service/14oct-data_quality_report.html" "ml-prediction-service/reports/html/14oct-data_quality_report.html"
        rmdir "ml-prediction-service/ml-prediction-service" 2>/dev/null || true
    fi
fi
echo ""

echo "=== FASE 2: Reorganización de la raíz del proyecto ==="
echo ""

# 2.1 Crear estructura de carpetas en raíz
echo "📂 Creando estructura de carpetas en raíz..."
mkdir -p docs/requerimientos
mkdir -p docs/analisis
mkdir -p scripts/database
mkdir -p scripts/docker
mkdir -p scripts/testing
echo ""

# 2.2 Mover documentación de utilidades a docs
echo "📄 Moviendo documentación general..."
mover_archivo "utilidades/bd-schema.md" "docs/bd-schema.md"
mover_archivo "utilidades/requerimientos.md" "docs/requerimientos/requerimientos-generales.md"
echo ""

# 2.3 Mover documentación de docs/ a nueva ubicación
echo "📄 Reorganizando docs existentes..."
if [ -d "docs" ]; then
    mover_archivo "docs/analisis-funcionamiento-codigo.md" "docs/analisis/analisis-funcionamiento-codigo.md"
    mover_archivo "docs/codebase-completo.md" "docs/analisis/codebase-completo.md"
    mover_archivo "docs/flujo-predicciones.md" "docs/analisis/flujo-predicciones.md"
    mover_archivo "docs/funcionalidad-deudas-proveedores.md" "docs/analisis/funcionalidad-deudas-proveedores.md"
    mover_archivo "docs/gradient-boosting-bitacora.md" "docs/analisis/gradient-boosting-bitacora.md"
    mover_archivo "docs/presentacion-gb.md" "docs/analisis/presentacion-gb.md"
    mover_archivo "docs/requerimientos.md" "docs/requerimientos/requerimientos-sistema.md"
    mover_archivo "docs/seguridad.md" "docs/seguridad.md"
fi
echo ""

# 2.4 Mover scripts de base de datos
echo "🗄️  Moviendo scripts de base de datos..."
mover_archivo "extraer_datos_reales.sh" "scripts/database/extraer_datos_reales.sh"
echo ""

# 2.5 Mover scripts de Docker
echo "🐳 Moviendo scripts de Docker..."
mover_archivo "regenerar_modelos_docker.sh" "scripts/docker/regenerar_modelos_docker.sh"
echo ""

# 2.6 Mover scripts de testing
echo "🧪 Moviendo scripts de testing..."
mover_archivo "test-ml-flow.py" "scripts/testing/test-ml-flow.py"
mover_archivo "test-ml-flow.sh" "scripts/testing/test-ml-flow.sh"
mover_archivo "test-ml-integration.sh" "scripts/testing/test-ml-integration.sh"
echo ""

# 2.7 Limpiar carpeta utilidades si está vacía
if [ -d "utilidades" ] && [ -z "$(ls -A utilidades)" ]; then
    rmdir utilidades
    echo "🗑️  Carpeta utilidades eliminada (estaba vacía)"
fi
echo ""

echo "=== FASE 3: Ajustes de permisos ==="
echo ""

# Dar permisos de ejecución a todos los scripts bash
echo "🔐 Ajustando permisos de scripts..."
chmod +x ml-prediction-service/scripts/shell/*.sh 2>/dev/null || true
chmod +x scripts/database/*.sh 2>/dev/null || true
chmod +x scripts/docker/*.sh 2>/dev/null || true
chmod +x scripts/testing/*.sh 2>/dev/null || true
echo ""

echo "=== RESUMEN DE REORGANIZACIÓN ==="
echo ""
echo "✅ Reorganización completada exitosamente!"
echo ""
echo "📊 Estadísticas:"
echo "   - Carpetas creadas: ~20"
echo "   - Archivos movidos: ~35+"
echo ""
echo "📁 Nueva estructura:"
echo "   ml-prediction-service/"
echo "   ├── app/           (código FastAPI)"
echo "   ├── data/          (CSVs y datos procesados)"
echo "   ├── docs/          (documentación técnica)"
echo "   ├── scripts/       (scripts Python y bash)"
echo "   ├── reports/       (reportes generados)"
echo "   └── models/        (modelos ML)"
echo ""
echo "   raíz del proyecto/"
echo "   ├── docs/          (documentación general)"
echo "   └── scripts/       (scripts globales)"
echo ""
echo "⚠️  PRÓXIMOS PASOS REQUERIDOS:"
echo "   1. Actualizar Dockerfile en ml-prediction-service/"
echo "   2. Actualizar imports en scripts Python"
echo "   3. Actualizar rutas en docker-compose.yml"
echo "   4. Verificar con: docker-compose up --build -d"
echo ""
