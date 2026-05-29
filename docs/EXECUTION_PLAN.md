# CampusConnect AI — Execution Plan (Atomic-Commit Roadmap)

The project is built iteratively. Each item below is one logical Git commit.

## Phase 0 — Project Foundation
- [x] 0.1 `chore: initialize repository and add root README + .gitignore`
- [ ] 0.2 `docs: add HLD, LLD and architecture diagrams`

## Phase 1 — Backend Skeleton & Database
- [ ] 1.1 `feat: bootstrap Spring Boot 3 project with core dependencies`
- [ ] 1.2 `feat: configure MySQL datasource and JPA properties`
- [ ] 1.3 `feat: add User entity, role enum and UserRepository`

## Phase 2 — Authentication & Security
- [ ] 2.1 `feat: add password encoder and base Spring Security config`
- [ ] 2.2 `feat: implement JWT generation and validation service`
- [ ] 2.3 `feat: add JWT authentication filter and entry point`
- [ ] 2.4 `feat: implement register and login endpoints`
- [ ] 2.5 `test: add unit tests for AuthService and JwtService`
- [ ] 2.6 `feat: add global exception handler and error response model`

## Phase 3 — Resource Module
- [ ] 3.1 `feat: add Resource entity and repository with filtering`
- [ ] 3.2 `feat: implement resource service with CRUD logic`
- [ ] 3.3 `feat: add resource REST controller with role-based access`
- [ ] 3.4 `test: add unit tests for ResourceService and controller`
- [ ] 3.5 `feat: seed initial admin user and sample resources`

## Phase 4 — AI Senior Chatbot
- [ ] 4.1 `feat: add Gemini WebClient config and chat DTOs`
- [ ] 4.2 `feat: implement chat service with system-prompted mentor`
- [ ] 4.3 `feat: add chat session and message entities with repositories`
- [ ] 4.4 `feat: persist chat history and expose chat endpoints`
- [ ] 4.5 `feat: add scheduled job to flush old chat history`
- [ ] 4.6 `test: add unit tests for ChatService with mocked Gemini`

## Phase 5 — Frontend Foundation
- [ ] 5.1 `feat: scaffold React 18 app with Vite and Tailwind CSS`
- [ ] 5.2 `feat: configure Axios client with JWT interceptor`
- [ ] 5.3 `feat: add auth context and protected route component`

## Phase 6 — Frontend Auth UI
- [ ] 6.1 `feat: build login page with form validation`
- [ ] 6.2 `feat: build registration page`

## Phase 7 — Frontend Resources UI
- [ ] 7.1 `feat: build dashboard and resources listing with filters`
- [ ] 7.2 `feat: build admin resource management UI`

## Phase 8 — Frontend Chatbot UI
- [ ] 8.1 `feat: build AI Senior chat widget UI`
- [ ] 8.2 `feat: integrate chat history and session list`

## Phase 9 — Polish, Docs & Hardening
- [ ] 9.1 `style: responsive design polish and loading/empty states`
- [ ] 9.2 `docs: add setup instructions, env templates and screenshots`
- [ ] 9.3 `test: increase coverage and add edge-case tests`
- [ ] 9.4 `chore: final cleanup, lint fixes and demo data`
- [ ] 9.5 `docs: add interview talking points and architecture summary`

---

## Build Schedule

- **Day 1 (Backend):** Phases 0-4 — foundation, Spring Boot skeleton + DB, JWT auth + tests, resource CRUD + tests, AI chatbot + tests.
- **Day 2 (Frontend + Polish):** Phases 5-9 — Vite/Tailwind setup, auth UI, resources UI, chatbot UI, polish/docs.
