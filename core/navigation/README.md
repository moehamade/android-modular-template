# Core Navigation Module

Type-safe navigation infrastructure using Navigation3 with modular route definitions.

## Purpose

Provides a clean abstraction over Navigation3, enabling type-safe navigation between features without tight coupling.

## Key Concepts

### NavKey Interface

All navigation routes implement `NavKey`:

```kotlin
@Serializable
sealed interface ProfileRoute : NavKey {
    @Serializable
    data class Detail(val userId: String) : ProfileRoute
    
    @Serializable
    data object List : ProfileRoute
}
```

### Navigator Wrapper

The `Navigator` class wraps Navigation3's controller and provides convenience methods:

```kotlin
class Navigator(private val navController: NavigationController) {
    fun navigateTo(route: NavKey) {
        navController.navigate(route)
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
}
```

### Cross-Feature Navigation

Features expose navigation routes through `:api` modules:

1. Define routes in `:feature:name:api`
2. Other features depend on the `:api` module
3. Navigate using extension functions on `Navigator`

```kotlin
// In :feature:profile:api
fun Navigator.navigateToProfile(userId: String) {
    navigateTo(ProfileRoute.Detail(userId))
}

// In :feature:recording
viewModel.onProfileClick { userId ->
    navigator.navigateToProfile(userId)
}
```

## Module Structure

```
com.example.navigation
├── NavKey.kt           # Base interface for routes
├── Navigator.kt        # Navigation wrapper
└── EntryProvider.kt    # Feature registration interface
```

## Usage in Features

### Define Routes

In your feature's `:api` module:

```kotlin
@Serializable
sealed interface RecordingRoute : NavKey {
    @Serializable
    data class Record(val sessionId: String?) : RecordingRoute
}
```

### Register with Hilt

In your feature implementation:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RecordingNavigationModule {
    @Provides
    @IntoSet
    fun provideRecordingEntry(): EntryProvider = EntryProvider {
        composable<RecordingRoute.Record> { backStackEntry ->
            val route = backStackEntry.toRoute<RecordingRoute.Record>()
            RecordingScreen(sessionId = route.sessionId)
        }
    }
}
```

### Navigate

```kotlin
@Composable
fun MyScreen(navigator: Navigator) {
    Button(onClick = { navigator.navigateTo(RecordingRoute.Record("123")) }) {
        Text("Start Recording")
    }
}
```

## Benefits

- **Type Safety**: Compile-time validation of route parameters
- **Decoupling**: Features don't depend on each other's implementations
- **Scalability**: Easy to add new features without changing existing code
- **Testability**: Navigator can be mocked in tests

## Dependencies

- Navigation3 (Compose navigation)
- Kotlinx Serialization (route serialization)
- Hilt (dependency injection)

This module has **no Android dependencies** - it's pure Kotlin navigation logic.

