FROM eclipse-temurin:17-jdk-alpine

COPY target/orchestrator-service-0.0.1-SNAPSHOT.jar orchestrator-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "orchestrator-service.jar"]