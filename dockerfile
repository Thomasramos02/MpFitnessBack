# Build stage
# Use Eclipse Temurin JDK 23 (official, disponível no Docker Hub)
FROM eclipse-temurin:23-jdk as build

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy only the files needed for Maven build
COPY pom.xml ./
COPY src ./src/

# Build the application using the system Maven installed above
RUN mvn -B -DskipTests clean package -q

# Final stage
# Runtime stage
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/MpFitness-*.jar MpFitness.jar

# Create non-root user for security
RUN useradd -m appuser && chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp /app/MpFitness.jar org.springframework.boot.loader.JarLauncher

# Run the application
ENTRYPOINT ["java", "-jar", "MpFitness.jar"]