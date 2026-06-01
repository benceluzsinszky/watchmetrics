# syntax=docker/dockerfile:1

FROM node:20-alpine AS css

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY src/main/frontend ./src/main/frontend
COPY src/main/resources/templates ./src/main/resources/templates
COPY src/main/kotlin ./src/main/kotlin
RUN npm run build:css

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Cached when dependency config is unchanged — avoids re-downloading on every deploy.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon -q

COPY src ./src
COPY --from=css /app/src/main/resources/static/css/app.css ./src/main/resources/static/css/app.css

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/build/libs/*.jar app.jar

USER app

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
