FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8085:8085

ARG JAR_FILE="/build/libs/assessment-service-0.0.1-SNAPSHOT.jar"

COPY ${JAR_FILE} /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"]
