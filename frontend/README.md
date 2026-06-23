# Energy Mix — Frontend

React 18 + TypeScript + Vite SPA, with Recharts for the pie charts. Part of the
[Energy Mix monorepo](../README.md).

## Run
```bash
cp .env.example .env        # set VITE_API_URL (defaults to http://localhost:8080)
npm install
npm run dev                 # http://localhost:5173
npm test                    # run the test suite
npm run build               # type-check + production build to dist/
```

## Configuration
| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_API_URL` | `http://localhost:8080` | backend base URL (baked in at build time) |

## Structure
- `src/api/` — typed API client and response types.
- `src/components/` — `EnergyMixDashboard` + `DailyMixCard` (charts) and `ChargingWindowForm` + `ChargingWindowResult` (planner).
