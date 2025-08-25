# ZPlex API

**zplex-api** is a Spring Boot–based backend service that powers the Zplex platform.  
It provides authentication, user management, and integrations with external services like **PostgreSQL**, **Redis**, and
**Google Drive**.

---

## 🚀 Features

- Secure authentication with JWT
- Admin user bootstrap
- PostgreSQL persistence
- Redis caching
- Google Drive integration for storage
- Configurable via environment variables

---

## 📦 Requirements

- Java 21+
- Maven 3.9+
- PostgreSQL
- Redis
- Google Cloud service account (for Drive integration)

---

## ⚙️ Configuration

The application is configured through environment variables and Spring Boot properties.  
Below are the required parameters:

### 🔑 Authentication & Security

| Variable         | Description                                | Example                |
|------------------|--------------------------------------------|------------------------|
| `ADMIN_PASSWORD` | Password for the default admin account.    | `sirzechs`             |
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

### 📂 Google Drive Integration

| Variable                         | Description                        | Example                                 |
|----------------------------------|------------------------------------|-----------------------------------------|
| `GOOGLE_DRIVE_CLIENT_EMAIL`      | Client email from service account. | `zplex@project.iam.gserviceaccount.com` |
| `GOOGLE_DRIVE_CLIENT_ID`         | Google API client ID.              | `123456789.apps.googleusercontent.com`  |
| `GOOGLE_DRIVE_PRIVATE_KEY_ID`    | Service account private key ID.    | `abc1234567890`                         |
| `GOOGLE_DRIVE_PRIVATE_KEY_PKCS8` | Base64-encoded PKCS8 private key.  | `-----BEGIN PRIVATE KEY----- ...`       |

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
GOOGLE_DRIVE_CLIENT_EMAIL=...
GOOGLE_DRIVE_CLIENT_ID=...
GOOGLE_DRIVE_PRIVATE_KEY_ID=...
GOOGLE_DRIVE_PRIVATE_KEY_PKCS8=...
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
