# syntax=docker/dockerfile:1

# --- Build stage: compile and extract the layered jar ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination target/extracted

# --- Runtime stage: JRE only, non-root user ---
FROM eclipse-temurin:25-jre-alpine
# Pick up OS security fixes released after the base image was built.
# A numeric user lets Kubernetes verify runAsNonRoot (see deploy/kubernetes).
RUN apk upgrade --no-cache && addgroup -S -g 10001 app && adduser -S -u 10001 -G app app
USER 10001:10001
WORKDIR /app

# Least frequently changing layers first, for better caching.
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

# 8080: API. 8081: Actuator with SPRING_PROFILES_ACTIVE=prod (keep it internal).
EXPOSE 8080 8081

# Used by Docker and compose; Kubernetes ignores it and uses the probes in deploy/kubernetes.
# Actuator is on 8081 with the prod profile and on 8080 otherwise.
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health/liveness 2>/dev/null | grep -q UP \
   || wget -qO- http://localhost:8080/actuator/health/liveness | grep -q UP
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
