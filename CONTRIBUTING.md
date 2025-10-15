# Contributing to Zencastr

Thanks for your interest in contributing to Zencastr! This guide will help you get started and understand our development workflow.

## Getting Started

### Prerequisites

- **Android Studio**: Jellyfish (2023.3.1) or newer
- **JDK**: 11 or higher
- **Kotlin**: 1.9.x
- **Gradle**: 8.5+ (via wrapper)

### Setting Up the Project

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Build the project: `./gradlew build`

## Architecture Overview

Zencastr follows Clean Architecture with a multi-module approach. Before contributing, familiarize yourself with:

- **Multi-module structure**: Features are isolated in separate modules
- **Convention plugins**: We use custom Gradle plugins in `build-logic/` to maintain consistency
- **Navigation3**: Type-safe navigation with sealed interfaces
- **Hilt**: Dependency injection throughout the app
- **Jetpack Compose**: All UI is built with Compose

Read [CLAUDE.md](CLAUDE.md) for detailed architecture guidance.

## Commit Message Format

We follow the Conventional Commits specification to keep our history clean and enable automated changelog generation.

### Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Types

- **feat**: New feature for the user
- **fix**: Bug fix
- **docs**: Documentation changes only
- **style**: Code formatting, missing semicolons, etc. (no logic change)
- **refactor**: Code changes that neither fix bugs nor add features
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Build system or dependency updates
- **ci**: CI/CD configuration changes
- **chore**: Maintenance tasks, tooling updates
- **revert**: Reverts a previous commit

### Scope

The scope should reference the module or feature being changed:

- `app`, `ui`, `navigation`, `network`, `data`, `domain`
- `recording`, `profile`
- `build-logic`, `gradle`

### Examples

**Simple commits:**

```bash
feat(recording): add pause/resume functionality
fix(auth): prevent token refresh loop on 401
docs(network): document TokenRefreshCallback pattern
refactor(ui): extract theme colors to sealed class
perf(data): cache user profile in memory
test(profile): add ViewModel state tests
build: upgrade Compose to 1.6.0
chore(deps): update Retrofit to 2.9.0
revert: "feat(auth): add Google login"
```

**Commit with body:**

```bash
fix(auth): handle expired tokens gracefully

Previously, when a token expired, the user was logged out immediately.
Now, we attempt to refresh the token once before logging them out,
providing a better user experience.

Closes #123
```

**Commit with breaking change:**

```bash
feat(api)!: migrate to new authentication flow

BREAKING CHANGE: Clients must now use OAuth2 tokens instead of API keys.
Update your TokenProvider implementation to return OAuth2 tokens.

Migration guide: docs/MIGRATION.md
Closes #456
```

### Guidelines

- Use present tense ("add feature" not "added feature")
- Keep the subject line under 72 characters
- Don't capitalize the first letter of the subject
- No period at the end of the subject
- Use the body to explain **what** and **why**, not **how**
- Reference issues with `Closes #123`, `Fixes #456`, or `Resolves #789`
- Mark breaking changes with `!` after the scope or `BREAKING CHANGE:` in footer

### Why Follow This Convention?

- **Automated changelogs**: Tools can generate release notes from commits
- **Semantic versioning**: Automatically determine version bumps (major/minor/patch)
- **Better history**: Easy to scan and understand what changed
- **Team consistency**: Everyone writes commits the same way

## Code Style

### Kotlin

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use meaningful variable names (avoid single letters except for iterators)
- Prefer immutability (`val` over `var`)

### Compose

- Keep composables small and focused
- Extract reusable UI into `:core:ui`
- Use `remember` and state hoisting appropriately
- Preview composables with `@Preview`

### Module Structure

When creating new files, follow these package conventions:

```
com.acksession.<module>
    ├── ui/           # Composables, screens
    ├── viewmodel/    # ViewModels
    ├── model/        # UI models, state classes
    └── navigation/   # Navigation routes (if needed)
```

## Creating New Modules

### Feature Module

Use the scaffolding task:

```bash
./gradlew createFeature -PfeatureName=myfeature
```

This creates:
- `:feature:myfeature` (implementation)
- `:feature:myfeature:api` (navigation routes)
- Necessary build files and manifests

