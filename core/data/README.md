# Core Data Module

Data layer implementation including repositories, data sources, and API service definitions.

## Purpose

Acts as the single source of truth for data access. Coordinates between network, local storage, and in-memory caching.

## Responsibilities

- Define API service interfaces
- Implement repositories following the Repository pattern
- Coordinate data from multiple sources (network, database, cache)
- Map between data models and domain models
- Implement `TokenRefreshCallback` for authentication

## Architecture

```
Repository → (Remote Data Source, Local Data Source) → Domain Models
```

### Example: User Repository

```kotlin
interface UserRepository {
    suspend fun getUser(id: String): Result<User>
    suspend fun updateProfile(user: User): Result<Unit>
}

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val userDao: UserDao,
    private val mapper: UserMapper
) : UserRepository {
    
    override suspend fun getUser(id: String): Result<User> = runCatching {
        // Try local cache first
        userDao.getUserById(id)?.let { return@runCatching mapper.toDomain(it) }
        
        // Fetch from network
        val response = userApi.getUser(id)
        val entity = mapper.toEntity(response)
        userDao.insert(entity)
        
        mapper.toDomain(entity)
    }
}
```

## Data Mapping

Each data source has its own model:

- **API Response Models**: JSON response structures (in `model/response/`)
- **Database Entities**: Room entities (in `model/entity/`)
- **Domain Models**: Business logic models from `:core:domain`

Use mappers to convert between layers:

```kotlin
class UserMapper @Inject constructor() {
    fun toDomain(entity: UserEntity): User = User(
        id = entity.id,
        name = entity.name,
        email = entity.email
    )
    
    fun toEntity(response: UserResponse): UserEntity = UserEntity(
        id = response.userId,
        name = response.fullName,
        email = response.emailAddress
    )
}
```

## API Service Definitions

Define Retrofit services here:

```kotlin
interface RecordingApiService {
    @GET("recordings")
    suspend fun getRecordings(): List<RecordingResponse>
    
    @POST("recordings")
    suspend fun createRecording(@Body request: CreateRecordingRequest): RecordingResponse
    
    @DELETE("recordings/{id}")
    suspend fun deleteRecording(@Path("id") id: String)
}
```

## Token Refresh Implementation

This module implements `TokenRefreshCallback` from `:core:network`:

```kotlin
@Singleton
class TokenRefreshCallbackImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenStorage: TokenStorage
) : TokenRefreshCallback {
    
    override suspend fun refreshToken(): String? = runCatching {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
        
        tokenStorage.saveAccessToken(response.accessToken)
        tokenStorage.saveRefreshToken(response.refreshToken)
        
        response.accessToken
    }.getOrNull()
}
```

## Caching Strategy

Implement appropriate caching based on data characteristics:

- **User Profile**: Cache with TTL, refresh on app start
- **Recordings List**: Cache, refresh on pull-to-refresh
- **Session Data**: No caching, always fetch fresh

Example with cache TTL:

```kotlin
class RecordingRepository @Inject constructor(
    private val api: RecordingApiService,
    private val cache: RecordingCache
) {
    suspend fun getRecordings(forceRefresh: Boolean = false): List<Recording> {
        if (!forceRefresh && cache.isValid()) {
            return cache.get()
        }
        
        val recordings = api.getRecordings().map { it.toDomain() }
        cache.set(recordings)
        return recordings
    }
}
```

## Error Handling

Wrap exceptions in domain-specific results:

```kotlin
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: DataException) : DataResult<Nothing>()
}

sealed class DataException : Exception() {
    data class NetworkError(val code: Int) : DataException()
    data object NoConnection : DataException()
    data object CacheExpired : DataException()
}
```

## Dependencies

- `:core:network` (Retrofit, API clients)
- `:core:domain` (domain models)
- `:core:datastore:preferences` (token storage)
- Room (local database, if needed)
- Hilt (dependency injection)

## Testing

Mock repositories in feature tests:

```kotlin
@Test
fun `should load user data on init`() = runTest {
    val mockRepo = mock<UserRepository> {
        onBlocking { getUser(any()) } doReturn Result.success(testUser)
    }
    
    val viewModel = UserViewModel(mockRepo)
    
    assertEquals(testUser, viewModel.state.value.user)
}
```

## Best Practices

- Keep repositories focused on a single domain entity
- Use coroutines for async operations
- Return `Result<T>` or sealed classes for error handling
- Don't expose data models directly - map to domain models
- Cache intelligently based on data volatility

