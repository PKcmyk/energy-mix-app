# Energy Mix — Backend

Spring Boot 4.1 (Java 25) REST API, built with Gradle. Part of the [Energy Mix monorepo](../README.md).

## Endpoints
- `GET /api/energy-mix` — aggregated mix for today, tomorrow, the day after.
- `GET /api/charging-window?hours={1-6}` — greenest charging window over the next two days.
- `GET /` — health check.

## Run
```bash
./gradlew bootRun          # http://localhost:8080
./gradlew test             # run the test suite
./gradlew spotlessApply    # apply code formatting (spotlessCheck verifies it)
./gradlew build            # spotlessCheck + tests + bootJar
```

## Spring profiles
| Profile | Use | Effect |
|---------|-----|--------|
| _(none)_ | default / deploy | env-driven config (see table below) |
| `local` | running on your machine | CORS for `localhost:5173`/`:8081`, DEBUG logging incl. upstream HTTP calls |
| `dev` | deployed non-prod | DEBUG application logging |

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## Caching
Upstream generation data is cached (Caffeine, 30-min TTL) per requested `(from, to)` range, so the
two endpoints — which request overlapping ranges — collapse into a single upstream call within the
TTL window.

## Configuration (env vars)
| Variable | Default | Purpose |
|----------|---------|---------|
| `PORT` | `8080` | HTTP port |
| `SPRING_PROFILES_ACTIVE` | _none_ | active Spring profile (`local`, `dev`) |
| `CARBON_INTENSITY_BASE_URL` | `https://api.carbonintensity.org.uk` | upstream API |
| `CORS_ALLOWED_ORIGINS` | `*` | comma-separated allowed front-end origins |

The core logic lives in [`EnergyMixService`](src/main/java/com/codibly/energymix/service/EnergyMixService.java).

CI (`.github/workflows/ci.yml`) runs `spotlessCheck`, then compilation, then the tests on every push and PR.
