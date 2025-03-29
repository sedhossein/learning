# Build stage (with Java 21)
FROM gradle:8-jdk21-alpine AS build
WORKDIR /app

# 1. Copy only essential build files first (layer caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle/wrapper/ gradle/wrapper/

# 2. Download dependencies (creates cache layer)
RUN gradle dependencies --no-daemon || true

# 3. Copy application source files (minimal copying)
COPY src/ ./src/

# 4. Build the application (only build jar without tests)
RUN gradle bootJar --no-daemon -x test \
    && mv /app/build/libs/*.jar /app/build/libs/app.jar

# Run stage (with Java 21 JRE)
FROM eclipse-temurin:21-alpine
WORKDIR /app

# 5. Copy the necessary files from the build stage
COPY --from=build /app/build/libs/app.jar ./app.jar
COPY --from=build /app/src/main/resources/ ./src/main/resources/

# Expose the application's port and set entry point
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
