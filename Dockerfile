FROM openjdk:23-slim AS base

WORKDIR /app

COPY target/interaction-service-0.0.1-SNAPSHOT.jar interaction-service-0.0.1-SNAPSHOT.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "interaction-service-0.0.1-SNAPSHOT.jar"]
