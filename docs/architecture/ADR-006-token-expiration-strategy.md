# ADR-006: Token Expiration Strategy (Proactive Refresh in Network Layer)

**Status:** Accepted
**Date:** 2025-01-19
**Deciders:** Engineering Team
**Related:** ADR-003 (Token Refresh Strategy), ADR-005 (Encrypted Token Storage)

---

## Context

The app uses JWT tokens for authentication with the following characteristics:
- **Access tokens** expire relatively quickly (e.g., 15 minutes)
- **Refresh tokens** have longer lifespan (e.g., 7 days)
- Server returns 401 Unauthorized when access token is expired
- `TokenAuthenticator` can automatically refresh expired tokens

We need to decide:
1. **Where** should token expiration checking logic live?
2. **When** should we check expiration - proactively or reactively?

### Architectural Layers
```
:core:domain       - Business logic, domain models
:core:network      - HTTP, OkHttp, Retrofit, interceptors
:core:data         - Repositories, data sources
:core:datastore    - Encrypted token storage
```

### Key Question
Should we check if a token is expired **before** sending a request, or only **after** receiving a 401?

---

## Decision

### 1. Token Expiration Logic Lives in Network Layer

**Decision:** Token expiration checking is implemented in `:core:network` (`AuthInterceptor`), NOT in `:core:domain` (`AuthTokens`).

**Rationale:**
- Token expiration is a **network optimization concern**, not domain logic
- Domain models should be pure data (no infrastructure concerns)
- Prevents `:core:network` from depending on `:core:domain` (wrong direction)

**Implementation:**
```kotlin
// ✅ network/config/AuthConfig.kt
object AuthConfig {
    const val TOKEN_EXPIRATION_BUFFER_MS = 5 * 60 * 1000L // 5 minutes
}

// ✅ network/interceptor/AuthInterceptor.kt
private fun isTokenExpired(): Boolean {
    val issuedAt = encryptedAuthStorage.getIssuedAt()
    val lifespan = encryptedAuthStorage.getAccessTokenLifespan()
    val elapsed = SystemClock.elapsedRealtime() - issuedAt
    return elapsed >= (lifespan - AuthConfig.TOKEN_EXPIRATION_BUFFER_MS)
}

// ✅ domain/model/AuthTokens.kt (pure data)
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val issuedAt: Long,
    val accessTokenLifespanMs: Long,
    val refreshTokenLifespanMs: Long
)
```

### 2. Use Proactive Refresh Strategy

**Decision:** Check token expiration **before** sending request (proactive), not only after 401 (reactive).

**Rationale:**
- **Performance:** Saves one network round trip (~200-300ms on typical networks)
- **UX:** Users see data faster, especially on first request after long inactivity
- **Server load:** Fewer failed requests to process
- **Cleaner logs:** 401s only indicate real auth failures, not routine expiration

**Flow Comparison:**

#### Reactive Only (Not Chosen)
```
User Action → Request (expired token) → 401 → Refresh → Retry → Success
              └─────────────────────────────┘
                    3 round trips
                    ~600ms on typical network
```

#### Proactive (Chosen)
```
User Action → Check expiration → Refresh → Request → Success
              └───────────────────────────────────┘
                    2 round trips
                    ~400ms on typical network
                    33% faster! ✅
```

### 3. Use 5-Minute Buffer Window

**Decision:** Refresh tokens 5 minutes **before** actual expiration.

**Rationale:**
- Prevents tokens expiring mid-request (network latency + server processing time)
- Industry standard (OAuth2 libraries use 3-5 minute buffers)
- For 15-minute access tokens: refresh at 10-minute mark
- Leaves 5 minutes for refresh operation to complete

---

## Consequences

### Positive ✅

1. **Performance Gain**
   - Saves 200-300ms on typical networks (33% reduction)
   - On 3G/poor signal: saves 500-1000ms (significant UX improvement)
   - Batch operations benefit most (10 concurrent requests → refresh once)

2. **Clean Architecture**
   - Domain models remain pure data (no infrastructure concerns)
   - Network layer owns network optimizations
   - No circular dependencies between modules

3. **Better Observability**
   - Server logs: 401s indicate real auth failures, not routine expiration
   - Easier to monitor authentication issues vs. normal token lifecycle

4. **Industry Standard**
   - Follows OAuth2 best practices
   - Same approach as Google, Facebook, Twitter SDKs

### Negative ⚠️

1. **Slightly More Complex Code**
   - ~20 extra lines for expiration checking
   - Need to cache token metadata (issuedAt, lifespan)
   - Trade-off: complexity vs. performance (we chose performance)

