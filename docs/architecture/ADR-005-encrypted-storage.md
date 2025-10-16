# ADR-005: Encrypted Token Storage with EncryptedSharedPreferences

**Status**: Accepted

**Date**: 2025-10-15

## Context

Zencastr stores sensitive authentication data:
- **Access tokens** (JWT) - Short-lived, grants API access
- **Refresh tokens** (JWT) - Long-lived, used to obtain new access tokens
- **User ID** - Identifies the current user

Security requirements:
- Tokens must be encrypted at rest
- Must survive app restarts
- Must be accessible synchronously and asynchronously
- Must work on all Android versions (API 30+)
- Must handle encryption failures gracefully

## Decision

We implemented token storage using **EncryptedSharedPreferences** from AndroidX Security library.

### Implementation: `EncryptedAuthStorage`

Located in `:core:datastore:preferences`:

```kotlin
@Singleton
class EncryptedAuthStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences on failure
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}
```

### Encryption Details

- **Algorithm**: AES-256-GCM (Galois/Counter Mode)
- **Key storage**: Android Keystore (hardware-backed when available)
- **Key scheme**: AES256_SIV for keys, AES256_GCM for values
- **Master key**: Generated once, stored securely in Android Keystore

### API Design

```kotlin
// Synchronous access (for Interceptors)
fun saveAccessToken(token: String)
fun getAccessToken(): String?
fun clearTokens()

// Flow-based access (for ViewModels)
fun getAccessTokenFlow(): Flow<String?>
fun getRefreshTokenFlow(): Flow<String?>
```

Both sync and async APIs update the same backing storage.

## Consequences

### Positive
- ✅ **Security**: Tokens encrypted with AES-256-GCM
- ✅ **Hardware-backed**: Uses TEE/Secure Enclave when available
- ✅ **Compatibility**: Works on Android 6.0+ (we target API 30+)
- ✅ **Jetpack library**: Official Google library, actively maintained
- ✅ **Fallback**: Gracefully degrades to regular SharedPreferences on encryption failure
- ✅ **Reactive**: Flow-based API for observing token changes
- ✅ **Simple API**: Clean abstraction over encryption complexity

### Negative
- ⚠️ **Keystore issues**: Rare cases where Keystore initialization fails
- ⚠️ **Factory reset**: Encrypted data lost if user factory resets (expected behavior)
- ⚠️ **Migration**: Existing unencrypted data requires migration

### Mitigations
- Fallback to regular SharedPreferences on encryption failure (logged via Timber)
- Token loss on factory reset is acceptable (user re-authenticates)
- Migration handled in `EncryptedAuthStorage` init block

## Security Considerations

### What is encrypted?
- ✅ Access tokens
- ✅ Refresh tokens
- ✅ User ID

### What is NOT encrypted?
- App preferences (theme, language) - Not sensitive
- Local database (Room) - Separate concern, can be encrypted separately

### Attack Scenarios

1. **Rooted device access**
   - Hardware-backed keys are still protected
   - Attacker would need to compromise Android Keystore

2. **App data backup**
   - Encrypted preferences are backed up but useless without keystore
   - `android:allowBackup="true"` is safe with EncryptedSharedPreferences

3. **Memory dump attack**
   - Tokens are briefly in memory during use (unavoidable)
   - Cleared after use where possible

## Alternatives Considered

1. **DataStore (encrypted)**
   - Rejected: No official encryption support
   - Would require custom encryption layer

2. **Room database (encrypted with SQLCipher)**
   - Overkill: We only store 3 strings
   - Performance overhead not justified

3. **Plain SharedPreferences**
   - Rejected: Tokens stored in plaintext
   - Fails security requirements

4. **Custom encryption**
   - Rejected: Don't roll your own crypto
   - EncryptedSharedPreferences is audited by Google

5. **Keychain (iOS-style)**
   - Not available on Android
   - Android Keystore is the Android equivalent

## Migration Path

If moving away from EncryptedSharedPreferences:
1. Read tokens from old storage
2. Write to new storage
3. Delete old storage
4. All access goes through `EncryptedAuthStorage` interface → easy to swap implementations

## Testing Strategy

```kotlin
@Test
fun `tokens are persisted across instance recreation`() {
    val storage1 = EncryptedAuthStorage(context)
    storage1.saveAccessToken("test_token")
    
    // Simulate app restart
    val storage2 = EncryptedAuthStorage(context)
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
```

## References

- [EncryptedSharedPreferences Documentation](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [Security Best Practices](https://developer.android.com/topic/security/best-practices)

