# ADR-005: Encrypted Token Storage with Google Tink

**Status**: Accepted (Updated 2025-10-19)

**Date**: 2025-10-15 (Originally), 2025-10-19 (Migrated to Tink)

## Context

This project stores sensitive authentication data:
- **Access tokens** (JWT) - Short-lived, grants API access
- **Refresh tokens** (JWT) - Long-lived, used to obtain new access tokens
- **User ID** - Identifies the current user

Security requirements:
- Tokens must be encrypted at rest
- Must survive app restarts
- Must be accessible synchronously and asynchronously
- Must work on all Android versions (API 30+)
- Must handle encryption failures gracefully

## Decision History

### Original Decision (2025-10-15)
Initially implemented token storage using **EncryptedSharedPreferences** from AndroidX Security library.

### Migration to Tink (2025-10-19)
Migrated to **Google Tink** + **DataStore** due to deprecation of `androidx.security:security-crypto` library:
- `EncryptedSharedPreferences` deprecated in April 2024 (version 1.1.0-alpha07)
- `MasterKey` deprecated - no further updates planned
- Google recommends direct use of platform APIs and Tink

## Current Implementation: `TinkAuthStorage`

Located in `:core:datastore:preferences`:

```kotlin
@Singleton
class TinkAuthStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScopeIO private val scope: CoroutineScope
) {
    // Thread-safe in-memory cache for synchronous access
    private val accessTokenCache = AtomicReference<String?>(null)
    private val refreshTokenCache = AtomicReference<String?>(null)

    init {
        // Populate cache from DataStore on app start (reactive)
        scope.launch {
            dataStore.data.collect { prefs ->
                accessTokenCache.set(prefs[Keys.ACCESS_TOKEN]?.let { decrypt(it) })
                refreshTokenCache.set(prefs[Keys.REFRESH_TOKEN]?.let { decrypt(it) })
            }
        }
    }

    private val aead: Aead by lazy {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private fun encrypt(plaintext: String): String {
        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val ciphertext = aead.encrypt(plaintextBytes, ASSOCIATED_DATA)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(base64Ciphertext: String): String {
        val ciphertext = Base64.decode(base64Ciphertext, Base64.NO_WRAP)
        val plaintext = aead.decrypt(ciphertext, ASSOCIATED_DATA)
        return String(plaintext, Charsets.UTF_8)
    }
}
```

### Encryption Details

- **Algorithm**: AES-256-GCM-HKDF (via Tink AEAD primitive)
- **Key storage**: Android Keystore (hardware-backed when available)
- **Master key URI**: `android-keystore://tink_master_key`
- **Storage backend**: DataStore Preferences (encrypted values stored as Base64)
- **Associated data**: `"auth_storage"` (prevents tampering)

### Why Tink?

1. **Production-grade**: Used internally by Google (Google Pay, Cloud, etc.)
2. **Actively maintained**: Latest version 1.18.0 (June 2025)
3. **Better security**: AEAD provides authenticated encryption (prevents tampering)
4. **Key rotation**: Built-in support for key rotation
5. **No ProGuard rules needed**: Works out of the box with R8
6. **Better performance**: More efficient than EncryptedSharedPreferences

### In-Memory Cache Pattern (2025-10-19)

To support synchronous access from OkHttp interceptors while maintaining async DataStore:

**Architecture:**
- **In-memory cache**: `AtomicReference` for thread-safe synchronous reads
- **DataStore**: Encrypted persistence layer
- **Auto-sync**: Cache automatically syncs with DataStore changes (reactive)

**Performance:**
- Synchronous getters read from cache (instant, no I/O blocking)
- Async setters update both cache and DataStore
- Zero `runBlocking` - follows Kotlin coroutine best practices

**Memory Management:**
- Cache cleared when app backgrounds (`onTrimMemory()`) to reduce memory dump attack window
- Cache auto-repopulates from encrypted DataStore when app resumes

### API Design

```kotlin
// Async setters (update cache + DataStore)
suspend fun saveAccessToken(token: String)
suspend fun saveRefreshToken(token: String)

// Synchronous getters (read from cache - instant)
fun getAccessToken(): String?
fun getRefreshToken(): String?

// Non-blocking clears (cache cleared immediately, DataStore async)
fun clearAuthTokens()
fun clearMemoryCache() // Clear cache only (not DataStore)

// Flow-based access (for ViewModels)
fun getAccessTokenFlow(): Flow<String?>
fun getRefreshTokenFlow(): Flow<String?>
```

