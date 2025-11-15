# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Android Modular Template** is a production-ready template project for building Android applications with Jetpack Compose and Clean Architecture. The project demonstrates modern best practices through multi-module architecture, type-safe navigation, secure authentication, and comprehensive Firebase integration.

**Note**: The recording, profile, and settings features are demo implementations showcasing architectural patterns. They serve as reference examples and can be replaced with your own features.

## Initial Setup Requirements

**IMPORTANT**: Before building, you MUST have `google-services.json` configured.

### Local Development Setup

1. **Firebase Configuration** (Required for any build):
   - Create Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add Android app with package: `com.example.myapp` (or your chosen package name)
   - Download `google-services.json` and place in `app/` directory
   - See `app/README_FIREBASE_SETUP.md` for detailed instructions
   - **Note**: Update package name after rebranding the project

2. **Verify Setup**:
   ```bash
   ./gradlew :app:assembleDevDebug
   ```

### CI/CD Setup (GitHub Actions)

Add to GitHub repository secrets (**Settings → Secrets and variables → Actions**):
- `GOOGLE_SERVICES_JSON` - Base64-encoded `google-services.json` file
  ```bash
  cat app/google-services.json | base64 | pbcopy
  ```

### Optional: Release Signing (for Play Store)

Only needed when ready to publish to Play Store:
- Generate keystore: See `MANUAL_SETUP_REQUIRED.md`
- Create `keystore.properties` file (gitignored)
- Uncomment signing config in `app/build.gradle.kts`

For detailed setup instructions, see `MANUAL_SETUP_REQUIRED.md` and `NEXT_STEPS.md`.

## Build Commands

### Build the project
```bash
./gradlew build
```

### Build specific module
```bash
./gradlew :app:assembleDebug
./gradlew :core:ui:assemble
```

### Build with product flavors
```bash
./gradlew :app:assembleDevDebug       # Dev environment, debug build
./gradlew :app:assembleDevRelease     # Dev environment, release build
./gradlew :app:assembleProdDebug      # Production environment, debug build
./gradlew :app:assembleProdRelease    # Production environment, release build
```

### Build release (with ProGuard/R8)
```bash
./gradlew assembleProdRelease         # Production release APK
./gradlew bundleProdRelease           # Production release AAB (for Play Store)
```

### Clean build
```bash
./gradlew clean build
```

### Build convention plugins (build-logic)
```bash
./gradlew :build-logic:assemble
```

### Run tests
```bash
./gradlew test                    # All unit tests
./gradlew :app:test              # App module tests
./gradlew :app:testDevDebug      # Run tests for specific variant
./gradlew connectedAndroidTest   # Instrumented tests (requires device/emulator)
```

### Code quality checks
```bash
./gradlew detekt                 # Static analysis
./gradlew lint                   # Android lint (requires google-services.json)
./gradlew lintDevDebug           # Lint specific variant
```

### Version management
```bash
./scripts/bump_version.sh patch  # 1.0.0 → 1.0.1 (bug fixes)
./scripts/bump_version.sh minor  # 1.0.0 → 1.1.0 (new features)
./scripts/bump_version.sh major  # 1.0.0 → 2.0.0 (breaking changes)
```
Version info stored in `version.properties` and automatically applied to builds.

### Create new feature module (scaffolding)
```bash
./gradlew createFeature -PfeatureName=myfeature
```
This automatically creates:
- `:feature:myfeature` - Feature implementation module
- `:feature:myfeature:api` - Navigation route definitions
- Build files, manifests, and NavKey boilerplate
- Updates `settings.gradle.kts`

### Install git hooks
```bash
./install-hooks.sh
```
This sets up pre-commit hooks that run Detekt and tests automatically.

## Architecture

### Multi-Module Structure

The project follows a modular architecture with clear separation of concerns:

```
:app                        - Main application module, wires features together with Navigation3
:core:ui                    - Shared UI components and design system (theme, colors, typography)
:core:common                - Infrastructure (dispatchers, coroutine scopes, DI qualifiers)
:core:navigation            - Navigation3 setup, Navigator wrapper, NavKey definitions
:core:network               - Network configuration (Retrofit, OkHttp, auth interceptors)
:core:data                  - Data layer (repositories, data sources, Room)
:core:domain                - Business logic layer (models, use cases, domain entities)
:core:datastore:preferences - Encrypted token storage using Google Tink + DataStore
:core:datastore:proto       - Proto DataStore definitions
:core:analytics             - Firebase Crashlytics, Analytics, Performance monitoring
:core:notifications         - Push notifications (FCM), notification channels
:core:remoteconfig          - Firebase Remote Config, feature flags, A/B testing
:feature:recording          - Demo: Recording feature (camera, permissions example)
:feature:profile            - Demo: Profile feature (CRUD operations example)
:feature:profile:api        - Profile routes for cross-feature navigation (no UI)
:feature:settings           - Demo: Settings feature (preferences example)
```

