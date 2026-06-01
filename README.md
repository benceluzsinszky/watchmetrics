# watchmetrics

A web app for exploring TV series ratings and analytics — search any show and see how it evolved episode by episode across IMDb, Rotten Tomatoes, and Metacritic.

## Stack

- Kotlin + Spring Boot 3
- Thymeleaf + HTMX
- Tailwind CSS v4

## Prerequisites

- **JDK 21** — [Eclipse Temurin](https://adoptium.net/) or similar
- **Node.js 20+** and npm — for Tailwind CSS build
- **TMDB API credentials** — copy `.env.example` to `.env` and fill in your keys from [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api)

## Run locally

```bash
# 1. Configure TMDB (once)
cp .env.example .env
# edit .env — TMDB_ACCESS_TOKEN and TMDB_API_KEY

# 2. Install frontend deps and build CSS
npm install
npm run build:css

# 3. Start the app (loads .env automatically)
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080).

### Development (hot reload)

One command starts everything — Tailwind CSS watch, Kotlin recompile, and Spring Boot with DevTools:

```bash
npm run dev
# or
./scripts/dev.sh
```

Stop with **Ctrl+C** (shuts down all processes).

| Change | Reload |
|--------|--------|
| `.kt` files | Kotlin recompiles → DevTools restarts app (~1–2s) |
| Thymeleaf HTML | DevTools restart |
| Tailwind / CSS | `app.css` rebuilds → refresh browser |

Optional: [LiveReload browser extension](https://chromewebstore.google.com/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei) auto-refreshes after backend restarts.

## Project layout

```bash
src/main/kotlin/com/watchmetrics/   # Kotlin source
src/main/resources/templates/       # Thymeleaf HTML
src/main/resources/static/          # Compiled CSS, assets
src/main/frontend/styles/           # Tailwind source
```
