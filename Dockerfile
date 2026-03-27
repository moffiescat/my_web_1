FROM openjdk:21-jdk-slim
LABEL maintainer="my-web-app"

WORKDIR /app

COPY target/my_web_1-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/config/ /app/config/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=config/"]
