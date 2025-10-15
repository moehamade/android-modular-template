# Core Network Module

Network infrastructure providing Retrofit, OkHttp, and authentication handling.

## Purpose

Centralizes all network configuration, interceptors, and authentication logic. Handles JWT token management and automatic refresh without circular dependencies.

## Architecture

This module follows the **Dependency Inversion Principle** to avoid circular dependencies:

```
:core:network (defines interfaces)
      ↓
:core:data (implements interfaces)
```

### Key Components

#### 1. Retrofit Configuration

Provides configured Retrofit instance with:
- JSON serialization via Kotlinx Serialization
- Logging interceptor (debug builds)
- Auth interceptor (adds access tokens)
- Token authenticator (handles 401 responses)

#### 2. AuthInterceptor

Automatically adds JWT access tokens to requests:

```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val token = tokenProvider.getAccessToken()
        val request = chain.request()
            .newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

#### 3. TokenAuthenticator

Intercepts 401 responses and refreshes tokens:

```kotlin
class TokenAuthenticator @Inject constructor(
    private val refreshCallback: TokenRefreshCallback
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val newToken = refreshCallback.refreshToken()
        return response.request
            .newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }
}
```

#### 4. TokenRefreshCallback Interface

Defined in `:core:network`, implemented in `:core:data`:

```kotlin
interface TokenRefreshCallback {
    suspend fun refreshToken(): String?
}
```

This inversion prevents `:core:network` from depending on `:core:data`.

## Usage in Data Layer

Implement the callback in `:core:data`:

```kotlin
@Singleton
class TokenRefreshCallbackImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenStorage: TokenStorage
) : TokenRefreshCallback {
    override suspend fun refreshToken(): String? {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        val response = authApi.refreshToken(refreshToken)
        tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        return response.accessToken
    }
}
```

## Configuration

Base URL is provided by the `:app` module via Hilt:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkConfigModule {
    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL
}
```

Network timeouts and logging levels are configured in `NetworkModule`.

## Adding New API Services

1. Define the service interface in `:core:data`:

```kotlin
interface UserApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): UserResponse
}
```

2. Provide it via Hilt:

```kotlin
@Provides
@Singleton
fun provideUserApi(retrofit: Retrofit): UserApiService {
    return retrofit.create(UserApiService::class.java)
}
```

3. Use in repositories:

```kotlin
class UserRepository @Inject constructor(
    private val userApi: UserApiService
) {
    suspend fun getUser(id: String) = userApi.getUser(id)
}
```

## Error Handling

Network errors are wrapped in domain-specific exceptions:

```kotlin
sealed class NetworkException : Exception() {
    data class HttpError(val code: Int, val body: String?) : NetworkException()
    data class Timeout(override val message: String) : NetworkException()
    data class NoConnection(override val message: String) : NetworkException()
}
```

## Dependencies

- Retrofit (HTTP client)
- OkHttp (underlying HTTP engine)
- Kotlinx Serialization (JSON parsing)
- Hilt (dependency injection)

## Security Notes

- Never log auth tokens in production
- Use HTTPS for all endpoints
- Store tokens securely via `:core:datastore:preferences`
- Implement certificate pinning for sensitive operations

