FROM eclipse-temurin:17-jdk-alpine

COPY target/payment-service-0.0.1-SNAPSHOT.jar payment-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "payment-service.jar"]