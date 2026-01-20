# Keycloak Setup for OpenMarket

This guide explains how to set up Keycloak for local development with the OpenMarket application.

## Architecture Overview

- **PostgreSQL**: Single container with two separate databases:
  - `openmarket` database: Used by the OpenMarket API (user: `openmarket_user`)
  - `keycloak` database: Used by Keycloak for IAM data (user: `keycloak_user`)
- **Keycloak**: Runs on port `8180` (mapped from container port 8080)
- **API**: Runs on port `8080`

## Starting the Services

```bash
docker-compose up -d
```

This will start:
1. PostgreSQL on port 5432
2. Keycloak on port 8180 (with `openmarket` realm automatically imported)
3. OpenMarket API on port 8080

## Automatic Configuration

The `openmarket` realm is **automatically imported** on first startup with:
- **Realm**: `openmarket`
- **Client**: `openmarket-api`
  - Client ID: `openmarket-api`
  - Client Secret: `openmarket-api-secret`
- **Roles**: `BUYER` and `SELLER`
- **Token Mappers**: Configured to include roles, email, and name in JWT

You can start using the API immediately without manual Keycloak configuration!

## Access Keycloak Admin Console (Optional)

If you need to manage users, roles, or configuration manually:

- URL: http://localhost:8180
- Username: `admin`
- Password: `admin`
- Realm: `openmarket`

## Manual Configuration (Optional - Only if not using auto-import)

### 1. Create the OpenMarket Realm

1. Click on the dropdown in the top-left (currently showing "master")
2. Click "Create Realm"
3. **Realm name**: `openmarket`
4. Click "Create"

### 2. Create Client for the API

1. Go to **Clients** → Click "Create client"
2. **General Settings**:
   - Client type: `OpenID Connect`
   - Client ID: `openmarket-api`
   - Click "Next"
3. **Capability config**:
   - Client authentication: `ON`
   - Authorization: `OFF`
   - Authentication flow:
     - ✅ Standard flow
     - ✅ Direct access grants
     - ✅ Service accounts roles
   - Click "Next"
4. **Login settings**:
   - Valid redirect URIs: `http://localhost:8080/*`
   - Valid post logout redirect URIs: `http://localhost:8080/*`
   - Web origins: `http://localhost:8080`
   - Click "Save"

### 4. Create Realm Roles

1. Go to **Realm roles** → Click "Create role"
2. Create the following roles:
   - Role name: `BUYER` → Save
   - Role name: `SELLER` → Save

### 5. Configure Client Roles in Token

1. Go to **Clients** → Select `openmarket-api`
2. Go to **Client scopes** tab
3. Click on `openmarket-api-dedicated`
4. Click **Add mapper** → **By configuration** → **User Realm Role**
5. Configure the mapper:
   - Name: `realm-roles`
   - Token Claim Name: `roles`
   - Add to ID token: `ON`
   - Add to access token: `ON`
   - Add to userinfo: `ON`
   - Click "Save"

## User Creation

### Using the API (Recommended)

The easiest way to create users is through the API's `/auth/register` endpoint:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password",
    "name": "Test User"
  }'
```

This will:
1. Create the user in Keycloak
2. Set their password
3. Return the userId

After registration, you can create buyer or seller profiles for this user.

### Via Keycloak UI (Alternative):
1. Go to **Users** → Click "Create new user"
2. Fill in:
   - Username: `test@example.com`
   - Email: `test@example.com`
   - First name: `Test`
   - Last name: `User`
   - Email verified: `ON`
3. Click "Create"
4. Go to **Credentials** tab → Click "Set password"
   - Password: `password`
   - Temporary: `OFF`
   - Click "Save"
5. Go to **Role mapping** tab → Click "Assign role"
   - Select `BUYER` or `SELLER`
   - Click "Assign"

## Getting an Access Token

After creating a user (via `/auth/register` or Keycloak UI), you can get an access token:

### Using Password Grant (Direct Access)

```bash
curl -X POST http://localhost:8180/realms/openmarket/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=openmarket-api" \
  -d "client_secret=openmarket-api-secret" \
  -d "grant_type=password" \
  -d "username=test@example.com" \
  -d "password=password" \
  -d "scope=openid profile email"
```

**Client Credentials:**
- Client ID: `openmarket-api`
- Client Secret: `openmarket-api-secret` (pre-configured in realm import)

## Using the Token with API

```bash
# Get the token
TOKEN=$(curl -s -X POST http://localhost:8180/realms/openmarket/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=openmarket-api" \
  -d "client_secret=openmarket-api-secret" \
  -d "grant_type=password" \
  -d "username=test@example.com" \
  -d "password=password" | jq -r '.access_token')

# Create a buyer profile
curl -X POST http://localhost:8080/profiles/buyer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultShippingAddress": "123 Main St, City, Country"
  }'

# Get your buyer profile
curl -X GET http://localhost:8080/profiles/buyer/me \
  -H "Authorization: Bearer $TOKEN"
```

## Database Separation

The PostgreSQL container has two separate databases:
- **openmarket**: API tables (buyer_profiles, seller_profiles, products, orders, etc.)
  - User: `openmarket_user`
  - Password: `secret`
- **keycloak**: Keycloak tables (managed automatically by Keycloak)
  - User: `keycloak_user`
  - Password: `keycloak_secret`

You can inspect both databases:

```bash
# Connect to OpenMarket database
docker exec -it openmarket-postgres psql -U openmarket_user -d openmarket

# List tables
\dt

# Exit
\q

# Connect to Keycloak database
docker exec -it openmarket-postgres psql -U keycloak_user -d keycloak

# List tables
\dt

# Exit
\q
```

## Troubleshooting

### Keycloak not starting
- Check logs: `docker logs openmarket-keycloak`
- Ensure PostgreSQL is healthy: `docker ps`
- Verify keycloak database exists: `docker exec openmarket-postgres psql -U postgres -c "\l"`
- Check keycloak user can connect: `docker exec openmarket-postgres psql -U keycloak_user -d keycloak -c "SELECT 1"`

### API cannot connect to Keycloak
- Verify Keycloak is accessible from API container
- Check that realm name is `openmarket`
- Verify issuer-uri in application.yml matches Keycloak configuration

### JWT validation errors
- Ensure the realm and client are configured correctly
- Check that the token is not expired
- Verify the issuer-uri matches the Keycloak realm

## Production Considerations

For production environments:
1. Use proper secrets management (not hardcoded passwords)
2. Enable HTTPS for Keycloak
3. Use separate PostgreSQL instances (or at least separate servers) for API and Keycloak
4. Configure proper CORS policies
5. Set up Keycloak behind a reverse proxy
6. Enable Keycloak clustering for high availability
7. Regular backup of both databases
8. Use connection pooling for both databases
9. Monitor database performance and set appropriate resource limits
