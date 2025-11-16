# Authentication API

**Base URL**: `https://api.MyApp.com/v1/auth`

**Status**: 🚧 Mock endpoints - Real API not yet implemented

---

## Endpoints

### POST /auth/register

Register a new user account.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

**Response** (201 Created):
```json
{
  "user": {
    "id": "user_123abc",
    "email": "user@example.com",
    "name": "John Doe",
    "createdAt": "2025-10-15T10:30:00Z"
  },
  "tokens": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

**Error Responses**:
- `400 Bad Request` - Invalid email or weak password
- `409 Conflict` - Email already registered

---

### POST /auth/login

Authenticate existing user.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "user": {
    "id": "user_123abc",
    "email": "user@example.com",
    "name": "John Doe"
  },
  "tokens": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

**Error Responses**:
- `401 Unauthorized` - Invalid credentials
- `429 Too Many Requests` - Rate limit exceeded

---

### POST /auth/refresh

Refresh access token using refresh token.

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

**Error Responses**:
- `401 Unauthorized` - Invalid or expired refresh token
- `403 Forbidden` - Refresh token revoked

**Implementation Notes**:
- Refresh tokens should rotate on each refresh
- Old refresh token is invalidated after successful refresh
- Access tokens expire in 60 minutes
- Refresh tokens expire in 30 days

---

### POST /auth/logout

Invalidate refresh token.

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (204 No Content)

**Error Responses**:
- `401 Unauthorized` - Invalid access token

---

### POST /auth/guest

Create a guest account (no email/password).

**Request Body**:
```json
{
  "deviceId": "device_abc123"
}
```

**Response** (201 Created):
```json
{
  "user": {
    "id": "guest_xyz789",
    "isGuest": true,
    "createdAt": "2025-10-15T10:30:00Z"
  },
  "tokens": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

---

### POST /auth/guest/convert

Convert guest account to full account.

**Headers**:
```
Authorization: Bearer {accessToken}
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

**Response** (200 OK):
```json
{
  "user": {
    "id": "user_123abc",
    "email": "user@example.com",
    "name": "John Doe",
    "isGuest": false
  }
}
```

**Error Responses**:
- `400 Bad Request` - Not a guest account
- `409 Conflict` - Email already registered

---

## Security Considerations

### Token Storage
- Access tokens: Short-lived (60 min), stored in memory
- Refresh tokens: Long-lived (30 days), stored in `EncryptedSharedPreferences`

### Token Refresh Flow
1. Client makes API request with access token
2. If 401, automatically call `/auth/refresh` with refresh token
3. Retry original request with new access token
4. If refresh fails, logout user

See `ADR-003-token-refresh-strategy.md` for implementation details.

### Rate Limiting
- Login: 5 attempts per 15 minutes per IP
- Register: 3 attempts per hour per IP
- Refresh: 10 attempts per minute per user

### Password Requirements
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character

---

## JWT Token Structure

### Access Token Claims
```json
{
  "sub": "user_123abc",
  "email": "user@example.com",
  "type": "access",
  "iat": 1697368200,
  "exp": 1697371800
}
```

### Refresh Token Claims
```json
{
  "sub": "user_123abc",
  "type": "refresh",
  "iat": 1697368200,
  "exp": 1699960200
}
```

---

## Frontend Implementation

Located in:
- `:core:domain/usecase/LoginUseCase.kt`
- `:core:domain/usecase/RegisterUseCase.kt`
- `:core:data/repository/AuthRepositoryImpl.kt`
- `:core:network/interceptor/TokenAuthenticator.kt`

