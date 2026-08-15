# ZPlex API

**zplex-api** is a Spring Boot–based backend service that powers the Zplex platform.  
It provides authentication, user management, and integrations with external services like **PostgreSQL**, **Redis**, and
**Google Drive**.

---

## 🚀 Features

- Secure authentication with JWT
- BCrypt password hashing (legacy SHA-256 hashes re-hashed on next login)
- Valkey/Redis brute-force rate-limiter on `/api/auth/login`
- Refresh-token revocation (per-user token version + Valkey access-jti deny-list; logout revokes)
- Admin user bootstrap
- Per-user access control (libraries, rating ceiling, per-title blacklist)
- Server-side watch state (`watch_progress`, `watchlist`, `played`, `playlist`), keyed by username
- PostgreSQL persistence
- Redis caching
- Configurable via environment variables

---

## 🔐 Admin user management

Endpoints under `/api/auth/admin/**` require the `UPDATE_USERS_CAPABILITIES` capability
(`DELETE /users/{username}` requires `DELETE_USERS`). Account creation via
`POST /api/auth/signup` is **admin-only** (same `UPDATE_USERS_CAPABILITIES` capability); the
first admin is provisioned at startup from the `ADMIN_PASSWORD` env. New accounts start with
**no access** (empty libraries, `maxRatingRank=0`, `allowUnrated=false`) until an admin grants it.

| Method & path | Body | Purpose |
|---------------|------|---------|
| `POST /api/auth/signup` | `{ firstName, lastName, username, password }` | Create a new (no-access) account — admin only |
| `GET /api/auth/admin/users` | — | List users with capabilities, library/rating access, and blacklist |
| `PUT /api/auth/admin/users/{username}/capabilities` | `{ capabilities: int[] }` | Set global capabilities |
| `PUT /api/auth/admin/users/{username}/access` | `{ allowedLibraries: int[], maxRatingRank: int, allowUnrated: bool }` | Set library scope + rating ceiling |
| `POST /api/auth/admin/users/{username}/blacklist` | `{ mediaType: SHOW\|MOVIE, tmdbId: int }` | Hide a specific title from the user |
| `DELETE /api/auth/admin/users/{username}/blacklist/{mediaType}/{tmdbId}` | — | Remove a blacklist entry |
| `DELETE /api/auth/admin/users/{username}` | — | Delete a user (admin account protected) |

Library ids: `1 = MOVIES`, `2 = SHOWS`. Rating ranks are served by `GET /api/config`
(`ratingRanks`). Effective visibility = within `allowedLibraries` AND rating rank ≤
`maxRatingRank` (NULL rating governed by `allowUnrated`), minus blacklisted titles.

This is enforced on the browse lists `GET /api/movie` and `GET /api/tvshows`, the
recently-added lists `GET /api/movie/latest` and `GET /api/tvshows/latest`, the daily
suggestions `GET /api/suggestion` and `GET /api/suggestion/search`, and the detail
endpoints `GET /api/movie/{id}`, `GET /api/tvshows/{id}` and its children
(`/seasons`, `/seasons/{seasonId}`): a disallowed library returns an empty response, and
over-rated or blacklisted titles are dropped. Detail endpoints return `404` (indistinguishable
from a missing title) when the title is denied or blacklisted. Suggestion responses are cached
per user (cache key includes an access fingerprint) so access changes take effect without
leaking restricted titles.

`POST /api/auth/login` is protected by a Valkey/Redis fixed-window rate-limiter keyed by
client IP (`X-Forwarded-For` first hop, else remote address). Failed attempts increment the
counter; a successful login resets it. Once the limit is exceeded the endpoint returns `429`
with a `Retry-After` header. Tunable via `zplex.login.rate-limit.max-attempts` (default `10`)
and `zplex.login.rate-limit.window-seconds` (default `900`).

CORS is denied by default. Cross-origin browser clients must be allow-listed via
`zplex.cors.allowed-origins` (comma-separated, e.g. `ZPLEX_CORS_ALLOWED_ORIGINS=https://app.example.com`);
native clients are unaffected.