**Pattern Benefits:**
- Industry-standard approach (used by OAuth2 libraries like AppAuth)
- OkHttp interceptors can access tokens synchronously
- No thread blocking (`runBlocking` eliminated)
- Reactive cache updates from DataStore changes

## Consequences

### Positive
- ✅ **Security**: AES-256-GCM authenticated encryption (AEAD)
- ✅ **Hardware-backed**: Uses TEE/Secure Enclave when available
- ✅ **Future-proof**: Google Tink is actively maintained (EncryptedSharedPreferences deprecated)
- ✅ **Production-ready**: Used by Google's own products
- ✅ **Tamper-proof**: AEAD prevents modification of encrypted data
- ✅ **Key rotation**: Built-in support for rotating encryption keys
- ✅ **No ProGuard issues**: Works seamlessly with R8/ProGuard
- ✅ **Modern storage**: Uses DataStore instead of SharedPreferences
- ✅ **Better performance**: Faster than EncryptedSharedPreferences

### Negative
- ⚠️ **Keystore issues**: Rare cases where Keystore initialization fails
- ⚠️ **Factory reset**: Encrypted data lost if user factory resets (expected behavior)
- ⚠️ **Migration**: Existing encrypted data from EncryptedSharedPreferences lost

### Mitigations
- Throws `SecurityException` on encryption failure (fail-fast, don't silently use unencrypted storage)
- Token loss on factory reset is acceptable (user re-authenticates)
- No migration needed - no existing users in production yet

## Security Considerations

### What is encrypted?
- ✅ Access tokens
- ✅ Refresh tokens
- ✅ User ID

### What is NOT encrypted?
- App preferences (theme, language) - Not sensitive
- Token metadata (timestamps, lifespans) - Not sensitive, stored in plain DataStore
- Local database (Room) - Separate concern, can be encrypted separately

### Attack Scenarios

1. **Rooted device access**
   - Hardware-backed keys are still protected
   - Attacker would need to compromise Android Keystore

2. **App data backup**
   - Keyset is backed up but useless without Android Keystore master key
   - `android:allowBackup="true"` is safe with Tink

3. **Memory dump attack**
   - Tokens are briefly in memory during use (unavoidable)
   - Cleared after use where possible

4. **Tampering attack**
   - AEAD prevents modification of encrypted data
   - Associated data ensures encryption context integrity

## Alternatives Considered

1. **EncryptedSharedPreferences (original choice)**
   - ❌ Deprecated in April 2024
   - ❌ No longer maintained by Google
   - ✅ Led us to migrate to Tink

2. **Community fork (dev.spght.encryptedprefs)**
   - ❌ Community-maintained, not Google official
   - ❌ Long-term sustainability concerns
   - ✅ Good interim solution but not long-term

3. **DataStore with manual AES encryption**
   - ❌ Higher risk of implementation mistakes
   - ❌ Need to handle key rotation manually
   - ❌ Crypto is hard - don't roll your own

4. **Plain DataStore**
   - ❌ Tokens stored in plaintext
   - ❌ Fails security requirements

5. **Room database (encrypted with SQLCipher)**
   - ❌ Overkill: We only store 3 strings
   - ❌ Performance overhead not justified

## Migration from EncryptedSharedPreferences

Since we had no production users, no migration code was needed. For future reference:

```kotlin
// Hypothetical migration code (not implemented)
class EncryptedStorageMigration(
    private val oldStorage: EncryptedAuthStorage,
    private val newStorage: TinkAuthStorage
) {
    fun migrate() {
        oldStorage.getAccessToken()?.let { newStorage.saveAccessToken(it) }
        oldStorage.getRefreshToken()?.let { newStorage.saveRefreshToken(it) }
        oldStorage.getCurrentUserId()?.let { newStorage.saveCurrentUserId(it) }
        oldStorage.clearAll() // Clean up old storage
    }
}
```

## Testing Strategy

```kotlin
@Test
fun `tokens are persisted across instance recreation`() {
    val storage1 = TinkAuthStorage(context, dataStore)
    storage1.saveAccessToken("test_token")

    // Simulate app restart
    val storage2 = TinkAuthStorage(context, dataStore)
    assertThat(storage2.getAccessToken()).isEqualTo("test_token")
}

@Test
fun `clearTokens removes all data`() {
    storage.saveAccessToken("access")
    storage.saveRefreshToken("refresh")
    storage.clearTokens()

    assertThat(storage.getAccessToken()).isNull()
    assertThat(storage.getRefreshToken()).isNull()
}

@Test
fun `encrypted data cannot be tampered with`() {
    storage.saveAccessToken("original_token")

    // Attempt to modify encrypted data directly in DataStore
    // Should fail decryption due to AEAD integrity check
    // ... (implementation details)
}
```

## Dependencies

```toml
# gradle/libs.versions.toml
[versions]
tink = "1.18.0"

[libraries]
tink-android = { module = "com.google.crypto.tink:tink-android", version.ref = "tink" }
```

## ProGuard Rules

Located in `core/datastore/preferences/consumer-rules.pro`:

```proguard
# Google Tink Crypto Library
-keep class com.google.crypto.tink.** { *; }

# Suppress warnings for Tink optional dependencies
-dontwarn com.google.crypto.tink.proto.**
-dontwarn com.google.api.client.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.joda.time.Instant

# Project-specific
-keep class com.example.datastore.preferences.TinkAuthStorage { *; }
```

## Related ADRs

- **[ADR-003: Token Refresh Strategy](ADR-003-token-refresh-strategy.md)** - How tokens are automatically refreshed (DIP pattern)
- **[ADR-006: Token Expiration Strategy](ADR-006-token-expiration-strategy.md)** - When to check token expiration (proactive refresh)

## References

- [Google Tink Documentation](https://developers.google.com/tink)
- [Tink Android Setup](https://developers.google.com/tink/setup/android)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [EncryptedSharedPreferences Deprecation](https://developer.android.com/jetpack/androidx/releases/security)
- [AEAD (Authenticated Encryption with Associated Data)](https://developers.google.com/tink/aead)

## Infrastructure: `:core:common` Module (2025-10-19)

Created dedicated `:core:common` module to resolve architectural dependency concerns:

**Problem:** `:core:datastore:preferences` needed application-scoped coroutine scopes, originally provided by `:core:domain`. This created a layering violation (datastore depending on domain for infrastructure concerns).

**Solution:** Extracted infrastructure components into new `:core:common` module:

```
:core:common/
├── di/
│   ├── DispatchersModule.kt       (provides CoroutineDispatchers)
│   ├── CoroutineScopesModule.kt   (provides application scopes)
│   └── qualifiers/
│       ├── Dispatcher.kt          (@Qualifier annotation + enum)
│       ├── ApplicationScope.kt
│       └── ApplicationScopeIO.kt
```

**Type-Safe Dispatcher Qualifiers:**

```kotlin
@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val dispatcher: AppDispatchers)

enum class AppDispatchers {
    Default,  // CPU-bound work
    IO,       // I/O operations
    Main,     // UI updates
    Unconfined // Tests/special cases
}

// Usage in TinkAuthStorage:
@Inject constructor(
    @ApplicationScopeIO private val scope: CoroutineScope
)
```

**Benefits:**
- ✅ Clean separation: Domain layer no longer owns infrastructure
- ✅ Reusable: All layers can depend on `:core:common` without circular dependencies
- ✅ Type-safe: `@Dispatcher(AppDispatchers.IO)` prevents mistakes at compile-time
- ✅ Follows Now in Android best practices

**Module Dependencies:**
```
:core:domain → :core:common (for dispatcher qualifiers)
:core:datastore:preferences → :core:common (for application scopes)
:core:data → :core:common (for dispatchers/scopes)
```

## Changelog

- **2025-10-15**: Initial implementation using EncryptedSharedPreferences
- **2025-10-19**:
  - Migrated to Google Tink + DataStore due to deprecation of AndroidX Security Crypto
  - Added in-memory cache pattern for synchronous access (eliminated `runBlocking`)
  - Created `:core:common` module for infrastructure (dispatchers, scopes)
  - Implemented memory management with `onTrimMemory()` hook
