FROM openjdk:21-jdk-alpine

ENV TZ=Asia/Kolkata
WORKDIR /app

COPY target/*.jar /app/zplex-api.jar

CMD ["java", "-jar", "/app/zplex-api.jar --server.port=${PORT}"]
