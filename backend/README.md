# Energy Mix — Backend

Spring Boot 3.4 (Java 21) REST API. Part of the [Energy Mix monorepo](../README.md).

## Endpoints
- `GET /api/energy-mix` — aggregated mix for today, tomorrow, the day after.
- `GET /api/charging-window?hours={1-6}` — greenest charging window over the next two days.
- `GET /` — health check.

## Run
```bash
./mvnw spring-boot:run     # http://localhost:8080
./mvnw test                # run the test suite
```

## Configuration (env vars)
| Variable | Default | Purpose |
|----------|---------|---------|
| `PORT` | `8080` | HTTP port |
| `CARBON_INTENSITY_BASE_URL` | `https://api.carbonintensity.org.uk` | upstream API |
| `CORS_ALLOWED_ORIGINS` | `*` | comma-separated allowed front-end origins |

The core logic lives in [`EnergyMixService`](src/main/java/com/codibly/energymix/service/EnergyMixService.java).
