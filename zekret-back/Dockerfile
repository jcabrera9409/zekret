FROM maven:3.9.6-eclipse-temurin-21 AS builder
RUN useradd -m -s /bin/bash appuser
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/logs /app/files \
 && chown -R appuser:appgroup /app
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]