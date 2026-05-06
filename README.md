# Food Allergen System (AllerScan)

Full-stack app: **Spring Boot 3** API + **Vue 3** SPA. Schema is managed with **Flyway** on PostgreSQL.

## Repo layout

- [`backend/`](backend/) — REST API (`/api/...`), JWT cookie + Bearer auth
- [`frontend/`](frontend/) — Vue + Vite UI; production expects `/api` on the **same origin** (reverse proxy)

## Production deployment (same domain + `/api` proxy)

1. **PostgreSQL** — create an empty database; credentials via `DB_*` (see [`backend/.env.example`](backend/.env.example)).
2. **Backend** — set environment variables and run with profile `prod`:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   - `APP_JWT_SECRET` — **required**, at least **32 bytes** (any long random string)
   - `APP_CORS_ALLOWED_ORIGINS` — comma-separated list of allowed browser origins (e.g. `https://yourdomain.com`)
   - Optional: `APP_JWT_COOKIE_SECURE=true` (typical when served over HTTPS)
3. On startup, **Flyway** runs migrations in `backend/src/main/resources/db/migration` (tables + allergen seed data).
4. **Frontend** — `npm run build` in [`frontend/`](frontend/), serve `dist/` as static files on the same host.
5. **Reverse proxy** — route `/api` (and `/api/**`) to the Spring Boot server; SPA fallback to `index.html` for client routes.

JWT uses an httpOnly cookie on `/`; with one domain and a path-based proxy, cookies work without cross-site CORS.

## Local development

**Backend** (from `backend/`):

```bash
./mvnw spring-boot-run
# or: mvn spring-boot-run
```

Default datasource: `localhost:5432`, database `postgres`, user/password `postgres` — override with `DB_*`. For an empty DB, Flyway creates schema + seeds allergens.

**Frontend** (from `frontend/`):

```bash
npm install
npm run dev
```

`VITE_BACKEND_ORIGIN` (see `frontend/.env.example`) sets the dev proxy target for `/api`.

## Security notes

- Do **not** commit real DB passwords or JWT secrets; use env vars in production.
- First registered user becomes **ADMIN** (existing app behavior); lock down open registration if exposing the app publicly.
