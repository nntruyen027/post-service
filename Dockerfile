FROM openjdk:23-slim AS base

WORKDIR /app

COPY target/post-service-0.0.1-SNAPSHOT.jar post-service-0.0.1-SNAPSHOT.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "post-service-0.0.1-SNAPSHOT.jar"]
