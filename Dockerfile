FROM openjdk:21-jdk-slim

WORKDIR /app

COPY my_web_1-1.0-SNAPSHOT.jar app.jar
COPY config/ /app/config/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
