# DataStore Preferences Module

Secure, type-safe storage for application preferences using Jetpack DataStore.

## Purpose

Provides persistent storage for user preferences and sensitive data like authentication tokens. Replaces SharedPreferences with a modern, coroutine-based API.

## What's Stored Here

- JWT access tokens
- JWT refresh tokens
- User preferences (theme, language, etc.)
- App settings

## Usage

### Token Storage

```kotlin
interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}

@Singleton
class TokenStorageImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {
    
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    
    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
        }
    }
    
    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[ACCESS_TOKEN_KEY] }.first()
    }
    
    override suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }
}
```

### User Preferences

```kotlin
data class UserPreferences(
    val theme: Theme = Theme.SYSTEM,
    val language: String = "en",
    val notificationsEnabled: Boolean = true
)

enum class Theme {
    LIGHT, DARK, SYSTEM
}

class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val THEME_KEY = stringPreferencesKey("theme")
    
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            theme = Theme.valueOf(prefs[THEME_KEY] ?: Theme.SYSTEM.name),
            language = prefs[LANGUAGE_KEY] ?: "en",
            notificationsEnabled = prefs[NOTIFICATIONS_KEY] ?: true
        )
    }
    
    suspend fun updateTheme(theme: Theme) {
        dataStore.edit { it[THEME_KEY] = theme.name }
    }
}
```

## Setup

DataStore is provided via Hilt:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("zencastr_prefs") }
        )
    }
}
```

## Security Considerations

- **Encryption**: For highly sensitive data, consider using EncryptedSharedPreferences or encrypt values before storing
- **Token Rotation**: Refresh tokens should expire and be rotated regularly
- **Clear on Logout**: Always clear tokens when user logs out

Example with encryption:

```kotlin
suspend fun saveSecureToken(token: String) {
    val encrypted = encryptionManager.encrypt(token)
    dataStore.edit { it[TOKEN_KEY] = encrypted }
}
```

## Migration from SharedPreferences

If migrating from SharedPreferences:

```kotlin
val dataStore = PreferenceDataStoreFactory.create(
    migrations = listOf(
        SharedPreferencesMigration(context, "old_prefs_name")
    ),
    produceFile = { context.preferencesDataStoreFile("zencastr_prefs") }
)
```

## Testing

Mock DataStore in tests:

```kotlin
@Test
fun `should save and retrieve token`() = runTest {
    val testDataStore = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(UnconfinedTestDispatcher()),
        produceFile = { testFile }
    )
    
    val storage = TokenStorageImpl(testDataStore)
    storage.saveAccessToken("test_token")
    
    assertEquals("test_token", storage.getAccessToken())
}
```

## Best Practices

- Use `Flow` to observe preference changes reactively
- Handle `IOException` when reading/writing
- Don't store large data (use Room for that)
- Prefer sealed classes for complex preference types
- Clear sensitive data on logout

