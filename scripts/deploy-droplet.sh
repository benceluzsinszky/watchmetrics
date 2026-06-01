#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

WITH_CADDY=false
if [[ "${1:-}" == "--with-caddy" ]]; then
  WITH_CADDY=true
fi

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example and add your API keys." >&2
  exit 1
fi

if $WITH_CADDY && grep -q 'watchmetrics.example.com' Caddyfile; then
  echo "Edit Caddyfile and replace watchmetrics.example.com with your domain." >&2
  exit 1
fi

PORT="${WATCHMETRICS_PORT:-8081}"
export WATCHMETRICS_PORT="$PORT"

docker compose build

if $WITH_CADDY; then
  docker compose --profile standalone up -d
else
  docker compose up -d watchmetrics
fi

docker compose ps

echo
echo "Watchmetrics listening on 127.0.0.1:${PORT}"
echo "Logs: docker compose logs -f watchmetrics"

if ! $WITH_CADDY; then
  echo
  echo "Port 80/443 already in use? Add this to your host Caddy/Nginx config:"
  echo
  echo "  watchmetrics.yourdomain.com {"
  echo "      reverse_proxy 127.0.0.1:${PORT}"
  echo "  }"
  echo
  echo "Then reload: sudo systemctl reload caddy"
fi
