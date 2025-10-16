# ADR-004: Gradle Convention Plugins System

**Status**: Accepted

**Date**: 2025-10-15

## Context

In multi-module Android projects, each module requires significant Gradle configuration:
- Android SDK versions (compileSdk, minSdk, targetSdk)
- Kotlin compiler options (jvmTarget, language version)
- Build features (Compose, BuildConfig, etc.)
- Dependencies (Hilt, testing libraries, etc.)

Without centralization, this leads to:
- **Duplication**: Same configuration repeated in every module
- **Inconsistency**: Modules use different SDK versions or compiler settings
- **Maintenance burden**: Updating settings requires changing dozens of files
- **Verbose build files**: 50-100 line `build.gradle.kts` files

## Decision

We implemented a **Gradle Convention Plugins** system in `build-logic/`:

### Available Convention Plugins

1. **`zencastr.android.application`** - For `:app` module
2. **`zencastr.android.library`** - For library modules
3. **`zencastr.android.feature`** - For feature modules (bundles library + compose + hilt)
4. **`zencastr.android.compose`** - Adds Jetpack Compose support
5. **`zencastr.android.hilt`** - Adds Hilt DI (with KSP)
6. **`zencastr.android.room`** - Adds Room database support
7. **`zencastr.android.network`** - Adds networking dependencies

**Note**: Detekt code quality is applied globally to all subprojects in the root `build.gradle.kts`.

### Centralized Configuration

All constants in `build-logic/src/main/kotlin/AndroidConfig.kt`:
```kotlin
object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 30
    const val TARGET_SDK = 36
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0"
    const val JVM_TARGET = "11"
    const val NAMESPACE_PREFIX = "com.acksession"
}
```

**To update SDK versions**: Change `AndroidConfig.kt` → all modules update automatically ✅

### Module Build File Example

**Before (without convention plugins)**:
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.acksession.feature.profile"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 30
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
```
**80+ lines of boilerplate!** 😱

**After (with convention plugins)**:
```kotlin
plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.profile"
}

dependencies {
    // Feature-specific dependencies only
    // Core modules and common deps are auto-included
}
```
**~10 lines total!** ✨

## Consequences

### Positive
- ✅ **DRY (Don't Repeat Yourself)**: Configuration defined once
- ✅ **Consistency**: All modules use same settings
- ✅ **Maintainability**: Update one file, all modules change
- ✅ **Scalability**: Adding modules is trivial
- ✅ **Type safety**: Kotlin DSL catches errors at compile time
- ✅ **Discoverability**: `./gradlew :build-logic:tasks` shows available plugins
- ✅ **Feature scaffolding**: Automated with `./gradlew createFeature`

### Negative
- ⚠️ **Initial setup**: Requires understanding of Gradle plugin development
- ⚠️ **Abstraction**: Team needs to know where config lives
- ⚠️ **IDE support**: Sometimes requires Gradle sync to recognize changes

### Mitigations
- Comprehensive documentation in `build-logic/README.md`
- `AndroidConfig.kt` has clear comments
- Convention plugin code is straightforward Kotlin

## Feature Scaffolding Task

Bonus: Convention plugins enabled automated feature creation:
```bash
./gradlew createFeature -PfeatureName=notifications
```

This automatically generates:
- `:feature:notifications` - Implementation module
- `:feature:notifications:api` - Navigation routes
- Build files with correct plugins
- AndroidManifest.xml
- NavKey boilerplate
- Updates `settings.gradle.kts`

**Saves 15-20 minutes per feature!**

## Alternatives Considered

1. **`buildSrc/` module**
   - Rejected: Changes to `buildSrc` invalidate entire build cache
   - Convention plugins use `includeBuild()` which is incremental

2. **Gradle composite builds**
   - Rejected: More complex setup than convention plugins
   - Less discoverable than our current approach

3. **Manual configuration in each module**
   - Rejected: Already experiencing pain from this approach
   - Doesn't scale past ~5 modules

4. **Version catalogs only (no convention plugins)**
   - Not sufficient: Catalogs manage dependencies, not configuration
   - Still requires repeating Android blocks in every module

## Migration Strategy

When adding new modules:
1. Use convention plugins from day one
2. Choose appropriate plugin: `feature`, `library`, or `application`
3. Only add module-specific config in `build.gradle.kts`

## References

- [Sharing build logic in multi-module projects](https://docs.gradle.org/current/samples/sample_convention_plugins.html)
- [Now in Android build-logic](https://github.com/android/nowinandroid/tree/main/build-logic)
- [Gradle Best Practices](https://developer.android.com/build/optimize-your-build)
