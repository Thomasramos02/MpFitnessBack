# Stage 1: Build
FROM ubuntu:latest AS build

RUN apt-get update && apt-get install -y openjdk-21-jdk maven && rm -rf /var/lib/apt/lists/*

COPY . .

RUN mvn clean install -DskipTests

# Stage 2: Runtime
FROM openjdk:21-jdk-slim

EXPOSE 8080

COPY --from=build /target/MpFitness-0.0.1-SNAPSHOT.jar /app.jar

# Variáveis de ambiente
ENV DB_URL=jdbc:postgresql://dpg-d2p5eu7diees73bj1h60-a:5432/MpFitnessBD
ENV DB_USERNAME=admin
ENV DB_PASSWORD=chGJKelCTQNJRXV2w4Hdri9P2p3esCiA
ENV UPLOAD_DIR=/opt/mpfitness/uploads
ENV MERCADO_PAGO_ACCESS_TOKEN=APP_USR-829557851095328-082820-c577f4cedaf1b3c1e87ff2f19055b70b
ENV CLOUDINARY_CLOUD_NAME=doqq1svld
ENV CLOUDINARY_API_KEY=666136823193588
ENV CLOUDINARY_API_SECRET=7wfhxpRnJ1rRNSlX_OY9tZDaIt4
ENV GOOGLE_CLIENT_ID=1052588841770-ge2si46h0arfgp5ii7nqg2642cd83uug.apps.googleusercontent.com
ENV GOOGLE_CLIENT_SECRET=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
ENV FRONTEND_URL=http://127.0.0.1:5500/MpFitnessFront/
ENV BACKEND_URL=http://localhost:8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
