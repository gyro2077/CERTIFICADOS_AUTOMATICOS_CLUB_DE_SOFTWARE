# --- Etapa 1: Construcción (Build) ---
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiamos los archivos necesarios para Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Damos permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargamos las dependencias (para aprovechar la caché de Docker)
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y construimos el JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Ejecución (Run) ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Render asigna un puerto dinámico en la variable de entorno PORT
ENV SERVER_PORT=${PORT:-8080}

# Exponemos el puerto (informativo para Docker)
EXPOSE ${SERVER_PORT}

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
