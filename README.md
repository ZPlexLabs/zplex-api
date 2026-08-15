# ZPlex API

**zplex-api** is a Spring Boot–based backend service that powers the Zplex platform.  
It provides authentication, user management, and integrations with external services like **PostgreSQL**, **Redis**, and
**Google Drive**.

---

## 🚀 Features

- Secure authentication with JWT
- Admin user bootstrap
- Per-user access control (libraries, rating ceiling, per-title blacklist)
- PostgreSQL persistence
- Redis caching
- Configurable via environment variables

---

## 🔐 Admin user management

Endpoints under `/api/auth/admin/**` require the `UPDATE_USERS_CAPABILITIES` capability
(`DELETE /users/{username}` requires `DELETE_USERS`):

| Method & path | Body | Purpose |
|---------------|------|---------|
| `GET /api/auth/admin/users` | — | List users with capabilities, library/rating access, and blacklist |
| `PUT /api/auth/admin/users/{username}/capabilities` | `{ capabilities: int[] }` | Set global capabilities |
| `PUT /api/auth/admin/users/{username}/access` | `{ allowedLibraries: int[], maxRatingRank: int, allowUnrated: bool }` | Set library scope + rating ceiling |
| `POST /api/auth/admin/users/{username}/blacklist` | `{ mediaType: SHOW\|MOVIE, tmdbId: int }` | Hide a specific title from the user |
| `DELETE /api/auth/admin/users/{username}/blacklist/{mediaType}/{tmdbId}` | — | Remove a blacklist entry |
| `DELETE /api/auth/admin/users/{username}` | — | Delete a user (admin account protected) |

Library ids: `1 = MOVIES`, `2 = SHOWS`. Rating ranks are served by `GET /api/config`
(`ratingRanks`). Effective visibility = within `allowedLibraries` AND rating rank ≤
`maxRatingRank` (NULL rating governed by `allowUnrated`), minus blacklisted titles.

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
`health`, `media`, `movies`, `suggestions`, `tvshows`). Requires **JDK 21** (set
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

