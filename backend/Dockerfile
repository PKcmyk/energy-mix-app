# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/energy-mix-backend-0.0.1.jar app.jar
# Spring reads the port from the PORT env var (defaults to 8080); Render sets it automatically.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