**Dependency Flow:**
- `:app` → feature modules, `:core:ui`, `:core:navigation`, `:core:network`, `:core:data`, `:core:analytics`, `:core:notifications`, `:core:remoteconfig`
- `:feature:*` → `:core:ui`, `:core:domain`, `:core:data`, `:core:navigation`
- `:feature:*:api` → `:core:navigation` only (sealed route interfaces, no implementation)
- `:core:data` → `:core:network`, `:core:domain`, `:core:datastore:preferences`, `:core:common`
- `:core:network` → `:core:datastore:preferences` (for token storage)
- `:core:domain` → `:core:common` (for dispatcher qualifiers in use cases)
- `:core:datastore:preferences` → `:core:common` (for application scopes)
- `:core:analytics` → standalone (Firebase Crashlytics, Analytics, Performance)
- `:core:notifications` → `:core:analytics` (for FCM token tracking)
- `:core:remoteconfig` → standalone (Firebase Remote Config)
- `:core:navigation` → standalone (Navigation3 wrappers)
- `:core:ui` → standalone (only UI/theme)
- `:core:common` → standalone (infrastructure only - dispatchers, scopes)

### Navigation Architecture (Navigation3)

The project uses **Navigation3** with a modular, type-safe navigation pattern:

1. **Route Definitions**: Each feature defines routes in a sealed interface implementing `NavKey` (e.g., `ProfileRoute`, `RecordingRoute`)
2. **API Modules**: For cross-feature navigation, create `:feature:name:api` modules containing only route definitions (no UI code)
3. **Hilt Integration**: Features register navigation entries using `@IntoSet` with `EntryProviderInstaller`
4. **Navigator Wrapper**: `:core:navigation` provides a `Navigator` class wrapping Navigation3's `NavigationController`

**Example route definition** (in `:feature:profile:api`):
```kotlin
@Serializable
sealed interface ProfileRoute : NavKey {
    @Serializable
    data class Profile(val userId: String, val name: String) : ProfileRoute
}

fun Navigator.navigateToProfile(userId: String, name: String) {
    navigateTo(ProfileRoute.Profile(userId, name))
}
```

**Cross-feature navigation**: Features depend on other features' `:api` modules to navigate without coupling to implementations. For example, `:feature:recording` can navigate to profile by depending on `:feature:profile:api` and calling `navigator.navigateToProfile()`.

### Network and Authentication Architecture

The project uses a clean network architecture with automatic JWT token management to avoid circular dependencies:

**Key Components:**
1. **`:core:network`** - Provides Retrofit, OkHttpClient, and auth interceptors
2. **`:core:data`** - Implements API services and token refresh logic
3. **`:core:datastore:preferences`** - Securely stores JWT tokens with AES-256-GCM encryption

**How Token Refresh Works (No Circular Dependencies):**
- `AuthInterceptor` (in `:core:network`) automatically adds access tokens to requests
- `TokenAuthenticator` (in `:core:network`) intercepts 401 responses and refreshes tokens
- The authenticator depends on a `TokenRefreshCallback` interface (defined in `:core:network`)
- `:core:data` provides the implementation of `TokenRefreshCallback` using `AuthApiService`
- This follows the **Dependency Inversion Principle** - network layer depends on abstraction, data layer provides implementation

**Dependency Flow for Network:**
```
:core:network (defines TokenRefreshCallback interface)
    ↓
:core:data (implements TokenRefreshCallback using AuthApiService)
```
No circular dependency! ✅

**Configuration:**
- API base URL is configured in `:app` module via `BuildConfig.API_BASE_URL`
- The `:app` module provides `@ApiBaseUrl` via Hilt for injection into Retrofit
- Network configuration (timeouts, logging) is centralized in `:core:network`

### Production Features and Monitoring

The project includes comprehensive production monitoring and debugging tools:

#### Firebase Integration

**`:core:analytics` Module** - Crash reporting, analytics, and performance monitoring:
- **Firebase Crashlytics**: Automatic crash reporting with CrashlyticsTree for production logging
- **Firebase Analytics**: Event tracking, screen views, user properties
- **Firebase Performance**: Automatic performance monitoring

