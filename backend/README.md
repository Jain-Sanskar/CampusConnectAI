# CampusConnect AI — Backend

Spring Boot 3 REST API (Java 17) for CampusConnect AI. Handles authentication,
the resource catalogue, and the AI Senior chat (server-side Gemini integration).

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use the bundled `mvnw`)
- MySQL 8 running locally on port 3306

## Configuration

All settings live in `src/main/resources/application.yml` and read from environment
variables with sensible local defaults. Override them as needed:

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/campusconnect?...` | JDBC connection string |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | _(empty)_ | Database password |
| `JWT_SECRET` | dev-only default | Base64 secret used to sign JWTs — **override in production** |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds |
| `GEMINI_API_KEY` | _(empty)_ | Google Gemini API key. If unset, the chat returns a graceful fallback reply |
| `ADMIN_EMAIL` | `admin@campusconnect.com` | Seeded admin account |
| `ADMIN_PASSWORD` | `Admin@123` | Seeded admin password — **change this** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origin(s) |

Create the database once:

```sql
CREATE DATABASE IF NOT EXISTS campusconnect;
```

Schema tables are created automatically by Hibernate (`ddl-auto: update`).

## Run

```bash
# from the backend/ folder
export GEMINI_API_KEY="your-key"   # optional, enables live AI replies
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. On first boot a `DataSeeder` creates the
admin user and a few sample resources if the database is empty.

## Test

```bash
mvn test
```

## API summary

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Register and receive a JWT |
| POST | `/api/auth/login` | public | Login and receive a JWT |
| GET | `/api/resources` | authenticated | List resources (`?category=&subject=` filters) |
| POST/PUT/DELETE | `/api/resources` | `ADMIN` | Manage resources |
| POST | `/api/chat` | authenticated | Send a message to the AI Senior |
| GET | `/api/chat/sessions` | authenticated | List the user's chat sessions |
| GET | `/api/chat/sessions/{id}` | authenticated | Fetch messages for a session |
