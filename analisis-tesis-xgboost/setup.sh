#!/bin/bash
# Script de instalación para Análisis de Abastecimiento con XGBoost
# ==================================================================

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  Instalación - Análisis de Abastecimiento con XGBoost         ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Verificar Python 3
if ! command -v python3 &> /dev/null; then
    echo "✗ Error: Python 3 no está instalado"
    exit 1
fi

echo "✓ Python 3 encontrado: $(python3 --version)"
echo ""

# Crear entorno virtual
echo "📦 Creando entorno virtual..."
python3 -m venv venv

if [ ! -d "venv" ]; then
    echo "✗ Error: No se pudo crear el entorno virtual"
    exit 1
fi

echo "✓ Entorno virtual creado"
echo ""

# Activar entorno virtual
echo "🔧 Activando entorno virtual..."
source venv/bin/activate

# Actualizar pip
echo "📦 Actualizando pip..."
pip install --upgrade pip

# Instalar dependencias
echo "📦 Instalando dependencias..."
pip install -r requirements.txt

if [ $? -ne 0 ]; then
    echo "✗ Error: Falló la instalación de dependencias"
    exit 1
fi

echo "✓ Dependencias instaladas exitosamente"
echo ""

# Crear archivos .gitkeep en carpetas
echo "📁 Creando estructura de carpetas..."
touch data/.gitkeep
touch models/.gitkeep
touch results/.gitkeep
touch notebooks/.gitkeep

echo "✓ Estructura de carpetas lista"
echo ""

# Verificar archivo .env
if [ ! -f ".env" ]; then
    echo "⚠ Advertencia: No existe archivo .env"
    echo "📝 Copia .env.example a .env y configura tus credenciales:"
    echo "   cp .env.example .env"
    echo "   nano .env"
    echo ""
fi

# Hacer ejecutable el script principal
chmod +x scripts/analisis_abastecimiento_xgboost.py

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  ✓ Instalación completada exitosamente                        ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Configurar credenciales: cp .env.example .env && nano .env"
echo "   2. Activar entorno: source venv/bin/activate"
echo "   3. Ejecutar análisis: python scripts/analisis_abastecimiento_xgboost.py"
echo ""
