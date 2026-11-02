# ============================================================
# AlertCity Backend - Dockerfile multi-stage
# ============================================================

# ---------- Etapa 1: Compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Descarga dependencias primero (se cachea mientras pom.xml no cambie)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia el código y empaqueta (tests se ejecutan aparte con Testcontainers)
COPY src ./src
RUN mvn -B -q clean package -DskipTests -Dasciidoctor.skip=true

# ---------- Etapa 2: Ejecución ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
