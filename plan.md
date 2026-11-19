# Explore KH — Project Plan

Working notes for continuing this project later. Read the "Resume here" section
first; the rest is reference.

Last updated: 17 November 2026

---

## 1. Where the project stands

| Area | Status |
| --- | --- |
| Android UI + local catalog | Done (Phase 1) |
| Backend B1 — skeleton, DB, migrations, health | Done |
| Backend B2 — public catalog API + seed | Done |
| Backend B3 — auth (register/login/JWT/refresh) | Not started |
| Backend B4 — reviews + bookmarks | Not started |
| Backend B5 — sync endpoint + admin CRUD | Not started |
| Backend B6 — docs, deployment, hardening | Not started |
| Android ↔ API integration | Not started (app still uses bundled JSON) |

The app is fully usable offline today. The backend is independently testable
with curl. Nothing is broken; the next step is simply choosing a phase.

---

## 2. Resume here

### 2.1 Quick sanity check (2 minutes)

```bash
# From the repo root
cd backend
go test ./...
go build ./...
```

If you only want to look at the app:

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 2.2 Start the backend locally

The previous session used a throwaway MariaDB in `/tmp/opencode`, which will
not survive a reboot. Either use your system MariaDB or Compose.

**Option A — system MariaDB**

```sql
CREATE DATABASE IF NOT EXISTS explore_kh
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'explore'@'localhost' IDENTIFIED BY 'explore_dev';
GRANT ALL PRIVILEGES ON explore_kh.* TO 'explore'@'localhost';
FLUSH PRIVILEGES;
```

```bash
cd backend
cp .env.example .env        # only the first time; edit DB_* if needed
go run ./cmd/api            # migrations run automatically, serves :8080
go run ./cmd/seed -file seeds/destinations.json   # one-time catalog load
```

**Option B — Docker Compose** (preferred if the plugin is available)

```bash
cd backend
docker compose up --build
docker compose exec api ./seed -file seeds/destinations.json
# API: http://localhost:8081   Adminer: http://localhost:8080
```

Note: this machine had Docker but no `docker compose` plugin, so the Compose
file is written but has never actually been executed. Verify it on a machine
that has Compose before relying on it.

### 2.3 Start the Android app

Open the repo root in Android Studio and run `app`. No backend is required.
For a command-line build:

```bash
./gradlew :app:assembleDebug
```

### 2.4 Recommended next phase

Do **Android ↔ API integration** before B3. Reason: it closes the loop on what
already exists (B1+B2), forces us to settle networking/error/loading patterns,
and makes the auth and bookmark phases easier to test from the UI. If the
assignment requires backend-first ordering, do B3 then integration.

---

## 3. Completed work

### 3.1 Android (Phase 1)

- Package: `com.example.android.cambodia25`
- Jetpack Compose, Material 3, single-activity navigation coordinator in
  `ui/screen/MainScreen.kt`
- Screens: home, province detail, discover, attraction detail, saved, profile
- 25 provinces and 50 attractions loaded from
  `app/src/main/assets/destinations.json`
- Bilingual English/Khmer text, coordinates, prices (KHR + USD), opening hours,
  ratings, and remote Wikimedia image URLs
- Coil image loading via a shared `DestinationImage` component
- Bookmark/save state is in-memory Compose state (not persisted yet)
- Directions hand-off via an external map intent
- APK builds and is packaged with the catalog asset

Key files:

- `app/src/main/assets/destinations.json` — catalog (single source for the app)
- `app/src/main/java/.../data/ExploreCatalog.kt` — JSON loader
- `app/src/main/java/.../ui/screen/MainScreen.kt` — navigation/state only
- `app/src/main/java/.../ui/screen/ExploreScreens.kt` — screen composables

### 3.2 Backend B1

