# Archivo: frontend/Dockerfile

# ---- Etapa de Construcción (Build Stage) ----
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos package.json y package-lock.json para cachear las dependencias
COPY package*.json ./
RUN npm install

# Copiamos el resto del código fuente
COPY . .
# Construimos la versión de producción
RUN npm run build

# ---- Etapa de Ejecución (Runtime Stage) ----
# Usamos Nginx, un servidor web ligero y de alto rendimiento
FROM nginx:stable-alpine

# Copiamos los archivos estáticos construidos a la carpeta que sirve Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiamos nuestra configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponemos el puerto 80, que es el puerto por defecto de Nginx
EXPOSE 80