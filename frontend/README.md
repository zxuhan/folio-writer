# Folio — Frontend

Vue 3 + Vite + Ant Design Vue. Talks to the Folio backend.

## Requirements

- Node.js 22+

## Stack

- Vue 3
- Vite
- TypeScript
- Ant Design Vue
- Axios
- Pinia
- Vue Router

## Develop

```bash
# Install dependencies
npm install

# Start dev server (port 5173, proxies /api to localhost:8567)
npm run dev

# Production build
npm run build

# Format
npm run format

# Lint
npm run lint
```

## Generate API client

The backend exposes an OpenAPI spec at `http://localhost:8567/api/v3/api-docs`.
With the backend running, regenerate the typed client:

```bash
npm run openapi2ts
```

## Layout

```
src/
├── api/           # Auto-generated from backend OpenAPI
├── assets/        # Static assets
├── components/    # Shared components
├── config/        # Runtime config (env.ts)
├── layouts/       # Layout components
├── pages/         # Page components
├── router/        # Route definitions
├── stores/        # Pinia stores
└── utils/         # Helpers
```
