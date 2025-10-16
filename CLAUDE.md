# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Zencastr is an Android application built with Jetpack Compose using a multi-module architecture following Clean Architecture principles. The project uses Gradle convention plugins to minimize boilerplate and maintain consistency across modules.

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

### Build release (with ProGuard/R8)
```bash
./gradlew assembleRelease
./gradlew bundleRelease  # For Play Store AAB
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
./gradlew connectedAndroidTest   # Instrumented tests (requires device/emulator)
```

### Code quality checks
```bash
./gradlew detekt                 # Static analysis
./gradlew lint                   # Android lint
```

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
:core:navigation            - Navigation3 setup, Navigator wrapper, NavKey definitions
:core:network               - Network configuration (Retrofit, OkHttp, auth interceptors)
:core:data                  - Data layer (repositories, data sources, Room)
:core:domain                - Business logic layer (models, use cases, domain entities)
:core:datastore:preferences - Encrypted token storage using EncryptedSharedPreferences
:core:datastore:proto       - Proto DataStore definitions
:feature:recording          - Recording feature (screen + logic)
:feature:profile            - Profile feature implementation
:feature:profile:api        - Profile routes for cross-feature navigation (no UI)
```

**Dependency Flow:**
- `:app` → feature modules, `:core:ui`, `:core:navigation`, `:core:network`, `:core:data`
- `:feature:*` → `:core:ui`, `:core:domain`, `:core:data`, `:core:navigation`
- `:feature:*:api` → `:core:navigation` only (sealed route interfaces, no implementation)
- `:core:data` → `:core:network`, `:core:domain`, `:core:datastore:preferences`
- `:core:network` → `:core:datastore:preferences` (for token storage)
- `:core:navigation` → standalone (Navigation3 wrappers)
- `:core:ui` → standalone (only UI/theme)
- `:core:domain` → pure Kotlin (no Android dependencies)

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

### Security

**Token Storage**:
- Access tokens and refresh tokens are encrypted using `EncryptedSharedPreferences`
- Encryption: AES-256-GCM with hardware-backed keys (Android Keystore)
- Fallback to regular SharedPreferences if encryption fails (logged)

**ProGuard/R8**:
- Release builds use R8 with comprehensive keep rules
- Configuration in `app/proguard-rules.pro`
- Includes rules for Kotlinx Serialization, Retrofit, Room, Hilt, etc.

### Convention Plugins System

**Critical:** This project uses Gradle convention plugins located in `build-logic/` to eliminate boilerplate. All modules use these plugins instead of directly configuring Android/Kotlin settings.

**Available Plugins:**
- `zencastr.android.application` - For `:app` module (includes SDK config, test dependencies, Kotlin setup)
- `zencastr.android.library` - For library modules (same as above, but for libraries)
- `zencastr.android.feature` - For feature modules (applies library + compose + hilt + core dependencies automatically)
- `zencastr.android.compose` - Adds Jetpack Compose support (must be applied after application/library plugin)
- `zencastr.android.hilt` - Adds Hilt dependency injection (KSP + dependencies)
- `zencastr.android.room` - Adds Room database support (KSP + dependencies)
- `zencastr.android.network` - Adds networking dependencies (Retrofit, OkHttp, Kotlinx Serialization)

**Note**: Detekt is applied globally to all subprojects in the root `build.gradle.kts` - no need to apply it per-module.

**Configuration Centralization:**
All Android build constants are in `build-logic/src/main/kotlin/AndroidConfig.kt`:
- `COMPILE_SDK = 36`
- `MIN_SDK = 30`
- `TARGET_SDK = 36`
- `JVM_TARGET = "11"`
- `NAMESPACE_PREFIX = "com.acksession"`

**To change SDK versions or build settings:** Edit `AndroidConfig.kt` - changes apply to all modules automatically.

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

GitHub Actions CI runs on every push/PR:
- Build all modules
- Run unit tests
- Run Detekt
- Run Android Lint
- Generate release APK

Configuration: `.github/workflows/ci.yml`

## Documentation

### Architecture Decision Records (ADRs)

Located in `docs/architecture/`:
- ADR-001: Multi-Module Architecture
- ADR-002: Navigation3 Adoption
- ADR-003: Token Refresh Strategy
- ADR-004: Convention Plugins System
- ADR-005: Encrypted Token Storage

These documents explain **why** architectural decisions were made.

### API Documentation

Located in `docs/api/`:
- Authentication endpoints (login, register, refresh)
- User profile management
- Recording session endpoints

⚠️ **Status**: APIs are currently mocked. Real implementation pending.

### Production Setup

See `docs/PRODUCTION_SETUP.md` for:
- Generating release keystore
- Configuring signing
- Adding Crashlytics
- Play Store submission
- Monitoring and troubleshooting

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
