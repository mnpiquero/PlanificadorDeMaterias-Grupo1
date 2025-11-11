# Etapa 1: Construcción con Maven
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias (esta capa se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto (pueden ser sobrescritas por docker-compose)
ENV SPRING_NEO4J_URI=bolt://neo4j:7687
ENV SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
ENV SPRING_NEO4J_AUTHENTICATION_PASSWORD=grupo123

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]

