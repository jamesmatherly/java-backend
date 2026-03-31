# API Endpoints

Base URL: `http://localhost:8080`

All `/api/**` endpoints require authentication via the `accessToken` HttpOnly cookie unless noted otherwise. Responses from authenticated endpoints always include a `ResponseDTO` wrapper where indicated.

### ResponseDTO wrapper

```json
{
  "success": true,
  "count": 1,
  "dataList": [...],
  "errorMessage": null
}
```

---

## Auth

Routes under `/auth/**` are public — no session cookie required.

### `POST /auth/login`

Authenticates with Cognito and sets session cookies. Creates a user record on first login.

**Request body**
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response** `200 OK`
```
"Login successful"
```

Sets three HttpOnly cookies: `accessToken`, `idToken`, `refreshToken`.

---

### `POST /auth/refresh`

Exchanges a refresh token for a new set of session cookies.

**Cookies required** `tokens`

**Response** `200 OK`
```
"Login successful"
```

Replaces the `accessToken`, `idToken`, and `refreshToken` cookies.

---

### `GET /auth/verify`

Returns the currently authenticated user.

**Cookies required** `accessToken`

**Response** `200 OK`
```json
{
  "id": "a1b2c3d4-...",
  "username": "user@example.com",
  "email": "user@example.com"
}
```

---

### `GET /auth/logout`

Clears all session cookies.

**Response** `200 OK`

---

## Users

### `GET /api/users/{id}`

Returns a user by their Cognito ID.

**Response** `200 OK` | `404 Not Found`
```json
{
  "id": "a1b2c3d4-...",
  "username": "user@example.com",
  "email": "user@example.com"
}
```

---

### `GET /api/users/username/{username}`

Looks up a user by username.

**Response** `200 OK` | `404 Not Found`

---

### `GET /api/users/email/{email}`

Looks up a user by email address.

**Response** `200 OK` | `404 Not Found`

---

### `GET /api/users/exists/username/{username}`

Checks whether a username is taken.

**Response** `200 OK`
```json
true
```

---

### `GET /api/users/exists/email/{email}`

Checks whether an email address is registered.

**Response** `200 OK`
```json
false
```

---

## Portfolios

### `GET /api/portfolios/user`

Returns all portfolios for the authenticated user, including positions and computed stats.

**Response** `200 OK` — `ResponseDTO<GetPortfoliosDTO>`

`GetPortfoliosDTO`:
```json
{
  "id": "portfolio-uuid",
  "name": "My Portfolio",
  "balance": 10000.00,
  "value": 12400.00,
  "profit": 2400.00,
  "profitPercent": 24.00,
  "positions": [
    {
      "id": "position-uuid",
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "quantity": 10,
      "entryPrice": 170.00,
      "currentPrice": 182.00,
      "change": 12.00,
      "changePercent": 7.06
    }
  ]
}
```

---

### `POST /api/portfolios`

Creates a new portfolio for the authenticated user. An initial cash position is seeded with the provided value.

**Request body**
```json
{
  "name": "Growth Portfolio",
  "description": "Long-term growth stocks",
  "value": 10000.00
}
```

**Response** `200 OK` — `ResponseDTO<Portfolio>` containing all portfolios for the user after creation.

---

## Positions

### `GET /api/positions?portfolioId={portfolioId}`

Returns all positions in the given portfolio that belong to the authenticated user.

**Query parameters**

| Parameter | Required | Description |
|---|---|---|
| `portfolioId` | Yes | Filter positions by portfolio |

**Response** `200 OK` — `ResponseDTO<Position>`

---

### `POST /api/positions`

Executes a trade. Immediately applies the transaction to the relevant position and cash balance. Returns all positions in the affected portfolio after execution.

**Request body**
```json
{
  "transactionType": "BUY_TO_OPEN",
  "portfolioId": "portfolio-uuid",
  "symbol": "AAPL",
  "quantity": 5,
  "limitPrice": null
}
```

**`transactionType` values**

| Value | Description |
|---|---|
| `BUY_TO_OPEN` | Open or add to a long position; deducts from cash |
| `SELL_TO_CLOSE` | Reduce or close a long position; credits cash |
| `SELL_TO_OPEN` | Open or add to a short position; credits cash |
| `BUY_TO_CLOSE` | Reduce or close a short position; deducts cash |

> **Note:** Setting `limitPrice` queues the order for deferred execution. Limit order processing is not yet implemented — only market (immediate) execution is currently active.

**Response** `200 OK` — `ResponseDTO<Position>` containing all positions in the portfolio after the transaction.

---

## Stocks

### `GET /api/stocks/{symbol}`

Returns current quote data for a stock. Results are cached in Redis for 72 hours.

**Path parameters**

| Parameter | Description |
|---|---|
| `symbol` | Ticker symbol, e.g. `AAPL` |

**Response** `200 OK`
```json
{
  "symbol": "AAPL",
  "name": "Apple Inc.",
  "price": 182.63,
  "change": 1.24,
  "changePercent": 0.68,
  "marketCap": null,
  "high": 183.10,
  "low": 180.95,
  "open": 181.40,
  "previousClose": 181.39
}
```

---

## Learning Journeys

### `GET /api/journeys`

Returns all learning journeys.

**Response** `200 OK`
```json
[
  {
    "id": "journey-uuid",
    "title": "Options Trading Basics",
    "isPrimary": true,
    "steps": [...]
  }
]
```

---

### `GET /api/journeys/{journeyId}`

Returns a single learning journey with its steps and resources.

**Response** `200 OK` | `404 Not Found`

---

### `POST /api/journeys`

Creates a new learning journey.

**Request body**
```json
{
  "title": "Intro to Technical Analysis"
}
```

**Response** `200 OK` — the created `LearningJourney`

---

### `PUT /api/journeys/{journeyId}`

Updates a learning journey.

**Request body**
```json
{
  "title": "Updated Title"
}
```

**Response** `200 OK` — the updated `LearningJourney`

---

### `DELETE /api/journeys/{journeyId}`

Deletes a learning journey and all of its steps and resources.

**Response** `200 OK`
```
"success"
```

---

### `PUT /api/journeys/{journeyId}/primary`

Marks a journey as the primary (featured) journey. Clears the primary flag from any previously primary journey.

**Response** `200 OK` — the updated `LearningJourney`

---

### `POST /api/journeys/{journeyId}/steps`

Adds a step to a learning journey.

**Request body**
```json
{
  "title": "Understanding Candlestick Charts",
  "description": "Learn how to read candlestick patterns",
  "icon": "chart-candlestick",
  "progress": "NOT_STARTED",
  "completed": false,
  "orderIndex": 1,
  "resources": []
}
```

**Response** `200 OK` — the updated `LearningJourney`

---

## Observability

These endpoints are provided by Spring Actuator and are publicly accessible.

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Application and dependency health |
| `GET /actuator/metrics` | Available metric names |
| `GET /actuator/metrics/{name}` | Value for a specific metric |
| `GET /actuator/prometheus` | Prometheus-format metrics scrape endpoint |