**Interface-based design** (`AnalyticsTracker`):
```kotlin
interface AnalyticsTracker {
    fun logEvent(eventName: String, params: Map<String, Any>? = null)
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun logException(throwable: Throwable, message: String? = null)
    fun setCustomKey(key: String, value: Any)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
```

**`:core:notifications` Module** - Push notifications and FCM:
- Firebase Cloud Messaging (FCM) integration
- Notification channels (Android O+)
- FCM token management and topic subscriptions
- Android 13+ notification permission handling

**Interface-based design** (example `NotificationManager` interface):
```kotlin
interface NotificationManager {
    fun createNotificationChannels()
    fun showNotification(channelId: String, notificationId: Int, title: String, message: String, autoCancel: Boolean = true)
    suspend fun getFcmToken(): String?
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
    fun hasNotificationPermission(context: Context): Boolean
    fun cancelNotification(notificationId: Int)
    fun cancelAllNotifications()
}
```

**`:core:remoteconfig` Module** - Feature flags and A/B testing:
- Firebase Remote Config integration
- Runtime feature flag management
- Default values for offline support
- 1-hour fetch interval (configurable)

**Interface-based design** (`FeatureFlagManager`):
```kotlin
interface FeatureFlagManager {
    suspend fun fetchAndActivate(): Result<Boolean>
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun getString(key: String, defaultValue: String = ""): String
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun getDouble(key: String, defaultValue: Double = 0.0): Double
}
```

#### Build Flavors

The app uses product flavors for environment separation:

```kotlin
productFlavors {
    create("dev") {
        dimension = "environment"
        applicationIdSuffix = ".dev"
        versionNameSuffix = "-dev"
        buildConfigField("String", "API_BASE_URL", "\"https://dev-api.example.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"development\"")
    }

    create("prod") {
        dimension = "environment"
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"production\"")
    }
}
```

**Available build variants**:
- `devDebug` - Development with debug tools
- `devRelease` - Development with release optimizations
- `prodDebug` - Production with debug tools (for testing)
- `prodRelease` - Production release build

#### Debug Tools

**LeakCanary** (debug builds only):
- Automatic memory leak detection
- Shows leak notifications with detailed traces
- Zero configuration required

**Chucker** (debug builds only):
- Network traffic inspector
- Shows all HTTP requests/responses in notification
- Searchable request history
- Auto-disabled in release builds (no-op dependency)

#### Production Logging

**Development-Safe Crash Handler**:
```kotlin
private fun setupGlobalExceptionHandler() {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        analytics.logException(throwable, "Uncaught exception: ${throwable.message}")
        analytics.setCustomKey("crash_thread", thread.name)

        if (BuildConfig.DEBUG) {
            // In debug, let app crash visibly for developer awareness
            defaultHandler?.uncaughtException(thread, throwable)
        } else {
            // In production, report to Crashlytics
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
```

**Conditional Timber Logging**:
- Debug builds: `Timber.DebugTree()` - verbose console logging
- Release builds: `CrashlyticsTree()` - logs to Crashlytics only

**Conditional HTTP Logging**:
- Debug builds: `HttpLoggingInterceptor.Level.BODY` - full request/response logging
- Release builds: `HttpLoggingInterceptor.Level.NONE` - no logging for security

The `:app` module provides `@Named("isDebug")` Boolean to `:core:network` for conditional logging configuration.

#### Backup and Security

**Backup Rules** (`app/src/main/res/xml/backup_rules.xml`):
- Includes databases and files
- Excludes encrypted DataStore (token storage)
- Excludes device-specific preferences
- Excludes cache directories

**Data Extraction Rules** (`app/src/main/res/xml/data_extraction_rules.xml`):
- Cloud backup rules (Android 12+)
- Device transfer rules
- Same security exclusions as backup rules

#### Deep Linking

**Custom URL Schemes**:
```xml
<data android:scheme="myapp" android:host="content" />
```
Example: `myapp://content/123`

**App Links** (verified HTTPS):
```xml
<data android:scheme="https" android:host="example.com" android:pathPrefix="/content" />
```
Example: `https://example.com/content/123`

#### Firebase Setup

**Required**: Add `google-services.json` from Firebase Console to `app/` directory.

