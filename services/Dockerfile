# syntax=docker/dockerfile:1.7
# One image recipe for every Spring Boot service:
#   docker build -f services/Dockerfile --build-arg SERVICE=order-service -t dinedash/order-service .
# Build context is the repository root so the shared module is available.

FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /src
COPY pom.xml .
COPY shared shared
COPY services services
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -pl services/${SERVICE} -am package -DskipTests

# Split the fat jar into layers so dependency layers are cached between releases
FROM eclipse-temurin:21-jre-alpine AS extract
ARG SERVICE
WORKDIR /extract
COPY --from=build /src/services/${SERVICE}/target/${SERVICE}.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination layers

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
ENV SERVICE_NAME=${SERVICE} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app && apk add --no-cache wget
WORKDIR /app
COPY --from=extract /extract/layers/dependencies/ ./
COPY --from=extract /extract/layers/spring-boot-loader/ ./
COPY --from=extract /extract/layers/snapshot-dependencies/ ./
COPY --from=extract /extract/layers/application/ ./
USER 10001
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- "http://localhost:${SERVER_PORT:-8080}/actuator/health/liveness" || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
