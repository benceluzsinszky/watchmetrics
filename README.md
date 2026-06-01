# watchmetrics

A web app for exploring TV series ratings and analytics — search any show and see how it evolved episode by episode across IMDb, Rotten Tomatoes, and Metacritic.

## Stack

- Kotlin + Spring Boot 3
- Thymeleaf + HTMX
- Tailwind CSS v4

## Prerequisites

- **JDK 21** — [Eclipse Temurin](https://adoptium.net/) or similar
- **Node.js 20+** and npm — for Tailwind CSS build

## Run locally

```bash
# 1. Install frontend deps and build CSS
npm install
npm run build:css

# 2. Start the app
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080).

### Development (CSS hot rebuild)

Run in separate terminals:

```bash
npm run watch:css
./gradlew bootRun
```

## Project layout

```bash
src/main/kotlin/com/watchmetrics/   # Kotlin source
src/main/resources/templates/       # Thymeleaf HTML
src/main/resources/static/          # Compiled CSS, assets
src/main/frontend/styles/           # Tailwind source
```
