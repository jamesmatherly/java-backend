# Java Backend

A Spring Boot REST API for a paper trading and investment learning platform. Users can manage simulated stock portfolios, execute trades, and track their progress through structured learning journeys.

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 17 |
| Framework | Spring Boot 3.2.3 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Auth | AWS Cognito (JWT via HttpOnly cookies) |
| Migrations | Liquibase |
| Stock Data | FinnHub API |
| Metrics | Actuator + Prometheus + Grafana |

## Prerequisites

- Docker and Docker Compose
- A [FinnHub](https://finnhub.io) API key
- An AWS Cognito User Pool (configured for `ADMIN_NO_SRP_AUTH`)

## Running with Docker Compose

1. Create a `.env` file in the project root:

    ```env
    POSTGRES_PASSWORD=your_postgres_password
    GRAFANA_PASSWORD=your_grafana_password
    ```

2. Set your FinnHub API key in `src/main/resources/application.properties`:

    ```properties
    finnhub.api.key=your_finnhub_api_key
    ```

3. Build and start all services:

    ```bash
    docker compose up --build
    ```

| Service | URL |
|---|---|
| API | http://localhost:8080 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

## Architecture

```
┌─────────────┐     JWT cookies      ┌──────────────────┐
│   Client    │ ──────────────────► │  Spring Boot API  │
└─────────────┘                      └────────┬─────────┘
                                              │
                    ┌─────────────────────────┼──────────────────────┐
                    │                         │                      │
             ┌──────▼──────┐      ┌──────────▼──────┐    ┌─────────▼────────┐
             │ PostgreSQL  │      │      Redis       │    │   AWS Cognito    │
             │  (primary   │      │  (stock quote    │    │  (auth / user    │
             │   store)    │      │    cache)        │    │   management)    │
             └─────────────┘      └─────────────────┘    └──────────────────┘
                                                                    │
                                                          ┌─────────▼────────┐
                                                          │   FinnHub API    │
                                                          │  (market data)   │
                                                          └──────────────────┘
```

### Data Model

```
User
 └── Portfolio (one-to-many)
      └── Position (one-to-many)
           └── stock_symbol = null  →  cash position
               stock_symbol = "AAPL" →  long position (quantity > 0)
               stock_symbol = "AAPL" →  short position (quantity < 0)

User
 └── LearningJourney (one-to-many)
      └── Step (one-to-many)
           └── Resource (one-to-many)
```

## Authentication

All requests to `/api/**` require a valid Cognito session. Authentication is handled via three HttpOnly cookies set at login:

| Cookie | Contents |
|---|---|
| `accessToken` | Cognito access token (validated on every request) |
| `idToken` | Cognito ID token (used to extract email on first login) |
| `refreshToken` | Cognito refresh token (used by `/auth/refresh`) |

User records are created automatically in the database on the first successful login.

## Trade Operations

Positions track both long and short holdings within a portfolio. One position per portfolio with a `null` stock symbol represents the cash balance.

| Transaction | Direction | Effect |
|---|---|---|
| `BUY_TO_OPEN` | Long | Deducts cash, creates or adds to a long position |
| `SELL_TO_CLOSE` | Long | Reduces long position, credits cash |
| `SELL_TO_OPEN` | Short | Creates or adds to a short position (negative quantity), credits cash |
| `BUY_TO_CLOSE` | Short | Reduces short position, deducts cash |

Limit orders are accepted by the API but deferred — immediate execution only is currently implemented.

## Development

To run locally without Docker, ensure PostgreSQL and Redis are running, then:

```bash
./mvnw spring-boot:run
```

Key configuration is in `src/main/resources/application.properties`. Override any property with an environment variable using Spring's relaxed binding (e.g. `SPRING_DATASOURCE_URL`).

### Observability

Spring Actuator endpoints are available at `/actuator`:

```
/actuator/health
/actuator/metrics
/actuator/prometheus
```

Prometheus scrapes these automatically when running via Docker Compose. Dashboards are provisioned into Grafana from `./grafana/provisioning`.

## API Reference

See [ENDPOINTS.md](ENDPOINTS.md) for the full endpoint reference.
