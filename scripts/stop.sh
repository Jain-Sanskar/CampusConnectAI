#!/usr/bin/env bash
# Stop CampusConnect AI components.
#   ./scripts/stop.sh         stops the backend and frontend (app processes)
#   ./scripts/stop.sh --all   also stops the shared MySQL service
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

STOP_DB=false
[ "${1:-}" = "--all" ] && STOP_DB=true

stop_proc() {
  local name="$1" pidfile="$2" pattern="$3"
  local stopped=false
  if [ -f "$pidfile" ]; then
    kill "$(cat "$pidfile")" >/dev/null 2>&1 && stopped=true
    rm -f "$pidfile"
  fi
  # Also clean up by pattern to catch child processes (e.g. the forked JVM).
  pkill -f "$pattern" >/dev/null 2>&1 && stopped=true
  $stopped && ok "$name stopped" || warn "$name was not running"
}

info "${BOLD}Stopping CampusConnect AI…${RESET}"
stop_proc "Frontend" "$RUN_DIR/frontend.pid" "vite"
stop_proc "Backend"  "$RUN_DIR/backend.pid"  "spring-boot:run"
pkill -f "CampusConnectApplication" >/dev/null 2>&1 && ok "Backend JVM stopped"

if $STOP_DB; then
  info "Stopping MySQL…"
  for f in mysql@8.4 mysql mysql@5.7; do
    brew services stop "$f" >/dev/null 2>&1
  done
  ok "MySQL stop requested"
else
  warn "MySQL left running (shared service). Use './scripts/stop.sh --all' to stop it too."
fi
