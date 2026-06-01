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

### Hot reload while developing

Use **three terminals** (restart `bootRun` once after adding DevTools):

```bash
# Terminal 1 — Tailwind rebuilds CSS on change
npm run watch:css

# Terminal 2 — Spring Boot app
./gradlew bootRun

# Terminal 3 — recompiles Kotlin when you save; DevTools restarts the app (~1–2s)
./gradlew compileKotlin --continuous
```

What picks up automatically:

| Change | How it reloads |
|--------|----------------|
| `.kt` files | Terminal 3 recompiles → DevTools fast restart |
| Thymeleaf HTML | DevTools restart (templates not cached in dev) |
| Tailwind / CSS source | Terminal 1 writes `app.css` → refresh browser |
| `application.yml` | DevTools restart |

Optional: install the [LiveReload browser extension](https://chromewebstore.google.com/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei) and DevTools will refresh the tab after a restart.

If you use IntelliJ/Cursor run configs instead of Gradle, enable **Build project automatically** so Kotlin recompiles on save; keep `bootRun` in a terminal or use the IDE run configuration with DevTools on the classpath.

## Project layout

```bash
src/main/kotlin/com/watchmetrics/   # Kotlin source
src/main/resources/templates/       # Thymeleaf HTML
src/main/resources/static/          # Compiled CSS, assets
src/main/frontend/styles/           # Tailwind source
```
