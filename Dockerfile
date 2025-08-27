# ----------------------
# Stage 1: Build
# ----------------------
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ----------------------
# Stage 2: Runtime
# ----------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV TZ=Asia/Kolkata
RUN echo "${TZ}" > /etc/timezone
COPY --from=build /app/api/target/zplex-api-*.jar zplex-api.jar
CMD ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "zplex-api.jar", "--server.port=${PORT}"]