See `app/README_FIREBASE_SETUP.md` for detailed setup instructions:
1. Create Firebase project
2. Add Android app with your package name (e.g., `com.example.myapp`)
3. Download `google-services.json`
4. Enable Firebase services (Crashlytics, Analytics, FCM, Remote Config)
5. Update package name after rebranding

**Build without Firebase will fail** - `google-services.json` is required.

### Security

**Token Storage** (`TinkAuthStorage` in `:core:datastore:preferences`):
- Access tokens and refresh tokens encrypted using **Google Tink** (production-grade crypto library)
- Encryption: AES-256-GCM-HKDF via Tink AEAD primitive with hardware-backed keys (Android Keystore)
- Storage backend: DataStore Preferences (encrypted values stored as Base64)
- **In-memory cache**: `AtomicReference` for thread-safe synchronous access (OkHttp interceptors)
- **Performance**: Zero `runBlocking` - synchronous getters read from cache, async setters update DataStore
- **Memory management**: Cache cleared when app backgrounds (`onTrimMemory()`) to reduce memory dump risk
- AEAD provides authenticated encryption preventing tampering
- Replaces deprecated `EncryptedSharedPreferences` (deprecated April 2024)
- Fail-fast on encryption errors (throws `SecurityException` - no silent fallback to unencrypted storage)

**ProGuard/R8**:
- Release builds use R8 with comprehensive keep rules
- Configuration in `app/proguard-rules.pro`
- Includes rules for:
  - Firebase (Crashlytics, Analytics, Messaging, Remote Config, Performance)
  - Kotlinx Serialization
  - Retrofit and OkHttp
  - Room Database
  - Hilt Dependency Injection
  - LeakCanary and Chucker (debug tools)

### Convention Plugins System

**Critical:** This project uses Gradle convention plugins located in `build-logic/` to eliminate boilerplate. All modules use these plugins instead of directly configuring Android/Kotlin settings.

**Available Plugins:**
- `convention.android.application` - For `:app` module (includes SDK config, test dependencies, Kotlin setup)
- `convention.android.library` - For library modules (same as above, but for libraries)
- `convention.android.feature` - For feature modules (applies library + compose + hilt + core dependencies automatically)
- `convention.android.compose` - Adds Jetpack Compose support (must be applied after application/library plugin)
- `convention.android.hilt` - Adds Hilt dependency injection (KSP + dependencies)
- `convention.android.room` - Adds Room database support (KSP + dependencies)
- `convention.android.network` - Adds networking dependencies (Retrofit, OkHttp, Kotlinx Serialization)

**Note**: Detekt is applied globally to all subprojects in the root `build.gradle.kts` - no need to apply it per-module.

**Configuration Centralization:**

**Build Configuration** (`build-logic/src/main/kotlin/AndroidConfig.kt`):
- `COMPILE_SDK = 36`
- `MIN_SDK = 30`
- `TARGET_SDK = 36`
- `JVM_TARGET = "11"`

**Branding Configuration** (`template.properties`):
- Package names (`package.base`, `package.app`)
- Project names (`project.name`, `project.name.lowercase`)
- App display name (`app.display.name`)
- Plugin ID prefix (`plugin.id.prefix`)

All modules access these via the `ProjectPropertiesConventionPlugin`, which reads from `template.properties`.

**To change SDK versions:** Edit `AndroidConfig.kt` - changes apply to all modules automatically.
**To rebrand the project:** Run `./rebrand.sh` or edit `template.properties` and rebuild.

## Coroutines and Dispatchers

The project uses a centralized infrastructure module (`:core:common`) for coroutine dispatchers and scopes:

### Type-Safe Dispatcher Injection

```kotlin
// Instead of old approach with separate annotations:
@IoDispatcher, @DefaultDispatcher, @MainDispatcher

// Use type-safe enum-based approach:
@Dispatcher(AppDispatchers.IO)
@Dispatcher(AppDispatchers.Default)
@Dispatcher(AppDispatchers.Main)
@Dispatcher(AppDispatchers.Unconfined)
```

**Example usage in use cases:**
```kotlin
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(email: String, password: String) =
        withContext(ioDispatcher) {
            // Login logic
        }
}
```

### Application Scopes

For long-running operations that survive component cancellations:

```kotlin
@ApplicationScope       // Uses Dispatchers.Default
@ApplicationScopeIO     // Uses Dispatchers.IO (for DataStore, network, etc.)
```

**Example usage in TinkAuthStorage:**
```kotlin
@Singleton
class TinkAuthStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScopeIO private val scope: CoroutineScope  // For DataStore writes
) {
    init {
        scope.launch {
            // Populate cache from DataStore (persists across app lifecycle)
        }
    }
}
```

