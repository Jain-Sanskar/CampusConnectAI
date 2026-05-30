# CampusConnect AI — Frontend

React 18 + Vite + Tailwind CSS single-page application for CampusConnect AI.

## Prerequisites

- Node.js 18+
- The backend API running (see [`../backend/README.md`](../backend/README.md))

## Configuration

Copy the example env file and adjust if your backend runs elsewhere:

```bash
cp .env.example .env
```

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Base URL of the backend API |

## Run

```bash
npm install
npm run dev
```

The app starts on `http://localhost:5173`.

## Build

```bash
npm run build      # outputs to dist/
npm run preview    # serve the production build locally
```

## Structure

```
src/
├── api/         # Axios client + per-feature API modules
├── components/  # Reusable UI (Navbar, ProtectedRoute, ResourceCard, …)
├── context/     # AuthContext (JWT + user state)
├── pages/       # Login, Register, Dashboard, AdminResources, Chat
├── App.jsx      # Routes
└── main.jsx     # Entry point
```

The JWT is stored in `localStorage` and attached to every request by an Axios
interceptor; a `401` response clears the session and redirects to login.
