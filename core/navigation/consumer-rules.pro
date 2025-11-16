# ================================================================================================
# :core:navigation Module - ProGuard Rules
# ================================================================================================
# This module provides Navigation3 wrappers and NavKey definitions.
# These rules ensure type-safe navigation routes work correctly after R8 optimization.

# ================================================================================================
# Kotlinx Serialization (for Navigation3 routes)
# ================================================================================================
# R8 conditional rules - only keeps what's actually serializable

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <2>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializer for classes that have @Serializable
-if @kotlinx.serialization.Serializable class **
-keep,allowshrinking,allowobfuscation class <1>$$serializer {
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializers for sealed and enum classes
-if @kotlinx.serialization.Serializable class ** {
    ** $serializer;
}
-keepclassmembers class <1> {
    ** $serializer;
}

# Project-specific: Keep navigation routes (they use @Serializable)
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class com.example.navigation.** { *; }
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class com.example.feature.**.api.** { *; }

# ================================================================================================
# Navigation3 (androidx.navigation3:navigation3-runtime:1.0.0-alpha11)
# ================================================================================================
# Navigation3 uses Kotlinx Serialization for type-safe routes

# Keep NavKey interface and implementations (marked with @Serializable)
-keep,allowobfuscation interface androidx.navigation3.runtime.NavKey
-keep,allowobfuscation,allowshrinking class * implements androidx.navigation3.runtime.NavKey { *; }

# Project-specific: Keep navigation routes (already handled by @Serializable rules above)
# RecordingRoute in com.example.feature.recording.api
# ProfileRoute in com.example.feature.profile.api
-keep,allowobfuscation class com.example.feature.recording.api.RecordingRoute** { *; }
-keep,allowobfuscation class com.example.feature.profile.api.ProfileRoute** { *; }

# Keep Navigation3 runtime classes
-keep class androidx.navigation3.runtime.** { *; }
-keep class androidx.lifecycle.viewmodel.navigation3.** { *; }
