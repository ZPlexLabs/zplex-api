FROM ubuntu:20.04

ENV TZ=Asia/Kolkata
WORKDIR /app

RUN apt-get update && \
apt-get install -y -q openjdk-21-jdk tzdata && \
ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime && \
echo "${TZ}" > /etc/timezone && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

COPY target/zplex-api*.jar /app/zplex-api.jar
CMD ["java", "-jar", "/app/zplex-api.jar", "--server.port=${PORT}", "--spring.profiles.active=prod", "--springdoc.api-docs.enabled=false", \
     "--spring.application.name=${SPRING_APPLICATION_NAME}", \
     "--spring.datasource.url=${SPRING_DATASOURCE_URL}", \
     "--spring.datasource.username=${SPRING_DATASOURCE_USERNAME}", \
     "--spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}"]