### Session revocation

Refresh tokens are stored server-side (`refresh_tokens`) and carry a per-user token version.
`POST /api/auth/logout` (authenticated) revokes the caller's current access token via a
Valkey deny-list (`revoked-jti:{jti}`, TTL = remaining token life) and deletes its refresh
token (the one in the request body if provided, otherwise all of the user's).
`POST /api/auth/logout/all` bumps the user's token version, deleting every refresh token and
invalidating all outstanding sessions. `POST /api/auth/refresh` rejects a refresh token whose
version no longer matches the user's current version (`401`).

## Stream grants (`/api/stream`)

`GET /api/stream/grant/{fileId}` requires the `STREAM` capability. The API resolves the file
to its movie or episode, applies the caller's library, parental-rating, and blacklist rules,
then returns a signed HS256 grant valid for approximately two minutes. The grant contains the
file id and caller username and is intended for the stream worker; it is not an API access token.

## 📺 Watch state (`/api/me`)

Per-user, server-side watch state is keyed only by the username from the authenticated JWT
(module `userdata`). These endpoints accept no client-supplied username or user id:

| Endpoint | Body / Response | Description |
|----------|-----------------|-------------|
| `PUT /api/me/progress` | `{ mediaType, tmdbId, seasonNumber?, episodeNumber?, progressMs, durationMs }` | Upsert resume position (season/episode default `0` for movies); auto-marks played at ≥90% |
| `GET /api/me/continue-watching` | `ContinueWatchingItem[]` | In-progress titles (finished items at ≥90% are excluded), newest first |
| `DELETE /api/me/continue-watching/{id}` | — | Dismiss a resumed title (scoped to the caller; `404` if not theirs) |
| `GET /api/me/history` | `ContinueWatchingItem[]` | Full watch history (all titles with a resume position), newest first |
| `GET /api/me/watchlist` | `WatchlistItemResponse[]` | Watchlist, newest first |
| `POST /api/me/watchlist` | `{ mediaType, tmdbId }` | Add a title to the watchlist (idempotent) |
| `DELETE /api/me/watchlist/{mediaType}/{tmdbId}` | — | Remove a title from the watchlist |
| `GET /api/me/played` | `PlayedResponse[]` | Played titles, newest first |
| `POST /api/me/played` | `{ mediaType, tmdbId, seasonNumber?, episodeNumber? }` | Mark a title as played (idempotent) |
| `DELETE /api/me/played/{mediaType}/{tmdbId}?seasonNumber=&episodeNumber=` | — | Unmark a title as played |

### Playlists (`/api/me/playlists`)

User-owned, ordered playlists (tables `playlist`, `playlist_item`), all scoped to the caller:

| Endpoint | Body / Response | Description |
|----------|-----------------|-------------|
| `GET /api/me/playlists` | `PlaylistResponse[]` | List playlists, newest-updated first |
| `POST /api/me/playlists` | `{ name }` → `PlaylistResponse` | Create a playlist (`201`) |
| `GET /api/me/playlists/{playlistId}` | `PlaylistDetailResponse` | Playlist with ordered items (`404` if not theirs) |
| `PUT /api/me/playlists/{playlistId}` | `{ name }` | Rename (`204`/`404`) |
| `DELETE /api/me/playlists/{playlistId}` | — | Delete playlist + its items (`204`/`404`) |
| `POST /api/me/playlists/{playlistId}/items` | `{ mediaType, tmdbId }` | Append a title (idempotent; `204`/`404`) |
| `DELETE /api/me/playlists/{playlistId}/items/{itemId}` | — | Remove a title (`204`/`404`) |
| `PUT /api/me/playlists/{playlistId}/items/order` | `{ itemIds: [...] }` | Reorder items by given id order (`204`/`404`) |

> All `/api/me/**` endpoints require the `VIEW` capability (enforced in `SecurityConfig`).

---

## 📦 Requirements

- Java 21+
- Maven 3.9+
- PostgreSQL
- Redis

---

## ⚙️ Configuration

The application is configured through environment variables and Spring Boot properties.  
Below are the required parameters:

### 🔑 Authentication & Security

| Variable         | Description                                | Example                |
|------------------|--------------------------------------------|------------------------|
| `ADMIN_PASSWORD` | Password for the default admin account.    | `admin123`             |
| `SECRET_KEY`     | 256-bit secret key for signing JWT tokens. | `supersecretkey123...` |

### 🗄 Database (PostgreSQL)

| Variable                  | Description                          | Example                             |
|---------------------------|--------------------------------------|-------------------------------------|
| `ZPLEX_DATABASE_URL`      | Database URL without `jdbc:` prefix. | `postgresql://localhost:5432/zplex` |
| `ZPLEX_DATABASE_USERNAME` | Database username.                   | `zplex_user`                        |
| `ZPLEX_DATABASE_PASSWORD` | Database password.                   | `mypassword`                        |

**Spring Boot Properties (auto-configured in `application.properties`):**

```properties
spring.datasource.url=jdbc:${ZPLEX_DATABASE_URL}?sslmode=require
spring.datasource.username=${ZPLEX_DATABASE_USERNAME}
spring.datasource.password=${ZPLEX_DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
````

### 📂 Streaming from Google Drive

Refer to [zplex-stream](https://github.com/ZPlexLabs/zplex-stream)

| Variable            | Description                               | Example                               |
|---------------------|-------------------------------------------|---------------------------------------|
| `ZPLEX_STREAM_HOST` | Public URL of your deployed zplex-stream. | `https://zplex-stream.**.workers.dev` |

### ⚡ Redis Cache

| Variable         | Description     | Example      |
|------------------|-----------------|--------------|
| `REDIS_HOST`     | Redis hostname. | `redis`      |
| `REDIS_PORT`     | Redis port.     | `6379`       |
| `REDIS_USERNAME` | Redis username. | `default`    |
| `REDIS_PASSWORD` | Redis password. | `mypassword` |

---

## ▶️ Running the Application

### 1. Clone the repo

```bash
git clone https://github.com/<your-username>/zplex-api.git
cd zplex-api
```

### 2. Set environment variables

Create a `.env` file (or export vars manually):

```bash
ADMIN_PASSWORD=sirzechs
SECRET_KEY=supersecretkey256bit
ZPLEX_DATABASE_URL=postgresql://localhost:5432/zplex
ZPLEX_DATABASE_USERNAME=zplex_user
ZPLEX_DATABASE_PASSWORD=mypassword
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=default
REDIS_PASSWORD=mypassword
ZPLEX_STREAM_HOST=https://zplex-stream.**.workers.dev
```

### 3. Build & run

```bash
./mvnw clean package
java -jar api/target/zplex-api-1.0.0.jar --server.port=62942
```

---

## 🛠 Development

Run directly with Maven:

```bash
./mvnw spring-boot:run -pl api
```

---

## 📚 Tech stack & dependencies

Multi-module Maven project (modules: `api`, `auth`, `common`, `config`, `filter-parser`,
`health`, `media`, `movies`, `suggestions`, `tvshows`, `userdata`). Requires **JDK 21** (set
`JAVA_HOME` to a 21 JDK if the system default is older).

| Dependency | Version | Notes |
|------------|---------|-------|
| Spring Boot | **4.1.0** | Upgraded from 3.5.5 → 4.x (Spring Framework 7 / Security 7). Builds clean, no code changes needed. |
| springdoc-openapi | 3.1.0 | Spring Boot 4 compatible line (was 2.8.x for Boot 3) |
| jjwt (io.jsonwebtoken) | 0.13.0 | JWT signing/verification |
| gson | 2.14.0 | JSON serialization |
| antlr4 | 4.13.2 | filter-parser DSL grammar |
| postgresql | managed by Boot BOM (42.7.x) | JDBC driver |

Build gate (no tests in this project): `./mvnw -q -DskipTests package`.

> Note: the `json.version` property in the parent POM is currently unused (no `org.json`
> dependency references it).

---

