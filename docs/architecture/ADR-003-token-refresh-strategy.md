# ADR-003: Token Refresh Strategy with Dependency Inversion

**Status**: Accepted

**Date**: 2025-10-15

## Context

Zencastr uses JWT authentication with:
- **Access tokens** - Short-lived (15-60 minutes)
- **Refresh tokens** - Long-lived (7-30 days)

When an access token expires, the API returns `401 Unauthorized`. We need to:
1. Automatically refresh the access token
2. Retry the failed request with the new token
3. Avoid circular dependencies between `:core:network` and `:core:data`

### The Circular Dependency Problem

```
:core:network needs ❌→ :core:data (to refresh tokens)
:core:data needs    ✅→ :core:network (to make API calls)
```

This creates a circular dependency that Gradle won't allow.

## Decision

We solved this using the **Dependency Inversion Principle**:

1. **`:core:network` defines an interface** for token refresh:
```kotlin
interface TokenRefreshCallback {
    suspend fun refreshToken(): String?
}
```

2. **`:core:data` implements the interface**:
```kotlin
@Singleton
class TokenRefreshCallbackImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenStorage: EncryptedAuthStorage
) : TokenRefreshCallback {
    override suspend fun refreshToken(): String? {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        val response = authApi.refreshToken(refreshToken)
        tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        return response.accessToken
    }
}
```

3. **`TokenAuthenticator` in `:core:network` uses the interface**:
```kotlin
class TokenAuthenticator @Inject constructor(
    private val callback: TokenRefreshCallback // ← Interface, not implementation
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            val newToken = runBlocking { callback.refreshToken() }
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
        return null
    }
}
```

### Dependency Flow
```
:core:network (defines TokenRefreshCallback interface)
    ↑
:core:data (implements TokenRefreshCallback)
```

No circular dependency! ✅

## Consequences

### Positive
- ✅ **No circular dependencies**: Clean module boundaries maintained
- ✅ **SOLID principles**: Dependency Inversion Principle correctly applied
- ✅ **Testability**: Easy to mock `TokenRefreshCallback` for testing
- ✅ **Automatic retry**: Failed requests are retried transparently
- ✅ **User experience**: Users don't see authentication errors for expired tokens
- ✅ **Scalability**: Pattern can be reused for other cross-module callbacks

### Negative
- ⚠️ **Indirection**: Requires understanding of DI and interfaces
- ⚠️ **Documentation needed**: Team must understand why this pattern exists

### Mitigations
- Comprehensive documentation in `:core:network/README.md`
- This ADR explains the reasoning
- Code comments explain the pattern

## Alternatives Considered

1. **Move token refresh to `:core:network`**
   - Rejected: Network layer shouldn't know about business logic
   - Violates separation of concerns

2. **Create a separate `:core:auth` module**
   - Considered but deemed overkill for current scope
   - Could be refactored later if auth logic grows

3. **Use events/callbacks via EventBus**
   - Rejected: Adds complexity and makes flow harder to trace
   - Interface dependency is cleaner and type-safe

## Implementation Details

### AuthInterceptor (adds tokens to requests)
```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenStorage: EncryptedAuthStorage
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val token = tokenStorage.getAccessToken()
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

### TokenAuthenticator (handles 401 responses)
```kotlin
// Automatically called by OkHttp when response is 401
class TokenAuthenticator @Inject constructor(
    private val callback: TokenRefreshCallback
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Refresh token and retry
    }
}
```

### Token Refresh Flow
```
1. Request with expired access token → API returns 401
2. TokenAuthenticator intercepts 401
3. Calls callback.refreshToken() (implemented in :core:data)
4. :core:data calls /auth/refresh endpoint
5. Saves new tokens to EncryptedAuthStorage
6. Returns new access token
7. TokenAuthenticator retries original request with new token
8. User sees no error ✅
```

## References

- [OkHttp Authenticator](https://square.github.io/okhttp/features/authentication/)
- [Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

