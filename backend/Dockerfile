# Archivo: backend/Dockerfile

# ---- Etapa de Construcción (Build Stage) ----
# Usamos una imagen con Maven y el JDK de Java para compilar el proyecto
FROM maven:3.9-eclipse-temurin-17 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos solo el pom.xml para aprovechar el cache de Docker
COPY pom.xml .
# Descargamos las dependencias
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente
COPY src ./src

# Empaquetamos la aplicación, saltando los tests para acelerar la construcción
RUN mvn package -DskipTests

# ---- Etapa de Ejecución (Runtime Stage) ----
# Usamos una imagen mucho más ligera solo con el entorno de ejecución de Java (JRE)
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copiamos el .jar construido desde la etapa de 'build'
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación cuando el contenedor se inicie
CMD ["java", "-jar", "app.jar"]