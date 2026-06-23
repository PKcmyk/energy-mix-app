# UK Energy Mix & EV Charging Planner

A small full-stack app that shows the **current and forecast electricity generation mix for Great Britain**
and computes the **greenest time window to charge an electric car** over the next two days.

Built for the Codibly IT Academy recruitment task. Monorepo with a Spring Boot backend and a React + TypeScript frontend.

| Part | Stack | Folder |
|------|-------|--------|
| Backend | Java 21 · Spring Boot 3.4 · `RestClient` | [`backend/`](./backend) |
| Frontend | React 18 · TypeScript · Vite · Recharts | [`frontend/`](./frontend) |

Data source: the public [UK Carbon Intensity API](https://carbonintensity.github.io/api-definitions/#get-generation-from-to).

---

## What it does

1. **Energy mix** — three pie charts (today, tomorrow, the day after) showing the average share of each
   generation source, with the **clean-energy percentage** highlighted on each card.
2. **Charging planner** — the user picks a charging duration of **1–6 hours**; the app returns the contiguous
   window with the highest average clean-energy share over the next two days, including start/end time and the
   average clean percentage.

**Clean energy** is defined (per the task) as the sum of: `biomass`, `nuclear`, `hydro`, `wind`, `solar`.

### How the numbers are computed

The API publishes data in **half-hour settlement periods**, so an *N*-hour window spans `N × 2` periods.

- **Daily mix:** periods are grouped by their date (Europe/London), then each fuel's share is averaged across
  that day's periods. The clean percentage is the averaged sum of the five clean sources.
- **Optimal window:** the backend slides a window of `hours × 2` consecutive periods across the two forecast
  days and picks the one with the highest average clean share. The window **may cross midnight**, and windows
  with gaps in the data are skipped. See [`EnergyMixService`](./backend/src/main/java/com/codibly/energymix/service/EnergyMixService.java).

---

## API

Base URL defaults to `http://localhost:8080`.

### `GET /api/energy-mix`
Returns today, tomorrow and the day after.
```json
[
  {
    "date": "2026-06-23",
    "generationMix": { "gas": 45.7, "wind": 23.1, "nuclear": 11.6, "...": 0.0 },
    "cleanEnergyPercentage": 50.34
  }
]
```

### `GET /api/charging-window?hours={1-6}`
```json
{
  "start": "2026-06-25T10:30:00Z",
  "end":   "2026-06-25T13:30:00Z",
  "averageCleanEnergyPercentage": 70.88
}
```
`hours` outside `1–6` → `400`. Not enough forecast data → `422`. Upstream API failure → `502`.

---

## Running locally

### Option A — Docker (everything at once)
```bash
docker compose up --build
```
- Frontend → http://localhost:8081
- Backend  → http://localhost:8080

### Option B — run each part directly

**Backend** (needs JDK 21+):
```bash
cd backend
./mvnw spring-boot:run        # or: mvn spring-boot:run
```

**Frontend** (needs Node 20+):
```bash
cd frontend
cp .env.example .env          # points VITE_API_URL at the backend
npm install
npm run dev                   # http://localhost:5173
```

---

## Tests

```bash
# Backend — 13 tests (service logic, API client, controller)
cd backend && mvn test

# Frontend — 8 tests (API client, components)
cd frontend && npm test
```

The backend tests cover the core domain logic with a mocked API client: daily grouping/averaging, the
window-selection algorithm (including the cross-midnight and non-contiguous cases), validation and error mapping.

---

## Deploying to Render

Both services deploy as Docker web services. Either use the [`render.yaml`](./render.yaml) blueprint, or create
two web services manually:

1. **Backend** — root directory `backend`, runtime *Docker*. Set `CORS_ALLOWED_ORIGINS` to the frontend URL.
2. **Frontend** — root directory `frontend`, runtime *Docker*. Set `VITE_API_URL` to the backend URL
   (it is baked into the static bundle at build time).

Both Dockerfiles bind to the `PORT` env var that Render injects.

---

## Project structure
```
.
├── backend/            Spring Boot API
│   ├── src/main/java/com/codibly/energymix/
│   │   ├── client/     Carbon Intensity API client + its DTOs
│   │   ├── config/     RestClient, Clock, CORS
│   │   ├── controller/ REST endpoints + error handling
│   │   ├── dto/        response DTOs (DailyMixDto, ChargingWindowDto)
│   │   └── service/    domain logic (EnergyMixService)
│   └── Dockerfile
├── frontend/           React + Vite SPA
│   ├── src/
│   │   ├── api/        typed API client
│   │   └── components/ dashboard, charts, charging planner
│   └── Dockerfile
├── docker-compose.yml
└── render.yaml
```
