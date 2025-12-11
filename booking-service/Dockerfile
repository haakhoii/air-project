FROM eclipse-temurin:17-jdk-alpine

COPY target/booking-service-0.0.1-SNAPSHOT.jar booking-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "booking-service.jar"]