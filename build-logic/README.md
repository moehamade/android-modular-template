# Build Logic

Custom Gradle convention plugins that standardize configuration across all modules.

## Purpose

Eliminates boilerplate by centralizing Android, Kotlin, Compose, and dependency configurations. Each module applies convention plugins instead of repeating setup code.

## Available Plugins

### Core Plugins

- **`zencastr.android.application`** - For the `:app` module
- **`zencastr.android.library`** - For all library modules
- **`zencastr.android.feature`** - For feature modules (includes library + compose + hilt + common dependencies)

### Add-on Plugins

- **`zencastr.android.compose`** - Adds Jetpack Compose support
- **`zencastr.android.hilt`** - Adds Hilt dependency injection
- **`zencastr.android.room`** - Adds Room database support
- **`zencastr.android.network`** - Adds Retrofit and networking dependencies

## Configuration

All build constants are centralized in `AndroidConfig.kt`:

```kotlin
object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 30
    const val TARGET_SDK = 36
    const val JVM_TARGET = "11"
    const val NAMESPACE_PREFIX = "com.acksession"
}
```

To change SDK versions or build settings, edit this file - changes apply to all modules automatically.

## Plugin Usage

### Feature Module Example

```kotlin
plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Only feature-specific dependencies
    // Core deps (:core:ui, :core:domain, etc.) are auto-included
}
```

### Library Module Example

```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.ui"
}
```

## What Gets Configured Automatically

Each plugin configures:

- **Android Settings**: SDK versions, Java version, build features
- **Kotlin Options**: JVM target, compiler arguments
- **Dependencies**: Common libraries for that module type
- **Build Features**: Compose, BuildConfig, etc.

## Modifying Plugins

To change a convention plugin:

1. Edit files in `src/main/kotlin/`
2. Gradle auto-detects changes via `includeBuild("build-logic")`
3. Sync project
4. Test with a single module first

## Plugin Structure

```
build-logic/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/kotlin/
    ├── AndroidConfig.kt                    # Central configuration
    ├── AndroidApplicationConventionPlugin.kt
    ├── AndroidLibraryConventionPlugin.kt
    ├── AndroidFeatureConventionPlugin.kt
    ├── AndroidComposeConventionPlugin.kt
    ├── AndroidHiltConventionPlugin.kt
    ├── AndroidRoomConventionPlugin.kt
    └── AndroidNetworkConventionPlugin.kt
```

## Benefits

- **Consistency**: All modules use the same configuration
- **Maintainability**: Change once, applies everywhere
- **Readability**: Module build files stay minimal (10-15 lines)
- **Type Safety**: Kotlin DSL with IDE support

## Common Dependencies

The `zencastr.android.feature` plugin automatically includes:

```kotlin
dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    // Plus Compose, Hilt, Coroutines, etc.
}
```

## Testing Plugins

After modifying a plugin:

```bash
# Build a single module
./gradlew :core:ui:assemble

# Build all modules
./gradlew build

# Check for configuration issues
./gradlew projects
```

## Best Practices

- Don't duplicate configuration in module build files
- Use `AndroidConfig` for all shared constants
- Keep plugins focused on a single concern
- Document significant changes
- Test changes before committing