**Benefits:**
- Compile-time safety (typo in dispatcher enum = compile error)
- Self-documenting (`AppDispatchers.IO` is clearer than `@IoDispatcher`)
- Centralized in `:core:common` - no layering violations
- Follows Now in Android best practices

## Code Quality

### Detekt

Static code analysis runs automatically on all modules:
```bash
./gradlew detekt
```

Configuration: `config/detekt/detekt.yml`

### Pre-commit Hooks

Install git hooks to run checks before commits:
```bash
./install-hooks.sh
```

Hooks run:
- Detekt static analysis
- Unit tests

To bypass (not recommended): `git commit --no-verify`

### CI/CD

**CI Workflow** (`.github/workflows/ci.yml`) - Runs on every push/PR:
- Build all modules
- Run unit tests
- Run Detekt static analysis
- Run Android Lint
- Assemble dev and prod release APKs

**Build Release APKs Workflow** (`.github/workflows/build-release.yml`) - Manual or on version tags:
- Builds unsigned release APKs (no keystore required)
- Supports flavor selection: dev, prod, or both
- Uploads APK artifacts for download
- Auto-creates GitHub releases on version tags (v*.*.*)
- Perfect for testing before Play Store deployment

**Usage**:
```bash
# Trigger via GitHub Actions UI:
# 1. Go to Actions tab → "Build Release APKs"
# 2. Click "Run workflow"
# 3. Select flavor (dev/prod/both)
# 4. Download from artifacts

# Or tag a version:
git tag v1.0.0
git push origin v1.0.0
# Auto-creates release with APKs attached
```

**Deploy Workflow** (disabled by default):
- See `MANUAL_SETUP_REQUIRED.md` for enabling Play Store deployment
- Requires keystore setup and Play Store service account

## Documentation

### Architecture Decision Records (ADRs)

Located in `docs/architecture/`:
- ADR-001: Multi-Module Architecture
- ADR-002: Navigation3 Adoption
- ADR-003: Token Refresh Strategy
- ADR-004: Convention Plugins System
- ADR-005: Encrypted Token Storage (migrated to Tink 2025-10-19)
- ADR-006: Token Expiration Strategy (proactive refresh with 5-minute buffer)

These documents explain **why** architectural decisions were made.

### API Documentation

Located in `docs/api/`:
- Authentication endpoints (login, register, refresh)
- User profile management
- Recording session endpoints

⚠️ **Status**: APIs are currently mocked. Real implementation pending.

### Production Setup and Implementation

Production readiness documentation:
- **`MANUAL_SETUP_REQUIRED.md`** - Step-by-step setup guide (Firebase, keystore, Play Store)
- **`NEXT_STEPS.md`** - Quick reference and current status
- **`app/README_FIREBASE_SETUP.md`** - Firebase Console setup instructions
- **`docs/PRODUCTION_SETUP.md`** - Release keystore, signing, Play Store submission

**Current Status: 100% Development Ready** 🎉

What's working now:
- ✅ Local builds (requires `google-services.json` setup)
- ✅ CI/CD pipelines (requires GitHub secret: `GOOGLE_SERVICES_JSON`)
- ✅ Build unsigned release APKs via GitHub Actions
- ✅ Firebase integration (Analytics, Crashlytics, FCM, Remote Config)
- ✅ All production features implemented

What's pending (only for Play Store deployment):
- ⏳ Keystore generation (when ready to publish)
- ⏳ Play Store service account (when ready to publish)
- ⏳ Deploy workflow (currently disabled, enable when ready)

## Production Build Configuration

### Build Optimizations

Gradle performance settings in `gradle.properties`:
- `org.gradle.parallel=true` - Parallel module builds
- `org.gradle.caching=true` - Build cache enabled
- `org.gradle.configureondemand=true` - Configure only needed modules
- `kotlin.incremental=true` - Incremental Kotlin compilation

### Release Build

Release builds use R8 (ProGuard) for:
- Code shrinking
- Code obfuscation
- Resource shrinking

Enable in `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(...)
    }
}
```

### Signing

Signing configuration in `app/build.gradle.kts` (commented by default).

Create `keystore.properties` (gitignored):
```properties
storeFile=../your-keystore.jks
storePassword=YOUR_PASSWORD
keyAlias=YOUR_ALIAS
keyPassword=YOUR_PASSWORD
```
