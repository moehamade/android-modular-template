# Build Logic - Convention Plugins

This module contains Gradle convention plugins to standardize and minimize build configuration across all modules in the Zencastr project.

## Overview

Convention plugins help eliminate boilerplate code by centralizing common Gradle configurations. Instead of repeating the same Android configuration in every module, we define it once in a convention plugin and apply it everywhere.

## Structure

```
build-logic/
├── build.gradle.kts          # Plugin definitions and dependencies
├── settings.gradle.kts       # Version catalog setup
├── README.md                 # This file
└── src/main/kotlin/
    ├── AndroidConfig.kt                      # Centralized constants
    ├── ProjectExt.kt                         # Extension functions
    ├── VersionCatalogExt.kt                  # Version catalog accessor
    ├── AndroidApplicationConventionPlugin.kt # App module plugin
    ├── AndroidLibraryConventionPlugin.kt     # Library module plugin
    ├── AndroidComposeConventionPlugin.kt     # Jetpack Compose plugin
    ├── AndroidHiltConventionPlugin.kt        # Hilt DI plugin
    └── AndroidRoomConventionPlugin.kt        # Room database plugin
```

## Available Convention Plugins

### 1. `zencastr.android.application`
For app modules. Applies:
- Android Application plugin
- Kotlin Android plugin
- Common Android configuration (compileSdk, minSdk, targetSdk, etc.)
- Test dependencies

**Usage:**
```kotlin
plugins {
    id("zencastr.android.application")
}
```

### 2. `zencastr.android.library`
For library modules. Applies:
- Android Library plugin
- Kotlin Android plugin
- Common Android configuration
- Consumer ProGuard rules
- Test dependencies

**Usage:**
```kotlin
plugins {
    id("zencastr.android.library")
}
```

### 3. `zencastr.android.compose`
For modules using Jetpack Compose. Applies:
- Kotlin Compose plugin
- Compose build features
- Compose BOM and common dependencies
- Debug and test dependencies for Compose

**Usage:**
```kotlin
plugins {
    id("zencastr.android.library") // or android.application
    id("zencastr.android.compose")
}
```

### 4. `zencastr.android.hilt`
For modules using Hilt dependency injection. Applies:
- Hilt Android plugin
- KSP plugin
- Hilt dependencies and annotation processor

**Usage:**
```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
}
```

### 5. `zencastr.android.room`
For modules using Room database. Applies:
- Room plugin
- KSP plugin
- Room dependencies and annotation processor
- Schema export directory configuration

**Usage:**
```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.room")
}
```

## Centralized Configuration

### AndroidConfig.kt
Contains all common Android build constants:
- `COMPILE_SDK = 36`
- `MIN_SDK = 30`
- `TARGET_SDK = 36`
- `VERSION_CODE = 1`
- `VERSION_NAME = "1.0"`
- `JAVA_VERSION = VERSION_11`
- `JVM_TARGET = "11"`

To update SDK versions or other build constants, modify this file once, and all modules will be updated.

## Benefits

1. **DRY Principle**: Define configuration once, use everywhere
2. **Consistency**: All modules use the same configuration
3. **Maintainability**: Update configuration in one place
4. **Minimal Boilerplate**: Module build files are now ~10 lines instead of ~50
5. **Easy Scaling**: Adding new modules is trivial - just apply the appropriate plugins

## Example: Before vs After

### Before (Old build.gradle.kts)
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.acksession.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // ... more dependencies
}
```

### After (New build.gradle.kts)
```kotlin
plugins {
    id("zencastr.android.library")
}

android {
    namespace = "com.acksession.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
```

**Result**: ~75% reduction in boilerplate code!

## Adding a New Module

To add a new module with the convention plugins:

1. Create the module directory
2. Create a minimal `build.gradle.kts`:

```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose") // if using Compose
    id("zencastr.android.hilt")    // if using Hilt
}

android {
    namespace = "com.acksession.yourmodule"
}

dependencies {
    // Only module-specific dependencies
}
```

3. Add the module to `settings.gradle.kts`

## Customization

If a specific module needs different configuration, you can still override in that module's `build.gradle.kts`:

```kotlin
plugins {
    id("zencastr.android.library")
}

android {
    namespace = "com.acksession.special"

    // Override specific configs
    defaultConfig {
        minSdk = 26  // Different from global MIN_SDK
    }
}
```

## Future Enhancements

Consider adding these convention plugins as the project grows:
- `zencastr.android.feature` - For feature modules with common dependencies
- `zencastr.android.testing` - For test-only modules
- `zencastr.kotlin.library` - For pure Kotlin modules (no Android)
- `zencastr.android.lint` - For custom lint configurations
