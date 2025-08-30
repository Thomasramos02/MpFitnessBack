FROM ubuntu:latest as build

RUN apt-get update && apt-get install openjdk-21-jdk maven -y
COPY . .

RUN mvn clean install

FROM openjdk:21-jdk-slim

EXPOSE 8080

COPY --from=build /target/MpFitnessBack-0.0.1-SNAPSHOT.jar MpFitnessBack-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/MpFitnessBack-0.0.1-SNAPSHOT.jar"]
