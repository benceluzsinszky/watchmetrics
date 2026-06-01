#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

log() {
  printf '\033[36m[dev]\033[0m %s\n' "$*"
}

warn() {
  printf '\033[33m[dev]\033[0m %s\n' "$*" >&2
}

cleanup() {
  log "Stopping dev processes..."
  local pid
  for pid in $(jobs -pr); do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}

trap cleanup EXIT INT TERM

if [[ ! -d node_modules ]]; then
  log "Installing npm dependencies..."
  npm install
fi

if [[ ! -f .env ]]; then
  warn ".env not found — copy .env.example and add TMDB/OMDb credentials."
else
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

log "Building CSS once..."
npm run build:css

log "Starting Tailwind watch (CSS hot reload)..."
npm run watch:css 2>&1 | sed 's/^/[css] /' &
CSS_PID=$!

log "Starting Kotlin continuous compile (backend hot reload via DevTools)..."
./gradlew compileKotlin --continuous -q 2>&1 | sed 's/^/[kotlin] /' &
KOTLIN_PID=$!

# Give the watcher a moment to attach before bootRun starts.
sleep 1

log "Starting Spring Boot on http://localhost:8080 ..."
log "Press Ctrl+C to stop all processes."
./gradlew bootRun 2>&1 | sed 's/^/[app] /'

# bootRun exited — cleanup trap stops css + kotlin watchers
kill "$CSS_PID" "$KOTLIN_PID" 2>/dev/null || true
