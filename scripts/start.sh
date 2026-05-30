#!/usr/bin/env bash
# Start every CampusConnect AI component. Anything already running is skipped.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

start_mysql() {
  if port_up "$DB_PORT"; then
    ok "MySQL already running (port $DB_PORT)"
  else
    info "Starting MySQL…"
    local formula=""
    for f in mysql@8.4 mysql mysql@5.7; do
      if brew list --formula 2>/dev/null | grep -qx "$f"; then formula="$f"; break; fi
    done
    if [ -z "$formula" ]; then
      err "No MySQL installed via Homebrew. Run ./scripts/setup.sh first."
      return 1
    fi
    brew services start "$formula" >/dev/null 2>&1 || warn "Could not start $formula via brew services."
    for _ in $(seq 1 30); do port_up "$DB_PORT" && break; sleep 1; done
    port_up "$DB_PORT" && ok "MySQL started" || { err "MySQL did not come up on port $DB_PORT"; return 1; }
  fi

  # Make sure the application database exists.
  local m; m="$(mysql_client)"
  if [ -n "$m" ]; then
    if "$m" -u root -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`;" >/dev/null 2>&1; then
      ok "Database '$DB_NAME' ready"
    else
      warn "Could not verify database '$DB_NAME' (check MySQL root credentials)."
    fi
  fi
}

start_backend() {
  if port_up "$BACKEND_PORT"; then
    ok "Backend already running (port $BACKEND_PORT)"
    return 0
  fi
  if ! detect_java17; then
    err "Java 17 not found. Run ./scripts/setup.sh first."
    return 1
  fi
  [ -z "${GEMINI_API_KEY:-}" ] && warn "GEMINI_API_KEY not set — AI chat will use the graceful fallback reply."

  info "Starting backend (JAVA_HOME=$JAVA_HOME)…"
  (
    cd "$BACKEND_DIR" || exit 1
    JAVA_HOME="$JAVA_HOME" nohup mvn -q -DskipTests spring-boot:run > "$RUN_DIR/backend.log" 2>&1 &
    echo $! > "$RUN_DIR/backend.pid"
  )
  for _ in $(seq 1 90); do port_up "$BACKEND_PORT" && break; sleep 1; done
  if port_up "$BACKEND_PORT"; then
    ok "Backend started → http://localhost:$BACKEND_PORT (logs: .run/backend.log)"
  else
    err "Backend failed to start — check .run/backend.log"
    return 1
  fi
}

start_frontend() {
  if port_up "$FRONTEND_PORT"; then
    ok "Frontend already running (port $FRONTEND_PORT)"
    return 0
  fi
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    info "Installing frontend dependencies…"
    ( cd "$FRONTEND_DIR" && npm install >/dev/null 2>&1 )
  fi
  [ ! -f "$FRONTEND_DIR/.env" ] && cp "$FRONTEND_DIR/.env.example" "$FRONTEND_DIR/.env"

  info "Starting frontend…"
  (
    cd "$FRONTEND_DIR" || exit 1
    nohup npm run dev > "$RUN_DIR/frontend.log" 2>&1 &
    echo $! > "$RUN_DIR/frontend.pid"
  )
  for _ in $(seq 1 30); do port_up "$FRONTEND_PORT" && break; sleep 1; done
  if port_up "$FRONTEND_PORT"; then
    ok "Frontend started → http://localhost:$FRONTEND_PORT (logs: .run/frontend.log)"
  else
    err "Frontend failed to start — check .run/frontend.log"
    return 1
  fi
}

info "${BOLD}Starting CampusConnect AI…${RESET}"
start_mysql
start_backend
start_frontend
info ""
info "${BOLD}Ready.${RESET}  App: http://localhost:$FRONTEND_PORT   API: http://localhost:$BACKEND_PORT/api"
info "Stop everything with ./scripts/stop.sh"
