# ========================================
# Folio - Backend Dockerfile
# ========================================

# ============ Build stage ============
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# 1. Copy pom.xml and download dependencies first (leverages Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============ Runtime stage ============
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install tools required for health check
RUN apk add --no-cache curl

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run the app as a non-root user (security best practice)
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expose port
EXPOSE 8123

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8123/api/health/ || exit 1

# JVM tuning options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Startup command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
