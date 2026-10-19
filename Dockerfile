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
RUN addgroup -S app && adduser -S -G app app
USER app
WORKDIR /app

# Least frequently changing layers first, for better caching.
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

# 8080: API. 8081: Actuator with SPRING_PROFILES_ACTIVE=prod (keep it internal).
EXPOSE 8080 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
