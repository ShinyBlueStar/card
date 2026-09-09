# syntax=docker/dockerfile:1.10
#
# Build from the repository root:
#   docker build -f card-container/Dockerfile \
#     --secret id=nexus_username,env=NEXUS_USERNAME \
#     --secret id=nexus_password,env=NEXUS_PASSWORD \
#     -t card-service:local .
#
# Nexus credentials are BuildKit secrets: they are never written into an image layer.

ARG REGISTRY=hub.bmicc.ir/library

########## Stage 1: build ##########
FROM ${REGISTRY}/maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Resolve dependencies first so this layer is cached while only source code changes
COPY ci/maven-settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml ./
COPY card-domain/pom.xml card-domain/
COPY card-domain/card-domain-core/pom.xml card-domain/card-domain-core/
COPY card-domain/card-application-service/pom.xml card-domain/card-application-service/
COPY card-application/pom.xml card-application/
COPY card-infrastructure/pom.xml card-infrastructure/
COPY card-container/pom.xml card-container/
RUN --mount=type=secret,id=nexus_username,env=NEXUS_USERNAME \
    --mount=type=secret,id=nexus_password,env=NEXUS_PASSWORD \
    --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp -s /usr/share/maven/ref/settings.xml dependency:go-offline -q || true

COPY . .
ARG MAVEN_ARGS="-DskipTests"
RUN --mount=type=secret,id=nexus_username,env=NEXUS_USERNAME \
    --mount=type=secret,id=nexus_password,env=NEXUS_PASSWORD \
    --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp -s /usr/share/maven/ref/settings.xml -pl card-container -am package ${MAVEN_ARGS} \
 && java -Djarmode=tools -jar card-container/target/card-service.jar extract --layers --launcher --destination /extracted

########## Stage 2: runtime ##########
FROM ${REGISTRY}/eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S -g 1001 app && adduser -S -u 1001 -G app app \
 && mkdir -p /app/uploads && chown -R app:app /app
WORKDIR /app

# One layer per Spring Boot layer: dependencies change rarely, application code often
COPY --from=build --chown=app:app /extracted/dependencies/ ./
COPY --from=build --chown=app:app /extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /extracted/application/ ./

USER app

ENV SERVER_PORT=8007 \
    FILE_STORAGE_PATH=/app/uploads/ \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8007
VOLUME ["/app/uploads"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/actuator/health" | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
