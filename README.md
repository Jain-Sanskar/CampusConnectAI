# CampusConnect AI

**An AI-Powered Academic Resource & Guidance Portal**

CampusConnect AI is a full-stack web application that gives college juniors and freshers a single, authenticated place to find organized academic resources (previous year questions, syllabus links, study notes) and to get instant mentorship from an **"AI Senior"** chatbot powered by Google Gemini.

---

## Why this project

College juniors often struggle to find reliable, organized academic resources in one place, and human seniors are not always available to answer academic, project, or placement questions. CampusConnect AI solves both problems:

1. A **centralized, authenticated resource portal** with categorized, searchable academic material.
2. An **AI Senior chatbot** — an LLM pre-prompted to behave like a helpful college mentor for tech stacks, placement prep, and core CS concepts.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 (Vite), Functional Components + Hooks, Tailwind CSS, Axios |
| Backend | Java 17, Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security) |
| Database | MySQL 8 |
| AI | Google Gemini API (server-side REST integration) |
| Auth | Spring Security with stateless JWT |
| Testing | JUnit 5, Mockito |

---

## Architecture

A classic 3-tier architecture with a server-side AI integration:

```
React 18 SPA  ──HTTPS + Bearer JWT──>  Spring Boot 3 REST API  ──JPA──>  MySQL 8
                                              │
                                              └──REST──>  Google Gemini API
```

The Gemini API is **never** called from the browser — all AI calls go through the backend so the API key stays secret.

Detailed design lives in [`docs/`](./docs).

---

## Repository Structure

```
CampusConnectAI/
├── backend/        # Spring Boot 3 REST API (Java 17)
├── frontend/       # React 18 + Vite + Tailwind SPA
├── docs/           # HLD, LLD and architecture notes
└── README.md
```

---

## Getting Started

Setup and run instructions for each module live in their respective folders and will be expanded as the project is built:

- Backend: see [`backend/README.md`](./backend/README.md)
- Frontend: see [`frontend/README.md`](./frontend/README.md)

---

## Status

This project is built iteratively, feature by feature. See [`docs/EXECUTION_PLAN.md`](./docs/EXECUTION_PLAN.md) for the roadmap.
