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

# Empaquetamos la aplicación, omitiendo completamente cualquier compilación o ejecución de tests
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.compile.skip=false -Dmaven.test.compile.skip=true

# ---- Etapa de Ejecución (Runtime Stage) ----
# Usamos una imagen mucho más ligera solo con el entorno de ejecución de Java (JRE)
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copiamos el .jar construido desde la etapa de 'build'
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación con perfil de producción y configuraciones específicas
CMD ["java", "-Dspring.profiles.active=prod", "-Dspring.security.user.name=", "-Dspring.security.user.password=", "-jar", "app.jar"]