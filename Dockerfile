# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven Wrapper and config
COPY mvnw .
COPY .mvn .mvn

# Copy project files
COPY pom.xml .
COPY src ./src

# Make sure mvnw is executable
RUN chmod +x mvnw

# Build using Maven Wrapper
RUN ./mvnw -q -e -DskipTests clean package

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
