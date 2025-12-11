FROM eclipse-temurin:17-jdk-alpine

COPY target/flight-service-0.0.1-SNAPSHOT.jar flight-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "flight-service.jar"]