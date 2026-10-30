# Etapa 1: compila el proyecto con Maven y Java 21.
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /proyecto
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
RUN mvn -B -q -f backend/pom.xml -DskipTests package

# Etapa 2: imagen final, solo con el JRE de Java 21.
FROM eclipse-temurin:21-jre
WORKDIR /aplicacion
COPY --from=compilacion /proyecto/backend/target/papeleria-matrix.jar aplicacion.jar

# Render corre la aplicación en modo demo, con una base de datos H2 y datos de ejemplo.
ENV SPRING_PROFILES_ACTIVE=demo
EXPOSE 8080

# Opciones de memoria y arranque pensadas para el plan gratuito de Render (512 MB y poco procesador).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-jar", "aplicacion.jar"]
