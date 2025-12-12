# ---------- Build Stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml first (cache dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the full project source
COPY src ./src

# Build the application (skip tests)
RUN mvn clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port for Render
EXPOSE 8080

# HEALTH CHECK — optional (Render uses its own)
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/healthz || exit 1

# Start the server
ENTRYPOINT ["java","-jar","/app/app.jar"]