- Go 1.25+ / Gin, module `github.com/explorekh/backend`
- Env-driven config (`internal/config`), `.env` support
- `sqlx` + explicit SQL (no ORM)
- Connection retry with pooling (`internal/database`)
- `golang-migrate` runner on startup (`RUN_MIGRATIONS=true`)
- `GET /health` (root and `/api/v1/health`) with DB ping
- CORS middleware, JSON 404 envelope, graceful shutdown
- Dockerfile (multi-stage, `golang:1.26-alpine`), Compose file, Makefile

### 3.3 Backend B2

- Seed command (`cmd/seed`) — idempotent upserts, catalog version bump
- Parsing helpers (`internal/seed/parser.go`) with unit tests:
  - `1,000 KHR` → `1000`
  - `$37.00` → `37`
  - `08:00 - 11:00, 14:00 - 17:00` → two ranges
  - `24 Hours` → `is_24_hours = true`
- Public endpoints under `/api/v1`:
  - `GET /provinces`
  - `GET /provinces/:id`
  - `GET /provinces/:id/attractions`
  - `GET /attractions`
  - `GET /attractions/:id`
  - `GET /categories`
- Attraction query params: `q`, `provinceId`, `category`, `minRating`,
  `maxPriceUsd`, `openNow`, `page`, `pageSize`, `sort`
- Sort options: `rating_desc` (default), `rating_asc`, `price_asc`,
  `price_desc`, `name_asc`, `name_desc`
- Response shapes: `{"data": ...}` and `{"data": [...], "meta": {...}}` for lists
- Errors: `{"error": {"code": "...", "message": "..."}}`
- Verified data: 25 provinces, 50 attractions, 45 hour ranges

---

## 4. Architecture and conventions

### 4.1 Decisions already made (keep these)

- Explicit SQL via `sqlx`, not GORM. Queries live in `internal/repository`.
- IDs stay identical to the Android dataset so cached data and bookmarks line up:
  - province IDs are slugs (`siem-reap`)
  - attraction IDs are numeric strings (`"1"`)
- `golang-migrate` for schema changes; never edit an applied migration, add a new
  numbered pair (`000002_...up.sql` / `.down.sql`).
- Opening hours are stored as one row per range in `attraction_hours` to support
  midday breaks. The display string is still kept on `attractions.opening_hours`.
- Numeric price columns (`price_khr_value`, `price_usd_value`) exist for filter
  and sort; display strings remain the source of truth for UI.
- API JSON field names mirror the Android model (`nameEn`, `titleEn`,
  `imageUrl`, `provinceId`, ...), so the app can map directly.
- Timezone is `Asia/Phnom_Penh`; `openNow` is computed against it.
- Khmer text everywhere → `utf8mb4` in schema, DSN, and client.

### 4.2 Repo layout

```
app/                                  Android app (Compose)
backend/
  cmd/api/                            API entrypoint
  cmd/seed/                           catalog importer
  internal/config/                    env parsing, DSNs
  internal/database/                  connect, retry, migrate
  internal/models/                    API structs
  internal/repository/                province + attraction SQL
  internal/handler/                   handlers, query validation
  internal/middleware/                CORS
  internal/router/                    routes, 404
  internal/seed/                      parser + tests
  migrations/                         golang-migrate files
  seeds/destinations.json             backend copy of the catalog
backend/README.md                     backend-specific docs
```

### 4.3 Current schema tables

`provinces`, `attractions`, `attraction_hours`, `users`, `refresh_tokens`,
`reviews`, `bookmarks`, `catalog_versions`.

The user/review/bookmark tables already exist and are unused — B3/B4 just need
handlers and repositories, not new migrations (except any column tweaks).

---

## 5. Remaining work

Each phase below should end with: `gofmt`, `go vet ./...`, `go test ./...`,
`go build ./...`, and a curl pass over the new endpoints.

### Phase A — Android ↔ API integration

Goal: the app reads the catalog from the API, with the bundled JSON as offline
fallback.

Tasks:

1. Add Retrofit + OkHttp + kotlinx.serialization (or Moshi) to the app.
2. Create `data/remote/` with DTOs matching the API JSON and a `CatalogApi`
   interface.
