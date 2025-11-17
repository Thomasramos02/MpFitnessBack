# Stage 1: Build
FROM ubuntu:latest AS build

RUN apt-get update && apt-get install -y openjdk-21-jdk maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mvn clean install -DskipTests

# Stage 2: Runtime
FROM openjdk:21-jdk-slim

WORKDIR /app
EXPOSE 8080

COPY --from=build /app/target/MpFitness-0.0.1-SNAPSHOT.jar /app/app.jar

# NÃO defina variáveis sensíveis aqui!
# Elas devem ser passadas pelo ambiente de execução (Render, Docker Compose, etc)

ENTRYPOINT ["java", "-jar", "/app/app.jar"]