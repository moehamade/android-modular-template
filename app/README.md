# App Module

Main application module that wires together all features and core modules.

## Purpose

This is the entry point of the application. It's intentionally kept minimal - its main responsibilities are:

- Define the application class
- Configure Hilt for dependency injection
- Set up Navigation3 with all feature routes
- Provide app-level configuration (API base URL, etc.)
- Apply the app theme

## Structure

```
app/
├── src/main/
│   ├── kotlin/com/acksession/zencastr/
│   │   ├── ZencastrApplication.kt      # Application class
│   │   ├── MainActivity.kt             # Single activity
│   │   ├── di/                         # App-level DI modules
│   │   └── navigation/                 # Navigation graph setup
│   ├── res/
│   │   ├── values/themes.xml           # XML theme (for Activity)
│   │   └── drawable/                   # App icon, splash screen
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Key Components

### Application Class

```kotlin
@HiltAndroidApp
class ZencastrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // App-level initialization
    }
}
```

### MainActivity

Single activity that hosts all Compose screens:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZencastrTheme {
                ZencastrApp()
            }
        }
    }
}
```

### Navigation Setup

```kotlin
@Composable
fun ZencastrApp(navigator: Navigator = rememberNavigator()) {
    val navController = rememberNavigationController()
    
    NavigationHost(
        navController = navController,
        startDestination = RecordingRoute.Setup(null)
    ) {
        // Features register themselves via Hilt's @IntoSet
    }
}
```

## App-Level Configuration

Provide configuration values via Hilt:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    
    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL
    
    @Provides
    @AppVersion
    fun provideAppVersion(): String = BuildConfig.VERSION_NAME
}
```

## Build Configuration

The `build.gradle.kts` is minimal thanks to convention plugins:

```kotlin
plugins {
    id("zencastr.android.application")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr"
    
    defaultConfig {
        applicationId = "com.acksession.zencastr"
        versionCode = 1
        versionName = "1.0.0"
        
        buildConfigField("String", "API_BASE_URL", "\"https://api.zencastr.com\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    
    implementation(project(":feature:recording"))
    implementation(project(":feature:profile"))
}
```

## Dependencies

The app module depends on:

- All `:core:*` modules
- All `:feature:*` implementation modules
- Navigation3 libraries
- Hilt Android

## Build Variants

Define build types for different environments:

```kotlin
buildTypes {
    debug {
        applicationIdSuffix = ".debug"
        isDebuggable = true
        buildConfigField("String", "API_BASE_URL", "\"https://api-dev.zencastr.com\"")
    }
    
    release {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        buildConfigField("String", "API_BASE_URL", "\"https://api.zencastr.com\"")
    }
}
```

## Permissions

Declare app-level permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Feature-specific permissions are declared in their respective modules.

## ProGuard Rules

Keep necessary classes for reflection-based libraries (in `proguard-rules.pro`):

```
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.acksession.**$$serializer { *; }

# Hilt
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
```

## Testing

App module tests focus on integration:

```kotlin
@HiltAndroidTest
class NavigationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun should_navigate_to_profile_from_recording() {
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.onNodeWithText("Edit Profile").assertExists()
    }
}
```

## Best Practices

- Keep business logic out of this module
- Don't add feature-specific code here
- Use BuildConfig for environment-specific values
- Let features register themselves via Hilt
- Keep the module as thin as possible

