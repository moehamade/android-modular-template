
This module should remain dependency-light. It depends only on:
- Compose UI libraries
- Material3

**Do not add** business logic, ViewModels, or data layer dependencies here.

## Theming Best Practices

- Use `MaterialTheme.colorScheme` instead of hardcoded colors
- Use `MaterialTheme.typography` for text styles
- Support both light and dark themes
- Test with dynamic color on Android 12+
# Core UI Module

Shared UI components, design system, and theme definitions used across all feature modules.

## Purpose

This module provides a consistent visual language throughout the app. Any reusable composable, theme element, or design token should live here.

## What's Inside

- **Theme System**: Material3 theme with dark/light mode support
- **Design Tokens**: Colors, typography, spacing
- **Reusable Components**: Buttons, cards, inputs, etc.
- **Common UI Utilities**: Modifiers, extensions

## Structure

```
com.acksession.ui
├── theme/
│   ├── Theme.kt        # ZencastrTheme composable
│   ├── Color.kt        # Color palette
│   └── Type.kt         # Typography definitions
└── components/         # Reusable composables
```

## Usage

Apply the theme in your feature module:

```kotlin
@Composable
fun MyScreen() {
    ZencastrTheme {
        // Your UI here
    }
}
```

Access theme values:

```kotlin
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.primary,
    style = MaterialTheme.typography.headlineMedium
)
```

## Adding New Components

When creating reusable components:

1. Place in `com.acksession.ui.components`
2. Keep them generic and configurable
3. Add `@Preview` annotations
4. Document parameters with KDoc

Example:

```kotlin
/**
 * A primary button following app design guidelines.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Optional modifier
 * @param enabled Whether the button is enabled
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Implementation
}
```

## Dependencies

