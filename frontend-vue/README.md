# AllerScan Frontend (Vue Service)

Separate Vue frontend service with the same UI/UX as the current app.

## What this service does

- Keeps design 1:1 by reusing original `app.css` classes and visual structure.
- Implements pages in Vue (`dashboard`, `analyze`, `history`, `profile`, `allergens`).
- Proxies `/api` requests to Spring Boot in dev mode.

## Run locally
# Vue 3 + Vite
1) Start backend first (`food-allergen-system`, Spring Boot on `http://localhost:8080`).
This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.
2) Start Vue service:

```bash
cd frontend-vue
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

## Optional env

- `VITE_BACKEND_ORIGIN` (default: `http://localhost:8080`) for Vite API proxy target.

Example:

```bash
VITE_BACKEND_ORIGIN=http://localhost:8080 npm run dev
```

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).
