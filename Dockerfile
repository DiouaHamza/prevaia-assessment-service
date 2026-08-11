FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8091:8091

ARG JAR_FILE="/build/libs/service-template-0.0.1-SNAPSHOT.jar"

COPY ${JAR_FILE} /app/service.jar

ENTRYPOINT ["java", "-jar", "/app/service.jar"]
