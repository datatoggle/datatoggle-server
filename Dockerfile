FROM openjdk:11-jre-slim
EXPOSE 8080
COPY build/libs/server-0.0.1-SNAPSHOT.jar server-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "server-0.0.1-SNAPSHOT.jar"]
