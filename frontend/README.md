# Energy Mix — Frontend

React 18 + TypeScript + Vite SPA, with Recharts for the pie charts. Part of the
[Energy Mix monorepo](../README.md).

## Run
```bash
npm install
npm run dev                 # http://localhost:5173 (Vite proxies /api to localhost:8080)
npm test                    # run the test suite
npm run build               # type-check + production build to dist/
```

## Configuration
The app calls the backend via relative `/api` URLs. In production the nginx container proxies them:

| Variable | Where | Default | Purpose |
|----------|-------|---------|---------|
| `BACKEND_URL` | nginx (runtime) | `http://localhost:8080` | backend the container proxies `/api` to |
| `PORT` | nginx (runtime) | `80` | port nginx listens on (Render injects it) |
| `VITE_API_URL` | build (optional) | _empty_ | set only to call a backend on a different origin directly |

## Structure
- `src/api/` — typed API client and response types.
- `src/components/` — `EnergyMixDashboard` + `DailyMixCard` (charts) and `ChargingWindowForm` + `ChargingWindowResult` (planner).
