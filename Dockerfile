FROM gradle:8.14-jdk24 AS builder

WORKDIR /app

# Copy minimal Gradle build files first, to warm up and pre-resolve dependencies
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY proto/build.gradle.kts proto/
COPY server/build.gradle.kts server/
RUN gradle --no-daemon dependencies || true

COPY . .
RUN gradle :server:bootJar --no-daemon -x test

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=builder /app/server/build/libs/*.jar biosamples-search.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "biosamples-search.jar"]
