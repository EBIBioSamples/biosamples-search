FROM gradle:8.12-jdk21 AS builder

WORKDIR /app

# Copy module build files only, to warm up and pre-resolve dependencies
COPY proto/build.gradle.kts proto/
COPY server/build.gradle.kts server/
RUN gradle --no-daemon clean build -x test || return 0

COPY . .
RUN gradle :server:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/server/build/libs/*.jar biosamples-search.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "biosamples-search.jar"]
