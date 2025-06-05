FROM openjdk:25-ea-21-slim-bookworm

WORKDIR /opt/app

COPY target/Blizzard-Bot-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