2. **Token Metadata Storage Required**
   - Must store `issuedAt`, `accessTokenLifespanMs`, `refreshTokenLifespanMs`
   - In-memory cache needed for synchronous access from OkHttp
   - Additional 24 bytes per token (negligible overhead)

3. **Buffer Window Can Waste Tokens**
   - If user closes app at 9 minutes, token expires unused
   - Acceptable: server-side validation is ultimate source of truth

### Neutral ℹ️

1. **Clock Synchronization**
   - Uses `SystemClock.elapsedRealtime()` (monotonic, immune to time changes)
   - Not affected by user changing device time or timezone
   - Server still validates all tokens (client-side check is optimization only)

---

## Alternatives Considered

### Alternative 1: Reactive-Only (No Proactive Check) ❌

**Approach:** Only refresh on 401, don't check expiration before sending request.

**Pros:**
- Simpler code (delete `isTokenExpired()` method)
- Fewer lines to maintain
- Still functionally correct

**Cons:**
- Extra network round trip on first request after expiration
- 200-300ms slower (33% performance degradation)
- Server logs filled with routine 401 errors

**Why Rejected:** Performance impact outweighs simplicity gain. Users notice 200ms delays.

---

### Alternative 2: Expiration Logic in Domain Model ❌

**Approach:** Add `isAccessTokenExpired()` method to `AuthTokens` class.

**Pros:**
- Expiration logic co-located with token data
- Reusable across layers

**Cons:**
- Violates Single Responsibility Principle (domain model shouldn't know about HTTP)
- Creates wrong dependency direction (network → domain)
- No actual use case for domain layer to check expiration

**Why Rejected:** Architectural smell. Domain models should be pure data.

---

### Alternative 3: Expiration Logic in Use Cases ❌

**Approach:** Check expiration in `LoginUseCase`, `RefreshTokenUseCase`, etc.

**Pros:**
- Use cases control when refresh happens
- Domain layer owns token lifecycle

**Cons:**
- Wrong layer - use cases don't know when HTTP requests are made
- Duplicated logic across multiple use cases
- Can't prevent 401s (request already sent by the time use case runs)

**Why Rejected:** Use cases run before repository calls, not before individual HTTP requests.

---

## Implementation Details

### Key Components

1. **`AuthConfig.kt`** (`:core:network`)
   - `TOKEN_EXPIRATION_BUFFER_MS = 5 * 60 * 1000L`
   - Network configuration constant

2. **`AuthInterceptor.kt`** (`:core:network`)
   - `isTokenExpired()`: Checks cached metadata
   - `createUnauthorizedResponse()`: Triggers refresh flow
   - Runs on every authenticated request

3. **`TinkAuthStorage.kt`** (`:core:datastore:preferences`)
   - Caches `issuedAt`, `accessTokenLifespan`, `refreshTokenLifespan`
   - In-memory `AtomicReference` for synchronous OkHttp access
   - Auto-syncs cache from DataStore on changes

4. **`AuthTokens.kt`** (`:core:domain`)
   - Pure data class (no methods removed except `isAccessTokenExpired()`)
   - `fromServerResponse()` factory for creating from API response

### Performance Characteristics

- **Cache Hit (typical):** 0ms (instant read from AtomicReference)
- **Cache Miss (app start):** ~0-50ms (wait for DataStore sync)
- **Proactive Refresh:** 200-400ms (network-dependent)
- **Reactive Refresh:** 400-600ms (one extra round trip)

---

## Monitoring & Metrics

### Success Metrics
- **Latency improvement:** Average request time for first-after-expiration requests
- **401 rate:** Should only see 401s for actual auth failures, not routine expiration
- **Refresh frequency:** Should align with token lifespan (e.g., every 10 minutes for 15-min tokens)

### Warning Signs
- High 401 rate → Clock skew or buffer too small
- Frequent refreshes → Buffer too large, wasting tokens
- Slow first requests → Cache not populated (check init block)

---

## References

- [RFC 6749: OAuth 2.0 (Token Expiration)](https://datatracker.ietf.org/doc/html/rfc6749#section-5.1)
- [OkHttp Interceptors](https://square.github.io/okhttp/features/interceptors/)
- ADR-003: Token Refresh Strategy (automatic refresh on 401)
- ADR-005: Encrypted Token Storage (Tink + DataStore)

---

## Changelog

- **2025-01-19:** Initial decision - proactive refresh in network layer
