FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle build --no-daemon || return 0


COPY . .
RUN gradle spotlessApply && gradle clean build -x test --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app


COPY --from=build /app/build/libs/*.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]