3. Base URL per build type:
   - debug emulator: `http://10.0.2.2:8080/`
   - physical device: machine LAN IP
   - release: HTTPS placeholder
4. Allow cleartext for local debug only — `network_security_config.xml` scoped
   to `10.0.2.2` / local IPs, or `usesCleartextTraffic` in the debug manifest.
5. Repository layer: try network, fall back to `destinations.json` on failure,
   expose loading/error/empty states.
6. Replace direct `ExploreCatalog` reads with the repository while keeping the
   existing model shape (avoid touching UI composables).
7. Cache the last successful response (simple file cache first; Room comes in a
   later phase if required).
8. Show a small offline/refresh indicator so failures are not silent.

Done when: with the API up, the app shows server data; with it down, the app
still opens with bundled data and no crash.

Gotchas:

- `127.0.0.1` inside the emulator is the emulator itself → use `10.0.2.2`.
- The API binds to all interfaces already (`HTTP_ADDR=:8080`), so LAN access
  works if the firewall allows it.
- Android 9+ blocks cleartext HTTP by default.

### Phase B3 — Auth

Tasks:

1. Add `internal/auth`: password hashing (bcrypt or argon2id), JWT signing,
   refresh token generation/hashing.
2. Add config: `JWT_SECRET`, access TTL, refresh TTL.
3. Endpoints:
   - `POST /api/v1/auth/register`
   - `POST /api/v1/auth/login`
   - `POST /api/v1/auth/refresh`
   - `POST /api/v1/auth/logout`
   - `GET /api/v1/me`
4. Middleware: parse `Authorization: Bearer`, load user, set in context.
5. Store refresh tokens hashed in `refresh_tokens`; support revocation.
6. Validation: email format, password length, duplicate email → 409.
7. Never return `password_hash`; keep the existing error envelope.

Done when: register → login → call `/me` → refresh → logout all work, and a
missing/invalid token yields 401.

### Phase B4 — Reviews and bookmarks

Tasks:

1. Reviews: list per attraction (`GET /attractions/:id/reviews`), create/update
   own review (`POST`/`PUT`), delete own review. One review per user per
   attraction (unique key already exists). Recompute the attraction's displayed
   rating or return an aggregate alongside.
2. Bookmarks: `GET /me/bookmarks`, `POST /me/bookmarks/:attractionId`,
   `DELETE /me/bookmarks/:attractionId`, optional notes.
3. All write endpoints require auth and return 403 for other users' rows.
4. Add pagination to review lists.

Done when: two users can review/bookmark independently, duplicates are
rejected cleanly, and the catalog rating reflects stored reviews (or is clearly
documented as the seed rating).

### Phase B5 — Sync and admin

Tasks:

1. `GET /api/v1/catalog/version` returning the `catalog_versions` value.
2. `GET /api/v1/catalog/sync?since=<version>` returning changed provinces and
   attractions, or a compact snapshot. Keep the payload shaped like the list
   endpoints so the app can reuse DTOs.
3. Bump `catalog_versions` inside the seed command and any admin write.
4. Admin-only endpoints (`role = 'admin'`):
   - `POST/PUT/DELETE /api/v1/admin/provinces`
   - `POST/PUT/DELETE /api/v1/admin/attractions`
   - enforce hour-range updates and numeric price recomputation
5. Android: periodic sync via WorkManager (only if the assignment requires it).

Done when: version changes after a seed, sync returns only new/changed rows, and
non-admins get 403 on admin routes.

### Phase B6 — Docs, tests, deployment

Tasks:

1. Handler tests with `httptest` against a seeded test DB (or sqlmock for pure
   validation cases).
2. Repository integration tests behind a build tag (`-tags=integration`).
3. Request logging/request IDs, structured logs.
4. Rate limiting on auth endpoints.
5. Production config: non-root DB user, real `JWT_SECRET`, exact CORS origins,
   HTTPS behind a reverse proxy.
