# CampusConnect AI — Interview Talking Points

A cheat sheet for explaining this project confidently. Read it once before an
interview and you'll be able to talk about *why* things were built the way they were —
which matters far more than the feature list.

---

## 1. The 30-second pitch

> "CampusConnect AI is a full-stack academic portal I built with React, Spring Boot and
> MySQL. Students log in to browse categorized study resources and chat with an
> 'AI Senior' — a Gemini-powered mentor that answers academic and placement questions.
> It's a classic 3-tier app with stateless JWT auth, role-based access control, and the
> AI call made server-side so the API key never reaches the browser."

---

## 2. Architecture at a glance

```
React 18 SPA  ──HTTPS + Bearer JWT──>  Spring Boot 3 REST API  ──JPA──>  MySQL 8
                                              │
                                              └──REST──>  Google Gemini API
```

- **Frontend (React + Vite + Tailwind):** SPA, talks to the backend only over REST.
- **Backend (Spring Boot):** layered as Controller → Service → Repository. Controllers
  are thin, business logic lives in services, persistence in Spring Data JPA repositories.
- **Database (MySQL):** users, resources, chat_sessions, chat_messages.
- **AI (Gemini):** called from the backend with a system prompt that gives the model its
  "AI Senior" persona.

---

## 3. Key technical decisions (and why)

| Decision | Why |
|---|---|
| **Stateless JWT** instead of server sessions | Scales horizontally, no session store, natural fit for a SPA + REST API. |
| **AI call on the backend**, not the browser | Keeps the Gemini API key secret and lets me add prompt logic, fallback and persistence. |
| **DTOs separate from entities** | The API contract never leaks DB structure; validation lives on request DTOs. |
| **Global exception handler** (`@RestControllerAdvice`) | Every error returns the same JSON shape with the right HTTP status — clean for the frontend. |
| **Role-based access** (`STUDENT` / `ADMIN`) | Only admins can create/edit/delete resources; enforced in Spring Security, not the UI. |
| **Scheduled cleanup job** for old chats | Chat history is only useful for a few days; a cron job flushes stale sessions automatically. |
| **Vite + Tailwind** | Fast dev/build and quick, consistent styling — modern, widely-adopted tooling. |

---

## 4. Security highlights

- Passwords hashed with **BCrypt** — never stored in plain text.
- JWT signed with an HMAC secret; the filter validates it on every protected request.
- A custom **`JwtAuthenticationFilter`** parses the `Authorization` header and sets the
  security context; a **`JwtAuthEntryPoint`** returns a clean `401` JSON for missing/invalid tokens.
- **CORS** is restricted to the known frontend origin.
- Authorization is enforced on the **server** (`hasRole("ADMIN")`), so hiding a button in
  the UI is just UX — the API is the real gatekeeper.

---

## 5. Testing approach

- **JUnit 5 + Mockito** for backend unit tests (services, controller, security mapping).
- Services are tested in isolation with mocked repositories — fast and deterministic.
- Edge cases covered: duplicate registration, bad credentials, resource-not-found,
  chat session ownership, and the AI fallback path when the API key is missing.

---

## 6. Problems I actually hit (great stories to tell)

1. **Spring Security swallowing error responses.** Protected routes returned `401` even for
   `404`/`400` cases because Spring's internal forward to `/error` was also being
   authenticated. Fixed by permitting the `ERROR` dispatcher type in the filter chain.
2. **Gemini API integration.** My first key returned `429` with zero quota; debugging with
   `curl` showed the key authenticated but had no allowance. The working setup needed the
   key in the `X-goog-api-key` header (not a query param) and the `gemini-flash-latest`
   model. I also added a **graceful fallback** so the chat degrades to a friendly message
   instead of erroring when the AI is unreachable.
3. **Keeping secrets out of code.** All secrets (DB password, JWT secret, Gemini key) are
   read from environment variables with safe local defaults.

---

## 7. Likely interview questions — and answers

- **"Why JWT over sessions?"** Stateless, scales without sticky sessions or a shared
  session store; the token carries the identity and the server just verifies the signature.
- **"How do you keep the AI key safe?"** It only lives on the server in an env var; the
  browser never sees it because all AI calls are proxied through my backend.
- **"What happens if Gemini is down?"** The service catches the failure and returns a
  fallback reply, and the user's message is still saved — the app stays usable.
- **"How is admin access enforced?"** Spring Security route rules require the `ADMIN` role
  for write operations on resources; the React UI just mirrors that for UX.
- **"How would you scale this?"** Stateless backend means I can run multiple instances
  behind a load balancer; MySQL can get read replicas; AI calls could move to a queue.

---

## 8. What I'd improve next

- Refresh tokens + token expiry handling on the client.
- File uploads (S3) in addition to URL-based resources.
- Streaming AI responses for a more "live" chat feel.
- Pagination and full-text search on resources.
- Integration tests with Testcontainers and a CI pipeline.
