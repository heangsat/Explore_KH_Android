# Explore KH API

Backend service for the Explore KH Android app. Go + Gin on top of MariaDB.
It serves the public catalog: the 25 provinces, the 50 destinations, categories,
and filtered or paginated attraction lists.

## Requirements

- Go 1.25 or newer
- MariaDB 11.x (or MySQL 8)
- Optional: Docker for the Compose setup

## Quick start with Docker

```bash
cp .env.example .env
docker compose up --build
```

The API listens on <http://localhost:8081> (the Compose file maps container port
8080 to 8081 so it does not clash with Adminer on 8080). Migrations run on
startup. Then load the catalog once:

```bash
docker compose exec api ./seed -file seeds/destinations.json
```

Adminer is available at <http://localhost:8080>. Use server `db`, user
`explore`, password `explore_dev`, database `explore_kh`.

## Local development

Create the database and a user, then point the service at it:

```sql
CREATE DATABASE explore_kh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'explore'@'localhost' IDENTIFIED BY 'explore_dev';
GRANT ALL PRIVILEGES ON explore_kh.* TO 'explore'@'localhost';
```

```bash
cp .env.example .env      # adjust DB_PORT and credentials if needed
go run ./cmd/api          # applies migrations, then serves on :8080
go run ./cmd/seed -file seeds/destinations.json
```

The seed command is idempotent. It upserts provinces and attractions, rebuilds
the `attraction_hours` ranges, and bumps `catalog_versions` so clients know the
catalog changed.

Handy targets: `make run`, `make seed`, `make test`, `make fmt`, `make vet`.

## Configuration

All settings come from the environment (a local `.env` is loaded when present).
See `.env.example` for the full list. The important ones:

| Variable | Default | Notes |
| --- | --- | --- |
| `HTTP_ADDR` | `:8080` | Listen address |
| `APP_TIMEZONE` | `Asia/Phnom_Penh` | Used for `openNow` filtering |
| `DB_HOST` / `DB_PORT` | `127.0.0.1` / `3306` | MariaDB connection |
| `DB_USER` / `DB_PASSWORD` | `explore` / `explore_dev` | |
| `DB_NAME` | `explore_kh` | |
| `RUN_MIGRATIONS` | `true` | Run `golang-migrate` on startup |
| `MIGRATIONS_PATH` | `file://migrations` | |
| `CORS_ALLOWED_ORIGINS` | `*` | Comma-separated list, or `*` |
| `SEED_FILE` | `seeds/destinations.json` | Default for the seed command |

## Endpoints

Base path is `/api/v1`. `GET /health` is also exposed at the root for probes.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/health` | Database ping and server time |
| GET | `/provinces` | All provinces, sorted by English name |
| GET | `/provinces/:id` | One province |
| GET | `/provinces/:id/attractions` | Attractions inside a province |
| GET | `/attractions` | Search, filter, sort, paginate |
| GET | `/attractions/:id` | One attraction |
| GET | `/categories` | Distinct categories in use |

Successful responses are wrapped in `{"data": ...}`. Lists add a `meta` object
with `page`, `pageSize`, `total`, and `totalPages`. Errors look like:

```json
{ "error": { "code": "not_found", "message": "Attraction not found." } }
```

### Attraction query parameters

- `q` — free text over titles, descriptions, and province names (English and Khmer)
- `provinceId` — province slug, for example `siem-reap`
- `category` — `Heritage`, `Eco-Tourism`, `Culture`, or `Leisure`
- `minRating` — 0 to 5
- `maxPriceUsd` — inclusive upper bound on the numeric USD price
- `openNow` — `true`/`false`; respects split opening hours in `attraction_hours`
- `page` — 1-based, default `1`
- `pageSize` — 1 to 100, default `20`
- `sort` — `rating_desc` (default), `rating_asc`, `price_asc`, `price_desc`,
  `name_asc`, `name_desc`

Invalid values return `400` with code `invalid_query`.

## Examples

```bash
curl 'http://localhost:8080/api/v1/provinces'
curl 'http://localhost:8080/api/v1/provinces/siem-reap'
curl 'http://localhost:8080/api/v1/provinces/siem-reap/attractions'

# Search and paginate
curl 'http://localhost:8080/api/v1/attractions?q=angkor&page=1&pageSize=10'

# Cheap places that are open right now
curl 'http://localhost:8080/api/v1/attractions?maxPriceUsd=5&openNow=true&sort=price_asc'

# Highly rated heritage sites
curl 'http://localhost:8080/api/v1/attractions?category=Heritage&minRating=4.5'
```

## Layout

```
cmd/api          API entrypoint, graceful shutdown
cmd/seed         catalog importer
internal/config  env parsing and DSNs
internal/database connection retry and migrations
internal/models  API shapes
internal/repository SQL for provinces and attractions
internal/handler HTTP handlers and query validation
internal/router  route table and 404 envelope
internal/middleware CORS
migrations       golang-migrate SQL files
seeds            destinations.json (same dataset the app ships with)
```

## Database notes

- IDs are kept stable with the Android dataset: province IDs are slugs
  (`siem-reap`) and attraction IDs are numeric strings (`"1"`), so local
  bookmarks and cached data still line up.
- Khmer text needs `utf8mb4`; the schema and DSN both set it.
- `attraction_hours` stores one row per opening range, which covers midday
  breaks such as `08:00 - 11:00, 14:00 - 17:00`. The original display string is
  still kept on `attractions.opening_hours`.
- `price_khr_value` and `price_usd_value` are parsed from the display strings at
  seed time and exist purely for filtering and sorting.
- Tables for users, refresh tokens, reviews, and bookmarks are already in the
  first migration, ready for the auth and review phases.

To roll the schema back: `migrate -path migrations -database "mysql://..." down 1`
(or drop and recreate the database in development).

## Tests

```bash
go test ./...
```

Current coverage is the seed parsing helpers (KHR/USD strings, split hours,
24-hour places). Repository and handler tests need a live database and are not
wired into `go test` yet.