6. Update `backend/README.md` and this file; add a deploy section.
7. Optional: CI (GitHub Actions) running `go vet`, `go test`, `go build`, and
   the Gradle debug build.

---

## 6. Verification playbook

### Backend (endpoint smoke test)

```bash
BASE=http://127.0.0.1:8080/api/v1

curl -s http://127.0.0.1:8080/health
curl -s $BASE/provinces | jq '.data | length'          # 25
curl -s $BASE/provinces/siem-reap | jq '.data.nameKh'
curl -s $BASE/provinces/siem-reap/attractions | jq '.data | length'
curl -s "$BASE/attractions?page=1&pageSize=5" | jq '.meta'
curl -s "$BASE/attractions?q=angkor" | jq '.data[].titleEn'
curl -s "$BASE/attractions?category=Heritage&minRating=4.5" | jq '.data | length'
curl -s "$BASE/attractions?maxPriceUsd=5&openNow=true&sort=price_asc" | jq '.data[].titleEn'
curl -s $BASE/categories | jq '.data'
curl -s -o /dev/null -w '%{http_code}\n' $BASE/attractions/9999   # 404
curl -s -o /dev/null -w '%{http_code}\n' "$BASE/attractions?page=0" # 400
```

Expected seed totals: **25** provinces, **50** attractions, **45** opening-hour
ranges, **10** pages at `pageSize=5`.

### Android

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
unzip -l app/build/outputs/apk/debug/app-debug.apk | grep destinations.json
```

Manual checks: home lists provinces, search filters, province detail opens,
attraction detail shows image/price/hours, save toggles, directions intent
opens a map app.

---

## 7. Known issues / open questions

- Docker Compose has not been executed here (no Compose plugin). Verify before
  shipping.
- `go.mod` says `go 1.25.11` while the Dockerfile uses `golang:1.26-alpine`.
  Fine today, but pin both to the same version for reproducibility.
- The Android save/bookmark state is in-memory only; it resets on process death.
  Persistence (Room/DataStore or backend bookmarks) is still TODO.
- Remote images come from Wikimedia URLs; some are duplicated across
  destinations. Attribution notice in the root `README.md` should be kept and
  reviewed before publishing.
- Search uses `LIKE` with a `%...%` pattern. A FULLTEXT index exists but is not
  used yet; revisit if search must scale.
- `openNow` assumes `attraction_hours` is complete. Attractions without parsed
  ranges are never "open now".
- No request IDs or structured logging yet; logs are plain Gin output.

---

## 8. Environment notes from the last session

- Temporary MariaDB 11.8 ran on port **3307**, socket
  `/tmp/opencode/mariadb-explore.sock`, datadir
  `/tmp/opencode/mariadb-explore`. Ephemeral — recreate or use system MariaDB.
- Temporary API ran on **port 8091** to avoid clashing with other services.
- Test env:
  ```bash
  DB_HOST=127.0.0.1 DB_PORT=3307 DB_USER=explore \
  DB_PASSWORD=explore_dev DB_NAME=explore_kh \
  go run ./cmd/api
  ```
- Stop leftovers:
  ```bash
  pkill -f 'cmd/api'
  mariadb-admin --socket=/tmp/opencode/mariadb-explore.sock -uroot shutdown
  ```
- Go 1.26.10, MariaDB 11.8.8, Docker 29.8.2 (no Compose), Podman available.
- Android SDK present; Gradle wrapper checked in.

---

## 9. Checklist for the next session

- [ ] Decide: integration first, or B3 auth first.
- [ ] Get MariaDB + API running (`go run ./cmd/api`, then seed if the DB is new).
- [ ] Confirm the smoke test above passes.
- [ ] Pick exactly one phase from section 5 and finish it end to end.
- [ ] Update this file: flip the status table, add notes and any new gotchas.
- [ ] Commit (this directory is not a git repo yet — consider `git init` and a
      first commit before large changes).
