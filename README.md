# OpenMarket

A marketplace application built with Spring Boot and Keycloak for authentication.

## Architecture

- **Authentication**: Keycloak (OAuth2/JWT)
- **Database**: PostgreSQL container with two separate databases:
  - `openmarket`: Application data (profiles, products, orders) - user: `openmarket_user`
  - `keycloak`: Keycloak data (users, roles, clients) - user: `keycloak_user`
- **API**: Spring Boot application

## Quick Start

### 1. Start all services

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port 5432
- Keycloak on port 8180 (realm auto-imported)
- OpenMarket API on port 8080

The `openmarket` realm is automatically configured with:
- Client: `openmarket-api` (secret: `openmarket-api-secret`)
- Roles: `BUYER` and `SELLER`

### 2. Test the API

See [API_USAGE.md](API_USAGE.md) for complete API documentation with examples.

**Quick Start:**

```bash
# 1. Register a new user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@example.com",
    "password": "password",
    "name": "John Doe"
  }'

# 2. Login to get access token
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@example.com",
    "password": "password"
  }')

# Extract access token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')

# 3. Create buyer profile
curl -X POST http://localhost:8080/profiles/buyer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"defaultShippingAddress": "123 Main St"}'

# 4. Get your profile
curl -X GET http://localhost:8080/profiles/buyer/me \
  -H "Authorization: Bearer $TOKEN"
```

## Development

### Compile application

```bash
./mvnw clean package
```

### Run with Docker Compose

```bash
./run.sh
```

### Run locally (without Docker)

1. Start PostgreSQL and Keycloak:
```bash
docker-compose up -d postgres keycloak
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

## API Endpoints

### Authentication (Public)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get access token
- `POST /auth/refresh` - Refresh access token using refresh token

### Authentication (Protected)
- `POST /auth/logout` - Logout and invalidate refresh token (requires token)

### Profiles (Public)
- `GET /profiles/buyer/{id}` - Get public buyer profile
- `GET /profiles/seller/{id}` - Get public seller profile

### Profiles (Protected - require JWT token)
- `POST /profiles/buyer` - Create buyer profile
- `POST /profiles/seller` - Create seller profile
- `GET /profiles/buyer/me` - Get your buyer profile
- `GET /profiles/seller/me` - Get your seller profile
- `PATCH /profiles/buyer/shipping-address` - Update shipping address
- `PATCH /profiles/seller/store-info` - Update store information

## Roadmap

1. ✅ Integrate Keycloak for authentication
2. ✅ Separate Account entity and use Profiles with userId
4. Encapsulate all the authorization operations in AuthorizationService
5. Create command again to encapsulate the domain logic
6. Add search products feature
7. Add createOrder feature
8. Add payment integration
9. Add order tracking
10. Add seller approval workflow