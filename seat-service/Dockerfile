FROM eclipse-temurin:17-jdk-alpine

COPY target/seat-service-0.0.1-SNAPSHOT.jar seat-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "seat-service.jar"]