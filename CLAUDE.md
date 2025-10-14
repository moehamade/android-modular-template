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

### Create new feature module (scaffolding)
```bash
./gradlew createFeature -PfeatureName=myfeature
```
This automatically creates:
- `:feature:myfeature` - Feature implementation module
- `:feature:myfeature:api` - Navigation route definitions
- Build files, manifests, and NavKey boilerplate
- Updates `settings.gradle.kts`

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
:core:datastore:preferences - DataStore preferences implementation (token storage)
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
3. **`:core:datastore:preferences`** - Securely stores JWT tokens

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

**Configuration Centralization:**
All Android build constants are in `build-logic/src/main/kotlin/AndroidConfig.kt`:
- `COMPILE_SDK = 36`
- `MIN_SDK = 30`
- `TARGET_SDK = 36`
- `JVM_TARGET = "11"`
- `NAMESPACE_PREFIX = "com.acksession"`

**To change SDK versions or build settings:** Edit `AndroidConfig.kt` - changes apply to all modules automatically.

### Theme and Design System

The design system lives in `:core:ui` at `com.acksession.ui.theme`:
- `Theme.kt` - Material3 theme with dark/light mode and dynamic color support
- `Color.kt` - Color palette definitions
- `Type.kt` - Typography definitions

**Important:** The app uses Compose theming (`ZencastrTheme`), but also requires a minimal XML theme in `/app/src/main/res/values/themes.xml` for the Activity window. Don't delete the XML theme - it's needed for the AndroidManifest.

## Adding New Modules

### Feature Module (`:feature:*`)
Use `zencastr.android.feature` which bundles library + compose + hilt + core dependencies:

```kotlin
plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.featurename"
}

dependencies {
    // Feature-specific dependencies only
    // Core dependencies (:core:ui, :core:domain, :core:data, :core:navigation) are auto-included
}
```

### API Module (`:feature:*:api`)
For navigation routes shared across features:

```kotlin
plugins {
    id("zencastr.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.featurename.api"
}

dependencies {
    api(project(":core:navigation"))
    api(libs.bundles.navigation3)
}
```

### Core/Library Module (`:core:*`)
For non-feature modules:

```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose") // if using Compose
    id("zencastr.android.hilt")    // if using Hilt
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.modulename"
}

dependencies {
    // Module-specific dependencies only
}
```

**Final steps for all modules:**
1. Add to `settings.gradle.kts`: `include(":category:modulename")`
2. Create `src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

## Important Patterns

### Namespace Convention
All modules use `AndroidConfig.NAMESPACE_PREFIX` for consistency:
- App: `"${AndroidConfig.NAMESPACE_PREFIX}.zencastr"` → `com.acksession.zencastr`
- Core UI: `"${AndroidConfig.NAMESPACE_PREFIX}.ui"` → `com.acksession.ui`
- Pattern: `"${AndroidConfig.NAMESPACE_PREFIX}.[modulename]"`

### Module Build Files Should Be Minimal
Due to convention plugins, a typical module `build.gradle.kts` is ~10-15 lines. Don't add Android configuration blocks (compileSdk, kotlinOptions, etc.) - these are handled by convention plugins.

**Good:**
```kotlin
plugins {
    id("zencastr.android.library")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.data"
}

dependencies {
    implementation(libs.retrofit)
}
```

**Bad (Don't do this):**
```kotlin
plugins {
    id("zencastr.android.library")
}

android {
    namespace = "com.acksession.data"
    compileSdk = 36  // ❌ Already in convention plugin

    kotlinOptions { // ❌ Already in convention plugin
        jvmTarget = "11"
    }
}
```

### Version Catalog Usage
Dependencies are managed in `gradle/libs.versions.toml`. Reference them as `libs.dependency.name`:
```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.compose)  // For dependency bundles
}
```

### Compose Plugin Order
The `zencastr.android.compose` plugin MUST be applied AFTER the application/library plugin:
```kotlin
plugins {
    id("zencastr.android.library")     // ✅ First
    id("zencastr.android.compose")     // ✅ Second
}
```

## Modifying Convention Plugins

When editing convention plugins in `build-logic/`:
1. Make changes in `build-logic/src/main/kotlin/`
2. Gradle auto-detects changes via `includeBuild("build-logic")`
3. Sync/rebuild - changes apply to all modules immediately
4. Test with a single module first: `./gradlew :core:ui:assemble`

## Project Structure Philosophy

- **:core:ui** - Reusable UI components, no business logic
- **:core:navigation** - Navigation3 wrapper and NavKey interfaces
- **:core:network** - Network infrastructure (Retrofit, OkHttp, auth interceptors)
- **:core:domain** - Pure Kotlin, business logic, no Android dependencies
- **:core:data** - Data sources, repositories, Room database, implements network callbacks
- **:core:datastore:preferences** - Secure token storage using DataStore
- **:feature:*** - Feature-specific UI and logic, depends on core modules
- **:feature:*:api** - Navigation routes only (sealed interfaces, no UI), enables cross-feature navigation
- **:app** - Minimal, wires features together with Navigation3, provides API base URL

**Cross-Feature Navigation Rule**: Feature modules should never depend on other feature modules directly. Instead, depend on the other feature's `:api` module for type-safe navigation. The `:api` module contains only route definitions (sealed interfaces with `@Serializable` and `NavKey`), ensuring features remain decoupled.

**Circular Dependency Prevention**: The `:core:network` module defines interfaces (like `TokenRefreshCallback`) that `:core:data` implements. This follows the Dependency Inversion Principle, preventing circular dependencies while allowing network interceptors to trigger data layer operations.