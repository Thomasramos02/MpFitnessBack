# Stage 1: Build
FROM ubuntu:22.04 AS build

# Instalar OpenJDK 17 e Maven
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Build do projeto Java (Spring Boot)
RUN mvn clean install -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

WORKDIR /app
EXPOSE 8080

# Copiar o JAR compilado
COPY --from=build /app/target/MpFitness-0.0.1-SNAPSHOT.jar /app/app.jar

# ENTRYPOINT para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
