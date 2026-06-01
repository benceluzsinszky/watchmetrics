FROM node:20-alpine AS css

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY src/main/frontend ./src/main/frontend
RUN npm run build:css

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
COPY --from=css /app/src/main/resources/static/css/app.css ./src/main/resources/static/css/app.css

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/build/libs/*.jar app.jar

USER app

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
