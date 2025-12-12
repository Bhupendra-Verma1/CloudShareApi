# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Copy entire project (pom.xml, src, mvnw, .mvn)
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build project using Maven Wrapper, skip tests
RUN ./mvnw clean package -DskipTests -B

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port for Spring Boot
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
