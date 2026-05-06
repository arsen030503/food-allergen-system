# AllerScan Frontend (Vue 3 + Vite)

SPA for the allergen detection dashboard. Global styles and layout tokens live in [`public/css/app.css`](public/css/app.css) (loaded from [`index.html`](index.html)). Vue entry only adds a minimal [`src/style.css`](src/style.css) so the cascade stays predictable.

## Run locally

1. Start the Spring Boot API (default `http://localhost:8080`).
2. From this folder:

```bash
npm install
npm run dev
```

App: `http://localhost:5173`. The dev server proxies `/api` to the backend (see [`vite.config.js`](vite.config.js)).

## Environment

- **`VITE_BACKEND_ORIGIN`** — proxy target for `/api` during `npm run dev` (default `http://localhost:8080`).  
  See [`.env.example`](.env.example).

Production: build static files and serve them **on the same domain** as the API with a reverse proxy that forwards `/api` to Spring Boot. Keep `<meta name="api-base" content="/api" />` in `index.html` (or set it in your deploy template) so the Axios client calls the proxied path.

## CORS

The browser origin must be listed in the backend setting **`APP_CORS_ALLOWED_ORIGINS`** if the UI and API are on different origins. For the recommended single-domain `/api` proxy, use your public site URL in that list (and localhost origins for development).

## Build

```bash
npm run build
```

Output: `dist/`. Configure your static host + SPA fallback to `index.html` for client-side routes.
