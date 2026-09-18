# Customer Rewards API

## Project structure

- `domain`: `Customer`, `PurchaseTransaction`, `Role`
- `repository`: JPA repositories for customers and purchases
- `service`: reward calculation, aggregation, and transaction services
- `controller`: authentication, customer transactions, and rewards reports
- `security`: JWT authentication, authorization, and rate limiting
- `exception`: centralized JSON error handling
- `config`: deterministic H2 startup seed data
- `test`: reward, security, controller, and rate-limit tests
- `OpenApiConfig`: Swagger/OpenAPI metadata and JWT bearer authentication scheme
- `RewardTierProperties`: validated, extensible reward thresholds and rates loaded from configuration

## API behavior

- Reward formula:
  - `$0–$50`: 0 points
  - `$50–$100`: 1 point per dollar over $50
  - Above `$100`: 2 points per dollar over $100
  - `$120` = `50 + 40` = **90 points**
- Whole-dollar calculation for amounts with cents.
- Reward tiers are configurable and extensible in `application.properties`:

```properties
rewards.tiers[0].threshold=0
rewards.tiers[0].rate=0
rewards.tiers[1].threshold=50
rewards.tiers[1].rate=1
rewards.tiers[2].threshold=100
rewards.tiers[2].rate=2
```

The first entry is the zero-point baseline. Add another matching threshold/rate pair to introduce a future tier, for example:

```properties
rewards.tiers[3].threshold=500
rewards.tiers[3].rate=3
```

The tiers are progressive. For example, a `$120` purchase earns `(100 - 50) * 1 + (120 - 100) * 2 = 90` points. The application fails during startup if thresholds are not strictly increasing, the baseline is missing, or rates are negative.
- Monthly and total reward reports.
- Customers can access their own data.
- Admins can access any customer.
- JWT login at:

```http
POST /api/auth/login
```

Seed accounts:

```text
admin / password
alice / password
bob / password
```

Protected endpoints include:

```http
GET /api/rewards/{username}/total
GET /api/rewards/{username}/monthly
GET /api/rewards/{username}/monthly/all
GET /api/customers/{username}/transactions
POST /api/customers/{username}/transactions
```

The default Spring Security form-login page is replaced with stateless JWT authentication. API errors return JSON, invalid requests are validated, and protected API requests are limited to 100 requests per minute per client IP with HTTP `429` responses. Rate limiting is configured with `app.rate-limit.*`; forwarded client addresses are only accepted when the immediate proxy is explicitly trusted. Local buckets are bounded and expire to prevent unbounded memory growth.

The current limiter intentionally uses local Bucket4j state. Before running multiple API instances, migrate the bucket store to a shared Redis-backed Bucket4j proxy (or equivalent shared backend) so limits are consistent across instances. Redis runtime dependencies are deliberately not included in this change.

Transaction creation requires an `Idempotency-Key` header. A retry with the same customer, key, amount, and date returns the original transaction; reusing a key with a different payload returns `409 Conflict`. Transaction history is paginated with `page` (default `0`) and `size` (default `20`, maximum `100`).

## Swagger API documentation

Start the application and open:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI JSON document is available at:

```text
http://localhost:8080/v3/api-docs
```

Click **Authorize** in Swagger UI and enter the JWT returned by `/api/auth/login` using the format:

```text
Bearer <your-jwt-token>
```

Swagger UI and the OpenAPI document are publicly accessible; protected business endpoints still require a valid JWT.

The Maven test suite includes focused rate-limit, pagination, idempotency, audit, and OpenAPI coverage.
