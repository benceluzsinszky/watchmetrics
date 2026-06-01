#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example and add your API keys." >&2
  exit 1
fi

if grep -q 'watchmetrics.example.com' Caddyfile; then
  echo "Edit Caddyfile and replace watchmetrics.example.com with your domain." >&2
  exit 1
fi

docker compose build
docker compose up -d
docker compose ps

echo
echo "Deployed. Check logs with: docker compose logs -f watchmetrics"
