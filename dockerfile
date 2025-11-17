# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copia primeiro os arquivos do Maven Wrapper
COPY .mvn/ .mvn
COPY mvnw ./
COPY pom.xml ./

# Dá permissão e baixa as dependências primeiro (cache eficiente)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila o projeto
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

COPY --from=build /app/target/MpFitness-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]