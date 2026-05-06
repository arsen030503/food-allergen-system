# AllerScan Backend (Spring Boot)

REST API for the food allergen detection app. Uses **PostgreSQL**, **JPA**, **Flyway** migrations, **JWT** (httpOnly cookie + optional `Authorization: Bearer`).

## Configuration

| Variable | Description |
|----------|-------------|
| `PORT` | Server port (default `8080`) |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | PostgreSQL connection |
| `APP_JWT_SECRET` | Signing secret (**≥ 32 bytes**). In `prod` profile, must be set explicitly. |
| `APP_JWT_EXPIRATION_MS` | Token lifetime (ms) |
| `APP_JWT_COOKIE_SECURE` | `true` when using HTTPS |
| `APP_JWT_COOKIE_SAME_SITE` | e.g. `Lax` |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins for CORS + credentials |
| `SPRING_PROFILES_ACTIVE=prod` | Enables [`application-prod.properties`](src/main/resources/application-prod.properties) |

Copy [`./.env.example`](./.env.example) as a checklist for your host (do not commit secrets).

## Database migrations

- Location: `src/main/resources/db/migration`
- Hibernate: `spring.jpa.hibernate.ddl-auto=validate` — schema is **only** created/updated by Flyway.

## Run

```bash
./mvnw spring-boot-run
```

## Production

```bash
export SPRING_PROFILES_ACTIVE=prod
export APP_JWT_SECRET='your-very-long-random-secret-at-least-32-chars'
export DB_HOST=... DB_PORT=5432 DB_NAME=... DB_USER=... DB_PASSWORD=...
./mvnw spring-boot:run
```

Tests use H2 in-memory with Flyway **disabled** (`src/test/resources/application.properties`).
