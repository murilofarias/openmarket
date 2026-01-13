# API Usage Guide

This guide demonstrates the complete authentication and profile management flow.

## Authentication Flow

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123",
    "name": "John Doe"
  }'
```

**Response:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "User registered successfully. Please login to get your access token."
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "name": "John Doe"
  }
}
```

**Save the tokens:**
```bash
# Extract tokens from response
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "password": "SecurePass123"}')

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.refreshToken')
```

### 3. Use Access Token

Now you can use the access token to make authenticated requests:

```bash
# Create a buyer profile
curl -X POST http://localhost:8080/profiles/buyer \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultShippingAddress": "123 Main Street, New York, NY 10001"
  }'
```

### 4. Refresh Token (when access token expires)

When your access token expires (after 3600 seconds / 1 hour), use the refresh token to get a new one:

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

**Response:** (Same as login response with new tokens)
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "name": "John Doe"
  }
}
```

### 5. Logout

Invalidate your refresh token when logging out:

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "'"$REFRESH_TOKEN"'"
  }'
```

**Response:** HTTP 204 No Content

## Profile Management

### Create Buyer Profile

```bash
curl -X POST http://localhost:8080/profiles/buyer \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultShippingAddress": "123 Main St, City, Country"
  }'
```

**Response:**
```json
{
  "profileId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Get Your Buyer Profile

```bash
curl -X GET http://localhost:8080/profiles/buyer/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "john@example.com",
  "name": "John Doe",
  "defaultShippingAddress": "123 Main St, City, Country",
  "totalOrders": 0,
  "rating": null,
  "createdAt": "2026-01-13T20:00:00"
}
```

### Create Seller Profile

```bash
curl -X POST http://localhost:8080/profiles/seller \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeName": "John'\''s Electronics",
    "storeDescription": "We sell the best electronics at great prices!"
  }'
```

**Response:**
```json
{
  "profileId": "b2c3d4e5-f6a7-8901-bcde-f12345678901"
}
```

### Get Your Seller Profile

```bash
curl -X GET http://localhost:8080/profiles/seller/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Response:**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "email": "john@example.com",
  "name": "John Doe",
  "storeName": "John's Electronics",
  "storeDescription": "We sell the best electronics at great prices!",
  "status": "PENDING_VERIFICATION",
  "rating": null,
  "createdAt": "2026-01-13T20:05:00"
}
```

### Update Shipping Address

```bash
curl -X PATCH http://localhost:8080/profiles/buyer/shipping-address \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '"456 Oak Avenue, Los Angeles, CA 90001"'
```

**Response:** HTTP 204 No Content

### Update Store Information

```bash
curl -X PATCH http://localhost:8080/profiles/seller/store-info \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeName": "John'\''s Premium Electronics",
    "storeDescription": "Premium electronics with warranty and fast shipping!"
  }'
```

**Response:** HTTP 204 No Content

### Get Public Profile (No Authentication Required)

Anyone can view public profiles:

```bash
# Get buyer profile by ID
curl -X GET http://localhost:8080/profiles/buyer/a1b2c3d4-e5f6-7890-abcd-ef1234567890

# Get seller profile by ID
curl -X GET http://localhost:8080/profiles/seller/b2c3d4e5-f6a7-8901-bcde-f12345678901
```

**Note:** Public profiles hide sensitive information like email for privacy.

## Complete Flow Example

Here's a complete script demonstrating the full flow:

```bash
#!/bin/bash

# 1. Register
echo "Registering user..."
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password", "name": "Test User"}'

echo -e "\n\n"

# 2. Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password"}')

echo $LOGIN_RESPONSE | jq '.'

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.refreshToken')

echo -e "\n\n"

# 3. Create buyer profile
echo "Creating buyer profile..."
BUYER_RESPONSE=$(curl -s -X POST http://localhost:8080/profiles/buyer \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"defaultShippingAddress": "123 Test St"}')

echo $BUYER_RESPONSE | jq '.'

echo -e "\n\n"

# 4. Get profile
echo "Getting buyer profile..."
curl -s -X GET http://localhost:8080/profiles/buyer/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.'

echo -e "\n\n"

# 5. Logout
echo "Logging out..."
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}"

echo -e "\n\nDone!"
```

## Error Responses

### Invalid Credentials (Login)

```json
{
  "timestamp": "2026-01-13T20:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Failed to authenticate user",
  "path": "/auth/login"
}
```

### Expired Token

```json
{
  "timestamp": "2026-01-13T20:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token has expired",
  "path": "/profiles/buyer/me"
}
```

### Profile Already Exists

```json
{
  "timestamp": "2026-01-13T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User already has a buyer profile",
  "path": "/profiles/buyer"
}
```

## Tips

1. **Store tokens securely**: Never store tokens in localStorage on web browsers. Use httpOnly cookies or secure storage.

2. **Refresh before expiration**: Implement auto-refresh logic to refresh tokens before they expire.

3. **Handle 401 responses**: When you receive a 401 Unauthorized, try refreshing the token. If refresh fails, redirect to login.

4. **Logout properly**: Always call the logout endpoint to invalidate refresh tokens when users log out.

5. **Use HTTPS in production**: Never transmit tokens over unencrypted connections.
