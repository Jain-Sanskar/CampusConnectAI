#!/usr/bin/env bash
# First-time setup for a fresh machine: install dependencies, then prepare the app.
# Targets macOS + Homebrew. After this, run ./scripts/start.sh
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

info "${BOLD}CampusConnect AI — first-time setup${RESET}"

OS="$(uname -s)"
if [ "$OS" != "Darwin" ]; then
  warn "This script targets macOS (Homebrew)."
  warn "On Linux, install JDK 17, Node 18+, Maven and MySQL 8 with your package manager, then run ./scripts/start.sh"
  exit 1
fi

# 1. Homebrew
if ! command -v brew >/dev/null 2>&1; then
  info "Installing Homebrew…"
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
else
  ok "Homebrew present"
fi

brew_install() {
  local f="$1"
  if brew list --formula 2>/dev/null | grep -qx "$f"; then
    ok "$f already installed"
  else
    info "Installing $f…"
    brew install "$f"
  fi
}

# 2. Runtime dependencies
brew_install openjdk@17
brew_install node
brew_install maven

# MySQL: skip if any mysql formula is already installed.
if brew list --formula 2>/dev/null | grep -qiE '^mysql'; then
  ok "MySQL already installed"
else
  brew_install mysql@8.4
fi

# 3. Frontend env + dependencies
if [ ! -f "$FRONTEND_DIR/.env" ]; then
  cp "$FRONTEND_DIR/.env.example" "$FRONTEND_DIR/.env"
  ok "Created frontend/.env from template"
fi
info "Installing frontend dependencies…"
( cd "$FRONTEND_DIR" && npm install )

# 4. Verify Java 17 is resolvable for the backend
if detect_java17; then
  ok "Java 17 detected at $JAVA_HOME"
else
  warn "Java 17 not detected. Homebrew's openjdk@17 is keg-only — start.sh sets JAVA_HOME automatically."
fi

info ""
ok "Setup complete."
info "Optional: export GEMINI_API_KEY=\"your-key\"   (enables live AI chat)"
info "Then run: ./scripts/start.sh"