### Manual Module Creation

If you need to create a core module manually:

1. Create the directory structure
2. Add `build.gradle.kts` using convention plugins:

```kotlin
plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose") // if needed
    id("zencastr.android.hilt")    // if needed
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.modulename"
}

dependencies {
    // Module-specific dependencies
}
```

3. Create `src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

4. Add to `settings.gradle.kts`:

```kotlin
include(":category:modulename")
```

5. Create a `README.md` documenting the module's purpose

## Testing

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific module
./gradlew :feature:recording:test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Writing Tests

- Place unit tests in `src/test/kotlin/`
- Place instrumented tests in `src/androidTest/kotlin/`
- Use meaningful test names: `should<ExpectedBehavior>When<Condition>()`
- Mock dependencies with Mockito or MockK
- Test ViewModels with `kotlinx-coroutines-test`

## Pull Request Process

1. **Create a branch**: Use a descriptive name like `feat/recording-pause` or `fix/profile-crash`
2. **Make your changes**: Follow the code style and commit message guidelines
3. **Write tests**: Add unit tests for new features
4. **Update docs**: If you change APIs or architecture, update relevant documentation
5. **Test locally**: Ensure `./gradlew build` passes
6. **Push and create PR**: Provide a clear description of what changed and why
7. **Address review feedback**: Be responsive to comments and suggestions

### PR Description Template

```markdown
## What changed?
Brief description of the changes

## Why?
Explain the motivation behind this change

## Testing
How was this tested? What edge cases were considered?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Follows code style guidelines
```

## Dependency Management

All dependencies are managed in `gradle/libs.versions.toml`. To add a new dependency:

1. Add the version to the `[versions]` section
2. Add the library to `[libraries]` section
3. Use it in modules as `libs.library.name`

Never hardcode versions in module `build.gradle.kts` files.

## Modifying Convention Plugins

Convention plugins live in `build-logic/`. When editing:

1. Make changes in `build-logic/src/main/kotlin/`
2. Test with a single module: `./gradlew :module:assemble`
3. Ensure all modules build: `./gradlew build`
4. Document significant changes in `build-logic/README.md`

## Navigation

When adding cross-feature navigation:

1. Define routes in the feature's `:api` module
2. Create sealed interfaces implementing `NavKey`
3. Add `@Serializable` to route classes
4. Provide extension functions on `Navigator` for type-safe navigation

Example:

```kotlin
// In :feature:myfeature:api
@Serializable
sealed interface MyFeatureRoute : NavKey {
    @Serializable
    data class Detail(val id: String) : MyFeatureRoute
}

fun Navigator.navigateToMyFeatureDetail(id: String) {
    navigateTo(MyFeatureRoute.Detail(id))
}
```

## Common Issues

### Circular Dependencies

If you encounter circular dependency errors:
- Check that `:core:network` doesn't depend on `:core:data`
- Use callback interfaces defined in the lower-level module
- Follow the Dependency Inversion Principle

### Build Failures After Dependency Changes

1. Stop the Gradle daemon: `./gradlew --stop`
2. Clean build: `./gradlew clean`
3. Invalidate caches in Android Studio: File → Invalidate Caches → Invalidate and Restart
4. Check `gradle/libs.versions.toml` for version conflicts

### KSP Issues

If Hilt or Room generated code isn't found:
1. Stop Gradle daemon: `./gradlew --stop` (often fixes KSP/Hilt issues)
2. Rebuild: `./gradlew clean build`
3. Ensure KSP plugin is applied in the module's `build.gradle.kts`
4. Check that annotation processors are configured correctly

### Gradle Daemon Issues

If builds are hanging or behaving strangely:
1. Stop all Gradle daemons: `./gradlew --stop`
2. Clear Gradle cache: `rm -rf ~/.gradle/caches/`
3. Sync project again

**Note**: The Gradle daemon can get stuck when KSP or Hilt process large codebases. Stopping it often resolves mysterious build failures.

## Questions?

- Check [CLAUDE.md](CLAUDE.md) for architecture details
- Review existing modules for patterns and examples
- Open an issue for discussion before major changes

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.
