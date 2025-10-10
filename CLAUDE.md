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

## Architecture

### Multi-Module Structure

The project follows a modular architecture with clear separation of concerns:

```
:app                  - Main application module, depends on feature modules and :core:ui
:core:ui              - Shared UI components and design system (theme, colors, typography)
:core:data            - Data layer (repositories, data sources, Room, network)
:core:domain          - Business logic layer (models, use cases, domain entities)
:feature              - Feature modules (future: split into :feature:recording, :feature:playback, etc.)
```

**Dependency Flow:**
- `:app` → `:core:ui`, `:feature`
- `:feature` → `:core:ui`, `:core:domain`, `:core:data`
- `:core:data` → `:core:domain`
- `:core:ui` → standalone (only UI/theme)
- `:core:domain` → pure Kotlin (no Android dependencies)

### Convention Plugins System

**Critical:** This project uses Gradle convention plugins located in `build-logic/` to eliminate boilerplate. All modules use these plugins instead of directly configuring Android/Kotlin settings.

**Available Plugins:**
- `zencastr.android.application` - For `:app` module (includes SDK config, test dependencies, Kotlin setup)
- `zencastr.android.library` - For library modules (same as above, but for libraries)
- `zencastr.android.compose` - Adds Jetpack Compose support (must be applied after application/library plugin)
- `zencastr.android.hilt` - Adds Hilt dependency injection (KSP + dependencies)
- `zencastr.android.room` - Adds Room database support (KSP + dependencies)

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

1. Create module directory under appropriate category (`core/`, `feature/`, etc.)
2. Create `build.gradle.kts`:
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
3. Add to `settings.gradle.kts`: `include(":category:modulename")`
4. Create `src/main/AndroidManifest.xml`:
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
- **:core:domain** - Pure Kotlin, business logic, no Android dependencies
- **:core:data** - Data sources, repositories, Room/Retrofit implementations
- **:feature** - Feature-specific UI and logic, depends on core modules
- **:app** - Minimal, just wires everything together

Feature modules should be self-contained and depend only on core modules, never on other features.