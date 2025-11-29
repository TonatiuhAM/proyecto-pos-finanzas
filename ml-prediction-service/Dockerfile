FROM python:3.11-slim

# Instalar dependencias del sistema incluyendo OpenMP
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libomp-dev \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Crear directorio de trabajo
WORKDIR /home/app

# Copiar archivos de requirements y instalar dependencias
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar estructura completa del proyecto
COPY app/ ./app/
COPY data/ ./data/
COPY scripts/ ./scripts/
COPY models/ ./models/

# Crear directorios adicionales si no existen
RUN mkdir -p models reports/html

# Exponer puerto
EXPOSE 8000

# Comando para iniciar la aplicación (ahora desde app/)
CMD ["python", "app/main.py"]