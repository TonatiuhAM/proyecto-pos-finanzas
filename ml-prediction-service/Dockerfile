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

# Copiar código de la aplicación
COPY main.py .
COPY pipeline.py .
COPY regenerar_modelos.py .

# Crear directorio para modelos
RUN mkdir -p models

# Exponer puerto
EXPOSE 8000

# Comando para iniciar la aplicación
CMD ["python", "main.py"]