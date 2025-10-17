# ================================================================================================
# :core:ui Module - ProGuard Rules
# ================================================================================================
# This module provides Jetpack Compose UI components, theme, and Coil image loading.
# These rules ensure Compose composables and image loading work correctly after R8 optimization.

# ================================================================================================
# Jetpack Compose (Minimal - R8 handles most)
# ================================================================================================
# Compose compiler plugin handles most optimization automatically

# Keep @Composable functions from being removed if unused
-keep,allowshrinking,allowobfuscation @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Compose runtime internals that use reflection
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.material3.**

# ================================================================================================
# Coil Image Loading 2.7.0
# ================================================================================================
# Minimal rules - Coil is R8-friendly

-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**
