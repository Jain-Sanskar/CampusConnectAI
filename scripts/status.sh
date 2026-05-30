#!/usr/bin/env bash
# Report which CampusConnect AI components are up or down.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

info "${BOLD}CampusConnect AI — component status${RESET}"

line() {
  local name="$1" port="$2"
  if port_up "$port"; then
    ok "$name UP   (port $port)"
  else
    err "$name DOWN (port $port)"
  fi
}

line "MySQL   " "$DB_PORT"
line "Backend " "$BACKEND_PORT"
line "Frontend" "$FRONTEND_PORT"

# A reachable-but-secured API answers 401 without a token, which confirms it's truly up.
if port_up "$BACKEND_PORT"; then
  code="$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$BACKEND_PORT/api/resources" 2>/dev/null || echo 000)"
  info "  backend API → HTTP $code (401 = up and secured, as expected without a token)"
fi

# Exit non-zero if anything core is down (handy for CI / chaining).
if port_up "$DB_PORT" && port_up "$BACKEND_PORT" && port_up "$FRONTEND_PORT"; then
  exit 0
else
  exit 1
fi
