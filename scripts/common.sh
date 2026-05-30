#!/usr/bin/env bash
# Shared configuration and helpers for the CampusConnect AI dev scripts.
# This file is sourced by status.sh / start.sh / stop.sh / setup.sh.

# Resolve the repo root from this file's location (works from any cwd).
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
RUN_DIR="$ROOT_DIR/.run"            # logs + pid files (git-ignored)

# Defaults can be overridden from the environment.
DB_NAME="${DB_NAME:-campusconnect}"
DB_PORT="${DB_PORT:-3306}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

mkdir -p "$RUN_DIR"

# Colours (only when writing to a terminal).
if [ -t 1 ]; then
  GREEN=$'\e[32m'; RED=$'\e[31m'; YELLOW=$'\e[33m'; BOLD=$'\e[1m'; RESET=$'\e[0m'
else
  GREEN=""; RED=""; YELLOW=""; BOLD=""; RESET=""
fi

info() { printf "%s\n" "$*"; }
ok()   { printf "${GREEN}\xE2\x9C\x93 %s${RESET}\n" "$*"; }
warn() { printf "${YELLOW}! %s${RESET}\n" "$*"; }
err()  { printf "${RED}\xE2\x9C\x97 %s${RESET}\n" "$*"; }

# True if something is listening on the given TCP port.
port_up() { lsof -nP -i :"$1" >/dev/null 2>&1; }

# Point JAVA_HOME at a Java 17 install if it isn't already.
detect_java17() {
  if [ -n "${JAVA_HOME:-}" ] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17'; then
    return 0
  fi
  for cand in /opt/homebrew/opt/openjdk@17 /usr/local/opt/openjdk@17; do
    if [ -x "$cand/bin/java" ]; then export JAVA_HOME="$cand"; return 0; fi
  done
  if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17)"; return 0
  fi
  return 1
}

# Echo a usable mysql client path, or nothing if none is found.
mysql_client() {
  for p in mysql /opt/homebrew/opt/mysql@8.4/bin/mysql /opt/homebrew/opt/mysql@5.7/bin/mysql; do
    if command -v "$p" >/dev/null 2>&1; then command -v "$p"; return; fi
    [ -x "$p" ] && { echo "$p"; return; }
  done
}
