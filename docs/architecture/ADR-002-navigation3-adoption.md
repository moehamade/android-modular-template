# ADR-002: Navigation3 Adoption

**Status**: Accepted

**Date**: 2025-10-15

## Context

Android navigation has evolved through several iterations:
- Manual fragment transactions (legacy)
- Navigation Component (Jetpack Navigation, aka Navigation2)
- **Navigation3** (latest, alpha as of Oct 2025)

For Zencastr, we needed a navigation solution that supports:
- Type-safe navigation with compile-time checks
- Deep linking support
- Multi-module navigation without coupling
- Compose-first design (no XML)
- Serializable route parameters

### Why Navigation3?

Navigation2 (Jetpack Navigation) can be made type-safe with SafeArgs and custom wrappers, but its string-based route system and XML navigation graphs are not ideal for a multi-module, Compose-first project. Navigation3 introduces a fundamentally different model:
- Routes are sealed interfaces and data classes, not strings.
- Arguments are serializable and validated at compile time.
- Features register their navigation via API modules, not implementation dependencies.
- Compose-first: no XML, no code generation, no fragile graph files.
- **Direct control over the navigation backstack and internals.** Unlike Navigation2, which is a black box, Navigation3 exposes the backstack and navigation state, allowing us to implement advanced flows, custom back handling, and debug navigation issues more easily.

We evaluated the risks of adopting an alpha library. The benefits—type safety, modularity, and future-proof design—outweighed the downsides. Migration is straightforward if instability arises, since our Navigator wrapper isolates the implementation.

## Decision

We adopted **Navigation3** with a custom `Navigator` wrapper and `:api` module pattern for cross-feature navigation.

### Implementation Details

1. **Route Definitions** - Each feature defines routes as sealed interfaces:
```kotlin
@Serializable
sealed interface ProfileRoute : NavKey {
    @Serializable
    data class Profile(val userId: String) : ProfileRoute
}
```

2. **API Modules** - Routes are published in `:feature:*:api` modules:
```kotlin
// :feature:profile:api
fun Navigator.navigateToProfile(userId: String) {
    navigateTo(ProfileRoute.Profile(userId))
}
```

3. **Navigator Wrapper** - `:core:navigation` provides abstraction:
```kotlin
class Navigator(private val controller: NavigationController) {
    fun navigateTo(route: NavKey) { ... }
    fun navigateBack() { ... }
}
```

4. **Feature Registration**
- Each feature exposes its navigation routes via its API module.
- The app module and other features depend only on the API modules, not the implementation.
- This keeps navigation decoupled and modular.

5. **Dependency Flow**:
```
:feature:recording (impl) → :feature:profile:api (routes only)
  ↓
:core:navigation (Navigator + NavKey)
```

### How Features Register Navigation

Each feature module registers its navigation destinations by providing an `EntryProviderInstaller` via Hilt. This installer is a function that installs the feature's navigation entries into the app's navigation graph, without the app module needing to know about the feature's implementation.

**Pattern:**
- Each feature defines a Hilt module (e.g., `ProfileNavigationModule`) with a `@Provides @IntoSet` function that returns an `EntryProviderInstaller`.
- The installer uses the `entry<Route>` DSL to declare composable destinations for each route.
- The app collects all installers and builds the navigation graph dynamically.

**Example (Profile feature):**
```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileNavigationModule {
    @IntoSet
    @Provides
    fun provideProfileEntryProvider(navigator: Navigator): EntryProviderInstaller = {
        entry<ProfileRoute.Profile> { route ->
            val profileScreenViewModel: ProfileScreenViewModel = hiltViewModel(
                creationCallback = { factory: ProfileScreenViewModel.Factory ->
                    factory.create(route)
                }
            )
            ProfileScreen(profileScreenViewModel = profileScreenViewModel)
        }
        entry<ProfileRoute.ProfileDialog> { route ->
            ProfileDialogContent(
                userId = route.userId,
                message = route.message,
                navigator = navigator
            )
        }
    }
}
```

This pattern ensures:
- Features self-register their navigation destinations
- No direct dependency on feature implementations
- Navigation graph is modular and extensible

See `core/navigation/EntryProviderInstaller.kt` for the type alias and documentation.

## Consequences

### Positive
- ✅ **Type safety**: Routes are data classes, compile-time parameter validation
- ✅ **Kotlinx Serialization**: Seamless argument passing with `@Serializable`
- ✅ **Decoupling**: Features navigate via APIs, not implementations
- ✅ **Refactoring safety**: Rename route parameters → compiler errors, not runtime crashes
- ✅ **Deep linking**: Automatic support via serializable routes
- ✅ **Future-proof**: Navigation3 is the future of Android navigation

### Negative
- ⚠️ **Alpha stability**: Navigation3 is still in alpha (as of Oct 2025)
- ⚠️ **Limited resources**: Fewer tutorials compared to Navigation Component
- ⚠️ **API changes**: Potential breaking changes before stable release

### Mitigations
- Wrapped Navigation3 in custom `Navigator` class for abstraction
- If Navigation3 has breaking changes, we only update `:core:navigation`
- Convention plugins make route definition boilerplate minimal
- Clear documentation in `:core:navigation` module README

## Alternatives Considered

1. **Navigation Component (Jetpack Navigation)**
   - Rejected: String-based routes are error-prone
   - XML navigation graphs don't fit multi-module architecture
   - SafeArgs is verbose and doesn't support complex types well
   - Type safety possible, but not as robust as Navigation3

2. **Compose Destinations (third-party)**
   - Rejected: Adds external dependency and code generation
   - Navigation3 will eventually become the standard

3. **Custom navigation solution**
   - Rejected: Reinventing the wheel
   - We'd have to solve all the problems Navigation3 already addresses

## Migration Path

If Navigation3 proves unstable:
1. Our `Navigator` wrapper isolates the implementation
2. We can swap out Navigation3 with Navigation Component
3. Route definitions (`@Serializable` sealed interfaces) stay the same
4. Only `:core:navigation` implementation changes

## References

- [Navigation3 Documentation](https://developer.android.com/guide/navigation/design/navigation3)
- [Type Safety in Navigation](https://developer.android.com/guide/navigation/design/type-safety)
