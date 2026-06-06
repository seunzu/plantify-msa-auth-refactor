FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /workspace
COPY common-auth-lib ./common-auth-lib
COPY demo-resource-service ./demo-resource-service
WORKDIR /workspace/demo-resource-service
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/demo-resource-service/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
