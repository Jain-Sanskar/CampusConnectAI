# CampusConnect AI — High-Level Design (HLD)

## 1. System Overview

CampusConnect AI is a 3-tier web application with a server-side AI integration:

1. **Presentation tier** — React 18 SPA (Vite + Tailwind + Axios).
2. **Application tier** — Spring Boot 3 REST API (Spring Web, Spring Data JPA, Spring Security, stateless JWT).
3. **Data tier** — MySQL 8.
4. **External service** — Google Gemini API, called **only** from the backend.

## 2. Architecture Diagram

```mermaid
flowchart LR
    subgraph Client["Browser - React 18 SPA"]
        UI["Components: Auth, Resources, Chatbot"]
        AX["Axios + JWT Interceptor"]
    end

    subgraph Server["Spring Boot 3 REST API"]
        SEC["Spring Security Filter Chain<br/>(JWT Auth Filter)"]
        CTRL["Controllers<br/>Auth / Resource / Chat"]
        SVC["Services (Business Logic)"]
        REPO["JPA Repositories"]
        GEM["Gemini REST Client (WebClient)"]
    end

    DB[("MySQL 8")]
    GAPI["Google Gemini API"]

    UI --> AX -->|HTTPS + Bearer JWT| SEC --> CTRL --> SVC
    SVC --> REPO --> DB
    SVC --> GEM -->|HTTPS REST| GAPI
```

## 3. Core Modules

| Module | Responsibility |
|---|---|
| **Auth** | Registration, login, JWT issuance/validation, role-based access. |
| **Resource** | CRUD + listing/filtering of academic resources by category/subject. |
| **AI Senior Chatbot** | Pre-prompted Gemini mentor; relays chat to Gemini, returns guidance, persists history. |
| **Security** | Stateless JWT filter, BCrypt hashing, CORS config, global exception handling. |
| **User** | Profile + roles (STUDENT, ADMIN). |

## 4. Key Data Flows

### Authentication (login)

```mermaid
sequenceDiagram
    participant R as React
    participant A as AuthController
    participant S as AuthService
    participant DB as MySQL
    R->>A: POST /api/auth/login {email, password}
    A->>S: authenticate(credentials)
    S->>DB: find user by email
    DB-->>S: user (hashed pw)
    S->>S: BCrypt match + generate JWT
    S-->>A: JWT + user info
    A-->>R: 200 {token, role}
    Note over R: stores token, attaches as Bearer on future calls
```

### AI Senior chat

```mermaid
sequenceDiagram
    participant R as React
    participant C as ChatController
    participant S as ChatService
    participant G as Gemini API
    participant DB as MySQL
    R->>C: POST /api/chat {message} + Bearer JWT
    C->>S: getReply(user, message)
    S->>S: build payload (system prompt + user msg)
    S->>G: POST generateContent
    G-->>S: AI response
    S->>DB: persist (session + message)
    S-->>C: reply text
    C-->>R: 200 {reply}
```

## 5. Security Design

- **Stateless JWT** in `Authorization: Bearer <token>` — no server session.
- **BCrypt** password hashing.
- **Role-based authorization**: `STUDENT` (view/download, chat), `ADMIN` (manage resources).
- **CORS**: explicit allow-list for the React dev origin.
- **Secrets** (JWT secret, Gemini API key, DB password) loaded from **environment variables** — never hardcoded.
- **Global exception handler** → generic client messages, detailed server-side logs.

## 6. Non-Functional Requirements

- **Performance:** non-blocking Gemini calls via WebClient; DB indexes on lookup columns.
- **Security:** Bean Validation on inputs, least-privilege DB user, no secrets in source.
- **Maintainability:** layered architecture; DTOs decouple API from entities.
- **Data lifecycle:** chat history retained for a configurable window, then flushed by a scheduled job.
- **Testability:** services and controllers unit-tested with JUnit 5 + Mockito.

## 7. Deployment View

- **Dev:** React (Vite dev server) ↔ Spring Boot (`:8080`) ↔ local MySQL 8.
- **Config** via `application.yml` + environment variables.
- **Future:** Dockerize backend + frontend; deploy DB as managed service (out of MVP scope).
