
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="dev@docsearch.com"
LABEL version="1.0.0"
LABEL description="Document Search Application"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN apk add --no-cache \
    curl \
    tzdata \
    && cp /usr/share/zoneinfo/Europe/Moscow /etc/localtime \
    && echo "Europe/Moscow" > /etc/timezone

RUN mkdir -p /app /data/uploads /app/logs && \
    chown -R appuser:appgroup /app /data /app/logs

USER appuser
WORKDIR /app

COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=40s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]