FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# Copiar el archivo pom.xml y los archivos de código fuente
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Hacer el mvnw ejecutable
RUN chmod +x ./mvnw

# Construir el JAR
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Copiar las dependencias y las clases compiladas
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Configurar el punto de entrada
ENTRYPOINT ["java","-cp","app:app/lib/*","com.inventario.inventario.InventarioApplication"]

# Exponer el puerto de la aplicación
EXPOSE 8082

# Variables de entorno con valores por defecto (serán sobrescritos por docker-compose)
ENV DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/pruebaDb
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=1@adminsis
ENV MICRO=inventario
ENV ENV=dev
ENV APP_PORT=8082
ENV API_KEY=123456ABC