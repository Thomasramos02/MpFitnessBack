FROM ubuntu:latest as build

run apt-get update
run apt-get install openjdk-17-jdk maven -y
copy . .

run apt-get install maven -y
run mvn clean install

from openjdk:17-jdk-slim

expose 8080

copy --from=build /target/MpFitnessBack-0.0.1-SNAPSHOT.jar MpFitnessBack-0.0.1-SNAPSHOT.jar

entrypoint ["java","-jar","/MpFitnessBack-0.0.1-SNAPSHOT.jar"]